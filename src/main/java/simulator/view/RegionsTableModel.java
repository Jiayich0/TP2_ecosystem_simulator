package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.EcoSysObserver;
import simulator.model.animal.Animal;
import simulator.model.animal.AnimalInfo;
import simulator.model.region.MapInfo;
import simulator.model.region.MapInfo.RegionData;
import simulator.model.region.RegionInfo;

@SuppressWarnings("serial")
class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {

	private String[] columns;
	private List<RegionData> regions;
	// private MapInfo map;

	RegionsTableModel(Controller ctrl) {
		initTable();
		ctrl.addObserver(this);
	}

	private void initTable() {
		Animal.Diet[] diets = Animal.Diet.values();

		columns = new String[diets.length + 3];
		columns[0] = "Row";
		columns[1] = "Col";
		columns[2] = "Desc.";
		for (int i = 0; i < diets.length; i++) {
			columns[i + 3] = diets[i].toString();
		}

		regions = new ArrayList<>();
		// map = null;
	}

	@Override
	public int getRowCount() {
		return regions.size();
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
		RegionData d = regions.get(rowIndex);

		if (columnIndex == 0)
			return d.row();

		if (columnIndex == 1)
			return d.col();

		if (columnIndex == 2)
			return d.r().toString();

		Animal.Diet diet = Animal.Diet.values()[columnIndex - 3];
		int count = 0;

		for (AnimalInfo a : d.r().getAnimalsInfo()) {
			if (a.getDiet() == diet)
				count++;
		}
		return count;
	}

	private void updateTable(MapInfo map) {
		// this.map = map;
		regions.clear();

		for (RegionData d : map) {
			regions.add(d);
		}

		fireTableDataChanged();
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateTable(map);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateTable(map);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateTable(map);
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		updateTable(map);
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateTable(map);
	}
}
