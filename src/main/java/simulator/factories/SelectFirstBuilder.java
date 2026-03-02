package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {

	public SelectFirstBuilder() {
		super("first", "Select First Strategy");
	}

	@Override
	protected SelectionStrategy createInstance(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("SelectFirstBuilder: createInstance: data es nulo");
		
		return new SelectFirst();
	}
}
