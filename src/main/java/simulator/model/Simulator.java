package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable {
	
	//TODO pr2
	private int _cols;
	private int _rows;
	private int _width;
	private int _height;
	private Factory<Animal> _animalsFactory;
	private Factory<Region> _regionsFactory;
	
	private RegionManager _regionManager;
	private List<Animal> _animals;
	private double _time;
	
	
	
	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory, Factory<Region> regionsFactory) {
		if (cols <= 0 || rows <= 0 || width <= 0 || height <= 0)
		    throw new IllegalArgumentException("Simulator: constructor: dimensiones invalidas");
		if (animalsFactory == null || regionsFactory == null)
		    throw new IllegalArgumentException("Simulator: constructor: factorias no pueden ser nulos");
		_cols = cols;
		_rows = rows;
		_width = width;
		_height = height;
		_animalsFactory = animalsFactory;
		_regionsFactory = regionsFactory;
		
		_regionManager = new RegionManager(cols, rows, width, height);
		_animals = new ArrayList<>();
		_time = 0.0;
	}
	
	private void setRegion(int row, int col, Region r) {
		if (r == null)
			throw new IllegalArgumentException("Simulator: region es nulo");

		_regionManager.setRegion(row, col, r);
	}
	
	public void setRegion(int row, int col, JSONObject rJson) {
		if (rJson == null)
			throw new IllegalArgumentException("Simulator: rJson es nulo");

		Region r = _regionsFactory.createInstance(rJson);
		setRegion(row, col, r);
	}
	
	private void addAnimal(Animal a) {
		if (a == null)
			throw new IllegalArgumentException("Simulator: animal es nulo");

		_animals.add(a);
		_regionManager.registerAnimal(a);
	}
	
	public void addAnimal(JSONObject aJson) {
		if (aJson == null)
			throw new IllegalArgumentException("Simulator: aJson es nulo");

		Animal a = _animalsFactory.createInstance(aJson);
		addAnimal(a);
	}
	
	public MapInfo getMapInfo() {
		return _regionManager;
	}
	
	public List<? extends AnimalInfo> getAnimals() {
		return Collections.unmodifiableList(_animals);
	}
	
	public double getTime() {
		return _time;
	}
	
	public void advance(double dt) {
		_time += dt;
		
		for (int i = _animals.size() - 1; i >= 0; i--) {
			Animal a = _animals.get(i);
			if (a.getState() == State.DEAD) {
				_regionManager.unregisterAnimal(a);
				_animals.remove(i);
			}
		}
		
		for (Animal a : _animals) {
			a.update(dt);
			_regionManager.updateanimalRegion(a);
		}
		
		_regionManager.updateAllRegions(dt);
		
		List<Animal> babies = new ArrayList<>();
		for (Animal a : _animals) {
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
		json.put("time", _time);
		json.put("state", _regionManager.asJSON());
		return json;
	}
}
