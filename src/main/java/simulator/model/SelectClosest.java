package simulator.model;

import java.util.List;

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
