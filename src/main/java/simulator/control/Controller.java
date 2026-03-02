package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {
	private Simulator _sim;

	public Controller (Simulator sim) {
		if (sim == null)
			throw new IllegalArgumentException("Controller: constructor: sim es nulo");
		_sim = sim;
	}

	public void loadData(JSONObject data) {
		if (data == null)
			throw new IllegalArgumentException("Controller: data es nulo");
		
		// regiones (optional)
		if (data.has("regions")) {
			JSONArray regions = data.getJSONArray("regions");
			
			for (int i = 0; i < regions.length(); i++) {
				JSONObject reg = regions.getJSONObject(i);

				JSONArray row = reg.getJSONArray("row");
				JSONArray col = reg.getJSONArray("col");
				JSONObject spec = reg.getJSONObject("spec");

				int rf = row.getInt(0);
				int rt = row.getInt(1);
				int cf = col.getInt(0);
				int ct = col.getInt(1);

				for (int r = rf; r <= rt; r++) {
					for (int c = cf; c <= ct; c++) {
						_sim.setRegion(r, c, spec);
					}
				}
			}
		}
		
		// animales
		JSONArray animals = data.getJSONArray("animals");
		
		for (int i = 0; i < animals.length(); i++) {
			JSONObject a = animals.getJSONObject(i);
			
			int amount = a.getInt("amount");
			if (amount <= 0)
				throw new IllegalArgumentException("Controller: loadData: amount tiene que ser > 0");
			
			JSONObject spec = a.getJSONObject("spec");
			for (int j = 0; j < amount; j++) {
				_sim.addAnimal(spec);
			}
		}
	}
	
	public void run(double t, double dt, boolean sv, OutputStream out) {
		if (out == null)
			throw new IllegalArgumentException("Controller: run: out es nulo");
		if (t < 0)
			throw new IllegalArgumentException("Controller: run: t debe ser >= 0");
		if (dt <= 0)
			throw new IllegalArgumentException("Controller: run: dt debe ser > 0");
		
		JSONObject initState = _sim.asJSON();
		
		SimpleObjectViewer view = null;
		if (sv) {
			MapInfo m = _sim.getMapInfo();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows());
			view.update(toAnimalsInfo(_sim.getAnimals()), _sim.getTime(), dt);
		}
		
		
		while (_sim.getTime() <= t) {
			_sim.advance(dt);
			if (sv) view.update(toAnimalsInfo(_sim.getAnimals()), _sim.getTime(), dt);
		}
		
		if (sv)
			view.close();
		
		JSONObject finalState = _sim.asJSON();
		
		JSONObject output = new JSONObject();
		output.put("in", initState);
		output.put("out", finalState);
		
		PrintStream p = new PrintStream(out);
		p.println(output.toString(2));
	}
	
	private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals)
			ol.add(new ObjInfo(a.getGeneticCode(), (int) a.getPosition().getX(), (int) a.getPosition().getY(), 8));
		return ol;
	}
}
