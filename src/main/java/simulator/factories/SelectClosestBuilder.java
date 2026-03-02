package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectClosest;
import simulator.model.SelectionStrategy;

public class SelectClosestBuilder extends Builder<SelectionStrategy> {

	public SelectClosestBuilder() {
		super("closest", "Select Closest Strategy");
	}

	@Override
	protected SelectionStrategy createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("SelectClosestBuilder: createInstance: data es nulo");

		return new SelectClosest();
	}

}
