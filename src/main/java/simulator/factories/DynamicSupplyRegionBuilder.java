package simulator.factories;

import org.json.JSONObject;

import simulator.model.Const;
import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

	
	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic Supply Region");
	}
	
	@Override
	protected void fillInData(JSONObject o) { }

	@Override
	protected Region createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("DynamicSupplyRegionBuilder: data es nulo");

		double factor = data.has("factor") ? data.getDouble("factor") : Const.FACTOR;
		double food = data.has("food") ? data.getDouble("food") : Const.INIT_FOOD;

		return new DynamicSupplyRegion(food, factor);
	}
}
