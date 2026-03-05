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
	public void updateAnimal(double dt) {
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
	}
	
	@Override
	protected double getMaxAge() {
	    return Const.MAX_AGE_SHEEP;
	}

	private void updateNormal(double dt) {
		advanceRandomDest(dt, Const.FOOD_DROP_RATE_SHEEP, Const.DESIRE_INCREASE_RATE_SHEEP);
		
		if (dangerSource == null) {
			List<Animal> peligros = regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.CARNIVORE);
			dangerSource = dangerStrategy.select(this, peligros);
		}

		if (dangerSource != null) {
			setState(State.DANGER);
		}
		else if (desire > Const.DESIRE_THRESHOLD_SHEEP) {
			setState(State.MATE);
		}
	}

	private void updateDanger(double dt) {
		if (dangerSource != null && dangerSource.getState() == State.DEAD) {
			dangerSource = null;
		}
		
		if (dangerSource == null) {
			advanceRandomDest(dt, Const.FOOD_DROP_RATE_SHEEP, Const.DESIRE_INCREASE_RATE_SHEEP);
		} else {
			// dirección contraria
			dest = pos.plus(pos.minus(dangerSource.getPosition()).direction());
			advanceDest(dt, Const.BOOST_FACTOR_SHEEP, Const.FOOD_DROP_BOOST_FACTOR_SHEEP, Const.FOOD_DROP_RATE_SHEEP, Const.DESIRE_INCREASE_RATE_SHEEP);
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
				advanceRandomDest(dt, Const.FOOD_DROP_RATE_SHEEP, Const.DESIRE_INCREASE_RATE_SHEEP);
			}
		}
		else {
			dest = mateTarget.getPosition();
			advanceDest(dt, Const.BOOST_FACTOR_SHEEP, Const.FOOD_DROP_BOOST_FACTOR_SHEEP, Const.FOOD_DROP_RATE_SHEEP, Const.DESIRE_INCREASE_RATE_SHEEP);
			
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
