import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import by.gsu.epamlab.Constants;
import by.gsu.epamlab.Controller;

public class Runner {

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(Constants.CONF_PROP_FILENAME);
			prop.load(input);
			int storeysNumber = Integer.parseInt(prop
					.getProperty(Constants.STOREYS_NUMBER));
			int elevatorCapacity = Integer.parseInt(prop
					.getProperty(Constants.ELEVATOR_CAPACITY));
			int passengersNumber = Integer.parseInt(prop
					.getProperty(Constants.PASSENGERS_NUMBER));
			int animationBoost = Integer.parseInt(prop
					.getProperty(Constants.ANIMATION_BOOST));
			new Controller(storeysNumber, elevatorCapacity, passengersNumber,
					animationBoost);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}