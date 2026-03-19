package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends AnimalBuilder {
	
	public WolfBuilder(Factory<SelectionStrategy> strategyFactory) {
		super("wolf", "Wolf", strategyFactory);
	}

	@Override
	protected Animal createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("WolfBuilder: data es nulo");
		
		SelectionStrategy mateStrategy = parseMateStrategy(data);
		SelectionStrategy huntStrategy = parseStrategy(data, "hunt_strategy");
		Vector2D pos = parsePos(data);
		
		return new Wolf(mateStrategy, huntStrategy, pos);
	}
}
