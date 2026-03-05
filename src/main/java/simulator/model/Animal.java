package simulator.model;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

	protected String geneticCode;
	protected Diet diet;
	protected State state;
	protected Vector2D pos;
	protected Vector2D dest;
	protected double energy;
	protected double speed;
	protected double age;
	protected double desire;
	protected double sightRange;
	protected Animal mateTarget;
	protected Animal baby;
	protected AnimalMapView regionMngr;
	protected SelectionStrategy mateStrategy;


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

		this.geneticCode = geneticCode;
		this.diet = diet;
		this.sightRange = sightRange;
		this.pos = pos;
		this.mateStrategy = mateStrategy;
		this.speed = Utils.getRandomizedParameter(initSpeed, 0.1);
		this.state = State.NORMAL;
		this.energy = Const.INIT_ENERGY;
		this.desire = 0.0;
		this.dest = null;
		this.mateTarget = null;
		this.baby = null;
		this.regionMngr = null;
	}

	protected Animal(Animal p1, Animal p2) {
		this.dest = null;
		this.mateTarget = null;
		this.baby = null;
		this.regionMngr = null;
		this.state = State.NORMAL;
		this.desire = 0.0;
		this.geneticCode = p1.geneticCode;
		this.diet = p1.diet;
		this.mateStrategy = p2.mateStrategy;
		this.energy = (p1.energy + p2.energy) / 2;
		this.pos = p1.getPosition().plus(Vector2D.get_random_vector(-1, 1).scale(Const.NEARBY_FACTOR * (Utils.RAND.nextGaussian() + 1)));
		this.sightRange = Utils.getRandomizedParameter((p1.getSightRange() + p2.getSightRange()) / 2, Const.MUTATION_TOLERANCE);
		this.speed = Utils.getRandomizedParameter((p1.getSpeed() + p2.getSpeed()) / 2, Const.MUTATION_TOLERANCE);
	}

	
	public void init(AnimalMapView regMngr) {
		regionMngr = regMngr;

		double width = regionMngr.getWidth();
		double height = regionMngr.getHeight();
		
		// random pos
		if (pos == null) {
			double x = Utils.RAND.nextDouble() * width;
			double y = Utils.RAND.nextDouble() * height;
			pos = new Vector2D(x, y);
		} else { // si cae fuera del mapa
			double x = pos.getX();
			double y = pos.getY();
			
			while (x >= width) x = (x - width);
			while (x < 0) x = (x + width);
			while (y >= height) y = (y - height);
			while (y < 0) y = (y + height);

			pos = new Vector2D(x, y);
		}
		
		// random dest
		double x = Utils.RAND.nextDouble() * width;
		double y = Utils.RAND.nextDouble() * height;
		dest = new Vector2D(x, y);
	}

	public Animal deliverBaby() {
		Animal b = baby;
		baby = null;
		return b;
	}

	protected void move(double speed) {
		pos = pos.plus(dest.minus(pos).direction().scale(speed));
	}

	protected void setState(State state) {
		this.state = state;
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
		json.put("pos", pos.asJSONArray());
		json.put("gcode", geneticCode);
		json.put("diet", diet.toString());
		json.put("state", state.toString());
		return json;
	}
	
	//=======================================
	//				REFACTOR
	//=======================================
	@Override
	public final void update(double dt) {
		if (state == State.DEAD) {
			return;
		}
		
		updateAnimal(dt);
		
		if (wrap()) {
			setState(State.NORMAL);
		}
		
		if (energy <= 0.0 || age > getMaxAge()) {
			setState(State.DEAD);
			return;
		}
		
		double food = regionMngr.getFood(this, dt);
		energy = energy + food;
		energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
	}
	
	protected abstract void updateAnimal(double dt);
	
	protected abstract double getMaxAge();
	
	private boolean wrap() {
	    double width = regionMngr.getWidth();
	    double height = regionMngr.getHeight();
	    double x = pos.getX();
	    double y = pos.getY();

	    if (x >= 0 && x < width && y >= 0 && y < height) return false;

	    while (x >= width) x -= width;
	    while (x < 0) x += width;
	    while (y >= height) y -= height;
	    while (y < 0) y += height;

	    pos = new Vector2D(x, y);
	    return true;
	}
	
	protected final void advanceRandomDest(double dt,  double foodDropRate, double desireIncreaseRate) {
		// i. dest cerca (8.0) -> dest random
		if (pos.distanceTo(dest) < Const.COLLISION_RANGE) {
			double x = Utils.RAND.nextDouble() * regionMngr.getWidth();
			double y = Utils.RAND.nextDouble() * regionMngr.getHeight();

			dest = new Vector2D(x, y);
		}
		
		// ii. llama a move
		double v = speed * dt * Math.exp((energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
		move(v);
		
		// iii. sumar edad
		age += dt;
		
		// iv. quitar energía
		energy -= foodDropRate * dt;
		energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
		
		// v. aumentar deseo
		desire += desireIncreaseRate * dt;
		desire = Utils.constrainValueInRange(desire, 0.0, Const.MAX_DESIRE);
	}
	
	protected final void advanceDest(double dt, double boostFactor, double foodDropBoostFactor, double foodDropRate, double desireIncreaseRate) {
		// i. llama a move
		double v = boostFactor * speed * dt * Math.exp((energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
		move(v);
		
		// ii. sumar edad
		age += dt;
		
		// iii. quitar energía
		energy -= foodDropRate * foodDropBoostFactor* dt;
		energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
		
		// iv. aumentar deseo
		desire += desireIncreaseRate * dt;
		desire = Utils.constrainValueInRange(desire, 0.0, Const.MAX_DESIRE);
	}
	
	
	//=======================================	
	//=======================================

	// getters
	@Override
	public State getState() {
		return state;
	}

	@Override
	public Vector2D getPosition() {
		return pos;
	}

	@Override
	public String getGeneticCode() {
		return geneticCode;
	}

	@Override
	public Diet getDiet() {
		return diet;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public double getSightRange() {
		return sightRange;
	}

	@Override
	public double getEnergy() {
		return energy;
	}

	@Override
	public double getAge() {
		return age;
	}

	@Override
	public Vector2D getDestination() {
		return dest;
	}

	@Override
	public boolean isPregnant() {
		return baby != null;
	}
}
