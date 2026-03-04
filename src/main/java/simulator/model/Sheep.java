package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

	private Animal _dangerSource;
	private SelectionStrategy _dangerStrategy;
	

	public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos) {
		super(Const.SHEEP_GENETIC_CODE, Diet.HERBIVORE, Const.INIT_SIGHT_SHEEP, Const.INIT_SPEED_SHEEP, mateStrategy, pos);
		if (dangerStrategy == null)
			throw new IllegalArgumentException("Sheep: constructora -> dangerStrategy: no puede ser nulo");
		_dangerStrategy = dangerStrategy;
		_dangerSource = null;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		_dangerStrategy = p1._dangerStrategy;
		_dangerSource = null;
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
		case DANGER:
			updateDanger(dt);
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
		
		// 4. si muere ponerlo DEAD
		if (_energy == 0.0 || _age > Const.MAX_AGE_SHEEP) {
			setState(State.DEAD);
			return;
		}
		
		// 5. si no esta muerto
		double food = _regionMngr.getFood(this, dt);
		_energy = _energy + food;
		_energy = Utils.constrainValueInRange(_energy, 0.0, Const.MAX_ENERGY);
	}

	private void updateNormal(double dt) {
		// 1 avance
		// i. dest cerca (8.0) -> dest random
		if (_pos.distanceTo(_dest) < Const.COLLISION_RANGE) {
			double x = Utils.RAND.nextDouble() * _regionMngr.getWidth();
			double y = Utils.RAND.nextDouble() * _regionMngr.getHeight();

			_dest = new Vector2D(x, y);
		}
		// ii. llama a move
		double v = _speed * dt * Math.exp((_energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
		move(v);
		// iii. sumar edad
		_age += dt;
		// iv. quitar energía
		_energy -= Const.FOOD_DROP_RATE_SHEEP * dt;
		_energy = Utils.constrainValueInRange(_energy, 0.0, Const.MAX_ENERGY);
		// v. aumentar deseo
		_desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
		_desire = Utils.constrainValueInRange(_desire, 0.0, Const.MAX_DESIRE);
		// 2 cambio de estado
		// i. busca nuevo peligro
		if (_dangerSource == null) {
			List<Animal> peligros = _regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.CARNIVORE);
			_dangerSource = _dangerStrategy.select(this, peligros);
		}
		// ii. si enceuntra peligro pone en danger, si no aumenta deseo
		if (_dangerSource != null) {
			setState(State.DANGER);
		}
		else if (_desire > Const.DESIRE_THRESHOLD_SHEEP) {
			setState(State.MATE);
		}
	}

	private void updateDanger(double dt) {
		// 1 dangerSource no es null y está muerto
		if (_dangerSource != null && _dangerSource.getState() == State.DEAD) {
			_dangerSource = null;
		}
		// 2 null -> igual que NORMAL
		if (_dangerSource == null) {
			if (_pos.distanceTo(_dest) < Const.COLLISION_RANGE) {
				double x = Utils.RAND.nextDouble() * _regionMngr.getWidth();
				double y = Utils.RAND.nextDouble() * _regionMngr.getHeight();
				_dest = new Vector2D(x, y);
			}

			double v = _speed * dt * Math.exp((_energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
			move(v);

			_age += dt;

			_energy -= Const.FOOD_DROP_RATE_SHEEP * dt;
			_energy = Utils.constrainValueInRange(_energy, 0.0, Const.MAX_ENERGY);

			_desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
			_desire = Utils.constrainValueInRange(_desire, 0.0, Const.MAX_DESIRE);

		} else {
			// i. dirección contraria
			_dest = _pos.plus(_pos.minus(_dangerSource.getPosition()).direction());
			// ii. llama a move
			double v = Const.BOOST_FACTOR_SHEEP * _speed * dt * Math.exp((_energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
			move(v);
			// iii. sumar edad
			_age += dt;
			// iv. quitar energía
			_energy -= Const.FOOD_DROP_RATE_SHEEP * Const.FOOD_DROP_BOOST_FACTOR_SHEEP * dt;
			_energy = Utils.constrainValueInRange(_energy, 0.0, Const.MAX_ENERGY);
			// v. aumentar deseo
			_desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
			_desire = Utils.constrainValueInRange(_desire, 0.0, Const.MAX_DESIRE);
		}
		
		if (_dangerSource == null || _pos.distanceTo(_dangerSource.getPosition()) > _sightRange) {
			List<Animal> peligros = _regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.CARNIVORE);
			_dangerSource = _dangerStrategy.select(this, peligros);
		}

		if (_dangerSource == null) {
			if (_desire < Const.DESIRE_THRESHOLD_SHEEP) {
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
				// avanza como NORMAL
				if (_pos.distanceTo(_dest) < Const.COLLISION_RANGE) {
					double x = Utils.RAND.nextDouble() * _regionMngr.getWidth();
					double y = Utils.RAND.nextDouble() * _regionMngr.getHeight();
					_dest = new Vector2D(x, y);
				}

				double v = _speed * dt * Math.exp((_energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
				move(v);

				_age += dt;

				_energy -= Const.FOOD_DROP_RATE_SHEEP * dt;
				_energy = Utils.constrainValueInRange(_energy, 0.0, Const.MAX_ENERGY);

				_desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
				_desire = Utils.constrainValueInRange(_desire, 0.0, Const.MAX_DESIRE);
			}

		}
		else {
			// i. perseguir
			_dest = _mateTarget.getPosition();
			
			double v = Const.BOOST_FACTOR_SHEEP * _speed * dt * Math.exp((_energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
			move(v);
			
			_age += dt;
			
			_energy -= Const.FOOD_DROP_RATE_SHEEP * Const.FOOD_DROP_BOOST_FACTOR_SHEEP * dt;
			_energy = Utils.constrainValueInRange(_energy, 0.0, Const.MAX_ENERGY);
			
			_desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
			_desire = Utils.constrainValueInRange(_desire, 0.0, Const.MAX_DESIRE);
			// iv. si está en su rango
			if (_pos.distanceTo(_mateTarget.getPosition()) < Const.COLLISION_RANGE) {
				_desire = 0.0;
				_mateTarget._desire = 0.0;
				
				if (!this.isPregnant()) {
					if (Utils.RAND.nextDouble() < Const.PREGNANT_PROBABILITY_SHEEP) {
						_baby = new Sheep(this, _mateTarget);
					}
				}
				
				_mateTarget = null;
			}
		}
		
		if (_dangerSource == null) {
			List<Animal> peligros = _regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.CARNIVORE);
			_dangerSource = _dangerStrategy.select(this, peligros);
		}
		
		if (_dangerSource != null) {
			setState(State.DANGER);
		}
		else if (_desire < Const.DESIRE_THRESHOLD_SHEEP) {
			setState(State.NORMAL);
		}
	}

	@Override
	protected void setNormalStateAction() {
		_dangerSource = null;
		_mateTarget = null;
	}

	@Override
	protected void setMateStateAction() {
		_dangerSource = null;
	}

	@Override
	protected void setHungerStateAction() {
	}

	@Override
	protected void setDangerStateAction() {
		_mateTarget = null;
	}

	@Override
	protected void setDeadStateAction() {
		_dangerSource = null;
		_mateTarget = null;
	}
}
