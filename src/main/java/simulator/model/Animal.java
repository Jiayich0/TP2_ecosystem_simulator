package simulator.model;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

	protected String _geneticCode;
	protected Diet _diet;
	protected State _state;
	protected Vector2D _pos;
	protected Vector2D _dest;
	protected double _energy;
	protected double _speed;
	protected double _age;
	protected double _desire;
	protected double _sightRange;
	protected Animal _mateTarget;
	protected Animal _baby;
	protected AnimalMapView _regionMngr;
	protected SelectionStrategy _mateStrategy;


	protected Animal(String geneticCode, Diet diet, double sightRange, double initSpeed, SelectionStrategy mateStrategy,
			Vector2D pos) {
		if (geneticCode.isEmpty())
			throw new IllegalArgumentException("Animal: constructora -> geneticCode: no puede ser vacío");
		if (sightRange <= 0)
			throw new IllegalArgumentException("Animal: constructora -> sightRange: debe ser positivo");
		if (initSpeed <= 0)
			throw new IllegalArgumentException("Animal: constructora -> initSpeed: debe ser positivo");
		if (mateStrategy == null)
			throw new IllegalArgumentException("Animal: constructora -> mateStrategy: no puede ser nulo");
		if (diet == null)
			throw new IllegalArgumentException("Animal: constructora -> diet: no puede ser nulo");

		_geneticCode = geneticCode;
		_diet = diet;
		_sightRange = sightRange;
		_pos = pos;
		_mateStrategy = mateStrategy;
		_speed = Utils.getRandomizedParameter(initSpeed, 0.1);
		_state = State.NORMAL;
		_energy = Const.INIT_ENERGY;
		_desire = 0.0;
		_dest = null;
		_mateTarget = null;
		_baby = null;
		_regionMngr = null;
	}

	protected Animal(Animal p1, Animal p2) {
		_dest = null;
		_mateTarget = null;
		_baby = null;
		_regionMngr = null;
		_state = State.NORMAL;
		_desire = 0.0;
		_geneticCode = p1._geneticCode;
		_diet = p1._diet;
		_mateStrategy = p2._mateStrategy;
		_energy = (p1._energy + p2._energy) / 2;
		_pos = p1.getPosition().plus(Vector2D.get_random_vector(-1, 1).scale(Const.NEARBY_FACTOR * (Utils.RAND.nextGaussian() + 1)));
		_sightRange = Utils.getRandomizedParameter((p1.getSightRange() + p2.getSightRange()) / 2, Const.MUTATION_TOLERANCE);
		_speed = Utils.getRandomizedParameter((p1.getSpeed() + p2.getSpeed()) / 2, Const.MUTATION_TOLERANCE);
	}

	
	public void init(AnimalMapView regMngr) {
		_regionMngr = regMngr;

		double width = _regionMngr.getWidth();
		double height = _regionMngr.getHeight();
		
		// random pos
		if (_pos == null) {
			double x = Utils.RAND.nextDouble() * width;
			double y = Utils.RAND.nextDouble() * height;
			_pos = new Vector2D(x, y);
		} else { // si cae fuera del mapa
			double x = _pos.getX();
			double y = _pos.getY();
			
			while (x >= width) x = (x - width);
			while (x < 0) x = (x + width);
			while (y >= height) y = (y - height);
			while (y < 0) y = (y + height);

			_pos = new Vector2D(x, y);
		}
		
		// random dest
		double x = Utils.RAND.nextDouble() * width;
		double y = Utils.RAND.nextDouble() * height;
		_dest = new Vector2D(x, y);
	}

	public Animal deliverBaby() {
		Animal baby = _baby;
		_baby = null;
		return baby;
	}

	protected void move(double speed) {
		_pos = _pos.plus(_dest.minus(_pos).direction().scale(speed));
	}

	protected void setState(State state) {
		_state = state;
		switch (state) {
		case NORMAL:
			setNormalStateAction();
			break;
		case HUNGER:
			setHungerStateAction();
			break;
		case MATE:
			setMateStateAction();
			break;
		case DANGER:
			setDangerStateAction();
			break;
		case DEAD:
			setDeadStateAction();
			break;
		default:
			break;
		}
	}

	abstract protected void setNormalStateAction();

	abstract protected void setMateStateAction();

	abstract protected void setHungerStateAction();

	abstract protected void setDangerStateAction();

	abstract protected void setDeadStateAction();

	public JSONObject asJSON() {
		JSONObject json = new JSONObject();
		json.put("pos", _pos.asJSONArray());
		json.put("gcode", _geneticCode);
		json.put("diet", _diet.toString());
		json.put("state", _state.toString());
		return json;
	}

	// getters
	@Override
	public State getState() {
		return _state;
	}

	@Override
	public Vector2D getPosition() {
		return _pos;
	}

	@Override
	public String getGeneticCode() {
		return _geneticCode;
	}

	@Override
	public Diet getDiet() {
		return _diet;
	}

	@Override
	public double getSpeed() {
		return _speed;
	}

	@Override
	public double getSightRange() {
		return _sightRange;
	}

	@Override
	public double getEnergy() {
		return _energy;
	}

	@Override
	public double getAge() {
		return _age;
	}

	@Override
	public Vector2D getDestination() {
		return _dest;
	}

	@Override
	public boolean isPregnant() {
		return _baby != null;
	}
}
