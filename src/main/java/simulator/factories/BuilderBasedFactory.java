package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {
	private Map<String, Builder<T>> _builders;
	private List<JSONObject> _buildersInfo;

	public BuilderBasedFactory() {
		_builders = new HashMap<>();
		_buildersInfo = new LinkedList<>();
	}

	public BuilderBasedFactory(List<Builder<T>> builders) {
		this();
		for (Builder<T> b : builders) {
			addBuilder(b);
		}
	}

	public void addBuilder(Builder<T> b) {
		if (b == null)
			throw new IllegalArgumentException("BuilderBasedFactory: builder es nulo");
		_builders.put(b.getTypeTag(), b);
		_buildersInfo.add(b.getInfo());
	}

	@Override
	public T createInstance(JSONObject info) {
		if (info == null) {
			throw new IllegalArgumentException("’info’ cannot be null");
		}
		
		String type = info.getString("type");
		Builder<T> b = _builders.get(type);
		
		if (b != null) {
			JSONObject data = info.has("data") ? info.getJSONObject("data") : new JSONObject();
			T o = b.createInstance(data);
			if (o != null) {
				return o;
			}
		}
		
		throw new IllegalArgumentException("Unrecognized ‘info’:" + info.toString());
	}

	@Override
	public List<JSONObject> getInfo() {
		return Collections.unmodifiableList(_buildersInfo);
	}
}