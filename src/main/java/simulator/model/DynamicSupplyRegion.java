package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {

	private double _actualFood;
	private double _growthFactor;
	
	private final static double FOOD_EAT_RATE_HERBS = 60.0;
	private final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
	private final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;

	public DynamicSupplyRegion(double initialFood, double growthFactor) {
		super();
		if (initialFood <= 0)
			throw new IllegalArgumentException("DynamicSupplyRegion: constructora -> InitialFood: debe ser positivo");
		if (growthFactor < 0)
			throw new IllegalArgumentException(
					"DynamicSupplyRegion: constructora -> GrowthFactor: no puede ser nagativo");
		_actualFood = initialFood;
		_growthFactor = growthFactor;
	}

	@Override
	public void update(double dt) {
		if (Utils.RAND.nextDouble() < 0.5) {
			_actualFood += _growthFactor * dt;
		}
	}

	@Override
	public double getFood(AnimalInfo a, double dt) {
		if (a.getDiet() == Diet.CARNIVORE) {
			return 0.0;
		}
		int n = getN();
		double amount = Math.min(_actualFood, FOOD_EAT_RATE_HERBS * Math.exp(-Math.max(0, n - FOOD_SHORTAGE_TH_HERBS) * FOOD_SHORTAGE_EXP_HERBS) * dt);
		_actualFood -= amount;
		return amount;
	}

	private int getN() {
		int n = 0;
		for (Animal animal : _animals) {
			if (animal.getDiet() == Diet.HERBIVORE) {
				n++;
			}
		}
		return n;
	}
}
