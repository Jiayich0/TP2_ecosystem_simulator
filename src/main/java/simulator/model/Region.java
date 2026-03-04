package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {
	protected List<Animal> animals;

	public Region() {
		this.animals = new ArrayList<>();
	}

	final void addAnimal(Animal a) {
		animals.add(a);
	}

	final void removeAnimal(Animal a) {
		animals.remove(a);
	}

	final List<Animal> getAnimals() { //
		return Collections.unmodifiableList(animals);
	}

	public JSONObject asJSON() {
		JSONObject json = new JSONObject();
		JSONArray jsArray = new JSONArray();
		for (Animal a : animals) {
			jsArray.put(a.asJSON());
		}
		json.put("animals", jsArray);
		return json;
	}
}
