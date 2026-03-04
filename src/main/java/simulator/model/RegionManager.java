package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;

public class RegionManager implements AnimalMapView {
	
	private int mapWidth;
	private int mapHeight;
	private int cols;
	private int rows;
	private int regWidth;
	private int regHeight;
	private Region[][] regions;
	private Map<Animal, Region> animalRegion;
	
	public RegionManager(int cols, int rows, int width, int height) {
		if (cols <= 0) throw new IllegalArgumentException("RegionManager: constructora -> cols: debe ser positiva");
		if (rows <= 0) throw new IllegalArgumentException("RegionManager: constructora -> rows: debe ser positiva");
		if (width <= 0) throw new IllegalArgumentException("RegionManager: constructora -> width: debe ser positiva");
		if (height <= 0) throw new IllegalArgumentException("RegionManager: constructora -> height: debe ser positiva");
		if (width % cols != 0) throw new IllegalArgumentException("RegionManager: width debe ser divisible entre cols");
		if (height % rows != 0) throw new IllegalArgumentException("RegionManager: height debe ser divisible entre rows");
		this.cols = cols;
		this.rows = rows;
		this.mapWidth = width;
		this.mapHeight = height;
		this.regWidth = width / cols;
		this.regHeight = height / rows;
		this.regions = new Region[rows][cols];
		this.animalRegion = new HashMap<>();
		
		for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                regions[i][j] = new DefaultRegion();
            }
        }
	}
	
	void setRegion(int row, int col, Region r) {
		Region oldReg = regions[row][col];
		regions[row][col] = r;
		
		List<Animal> oldAnimals = new ArrayList<>(oldReg.getAnimals());
		
		for(Animal a : oldAnimals) {
			oldReg.removeAnimal(a);
			r.addAnimal(a);
			animalRegion.put(a, r);
		}
	}
	
	private Region getReg(Animal a) {
		int y = (int)a.getPosition().getY();
		int x = (int)a.getPosition().getX();
		int yReg = y / regHeight;
		int xReg = x / regWidth;
		return regions[yReg][xReg];
	}
	
	void registerAnimal(Animal a) {
		a.init(this);
		
		Region reg = getReg(a);
		reg.addAnimal(a);
		animalRegion.put(a, reg);
	}
	
	void unregisterAnimal(Animal a) {
		Region reg = animalRegion.get(a);
		if (reg != null) {
	        reg.removeAnimal(a);
	    }
		animalRegion.remove(a);
	}
	
	void updateanimalRegion(Animal a) {
		Region newReg = getReg(a);
		Region oldReg = animalRegion.get(a);
		
		if (oldReg != newReg) {
			if (oldReg != null) {
	            oldReg.removeAnimal(a);
	        }
			newReg.addAnimal(a);
	        animalRegion.put(a, newReg);
		}
	}
	
	@Override
	public double getFood(AnimalInfo a, double dt) {
		Animal animal = (Animal)a;
		Region r = animalRegion.get(animal);
		return r.getFood(a, dt);
	}
	
	void updateAllRegions(double dt) {
		for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
            	regions[i][j].update(dt);
            }
        }
	}
	
	@Override
	public List<Animal> getAnimalsInRange(Animal a, Predicate<Animal> filter) {
		List<Animal> animalesVisibles = new ArrayList<>();
		
		// coords actuales y radio
		double r = a.getSightRange();
		double aX = a.getPosition().getX();
		double aY = a.getPosition().getY();
		
		// caja visible del animal (cuadrado)
		double minX = Utils.constrainValueInRange(aX - r, 0.0, mapWidth - 1);
		double maxX = Utils.constrainValueInRange(aX + r, 0.0, mapWidth - 1);
		double minY = Utils.constrainValueInRange(aY - r, 0.0, mapHeight - 1);
		double maxY = Utils.constrainValueInRange(aY + r, 0.0, mapHeight - 1);
		
		// por regiones
		int minCol = (int) Utils.constrainValueInRange(minX / regWidth, 0, cols - 1);
		int maxCol = (int) Utils.constrainValueInRange(maxX / regWidth, 0, cols - 1);
		int minRow = (int) Utils.constrainValueInRange(minY / regHeight, 0, rows - 1);
		int maxRow = (int) Utils.constrainValueInRange(maxY / regHeight, 0, rows - 1);
		
		// iterar sobre las regiones que alcanzan el rango de vision
		for (int row = minRow; row <= maxRow; row++) {
			for (int col = minCol; col <= maxCol; col++) {
				Region reg = regions[row][col];
				
				for (Animal otroAnimal : reg.getAnimals()) {
					if (otroAnimal == a) continue;
					if (!filter.test(otroAnimal)) continue;
					// con distnaceTo la "caja" se vuelve "circular"
					if (a.getPosition().distanceTo(otroAnimal.getPosition()) <= r) {
						animalesVisibles.add(otroAnimal);
					}
				}
			}
		}
		return animalesVisibles;
	}
	
	public JSONObject asJSON() {
		JSONObject json = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
            	Region reg = regions[i][j];
            	JSONObject jsonReg = new JSONObject();
            	
            	jsonReg.put("row", i);
            	jsonReg.put("col", j);
            	jsonReg.put("data", reg.asJSON());
            	
        		jsonArr.put(jsonReg);
            }
		}
		json.put("regions", jsonArr);
		
		return json;
	}	
	
	@Override
	public int getCols() {
		return cols;
	}
	@Override
	public int getRows() {
		return rows;
	}
	@Override
	public int getWidth() {
		return mapWidth;
	}
	@Override
	public int getHeight() {
		return mapHeight;
	}
	@Override
	public int getRegionWidth() {
		return regWidth;
	}
	@Override
	public int getRegionHeight() {
		return regHeight;
	}
}
