package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.animal.Animal;
import simulator.model.animal.Sheep;
import simulator.model.strategy.SelectionStrategy;

public class SheepBuilder extends AnimalBuilder {

	public SheepBuilder(Factory<SelectionStrategy> strategyFactory) {
		super("sheep", "Sheep", strategyFactory);
	}

	@Override
	protected Animal createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("SheepBuilder: data es nulo");
		
		SelectionStrategy mateStrategy = parseMateStrategy(data);
		SelectionStrategy dangerStrategy = parseStrategy(data, "danger_strategy");
		Vector2D pos = parsePos(data);
		
		return new Sheep(mateStrategy, dangerStrategy, pos);
	}
}
