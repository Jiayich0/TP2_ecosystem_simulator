package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Const;
import simulator.model.region.DynamicSupplyRegion;
import simulator.model.region.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic food supply");
	}

	@Override
	protected void fillInData(JSONObject o) {
		o.put("factor", "food increase factor (optional, default 2.0)");
		o.put("food", "initial amount of food (optional, default 100.0)");
	}

	@Override
	protected Region createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("DynamicSupplyRegionBuilder: data es nulo");

		double factor = data.has("factor") ? data.getDouble("factor") : Const.FACTOR;
		double food = data.has("food") ? data.getDouble("food") : Const.INIT_FOOD;

		return new DynamicSupplyRegion(food, factor);
	}
}
