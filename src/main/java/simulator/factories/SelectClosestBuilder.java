package simulator.factories;

import org.json.JSONObject;

import simulator.model.strategy.SelectClosest;
import simulator.model.strategy.SelectionStrategy;

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
