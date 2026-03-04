package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

	private Animal dangerSource;
	private SelectionStrategy dangerStrategy;
	

	public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos) {
		super(Const.SHEEP_GENETIC_CODE, Diet.HERBIVORE, Const.INIT_SIGHT_SHEEP, Const.INIT_SPEED_SHEEP, mateStrategy, pos);
		if (dangerStrategy == null)
			throw new IllegalArgumentException("Sheep: constructora -> dangerStrategy: no puede ser nulo");
		this.dangerStrategy = dangerStrategy;
		this.dangerSource = null;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		this.dangerStrategy = p1.dangerStrategy;
		this.dangerSource = null;
	}

	@Override
	public void update(double dt) {
		// 1. si es dead no hacer nada
		if (state == State.DEAD) {
			return;
		}
		
		// 2, actualizar según estado
		switch (state) {
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
		double width = regionMngr.getWidth();
		double height = regionMngr.getHeight();
		double x = pos.getX();
		double y = pos.getY();
		if (x < 0 || x >= width || y < 0 || y >= height) {
			while (x >= width) x = (x - width);
			while (x < 0) x = (x + width);
			while (y >= height) y = (y - height);
			while (y < 0) y = (y + height);
			
			pos = new Vector2D(x, y);
			setState(State.NORMAL);
		}
		
		// 4. si muere ponerlo DEAD
		if (energy == 0.0 || age > Const.MAX_AGE_SHEEP) {
			setState(State.DEAD);
			return;
		}
		
		// 5. si no esta muerto
		double food = regionMngr.getFood(this, dt);
		energy = energy + food;
		energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
	}

	private void updateNormal(double dt) {
		// 1 avance
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
		energy -= Const.FOOD_DROP_RATE_SHEEP * dt;
		energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
		// v. aumentar deseo
		desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
		desire = Utils.constrainValueInRange(desire, 0.0, Const.MAX_DESIRE);
		// 2 cambio de estado
		// i. busca nuevo peligro
		if (dangerSource == null) {
			List<Animal> peligros = regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.CARNIVORE);
			dangerSource = dangerStrategy.select(this, peligros);
		}
		// ii. si enceuntra peligro pone en danger, si no aumenta deseo
		if (dangerSource != null) {
			setState(State.DANGER);
		}
		else if (desire > Const.DESIRE_THRESHOLD_SHEEP) {
			setState(State.MATE);
		}
	}

	private void updateDanger(double dt) {
		// 1 dangerSource no es null y está muerto
		if (dangerSource != null && dangerSource.getState() == State.DEAD) {
			dangerSource = null;
		}
		// 2 null -> igual que NORMAL
		if (dangerSource == null) {
			if (pos.distanceTo(dest) < Const.COLLISION_RANGE) {
				double x = Utils.RAND.nextDouble() * regionMngr.getWidth();
				double y = Utils.RAND.nextDouble() * regionMngr.getHeight();
				dest = new Vector2D(x, y);
			}

			double v = speed * dt * Math.exp((energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
			move(v);

			age += dt;

			energy -= Const.FOOD_DROP_RATE_SHEEP * dt;
			energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);

			desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
			desire = Utils.constrainValueInRange(desire, 0.0, Const.MAX_DESIRE);

		} else {
			// i. dirección contraria
			dest = pos.plus(pos.minus(dangerSource.getPosition()).direction());
			// ii. llama a move
			double v = Const.BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
			move(v);
			// iii. sumar edad
			age += dt;
			// iv. quitar energía
			energy -= Const.FOOD_DROP_RATE_SHEEP * Const.FOOD_DROP_BOOST_FACTOR_SHEEP * dt;
			energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
			// v. aumentar deseo
			desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
			desire = Utils.constrainValueInRange(desire, 0.0, Const.MAX_DESIRE);
		}
		
		if (dangerSource == null || pos.distanceTo(dangerSource.getPosition()) > sightRange) {
			List<Animal> peligros = regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.CARNIVORE);
			dangerSource = dangerStrategy.select(this, peligros);
		}

		if (dangerSource == null) {
			if (desire < Const.DESIRE_THRESHOLD_SHEEP) {
				setState(State.NORMAL);
			} else {
				setState(State.MATE);
			}
		}
	}

	private void updateMate(double dt) {
		if (mateTarget != null) {
			if (mateTarget.getState() == State.DEAD || pos.distanceTo(mateTarget.getPosition()) > sightRange) {
				mateTarget = null;
			}
		}
		if (mateTarget == null) {
			List<Animal> candidatos = regionMngr.getAnimalsInRange(this, an -> an.getGeneticCode().equals(this.getGeneticCode()));
			mateTarget = mateStrategy.select(this, candidatos);
			
			if (mateTarget == null) {
				// avanza como NORMAL
				if (pos.distanceTo(dest) < Const.COLLISION_RANGE) {
					double x = Utils.RAND.nextDouble() * regionMngr.getWidth();
					double y = Utils.RAND.nextDouble() * regionMngr.getHeight();
					dest = new Vector2D(x, y);
				}

				double v = speed * dt * Math.exp((energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
				move(v);

				age += dt;

				energy -= Const.FOOD_DROP_RATE_SHEEP * dt;
				energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);

				desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
				desire = Utils.constrainValueInRange(desire, 0.0, Const.MAX_DESIRE);
			}

		}
		else {
			// i. perseguir
			dest = mateTarget.getPosition();
			
			double v = Const.BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - Const.MAX_ENERGY) * Const.HUNGER_DECAY_EXP_FACTOR);
			move(v);
			
			age += dt;
			
			energy -= Const.FOOD_DROP_RATE_SHEEP * Const.FOOD_DROP_BOOST_FACTOR_SHEEP * dt;
			energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
			
			desire += Const.DESIRE_INCREASE_RATE_SHEEP * dt;
			desire = Utils.constrainValueInRange(desire, 0.0, Const.MAX_DESIRE);
			// iv. si está en su rango
			if (pos.distanceTo(mateTarget.getPosition()) < Const.COLLISION_RANGE) {
				desire = 0.0;
				mateTarget.desire = 0.0;
				
				if (!this.isPregnant()) {
					if (Utils.RAND.nextDouble() < Const.PREGNANT_PROBABILITY_SHEEP) {
						baby = new Sheep(this, mateTarget);
					}
				}
				
				mateTarget = null;
			}
		}
		
		if (dangerSource == null) {
			List<Animal> peligros = regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.CARNIVORE);
			dangerSource = dangerStrategy.select(this, peligros);
		}
		
		if (dangerSource != null) {
			setState(State.DANGER);
		}
		else if (desire < Const.DESIRE_THRESHOLD_SHEEP) {
			setState(State.NORMAL);
		}
	}

	@Override
	protected void setNormalStateAction() {
		dangerSource = null;
		mateTarget = null;
	}

	@Override
	protected void setMateStateAction() {
		dangerSource = null;
	}

	@Override
	protected void setHungerStateAction() {
	}

	@Override
	protected void setDangerStateAction() {
		mateTarget = null;
	}

	@Override
	protected void setDeadStateAction() {
		dangerSource = null;
		mateTarget = null;
	}
}
