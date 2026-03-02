package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegionManager implements AnimalMapView {
	
	private int _mapWidth;
	private int _mapHeight;
	private int _cols;
	private int _rows;
	private int _regWidth;
	private int _regHeight;
	private Region[][] _regions;
	private Map<Animal, Region> _animalRegion;
	
	public RegionManager(int cols, int rows, int width, int height) {
		if (cols <= 0) throw new IllegalArgumentException("RegionManager: constructora -> cols: debe ser positiva");
		if (rows <= 0) throw new IllegalArgumentException("RegionManager: constructora -> rows: debe ser positiva");
		if (width <= 0) throw new IllegalArgumentException("RegionManager: constructora -> width: debe ser positiva");
		if (height <= 0) throw new IllegalArgumentException("RegionManager: constructora -> height: debe ser positiva");
		if (width % cols != 0) throw new IllegalArgumentException("RegionManager: width debe ser divisible entre cols");
		if (height % rows != 0) throw new IllegalArgumentException("RegionManager: height debe ser divisible entre rows");
		_cols = cols;
		_rows = rows;
		_mapWidth = width;
		_mapHeight = height;
		_regWidth = width / cols;
		_regHeight = height / rows;
		_regions = new Region[rows][cols];
		_animalRegion = new HashMap<>();
		
		for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                _regions[i][j] = new DefaultRegion();
            }
        }
	}
	
	void setRegion(int row, int col, Region r) {
		Region oldReg = _regions[row][col];
		_regions[row][col] = r;
		
		List<Animal> oldAnimals = new ArrayList<>(oldReg.getAnimals());
		
		for(Animal a : oldAnimals) {
			oldReg.removeAnimal(a);
			r.addAnimal(a);
			_animalRegion.put(a, r);
		}
	}
	
	private Region getReg(Animal a) {
		int y = (int)a.getPosition().getY();
		int x = (int)a.getPosition().getX();
		int yReg = y / _regHeight;
		int xReg = x / _regWidth;
		return _regions[yReg][xReg];
	}
	
	void registerAnimal(Animal a) {
		a.init(this);
		
		Region reg = getReg(a);
		reg.addAnimal(a);
		_animalRegion.put(a, reg);
	}
	
	void unregisterAnimal(Animal a) {
		Region reg = _animalRegion.get(a);
		if (reg != null) {
	        reg.removeAnimal(a);
	    }
		_animalRegion.remove(a);
	}
	
	void updateanimalRegion(Animal a) {
		Region newReg = getReg(a);
		Region oldReg = _animalRegion.get(a);
		
		if (oldReg != newReg) {
			if (oldReg != null) {
	            oldReg.removeAnimal(a);
	        }
			newReg.addAnimal(a);
	        _animalRegion.put(a, newReg);
		}
	}
	
	@Override
	public double getFood(AnimalInfo a, double dt) {
		Animal animal = (Animal)a;
		Region r = _animalRegion.get(animal);
		return r.getFood(a, dt);
	}
	
	void updateAllRegions(double dt) {
		for (int i = 0; i < _rows; i++) {
            for (int j = 0; j < _cols; j++) {
            	_regions[i][j].update(dt);
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
		double minX = aX - r;
		double maxX = aX + r;
		double minY = aY - r;
		double maxY = aY + r;
		
		// evitar salirse del mapa (mapHeight-Width)
		if (minX < 0) minX = 0;
		if (minY < 0) minY = 0;
		if (maxX >= _mapWidth) maxX = _mapWidth - 1;
		if (maxY >= _mapHeight) maxY = _mapHeight - 1;
		
		// se podría buscar con esta iformación en el mapa pero sería muy caro, mejor restringirlo a regiones:
		
		// de coords/pos a region
		int minCol = (int)(minX / _regWidth);
		int maxCol = (int)(maxX / _regWidth);
		int minRow = (int)(minY / _regHeight);
		int maxRow = (int)(maxY / _regHeight);
		
		// evitar salirse del mapa (matriz region[rows][cols]
		if (minCol < 0) minCol = 0;
		if (minRow < 0) minRow = 0;
		if (maxCol >= _cols) maxCol = _cols - 1;
		if (maxRow >= _rows) maxRow = _rows - 1;
		
		// iterar sobre las regiones que alcanzan el rango de vision
		for (int row = minRow; row <= maxRow; row++) {
			for (int col = minCol; col <= maxCol; col++) {
				Region reg = _regions[row][col];
				
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
		
		for (int i = 0; i < _rows; i++) {
            for (int j = 0; j < _cols; j++) {
            	Region reg = _regions[i][j];
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
		return _cols;
	}
	@Override
	public int getRows() {
		return _rows;
	}
	@Override
	public int getWidth() {
		return _mapWidth;
	}
	@Override
	public int getHeight() {
		return _mapHeight;
	}
	@Override
	public int getRegionWidth() {
		return _regWidth;
	}
	@Override
	public int getRegionHeight() {
		return _regHeight;
	}
}
