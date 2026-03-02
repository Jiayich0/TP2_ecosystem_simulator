package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

	private Animal _huntTarget;
	private SelectionStrategy _huntingStrategy;
	
	private final static String WOLF_GENETIC_CODE = "Wolf";
	private final static double INIT_SIGHT_WOLF = 50;
	private final static double INIT_SPEED_WOLF = 60;
	private final static double BOOST_FACTOR_WOLF = 3.0;
	private final static double MAX_AGE_WOLF = 14.0;
	private final static double FOOD_THRSHOLD_WOLF = 50.0;
	private final static double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
	private final static double FOOD_DROP_RATE_WOLF = 18.0;
	private final static double FOOD_DROP_DESIRE_WOLF = 10.0;
	private final static double FOOD_EAT_VALUE_WOLF = 50.0;
	private final static double DESIRE_THRESHOLD_WOLF = 65.0;
	private final static double DESIRE_INCREASE_RATE_WOLF = 30.0;
	private final static double PREGNANT_PROBABILITY_WOLF = 0.75;

	public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos) {
		super(WOLF_GENETIC_CODE, Diet.CARNIVORE, INIT_SIGHT_WOLF, INIT_SPEED_WOLF, mateStrategy, pos);
		if (huntingStrategy == null)
			throw new IllegalArgumentException("Wolf: constructora -> huntingStrategy: no puede ser nulo");
		_huntingStrategy = huntingStrategy;
		_huntTarget = null;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		_huntingStrategy = p1._huntingStrategy;
		_huntTarget = null;
	}

	@Override
	public void update(double dt) {
		// 1. si es dead no hacer nada
		if (_state == State.DEAD) {
			return;
		}
		
		// 2, actualizar según estado
		switch (_state) {
		case NORMAL:
			updateNormal(dt);
			break;
		case HUNGER:
			updateHunger(dt);
			break;
		case MATE:
			updateMate(dt);
			break;
		default:
			break;
		}
		
		// 3. si pos fuera del mapa y ponerlo NORMAL
		double width = _regionMngr.getWidth();
		double height = _regionMngr.getHeight();
		double x = _pos.getX();
		double y = _pos.getY();
		if (x < 0 || x >= width || y < 0 || y >= height) {
			while (x >= width) x = (x - width);
			while (x < 0) x = (x + width);
			while (y >= height) y = (y - height);
			while (y < 0) y = (y + height);
			
			_pos = new Vector2D(x, y);
			setState(State.NORMAL);
		}
		
		// 4. si esta muerto ponerlo DEAD
		if (_energy == 0.0 || _age > MAX_AGE_WOLF) {
			setState(State.DEAD);
			return;
		}
		
		// 5. si no esta muerto
		double food = _regionMngr.getFood(this, dt);
		_energy = _energy + food;
		if (_energy < 0.0) _energy = 0.0;
		if (_energy > MAX_ENERGY) _energy = MAX_ENERGY;
	}
	
	private void updateNormal(double dt) {
		if (_pos.distanceTo(_dest) < COLLISION_RANGE) {
			double x = Utils.RAND.nextDouble() * _regionMngr.getWidth();
			double y = Utils.RAND.nextDouble() * _regionMngr.getHeight();
			_dest = new Vector2D(x, y);
		}
		
		double v = _speed * dt * Math.exp((_energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR);
		move(v);
		
		_age += dt;
		
		_energy -= FOOD_DROP_RATE_WOLF * dt;
		if (_energy < 0.0) _energy = 0.0;
		if (_energy > MAX_ENERGY) _energy = MAX_ENERGY;
		
		_desire += DESIRE_INCREASE_RATE_WOLF * dt;
		if (_desire < 0.0) _desire = 0.0;
		if (_desire > MAX_DESIRE) _desire = MAX_DESIRE;
		
		if (_energy < FOOD_THRSHOLD_WOLF) {
			setState(State.HUNGER);
		}
		else if (_desire > DESIRE_THRESHOLD_WOLF) {
			setState(State.MATE);
		}
	}
	
	private void updateHunger(double dt) {
		if (_huntTarget == null || _huntTarget.getState() == State.DEAD || _pos.distanceTo(_huntTarget.getPosition()) > _sightRange) {
			List<Animal> herbivoros = _regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.HERBIVORE);
			_huntTarget = _huntingStrategy.select(this, herbivoros);
		}
		
		if (_huntTarget == null) {
			if (_pos.distanceTo(_dest) < COLLISION_RANGE) {
				double x = Utils.RAND.nextDouble() * _regionMngr.getWidth();
				double y = Utils.RAND.nextDouble() * _regionMngr.getHeight();
				_dest = new Vector2D(x, y);
			}

			double v = _speed * dt * Math.exp((_energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR);
			move(v);

			_age += dt;

			_energy -= FOOD_DROP_RATE_WOLF * dt;
			if (_energy < 0.0) _energy = 0.0;
			if (_energy > MAX_ENERGY) _energy = MAX_ENERGY;

			_desire += DESIRE_INCREASE_RATE_WOLF * dt;
			if (_desire < 0.0) _desire = 0.0;
			if (_desire > MAX_DESIRE) _desire = MAX_DESIRE;
		} else {
			_dest = _huntTarget.getPosition();
			
			double v = BOOST_FACTOR_WOLF * _speed * dt * Math.exp((_energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR);
			move(v);
			
			_age += dt;
			
			_energy -= FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt;
			if (_energy < 0.0) _energy = 0.0;
			if (_energy > MAX_ENERGY) _energy = MAX_ENERGY;
			
			_desire += DESIRE_INCREASE_RATE_WOLF * dt;
			if (_desire < 0.0) _desire = 0.0;
			if (_desire > MAX_DESIRE) _desire = MAX_DESIRE;
			
			if (_pos.distanceTo(_huntTarget.getPosition()) < COLLISION_RANGE) {

				_huntTarget.setState(State.DEAD);
				_huntTarget = null;

				_energy += FOOD_EAT_VALUE_WOLF;
				if (_energy > MAX_ENERGY) _energy = MAX_ENERGY;
			}
		}
		
		if (_energy > FOOD_THRSHOLD_WOLF) {
			if (_desire < DESIRE_THRESHOLD_WOLF) {
				setState(State.NORMAL);
			} else {
				setState(State.MATE);
			}
		}
	}
	
	private void updateMate(double dt) {
		if (_mateTarget != null) {
			if (_mateTarget.getState() == State.DEAD || _pos.distanceTo(_mateTarget.getPosition()) > _sightRange) {
				_mateTarget = null;
			}
		}
		
		if (_mateTarget == null) {
			List<Animal> candidatos = _regionMngr.getAnimalsInRange(this, an -> an.getGeneticCode().equals(this.getGeneticCode()));

			_mateTarget = _mateStrategy.select(this, candidatos);
			
			if (_mateTarget == null) {
				if (_pos.distanceTo(_dest) < COLLISION_RANGE) {
					double x = Utils.RAND.nextDouble() * _regionMngr.getWidth();
					double y = Utils.RAND.nextDouble() * _regionMngr.getHeight();
					_dest = new Vector2D(x, y);
				}

				double v = _speed * dt * Math.exp((_energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR);
				move(v);

				_age += dt;

				_energy -= FOOD_DROP_RATE_WOLF * dt;
				if (_energy < 0.0) _energy = 0.0;
				if (_energy > MAX_ENERGY) _energy = MAX_ENERGY;

				_desire += DESIRE_INCREASE_RATE_WOLF * dt;
				if (_desire < 0.0) _desire = 0.0;
				if (_desire > MAX_DESIRE) _desire = MAX_DESIRE;
			}
		}
		else {
			_dest = _mateTarget.getPosition();
			
			double v = BOOST_FACTOR_WOLF * _speed * dt * Math.exp((_energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR);
			move(v);
			
			_age += dt;
			
			_energy -= FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt;
			if (_energy < 0.0) _energy = 0.0;
			if (_energy > MAX_ENERGY) _energy = MAX_ENERGY;
			
			_desire += DESIRE_INCREASE_RATE_WOLF * dt;
			if (_desire < 0.0) _desire = 0.0;
			if (_desire > MAX_DESIRE) _desire = MAX_DESIRE;
			
			if (_pos.distanceTo(_mateTarget.getPosition()) < COLLISION_RANGE) {
				_desire = 0.0;
				_mateTarget._desire = 0.0;
				
				if (!this.isPregnant()) {
					if (Utils.RAND.nextDouble() < PREGNANT_PROBABILITY_WOLF) {
						_baby = new Wolf(this, _mateTarget);
					}
				}
				
				_energy -= FOOD_DROP_DESIRE_WOLF;
				if (_energy < 0.0) _energy = 0.0;
				
				_mateTarget = null;
			}
		}
		
		if (_energy < FOOD_THRSHOLD_WOLF) {
			setState(State.HUNGER);
		}
		else if (_desire < DESIRE_THRESHOLD_WOLF) {
			setState(State.NORMAL);
		}
	}

	@Override
	protected void setNormalStateAction() {
		_huntTarget = null;
		_mateTarget = null;
	}

	@Override
	protected void setMateStateAction() {
		_huntTarget = null;
	}

	@Override
	protected void setHungerStateAction() {
		_mateTarget = null;
	}

	@Override
	protected void setDangerStateAction() {
	}

	@Override
	protected void setDeadStateAction() {
		_huntTarget = null;
		_mateTarget = null;
	}
}
