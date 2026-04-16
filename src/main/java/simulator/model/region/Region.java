package simulator.model.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.Entity;
import simulator.model.animal.Animal;
import simulator.model.animal.AnimalInfo;

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

	public List<AnimalInfo> getAnimalsInfo() {
		return new ArrayList<>(animals); // se puede usar Collections.unmodifiableList(animals);
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
