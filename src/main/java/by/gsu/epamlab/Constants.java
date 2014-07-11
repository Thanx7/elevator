package by.gsu.epamlab;

public class Constants {
	// Main
	public static final String RESOURCES_PATH = "src/main/resources/";
	public static final String LOG_PROP_FILENAME = RESOURCES_PATH
			+ "log4j.properties";
	public static final String CONF_PROP_FILENAME = RESOURCES_PATH
			+ "config.properties";
	public static final String LOG_FILENAME = RESOURCES_PATH + "controller.log";
	public static final String STOREYS_NUMBER = "storeysNumber";
	public static final String ELEVATOR_CAPACITY = "elevatorCapacity";
	public static final String PASSENGERS_NUMBER = "passengersNumber";
	public static final String ANIMATION_BOOST = "animationBoost";

	// Passenger
	public final static String NOT_STARTED = "NOT_STARTED";
	public final static String IN_PROGRESS = "IN_PROGRESS";
	public final static String COMPLETED = "COMPLETED";
	public final static String ABORTED = "ABORTED";

	public static final int DISPATCH_STOREY_CONTAINER = 1;
	public static final int ELEVATOR_CONTAINER = 2;
	public static final int ARRIVAL_STOREY_CONTAINER = 3;

	// Apache Logging Service - log4j
	public final static String STARTING_TRANSPORTATION = "STARTING_TRANSPORTATION";
	public final static String COMPLETION_TRANSPORTATION = "COMPLETION_TRANSPORTATION";
	public final static String COMPLETION_LOG1 = " ----- dispatch.isEmpty(), elevatorContainer.isEmpty()";
	public final static String COMPLETION_LOG2 = " ----- all TransportationStateCompleted";
	public final static String COMPLETION_LOG3 = " ----- equalsFloors (destinationStorey == arrival/key)";
	public final static String COMPLETION_LOG4 = " ----- equalsQuantity (ArrivalMapQuantity == passengersNumber)";
	public final static String ABORTING_TRANSPORTATION = "ABORTING_TRANSPORTATION";

	// Animation
	public static final String ELEVATOR = RESOURCES_PATH + "elevator.jpg";
	public static final String PASSENGER = RESOURCES_PATH + "smiley.png";
	public static final int ELEVATOR_HEIGHT = 1000;
	public static final int ELEVATOR_WIDTH = 1400;
	public static final int PASSENGERS_DISTANCE = 15;
	public static final int ROW = 15;
	public static final int ELEVATOR_X = 240;
	public static final int ELEVATOR_Y = 350;
	public static final int PASSENGER_X = 50;
	public static final int PASSENGER_Y = 360;
	public static final int PASSENGERS_ELEVATOR_X = 250;
	public static final int ARRIVAL_DISPATCH_Y = 200;
	public static final String ARRIVAL = "Arrival:";
	public static final int ARRIVAL_Y = 120;
	public static final String DISPATCH = "Dispatch:";
	public static final int DISPATCH_Y = 750;
	public static final int ELEVATOR_RANDOM = 25;
	public static final int BUTTON_X = 0;
	public static final int BUTTON_Y = 0;
	public static final int BUTTON_WIDTH = 200;
	public static final int BUTTON_HEIGHT = 30;
	public static final int WAIT_ALL_THREADS_DIE = 100;
}