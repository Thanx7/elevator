package by.gsu.epamlab.beans;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import by.gsu.epamlab.Constants;
import by.gsu.epamlab.Controller;

public class Passenger extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	private Controller controller;
	private int passengerId;
	private int initialStorey;
	private int destinationStorey;
	private int container;
	private String transportationState;
	private byte[] lockEntry = new byte[0];
	private byte[] lockOut = new byte[0];
	private static volatile int countNotifications1 = 0;
	private static volatile int countNotifications2 = 0;

	public Image img = new ImageIcon(Constants.PASSENGER).getImage();
	public int x = Constants.PASSENGER_X;
	public int xMemory;
	public int y = Constants.PASSENGER_Y;

	public Passenger(Controller controller, int id, int initialStorey,
			int destinationStorey) {
		super();
		this.controller = controller;
		this.passengerId = id;
		this.initialStorey = initialStorey;
		this.destinationStorey = destinationStorey;
		this.container = Constants.DISPATCH_STOREY_CONTAINER;
		this.transportationState = Constants.NOT_STARTED;
		x = x + (id - 1) * Constants.PASSENGERS_DISTANCE;
	}

	public Image getImg() {
		return img;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getPassengerId() {
		return passengerId;
	}

	public int getInitialStorey() {
		return initialStorey;
	}

	public int getDestinationStorey() {
		return destinationStorey;
	}

	public int getPassangerContainer() {
		return container;
	}

	public String getTransportationState() {
		return transportationState;
	}

	public byte[] getLockEntry() {
		return lockEntry;
	}

	public byte[] getLockOut() {
		return lockOut;
	}

	public void setContainer(int container) {
		this.container = container;
	}

	public void setTransportationState(String transportationState) {
		this.transportationState = transportationState;
	}

	@Override
	public void paint(Graphics g) {
		g = (Graphics2D) g;
		g.drawImage(img, x, y, null);
		g.drawString("id:" + Integer.toString(getPassengerId()), x, y - 3
				* Constants.ROW);
		g.drawString("" + Integer.toString(getInitialStorey()), x, y - 2
				* Constants.ROW);
		g.drawString("->" + Integer.toString(getDestinationStorey()), x, y
				- Constants.ROW);
	}

	// animation
	public void loadElevator() {
		if (controller.getPassengersNumber() > Constants.ELEVATOR_RANDOM) {
			xMemory = x;
			Random generator = new Random();
			x = 100 + generator.nextInt(520);
		} else {
			x = x + Constants.PASSENGERS_ELEVATOR_X;
		}
		y = y - Constants.ARRIVAL_DISPATCH_Y / 2;
	}

	// animation
	public void unloadElevator() {
		if (controller.getPassengersNumber() > Constants.ELEVATOR_RANDOM) {
			x = xMemory;
		} else {
			x = x - Constants.PASSENGERS_ELEVATOR_X;
		}
		y = y - Constants.ARRIVAL_DISPATCH_Y;
	}

	@Override
	public void run() {
		this.setTransportationState(Constants.IN_PROGRESS);

		synchronized (lockEntry) {
			try {
				lockEntry.wait();
			} catch (InterruptedException e) {
			}
		}

		if (!controller.isAbort()) {
			// passenger goes to the elevator
			controller.load(this);
			countNotifications1++;
			this.setContainer(Constants.ELEVATOR_CONTAINER);

			// notify if all passengers loaded
			byte[] elevatorLock = controller.getElevatorLock();
			synchronized (elevatorLock) {
				if (countNotifications1 == controller
						.getIssuedNotificationsLoad()) {
					countNotifications1 = 0;
					elevatorLock.notify();
				}
			}

			synchronized (lockOut) {
				try {
					lockOut.wait();
				} catch (InterruptedException e) {
				}
			}
			if (!controller.isAbort()) {
				// passenger goes to the arrival floor
				controller.unload(this);
				countNotifications2++;
				this.setContainer(Constants.ARRIVAL_STOREY_CONTAINER);
				this.setTransportationState(Constants.COMPLETED);

				// notify if all passengers unloaded
				synchronized (elevatorLock) {
					if (countNotifications2 == controller
							.getIssuedNotificationsUnload()) {
						countNotifications2 = 0;
						elevatorLock.notify();
					}
				}
			} else {
				this.setTransportationState(Constants.ABORTED);
			}
		} else {
			this.setTransportationState(Constants.ABORTED);
		}
	}

	@Override
	public String toString() {
		return "Passenger [id=" + passengerId + ", iS=" + initialStorey
				+ ", dS=" + destinationStorey + ", cont=" + container
				+ ", trans=" + transportationState + "]";
	}
}