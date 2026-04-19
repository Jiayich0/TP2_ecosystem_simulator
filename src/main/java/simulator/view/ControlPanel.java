package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.misc.Const;

@SuppressWarnings("serial")
class ControlPanel extends JPanel {

	private Controller ctrl;
	private ChangeRegionsDialog changeRegionsDialog;

	private JToolBar toolBar;
	private JFileChooser fc;
	private boolean stopped = true; // utilizado en los botones de run/stop
	private JButton openButton;
	private JButton viewerButton;
	private JButton regionsButton;
	private JButton runButton;
	private JButton stopButton;
	private JSpinner stepsSpinner;
	private JTextField deltaTimeText;
	private JButton quitButton;

	ControlPanel(Controller ctrl) {
		this.ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());

		toolBar = new JToolBar();
		add(toolBar, BorderLayout.PAGE_START);

		initToolBar();

		fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));

		stopButton.setEnabled(false);

		this.changeRegionsDialog = new ChangeRegionsDialog(this.ctrl);

	}

	private void initToolBar() {
		
		this.openButton = createButton("Open", Const.OPEN_ICON, () -> openAction());
		this.toolBar.addSeparator();
		this.viewerButton = createButton("Viewer", Const.VIEWER_ICON, () -> viewerAction());
		this.regionsButton = createButton("Regions", Const.REGIONS_ICON, () -> regionsAction());
		this.toolBar.addSeparator();
		this.runButton = createButton("Run", Const.RUN_ICON, () -> runAction());
		this.stopButton = createButton("Stop", Const.STOP_ICON, () -> stopAction());
		this.stepsSpinner = createLabeledSpinner("Steps", Const.STEPS_INITIAL_VALUE, Const.STEPS_MIN, Const.STEPS_MAX, Const.STEPS_INCREMENT);
		this.deltaTimeText = createLabeledTextField("Delta-Time", String.valueOf(Main.dt), Const.DELTA_TIME_TEXT_COLS);
		this.toolBar.addSeparator();
		this.quitButton = createButton("Quit", Const.EXIT_ICON, () -> ViewUtils.quit(this));
		
	}

	private JButton createButton(String name, String iconPath, Runnable action) {
		JButton button = new JButton();
		button.setToolTipText(name);
		button.setIcon(loadIcon(iconPath));
		button.addActionListener(e -> action.run());

		if ("Quit".equals(name)) {
			this.toolBar.add(Box.createGlue()); // this aligns the button to the right
		}
		this.toolBar.add(button);

		return button;
	}
	
	private ImageIcon loadIcon(String iconPath) {
		URL url = getClass().getClassLoader().getResource(iconPath);
		if (url == null) {
			throw new IllegalArgumentException("Icon not found: " + iconPath);
		}
		return new ImageIcon(url);
	}
	
	private JSpinner createLabeledSpinner(String name, int initialValue, int min, int max, int increment) {
		JLabel label = new JLabel(" " + name + ": ");
		this.toolBar.add(label);
		
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialValue, min, max, increment));
		spinner.setPreferredSize(new Dimension(80, spinner.getPreferredSize().height));
		this.toolBar.add(spinner);
		
		return spinner;
	}
	
	private JTextField createLabeledTextField(String name, String defaultText, int columns) {
		JLabel label = new JLabel(" " + name + ": ");
		this.toolBar.add(label);
		
		JTextField textField = new JTextField(defaultText, columns);
		this.toolBar.add(textField);
		
		return textField;
	}

	private void openAction() {
		int returnValue = fc.showOpenDialog(ViewUtils.getWindow(this));

		if (returnValue != JFileChooser.APPROVE_OPTION)
			return;

		try (InputStream in = new FileInputStream(fc.getSelectedFile())) {
			JSONObject data = new JSONObject(new JSONTokener(in));

			int cols = data.getInt("cols");
			int rows = data.getInt("rows");
			int width = data.getInt("width");
			int height = data.getInt("height");

			ctrl.reset(cols, rows, width, height);
			ctrl.loadData(data);

		} catch (Exception ex) {
			ViewUtils.showErrorMsg(this, ex.getMessage());
		}
	}

	private void viewerAction() {
		new MapWindow(ViewUtils.getWindow(this), this.ctrl);
	}

	private void regionsAction() {
		this.changeRegionsDialog.open(ViewUtils.getWindow(this));
	}

	private void runAction() {
		try {
			this.stopped = false;
			enableButtons(true);

			int n = (Integer) this.stepsSpinner.getValue();
			double dt = Double.parseDouble(this.deltaTimeText.getText());

			runSim(n, dt);
		} catch (Exception e) {
			this.stopped = true;
			enableButtons(false);
			ViewUtils.showErrorMsg(this, e.getMessage());
		}
	}

	private void runSim(int n, double dt) {
		if (n > 0 && !this.stopped) {
			try {
				this.ctrl.advance(dt);
				SwingUtilities.invokeLater(() -> runSim(n - 1, dt));
			} catch (Exception e) {
				ViewUtils.showErrorMsg(this, e.getMessage());
				this.stopped = true;
				enableButtons(false);
			}
		} else {
			this.stopped = true;
			enableButtons(false);
		}
	}

	private void stopAction() {
		this.stopped = true;
		enableButtons(false); // se podria poner enableButtons(!stopped);
	}

	private void enableButtons(boolean running) {
		openButton.setEnabled(!running);
		runButton.setEnabled(!running);
		quitButton.setEnabled(!running);
		viewerButton.setEnabled(!running);
		regionsButton.setEnabled(!running);
		stepsSpinner.setEnabled(!running);
		deltaTimeText.setEnabled(!running);

		stopButton.setEnabled(running);
	}
}
