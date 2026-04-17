package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.EcoSysObserver;
import simulator.model.animal.Animal;
import simulator.model.animal.AnimalInfo;
import simulator.model.region.MapInfo;
import simulator.model.region.RegionInfo;

@SuppressWarnings("serial")
class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {

	private String[] columns;
	private List<String> species;
	private Map<String, int[]> data;

	SpeciesTableModel(Controller ctrl) {
		initTable();
		ctrl.addObserver(this);
	}

	private void initTable() {
		Animal.State[] states = Animal.State.values();

		columns = new String[states.length + 1];
		columns[0] = "Species";
		for (int i = 0; i < states.length; i++) {
			columns[i + 1] = states[i].toString();
		}

		species = new ArrayList<>();
		data = new HashMap<>();
	}

	@Override
	public int getRowCount() {
		return species.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String code = species.get(rowIndex);

		if (columnIndex == 0)
			return code;

		return data.get(code)[columnIndex - 1];
	}

	private void updateTable(List<AnimalInfo> animals) {
		species.clear();
		data.clear();

		int statesCount = Animal.State.values().length;

		for (AnimalInfo a : animals) {
			String code = a.getGeneticCode();

			if (!data.containsKey(code)) {
				data.put(code, new int[statesCount]);
				species.add(code);
			}

			int stateIndex = a.getState().ordinal();
			data.get(code)[stateIndex]++;
		}

		fireTableDataChanged();
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateTable(animals);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateTable(animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateTable(animals);
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateTable(animals);
	}
}
