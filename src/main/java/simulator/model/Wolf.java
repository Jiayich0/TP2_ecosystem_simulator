package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

	private Animal huntTarget;
	private SelectionStrategy huntingStrategy;


	public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos) {
		super(Const.WOLF_GENETIC_CODE, Diet.CARNIVORE, Const.INIT_SIGHT_WOLF, Const.INIT_SPEED_WOLF, mateStrategy, pos);
		if (huntingStrategy == null)
			throw new IllegalArgumentException("Wolf: constructora -> huntingStrategy: no puede ser nulo");
		this.huntingStrategy = huntingStrategy;
		this.huntTarget = null;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		this.huntingStrategy = p1.huntingStrategy;
		this.huntTarget = null;
	}

	@Override
	public void updateAnimal(double dt) {
		switch (state) {
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
	}
	
	@Override
	protected double getMaxAge() {
		return Const.MAX_AGE_WOLF;
	}
	
	private void updateNormal(double dt) {
		advanceRandomDest(dt, Const.FOOD_DROP_RATE_WOLF, Const.DESIRE_INCREASE_RATE_WOLF);
		
		if (energy < Const.FOOD_THRSHOLD_WOLF) {
			setState(State.HUNGER);
		}
		else if (desire > Const.DESIRE_THRESHOLD_WOLF) {
			setState(State.MATE);
		}
	}
	
	private void updateHunger(double dt) {
		if (huntTarget == null || huntTarget.getState() == State.DEAD || pos.distanceTo(huntTarget.getPosition()) > sightRange) {
			List<Animal> herbivoros = regionMngr.getAnimalsInRange(this, an -> an.getDiet() == Diet.HERBIVORE);
			huntTarget = huntingStrategy.select(this, herbivoros);
		}
		
		if (huntTarget == null) {
			advanceRandomDest(dt, Const.FOOD_DROP_RATE_WOLF, Const.DESIRE_INCREASE_RATE_WOLF);
		} else {
			dest = huntTarget.getPosition();
			advanceDest(dt, Const.BOOST_FACTOR_WOLF, Const.FOOD_DROP_BOOST_FACTOR_WOLF, Const.FOOD_DROP_RATE_WOLF, Const.DESIRE_INCREASE_RATE_WOLF);
			
			if (pos.distanceTo(huntTarget.getPosition()) < Const.COLLISION_RANGE) {

				huntTarget.setState(State.DEAD);
				huntTarget = null;

				energy += Const.FOOD_EAT_VALUE_WOLF;
				energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
			}
		}
		
		if (energy > Const.FOOD_THRSHOLD_WOLF) {
			if (desire < Const.DESIRE_THRESHOLD_WOLF) {
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
				advanceRandomDest(dt, Const.FOOD_DROP_RATE_WOLF, Const.DESIRE_INCREASE_RATE_WOLF);
			}
		}
		else {
			dest = mateTarget.getPosition();
			advanceDest(dt, Const.BOOST_FACTOR_WOLF, Const.FOOD_DROP_BOOST_FACTOR_WOLF, Const.FOOD_DROP_RATE_WOLF, Const.DESIRE_INCREASE_RATE_WOLF);
			
			if (pos.distanceTo(mateTarget.getPosition()) < Const.COLLISION_RANGE) {
				desire = 0.0;
				mateTarget.desire = 0.0;
				
				if (!this.isPregnant()) {
					if (Utils.RAND.nextDouble() < Const.PREGNANT_PROBABILITY_WOLF) {
						baby = new Wolf(this, mateTarget);
					}
				}
				
				energy -= Const.FOOD_DROP_DESIRE_WOLF;
				energy = Utils.constrainValueInRange(energy, 0.0, Const.MAX_ENERGY);
				
				mateTarget = null;
			}
		}
		
		if (energy < Const.FOOD_THRSHOLD_WOLF) {
			setState(State.HUNGER);
		}
		else if (desire < Const.DESIRE_THRESHOLD_WOLF) {
			setState(State.NORMAL);
		}
	}

	@Override
	protected void setNormalStateAction() {
		huntTarget = null;
		mateTarget = null;
	}

	@Override
	protected void setMateStateAction() {
		huntTarget = null;
	}

	@Override
	protected void setHungerStateAction() {
		mateTarget = null;
	}

	@Override
	protected void setDangerStateAction() {
	}

	@Override
	protected void setDeadStateAction() {
		huntTarget = null;
		mateTarget = null;
	}
}
