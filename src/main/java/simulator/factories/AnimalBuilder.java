package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.animal.Animal;
import simulator.model.strategy.SelectionStrategy;

public abstract class AnimalBuilder extends Builder<Animal> {

	protected Factory<SelectionStrategy> strategyFactory;

	protected AnimalBuilder(String type, String desc, Factory<SelectionStrategy> strategyFactory) {
		super(type, desc);
		if (strategyFactory == null)
			throw new IllegalArgumentException("AnimalBuilder: strategyFactory es nulo");
		this.strategyFactory = strategyFactory;
	}

	@Override
	protected void fillInData(JSONObject o) {
	}

	protected SelectionStrategy parseStrategy(JSONObject data, String key) {
		if (data.has(key)) {
			JSONObject strategyData = data.getJSONObject(key);
			return strategyFactory.createInstance(strategyData);
		}
		return strategyFactory.createInstance(new JSONObject().put("type", "first"));
	}

	protected SelectionStrategy parseMateStrategy(JSONObject data) {
		return parseStrategy(data, "mate_strategy");
	}

	protected Vector2D parsePos(JSONObject data) {
		if (!data.has("pos"))
			return null;

		JSONObject posObj = data.getJSONObject("pos");

		JSONArray xR = posObj.getJSONArray("x_range");
		JSONArray yR = posObj.getJSONArray("y_range");

		if (xR.length() != 2 || yR.length() != 2)
			throw new IllegalArgumentException("AnimalBuilder: x_range/y_range deben tener 2 valores");

		double xMin = xR.getDouble(0);
		double xMax = xR.getDouble(1);
		double yMin = yR.getDouble(0);
		double yMax = yR.getDouble(1);

		if (xMin > xMax || yMin > yMax)
			throw new IllegalArgumentException("AnimalBuilder: rango inválido (min > max)");

		double x = xMin + Utils.RAND.nextDouble() * (xMax - xMin);
		double y = yMin + Utils.RAND.nextDouble() * (yMax - yMin);

		return new Vector2D(x, y);
	}
}
