package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import simulator.control.Controller;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private Controller ctrl;

	public MainWindow(Controller ctrl) {
		super("[ECOSYSTEM SIMULATOR]");
		this.ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);

		ControlPanel controlPanel = new ControlPanel(ctrl);
		mainPanel.add(controlPanel, BorderLayout.PAGE_START);

		StatusBar statusBar = new StatusBar(ctrl);
		mainPanel.add(statusBar, BorderLayout.PAGE_END);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		InfoTable speciesTable = new InfoTable("Species", new SpeciesTableModel(this.ctrl));
		speciesTable.setPreferredSize(new Dimension(500, 250));
		contentPanel.add(speciesTable);

		InfoTable regionsTable = new InfoTable("Regions", new RegionsTableModel(this.ctrl));
		regionsTable.setPreferredSize(new Dimension(500, 250));
		contentPanel.add(regionsTable);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ViewUtils.quit(MainWindow.this);
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}