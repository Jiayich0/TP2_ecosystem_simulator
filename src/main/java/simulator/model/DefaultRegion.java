package simulator.model;

public class DefaultRegion extends Region {
	
	private final static double FOOD_EAT_RATE_HERBS = 60.0;
	private final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
	private final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;
	
	@Override
	public void update(double dt) {
		// nada
	}

	@Override
	public double getFood(AnimalInfo a, double dt) {
		if (a.getDiet() == Diet.CARNIVORE) {
			return 0.0;
		}
		int n = getN();
		return FOOD_EAT_RATE_HERBS * Math.exp(-Math.max(0, n - FOOD_SHORTAGE_TH_HERBS) * FOOD_SHORTAGE_EXP_HERBS) * dt;
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