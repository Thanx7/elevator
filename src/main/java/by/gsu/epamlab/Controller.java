package by.gsu.epamlab;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import by.gsu.epamlab.beans.Passenger;

public class Controller extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(Controller.class);

	private int storeysNumber;
	private int elevatorCapacity;
	private int passengersNumber;
	private int animationBoost;
	private Map<Integer, List<Passenger>> dispatch = new HashMap<>();
	private List<Passenger> elevatorContainer = new ArrayList<>();
	private Map<Integer, List<Passenger>> arrival = new HashMap<>();
	private int currentFloor = 1;

	private byte[] elevatorLock = new byte[0];
	private int issuedNotificationsLoad = 0;
	private int issuedNotificationsUnload = 0;

	private Image img = new ImageIcon(Constants.ELEVATOR).getImage();
	private JFrame f;
	private JPanel panel;
	private JLabel label, labelInfo;
	private int x = Constants.ELEVATOR_X;
	private int y = Constants.ELEVATOR_Y;
	private boolean abort = false;
	private JButton button, button2, button3;
	private byte[] lock = new byte[0];

	public Controller(int storeysNumber, int elevatorCapacity,
			int passengersNumber, int animationBoost) {
		super();
		this.storeysNumber = storeysNumber;
		this.elevatorCapacity = elevatorCapacity;
		this.passengersNumber = passengersNumber;
		this.animationBoost = animationBoost;
		eController();
	}

	public int getPassengersNumber() {
		return passengersNumber;
	}

	public int getCurrentFloor() {
		return currentFloor;
	}

	public byte[] getElevatorLock() {
		return elevatorLock;
	}

	public int getIssuedNotificationsLoad() {
		return issuedNotificationsLoad;
	}

	public int getIssuedNotificationsUnload() {
		return issuedNotificationsUnload;
	}

	public boolean isAbort() {
		return abort;
	}

	public void paint(Graphics g) {
		g = (Graphics2D) g;
		g.drawImage(img, x, y, null);
		g.drawString(Constants.ARRIVAL, Constants.PASSENGER_X,
				Constants.ARRIVAL_Y);
		g.drawString(Constants.DISPATCH, Constants.PASSENGER_X,
				Constants.DISPATCH_Y);
		g.drawString("Current Floor: " + Integer.toString(currentFloor), x, y
				- Constants.ROW);
	}

	private void eController() {
		// random distribution of the passengers in the initial dispatch map
		dispatchNotStarted();
		creatingThreads();

		if (animationBoost > 0) {
			f.add(this);
			f.setVisible(true);
			try {
				Thread.sleep(animationBoost);
			} catch (InterruptedException e) {
			}
		}

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(Constants.LOG_PROP_FILENAME));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(props);
		LOGGER.info(Constants.STARTING_TRANSPORTATION);
		boolean up = true;// initial direction of the elevator is up
		do { // main loop
			notifyElevatorPassengers();
			notifyDispatchPassengers(up);

			if (allTasksCopmleted() || abort)
				break;

			// moving from the current floor to the next floor
			int current = currentFloor;
			if (up) {
				currentFloor++;
				if (currentFloor > storeysNumber) {
					up = false; // elevator turns down
					currentFloor -= 2;
				}
			} else {
				currentFloor--;
				if (currentFloor < 1) {
					up = true; // elevator turns up
					currentFloor += 2;
				}
			}
			int next = currentFloor;

			String formatted = String.format(
					"MOVING_ELEVATOR (from story-%s to story-%s)", current,
					next);
			LOGGER.info(formatted);

			if (animationBoost > 0) {
				label.setText(formatted);
				f.repaint();
				f.setVisible(true);
				try {
					Thread.sleep(animationBoost);
				} catch (InterruptedException e) {
				}
			}
		} while (true);
	}

	// check if all the tasks are completed
	private boolean allTasksCopmleted() {

		boolean allTransportationStateCompleted = true;
		boolean equalsFloors = true;
		int count = 0;
		for (Map.Entry<Integer, List<Passenger>> entry : arrival.entrySet()) {
			List<Passenger> passengerList = entry.getValue();
			count += passengerList.size();
			for (Passenger p : passengerList) {
				if (p.getTransportationState() != Constants.COMPLETED) {
					allTransportationStateCompleted = false;
				}
				if (p.getDestinationStorey() != entry.getKey()) {
					equalsFloors = false;
				}
			}
		}

		boolean equalsQuantity = false;
		if (count == passengersNumber)
			equalsQuantity = true;

		if (dispatch.isEmpty() && elevatorContainer.isEmpty()
				&& allTransportationStateCompleted && equalsFloors
				&& equalsQuantity) {
			LOGGER.info(Constants.COMPLETION_TRANSPORTATION);
			LOGGER.info(Constants.COMPLETION_LOG1);
			LOGGER.info(Constants.COMPLETION_LOG2);
			LOGGER.info(Constants.COMPLETION_LOG3);
			LOGGER.info(Constants.COMPLETION_LOG4);
			if (animationBoost > 0) {
				panel.remove(label);
				f.repaint();
				label = new JLabel(Constants.COMPLETION_TRANSPORTATION);
				label.setBounds(
						Constants.BUTTON_X + 2 * Constants.BUTTON_WIDTH,
						Constants.BUTTON_Y, Constants.BUTTON_WIDTH * 2,
						Constants.BUTTON_HEIGHT);
				panel.add(label);
				lastButton(Color.GREEN);
			}
			return true;
		}
		return false;
	}

	private void notifyElevatorPassengers() {
		issuedNotificationsUnload = 0;
		List<Passenger> tmp = new ArrayList<>();

		for (Passenger p : elevatorContainer) {
			if (this.getCurrentFloor() == p.getDestinationStorey()) {
				issuedNotificationsUnload++;
				tmp.add(p);
			}
		}

		// wait for all passengers unload
		if (issuedNotificationsUnload > 0) {
			synchronized (elevatorLock) {
				for (Passenger p : tmp) {
					byte[] passengerLock = p.getLockOut();
					synchronized (passengerLock) {
						passengerLock.notify();
					}
				}
				try {
					elevatorLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public synchronized void unload(Passenger p) {
		String formatted = String.format(
				"DEBOARDING_OF_PASSENGER ( %s on story-%s)",
				p.getPassengerId(), p.getDestinationStorey());
		LOGGER.info(formatted);
		if (animationBoost > 0) {
			label.setText(formatted);
		}

		// put the passenger into the arrival map
		List<Passenger> arrivalStoreyContainer;
		try {
			arrivalStoreyContainer = arrival.get(p.getDestinationStorey());
			arrivalStoreyContainer.add(p);
		} catch (NullPointerException e) {
			// create new container if not exist
			arrivalStoreyContainer = new ArrayList<>();
			arrivalStoreyContainer.add(p);
			arrival.put(p.getDestinationStorey(), arrivalStoreyContainer);
		}

		// delete the passenger from the elevator
		for (ListIterator<Passenger> it = elevatorContainer.listIterator(); it
				.hasNext();) {
			if (it.next().equals(p)) {
				it.remove();

				if (animationBoost > 0) {
					p.unloadElevator();
					f.repaint();
					f.setVisible(true);
					try {
						Thread.sleep(animationBoost);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private void notifyDispatchPassengers(boolean up) {
		issuedNotificationsLoad = 0;
		int elevatorPassengers = elevatorContainer.size();

		// permissionsEnterElevator is the number of free places in the elevator
		int permissionsEnterElevator = elevatorCapacity - elevatorPassengers;
		List<Passenger> tmp = new ArrayList<>();

		for (Map.Entry<Integer, List<Passenger>> entry : dispatch.entrySet()) {
			if (entry.getKey().equals(currentFloor)) {
				for (Passenger p : entry.getValue()) {
					// take only people the same direction with the elevator
					boolean sameDirection = false;
					if (((p.getInitialStorey() < p.getDestinationStorey()) && up)
							|| p.getInitialStorey() == 1)
						sameDirection = true;
					if (((p.getInitialStorey() > p.getDestinationStorey()) && !up)
							|| p.getInitialStorey() == storeysNumber)
						sameDirection = true;
					if (permissionsEnterElevator > 0 && sameDirection) {
						issuedNotificationsLoad++;
						permissionsEnterElevator--;
						tmp.add(p);
					}
				}
			}
		}

		// wait for passengers load
		if (issuedNotificationsLoad > 0) {
			synchronized (elevatorLock) {
				try {
					for (Passenger p : tmp) {
						byte[] passengerLock = p.getLockEntry();
						synchronized (passengerLock) {
							passengerLock.notify();
						}
					}
					elevatorLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public synchronized void load(Passenger p) {
		String formatted = String.format(
				"BOARDING_OF_PASSENGER ( %s on story-%s)", p.getPassengerId(),
				p.getInitialStorey());
		LOGGER.info(formatted);

		if (animationBoost > 0) {
			label.setText(formatted);
			p.loadElevator();
			f.repaint();
			f.setVisible(true);
			try {
				Thread.sleep(animationBoost);
			} catch (InterruptedException e) {
			}
		}

		elevatorContainer.add(p);

		// passenger has entered to the elevator
		// so, we have to delete the passenger in the "dispatch" map
		for (Iterator<Map.Entry<Integer, List<Passenger>>> itMap = dispatch
				.entrySet().iterator(); itMap.hasNext();) {
			Map.Entry<Integer, List<Passenger>> entry = itMap.next();
			if (entry.getKey().equals(p.getInitialStorey())) {
				List<Passenger> passengerList = entry.getValue();
				for (ListIterator<Passenger> it = passengerList.listIterator(); it
						.hasNext();) {
					if (it.next().equals(p)) {
						it.remove();
					}
				}
				// if the storey is empty, we delete this empty
				// storey from the "dispatch" map
				if (passengerList.isEmpty()) {
					itMap.remove();
				}
			}
		}
	}

	// random distribution of the passengers
	private void dispatchNotStarted() {
		if (animationBoost > 0) {
			f = new JFrame("Elevator");
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setSize(Constants.ELEVATOR_WIDTH, Constants.ELEVATOR_HEIGHT);

			button = new JButton();
			button.setText("Start");
			button.setBounds(Constants.BUTTON_X, Constants.BUTTON_Y,
					Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT);
			button.addActionListener(new ButtonListener());
			labelInfo = new JLabel("INFO: ");
			labelInfo.setBounds(Constants.BUTTON_X + Constants.BUTTON_WIDTH,
					Constants.BUTTON_Y, Constants.BUTTON_WIDTH,
					Constants.BUTTON_HEIGHT);
			labelInfo.setHorizontalAlignment(JLabel.RIGHT);
			label = new JLabel();
			label.setBounds(Constants.BUTTON_X + 2 * Constants.BUTTON_WIDTH,
					Constants.BUTTON_Y, Constants.BUTTON_WIDTH * 2,
					Constants.BUTTON_HEIGHT);
			panel = new JPanel(null);
			panel.setBackground(new Color(0, 0, 0, 0));
			panel.add(button);
			panel.add(labelInfo);
			panel.add(label);
			f.add(panel);
			f.setVisible(true);
		}

		for (int id = 1; id <= passengersNumber; id++) {
			Random generator = new Random();
			int randomInitialStorey = generator.nextInt(storeysNumber) + 1;
			int randomDestinationStorey;
			do {
				generator = new Random();
				randomDestinationStorey = generator.nextInt(storeysNumber) + 1;
			} while (randomInitialStorey == randomDestinationStorey);

			Passenger p = new Passenger(this, id, randomInitialStorey,
					randomDestinationStorey);
			if (animationBoost > 0) {
				f.add(p);
				f.setVisible(true);
			}

			List<Passenger> dispatchStoreyContainer;

			try {
				dispatchStoreyContainer = dispatch.get(randomInitialStorey);
				dispatchStoreyContainer.add(p);
			} catch (NullPointerException e) {
				dispatchStoreyContainer = new ArrayList<>();
				dispatchStoreyContainer.add(p);
				dispatch.put(randomInitialStorey, dispatchStoreyContainer);
			}
		}

		if (animationBoost > 0) {
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void creatingThreads() {
		for (Map.Entry<Integer, List<Passenger>> entry : dispatch.entrySet()) {
			for (Passenger p : entry.getValue()) {
				Thread TransportationTask = new Thread(p);
				TransportationTask.start();
			}
		}
		;
	}

	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			label.setText(Constants.STARTING_TRANSPORTATION);
			button2 = new JButton();
			button2.setBackground(Color.CYAN);
			button2.setText("Abort");
			button2.setBounds(Constants.BUTTON_X, Constants.BUTTON_Y,
					Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT);
			button2.addActionListener(new ButtonListener2());
			panel.remove(button);
			panel.add(button2);
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	private class ButtonListener2 implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			abort = true;
			try {
				if (animationBoost < Constants.WAIT_ALL_THREADS_DIE)
					Thread.sleep(Constants.WAIT_ALL_THREADS_DIE);
				else
					Thread.sleep(animationBoost * 2);
			} catch (InterruptedException e) {
			}
			LOGGER.info(Constants.ABORTING_TRANSPORTATION);

			panel.remove(label);
			f.repaint();

			label = new JLabel(Constants.ABORTING_TRANSPORTATION);
			label.setBounds(Constants.BUTTON_X + 2 * Constants.BUTTON_WIDTH,
					Constants.BUTTON_Y, Constants.BUTTON_WIDTH * 2,
					Constants.BUTTON_HEIGHT);
			panel.add(label);

			lastButton(Color.RED);
		}
	}

	private void lastButton(Color c) {
		button3 = new JButton();
		button3.setBackground(c);
		button3.setText("VIEW LOF FILE");
		button3.setBounds(Constants.BUTTON_X, Constants.BUTTON_Y,
				Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT);
		button3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Desktop dt = Desktop.getDesktop();
				try {
					dt.open(Paths.get(Constants.LOG_FILENAME).toFile());
				} catch (IOException e) {
				}
			}
		});
		panel.add(button3);
		panel.remove(button2);
		panel.repaint();
	}
}