package simulator.model.region;

import simulator.model.animal.AnimalInfo;

public interface FoodSupplier {
	double getFood(AnimalInfo a, double dt);
}
