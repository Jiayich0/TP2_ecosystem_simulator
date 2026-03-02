package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {
	protected List<Animal> _animals;

	public Region() {
		_animals = new ArrayList<>();
	}

	final void addAnimal(Animal a) {
		_animals.add(a);
	}

	final void removeAnimal(Animal a) {
		_animals.remove(a);
	}

	final List<Animal> getAnimals() { //
		return Collections.unmodifiableList(_animals);
	}

	public JSONObject asJSON() {
		JSONObject json = new JSONObject();
		JSONArray animals = new JSONArray();
		for (Animal a : _animals) {
			animals.put(a.asJSON());
		}
		json.put("animals", animals);
		return json;
	}
}
