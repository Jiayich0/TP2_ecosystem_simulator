package simulator.model.strategy;

import java.util.List;

import simulator.model.animal.Animal;

public class SelectClosest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty()) {
            return null;
        }
		
		Animal closest = as.get(0);
		double minDist = a.getPosition().distanceTo(closest.getPosition());
		
		for(Animal animal: as) {
			double dist = a.getPosition().distanceTo(animal.getPosition());
			if(dist < minDist) {
				minDist = dist;
				closest = animal;
			}
		}
		
		return closest;
	}

}
