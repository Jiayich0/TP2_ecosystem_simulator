package simulator.misc;

public final class Const {

	// Animal
	public final static double INIT_ENERGY = 100.0;
	public final static double MUTATION_TOLERANCE = 0.2;
	public final static double NEARBY_FACTOR = 60.0;

	// Sheep & Wolf & (subclases Animal)
	public final static double COLLISION_RANGE = 8.0;
	public final static double HUNGER_DECAY_EXP_FACTOR = 0.007;
	public final static double MAX_ENERGY = 100.0;
	public final static double MAX_DESIRE = 100.0;

	// Sheep
	public final static String SHEEP_GENETIC_CODE = "Sheep";
	public final static double INIT_SIGHT_SHEEP = 40.0;
	public final static double INIT_SPEED_SHEEP = 35.0;
	public final static double BOOST_FACTOR_SHEEP = 2.0;
	public final static double MAX_AGE_SHEEP = 8.0;
	public final static double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
	public final static double FOOD_DROP_RATE_SHEEP = 20.0;
	public final static double DESIRE_THRESHOLD_SHEEP = 65.0;
	public final static double DESIRE_INCREASE_RATE_SHEEP = 40.0;
	public final static double PREGNANT_PROBABILITY_SHEEP = 0.9;

	// Wolf
	public final static String WOLF_GENETIC_CODE = "Wolf";
	public final static double INIT_SIGHT_WOLF = 50;
	public final static double INIT_SPEED_WOLF = 60;
	public final static double BOOST_FACTOR_WOLF = 3.0;
	public final static double MAX_AGE_WOLF = 14.0;
	public final static double FOOD_THRSHOLD_WOLF = 50.0;
	public final static double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
	public final static double FOOD_DROP_RATE_WOLF = 18.0;
	public final static double FOOD_DROP_DESIRE_WOLF = 10.0;
	public final static double FOOD_EAT_VALUE_WOLF = 50.0;
	public final static double DESIRE_THRESHOLD_WOLF = 65.0;
	public final static double DESIRE_INCREASE_RATE_WOLF = 30.0;
	public final static double PREGNANT_PROBABILITY_WOLF = 0.75;

	// DefaultRegion & DynamicSupplyRegion & (subclases Region)
	public final static double FOOD_EAT_RATE_HERBS = 60.0;
	public final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
	public final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;

	// DynamicSupplyRegionBuilder
	public final static double FACTOR = 2.0;
	public final static double INIT_FOOD = 1000.0;

	// Main
	public final static Double DEFAULT_TIME = 10.0; // in seconds
	public final static Double DEFAULT_DT = 0.03;

	// ControlPanel
	public final static String OPEN_ICON = "icons/open.png";
	public final static String VIEWER_ICON = "icons/viewer.png";
	public final static String REGIONS_ICON = "icons/regions.png";
	public final static String RUN_ICON = "icons/run.png";
	public final static String STOP_ICON = "icons/stop.png";
	public final static String EXIT_ICON = "icons/exit.png";
	
	public final static String OPEN_TOOLTIP = "Load an input file into the simulator";
	public final static String VIEWER_TOOLTIP = "Map Viewer";
	public final static String REGIONS_TOOLTIP = "Change Regions";
	public final static String RUN_TOOLTIP = "Run the simulator";
	public final static String STOP_TOOLTIP = "Stop the simulator";
	public final static String STEPS_TOOLTIP = "Simulation steps to run: 1-10000";
	public final static String DELTA_TIME_TOOLTIP = "Real time (seconds) corresponding to a step";
	public final static String EXIT_TOOLTIP = "Exit";

	public final static int STEPS_INITIAL_VALUE = 10000;
	public final static int STEPS_MIN = 1;
	public final static int STEPS_MAX = Integer.MAX_VALUE;
	public final static int STEPS_INCREMENT = 1;
	public final static int DELTA_TIME_TEXT_COLS = 5;

	// Para la velocidad de la demo
	public static final double DEMO_SPEED_FACTOR = 5.25;
}
