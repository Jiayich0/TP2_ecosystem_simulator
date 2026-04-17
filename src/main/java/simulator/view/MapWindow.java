package simulator.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.EcoSysObserver;
import simulator.model.animal.AnimalInfo;
import simulator.model.region.MapInfo;
import simulator.model.region.RegionInfo;

@SuppressWarnings("serial")
class MapWindow extends JFrame implements EcoSysObserver {

	private Controller ctrl;
	private AbstractMapViewer viewer;
	private Frame parent;

	MapWindow(Frame parent, Controller ctrl) {
		super("[MAP VIEWER]");
		this.ctrl = ctrl;
		this.parent = parent;
		initGUI();
		this.ctrl.addObserver(this);
	}

	private void initGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);

		this.viewer = new MapViewer();
		mainPanel.add(this.viewer, BorderLayout.CENTER);

		addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				ctrl.removeObserver(MapWindow.this);
				dispose();
			}

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});

		pack();
		if (this.parent != null)
			setLocation(this.parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
					this.parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);

		setResizable(false);
		setVisible(true);
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> {
			this.viewer.reset(time, map, animals);
			pack();
		});
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> {
			this.viewer.reset(time, map, animals);
			pack();
		});
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		SwingUtilities.invokeLater(() -> {
			this.viewer.update(animals, time);
		});
	}
}
