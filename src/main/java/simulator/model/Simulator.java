package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;
import simulator.model.animal.Animal;
import simulator.model.animal.AnimalInfo;
import simulator.model.region.MapInfo;
import simulator.model.region.Region;
import simulator.model.region.RegionManager;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

	@SuppressWarnings("unused")
	private int cols, rows, width, height;

	private Factory<Animal> animalsFactory;
	private Factory<Region> regionsFactory;

	private RegionManager regionManager;
	private List<Animal> animals;
	private double time;

	private List<EcoSysObserver> observers;

	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory,
			Factory<Region> regionsFactory) {
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

		this.observers = new ArrayList<>();
	}

	public void reset(int cols, int rows, int width, int height) {
		if (cols <= 0 || rows <= 0 || width <= 0 || height <= 0)
			throw new IllegalArgumentException("Simulator: reset: dimensiones invalidas");
		this.cols = cols;
		this.rows = rows;
		this.width = width;
		this.height = height;

		this.regionManager = new RegionManager(cols, rows, width, height);
		this.animals = new ArrayList<>();
		this.time = 0.0;

		notifyOnReset();
	}

	private void setRegion(int row, int col, Region r) {
		if (r == null)
			throw new IllegalArgumentException("Simulator: region es nulo");

		regionManager.setRegion(row, col, r);
		notifyOnRegionSet(row, col, r);
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
		notifyOnAnimalAdded(a);
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
			if (a.getState() == Animal.State.DEAD) {
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

		notifyOnAdvance(dt);
	}

	@Override
	public JSONObject asJSON() {
		JSONObject json = new JSONObject();
		json.put("time", time);
		json.put("state", regionManager.asJSON());
		return json;
	}

	@Override
	public void addObserver(EcoSysObserver o) {
		if (o == null)
			throw new IllegalArgumentException("observer nulo");
		if (!observers.contains(o)) {
			observers.add(o);
			notifyOnRegister(o);
		}
	}

	@Override
	public void removeObserver(EcoSysObserver o) {
		if (o == null)
			throw new IllegalArgumentException("observer nulo");

		observers.remove(o);
	}

	private List<AnimalInfo> getAnimalsInfoList() {
		return new ArrayList<>(animals);
	}

	private void notifyOnRegister(EcoSysObserver o) {
		o.onRegister(time, regionManager, getAnimalsInfoList());
	}

	private void notifyOnReset() {
		List<AnimalInfo> animalsInfo = getAnimalsInfoList();
		for (EcoSysObserver o : observers) {
			o.onReset(time, regionManager, animalsInfo);
		}
	}

	private void notifyOnAnimalAdded(Animal a) {
		List<AnimalInfo> animalsInfo = getAnimalsInfoList();
		for (EcoSysObserver o : observers) {
			o.onAnimalAdded(time, regionManager, animalsInfo, a);
		}
	}

	private void notifyOnRegionSet(int row, int col, Region r) {
		for (EcoSysObserver o : observers) {
			o.onRegionSet(row, col, regionManager, r);
		}
	}

	private void notifyOnAdvance(double dt) {
		List<AnimalInfo> animalsInfo = getAnimalsInfoList();
		for (EcoSysObserver o : observers) {
			o.onAdvance(time, regionManager, animalsInfo, dt);
		}
	}
}
