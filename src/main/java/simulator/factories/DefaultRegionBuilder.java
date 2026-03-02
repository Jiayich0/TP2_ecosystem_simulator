package simulator.factories;

import org.json.JSONObject;

import simulator.model.DefaultRegion;
import simulator.model.Region;

public class DefaultRegionBuilder extends Builder<Region> {
	public DefaultRegionBuilder() {
		super("default", "Default Region");
	}
	
	@Override
	protected void fillInData(JSONObject o) { }
	
	@Override
	protected Region createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("DefaultRegionBuilder: data es nulo");

		return new DefaultRegion();
	}
}
