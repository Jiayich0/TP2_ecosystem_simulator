package simulator.factories;

import org.json.JSONObject;

import simulator.model.region.DefaultRegion;
import simulator.model.region.Region;

public class DefaultRegionBuilder extends Builder<Region> {

	public DefaultRegionBuilder() {
		super("default", "Infinite food supply");
	}

	@Override
	protected void fillInData(JSONObject o) {

	}

	@Override
	protected Region createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("DefaultRegionBuilder: data es nulo");

		return new DefaultRegion();
	}
}
