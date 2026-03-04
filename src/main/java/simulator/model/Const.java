package simulator.model;

public final class Const {
	
	public Const() { }
	
	// Animal
	public final static double INIT_ENERGY = 100.0;
	protected final static double MUTATION_TOLERANCE = 0.2;
	protected final static double NEARBY_FACTOR = 60.0;
	
	// Sheep & Wolf & (subclases Animal)
	protected final static double COLLISION_RANGE = 8.0;
	protected final static double HUNGER_DECAY_EXP_FACTOR = 0.007;
	protected final static double MAX_ENERGY = 100.0;
	protected final static double MAX_DESIRE = 100.0;
	
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
	
}
