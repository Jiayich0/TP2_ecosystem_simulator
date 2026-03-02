package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends Builder<Animal> {
	private Factory<SelectionStrategy> _strategyFactory;

	public SheepBuilder(Factory<SelectionStrategy> strategyFactory) {
		super("sheep", "Sheep");
		if (strategyFactory == null)
			throw new IllegalArgumentException("SheepBuilder: strategyFactory es nulo");
		_strategyFactory = strategyFactory;
	}

	@Override
	protected void fillInData(JSONObject o) { }

	@Override
	protected Animal createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("SheepBuilder: data es nulo");
		
		SelectionStrategy mateStrategy = parseStrategy(data, "mate_strategy");
		SelectionStrategy dangerStrategy = parseStrategy(data, "danger_strategy");
		Vector2D pos = parsePos(data);
		
		return new Sheep(mateStrategy, dangerStrategy, pos);
	}
	
	private SelectionStrategy parseStrategy(JSONObject data, String key) {
		if (data.has(key)) {
			JSONObject s = data.getJSONObject(key);
			return _strategyFactory.createInstance(s);
		} else {
			return _strategyFactory.createInstance(new JSONObject().put("type", "first"));
		}
	}
	
	private Vector2D parsePos(JSONObject data) {
		if (!data.has("pos"))
			return null;

		JSONObject posObj = data.getJSONObject("pos");
		
		JSONArray xR = posObj.getJSONArray("x_range");
		JSONArray yR = posObj.getJSONArray("y_range");
		if (xR.length() != 2 || yR.length() != 2)
			throw new IllegalArgumentException("SheepBuilder: x_range/y_range deben tener 2 valores");
		
		double xMin = xR.getDouble(0);
		double xMax = xR.getDouble(1);
		double yMin = yR.getDouble(0);
		double yMax = yR.getDouble(1);
		
		if (xMin > xMax || yMin > yMax)
			throw new IllegalArgumentException("SheepBuilder: rango inválido (min > max)");
		
		double x = xMin + Utils.RAND.nextDouble() * (xMax - xMin);
		double y = yMin + Utils.RAND.nextDouble() * (yMax - yMin);

		return new Vector2D(x, y);
	}
}
