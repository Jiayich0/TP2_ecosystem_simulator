package simulator.model.strategy;

import java.util.List;

import simulator.model.animal.Animal;

public class SelectYoungest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty()) {
            return null;
        }
		
		Animal youngest = as.get(0);

		for(Animal animal: as) {
			if(animal.getAge() < youngest.getAge()) {
				youngest = animal;
			}
		}
		
		return youngest;
	}
	
}
