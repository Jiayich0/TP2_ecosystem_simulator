package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable {
	
	//TODO pr2
	private int cols;
	private int rows;
	private int width;
	private int height;
	private Factory<Animal> animalsFactory;
	private Factory<Region> regionsFactory;
	
	private RegionManager regionManager;
	private List<Animal> animals;
	private double time;
	
	
	
	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory, Factory<Region> regionsFactory) {
		if (cols <= 0 || rows <= 0 || width <= 0 || height <= 0)
		    throw new IllegalArgumentException("Simulator: constructor: dimensiones invalidas");
		if (animalsFactory == null || regionsFactory == null)
		    throw new IllegalArgumentException("Simulator: constructor: factorias no pueden ser nulos");
		this.cols = cols;
		this.rows = rows;
		this.width = width;
		this.height = height;
		this.animalsFactory = animalsFactory;
		this.regionsFactory = regionsFactory;
		
		this.regionManager = new RegionManager(cols, rows, width, height);
		this.animals = new ArrayList<>();
		this.time = 0.0;
	}
	
	private void setRegion(int row, int col, Region r) {
		if (r == null)
			throw new IllegalArgumentException("Simulator: region es nulo");

		regionManager.setRegion(row, col, r);
	}
	
	public void setRegion(int row, int col, JSONObject rJson) {
		if (rJson == null)
			throw new IllegalArgumentException("Simulator: rJson es nulo");

		Region r = regionsFactory.createInstance(rJson);
		setRegion(row, col, r);
	}
	
	private void addAnimal(Animal a) {
		if (a == null)
			throw new IllegalArgumentException("Simulator: animal es nulo");

		animals.add(a);
		regionManager.registerAnimal(a);
	}
	
	public void addAnimal(JSONObject aJson) {
		if (aJson == null)
			throw new IllegalArgumentException("Simulator: aJson es nulo");

		Animal a = animalsFactory.createInstance(aJson);
		addAnimal(a);
	}
	
	public MapInfo getMapInfo() {
		return regionManager;
	}
	
	public List<? extends AnimalInfo> getAnimals() {
		return Collections.unmodifiableList(animals);
	}
	
	public double getTime() {
		return time;
	}
	
	public void advance(double dt) {
		time += dt;
		
		for (int i = animals.size() - 1; i >= 0; i--) {
			Animal a = animals.get(i);
			if (a.getState() == State.DEAD) {
				regionManager.unregisterAnimal(a);
				animals.remove(i);
			}
		}
		
		for (Animal a : animals) {
			a.update(dt);
			regionManager.updateanimalRegion(a);
		}
		
		regionManager.updateAllRegions(dt);
		
		List<Animal> babies = new ArrayList<>();
		for (Animal a : animals) {
			if (a.isPregnant()) {
				Animal b = a.deliverBaby();
				if (b != null) {
					babies.add(b);
				}
			}
		}
		for (Animal b : babies) {
			addAnimal(b);
		}
	}
	
	@Override
	public JSONObject asJSON() {
		JSONObject json = new JSONObject();
		json.put("time", time);
		json.put("state", regionManager.asJSON());
		return json;
	}
}
