package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import simulator.control.Controller;
import simulator.model.EcoSysObserver;
import simulator.model.animal.AnimalInfo;
import simulator.model.region.MapInfo;
import simulator.model.region.RegionInfo;

@SuppressWarnings("serial")
class StatusBar extends JPanel implements EcoSysObserver {

	private JLabel timeLabel;
	private JLabel animalsLabel;
	private JLabel dimensionLabel;

	StatusBar(Controller ctrl) {
		initGUI();
		ctrl.addObserver(this);
	}

	private void initGUI() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createBevelBorder(1));

		timeLabel = new JLabel("Time: 0.000");
		animalsLabel = new JLabel("Total Animals: 0");
		dimensionLabel = new JLabel("Dimension: 0x0 0x0");

		add(timeLabel);
		add(createSeparator());

		add(animalsLabel);
		add(createSeparator());

		add(dimensionLabel);
	}

	private JSeparator createSeparator() {
		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		s.setPreferredSize(new Dimension(10, 20));
		return s;
	}

	private void updateValues(double time, MapInfo map, List<AnimalInfo> animals) {
		timeLabel.setText("Time: " + String.format("%.3f", time)); // 0.000
		animalsLabel.setText("Total Animals: " + animals.size());
		dimensionLabel.setText(
				"Dimension: " + map.getWidth() + "x" + map.getHeight() + " " + map.getCols() + "x" + map.getRows());
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateValues(time, map, animals);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateValues(time, map, animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateValues(time, map, animals);
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateValues(time, map, animals);
	}

}