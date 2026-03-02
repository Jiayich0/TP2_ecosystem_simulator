package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectYoungest;
import simulator.model.SelectionStrategy;

public class SelectYoungestBuilder extends Builder<SelectionStrategy> {

	public SelectYoungestBuilder() {
		super("youngest", "Select Youngest Strategy");
	}

	@Override
	protected SelectionStrategy createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("SelectYoungestBuilder: createInstance: data es nulo");

		return new SelectYoungest();
	}
}