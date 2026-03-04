package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {

	private double actualFood;
	private double growthFactor;
	

	public DynamicSupplyRegion(double initialFood, double growthFactor) {
		super();
		if (initialFood <= 0)
			throw new IllegalArgumentException("DynamicSupplyRegion: constructora -> InitialFood: debe ser positivo");
		if (growthFactor < 0)
			throw new IllegalArgumentException(
					"DynamicSupplyRegion: constructora -> GrowthFactor: no puede ser nagativo");
		this.actualFood = initialFood;
		this.growthFactor = growthFactor;
	}

	@Override
	public void update(double dt) {
		if (Utils.RAND.nextDouble() < 0.5) {
			actualFood += growthFactor * dt;
		}
	}

	@Override
	public double getFood(AnimalInfo a, double dt) {
		if (a.getDiet() == Diet.CARNIVORE) {
			return 0.0;
		}
		int n = getN();
		double amount = Math.min(actualFood, Const.FOOD_EAT_RATE_HERBS * Math.exp(-Math.max(0, n - Const.FOOD_SHORTAGE_TH_HERBS) * Const.FOOD_SHORTAGE_EXP_HERBS) * dt);
		actualFood -= amount;
		return amount;
	}

	private int getN() {
		int n = 0;
		for (Animal animal : animals) {
			if (animal.getDiet() == Diet.HERBIVORE) {
				n++;
			}
		}
		return n;
	}
}
