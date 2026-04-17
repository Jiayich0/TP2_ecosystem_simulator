package simulator.view;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;

@SuppressWarnings("serial")
class ControlPanel extends JPanel {

	private Controller ctrl;
	// private ChangeRegionsDialog changeRegionsDialog;

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

		// TODO Inicializar this.changeRegionsDialog con instancias del diálogo de
		// cambio
		// de regiones

	}

	private void initToolBar() {
		this.openButton = createButton("Open", "open", () -> openAction());
		this.viewerButton = createButton("Viewer", "viewer", () -> noAction());
		this.regionsButton = createButton("Regions", "regions", () -> noAction());
		this.runButton = createButton("Run", "run", () -> runAction());
		this.stopButton = createButton("Stop", "stop", () -> stopAction());

		// Steps Spinner
		this.stepsSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, Integer.MAX_VALUE, 100));
		this.toolBar.add(stepsSpinner);
		this.toolBar.addSeparator();

		// Delta-Time TextField
		this.deltaTimeText = new JTextField("0.03", 5);
		this.toolBar.add(deltaTimeText);
		this.toolBar.addSeparator();

		this.quitButton = createButton("Quit", "exit", () -> ViewUtils.quit(this));
	}

	private JButton createButton(String name, String icon, Runnable action) {
		JButton button = new JButton();
		button.setToolTipText(name);
		button.setIcon(new ImageIcon("src/main/resources/extra/icons/" + icon + ".png"));
		// name.toUpercase() y quitar icon pero quit != exit
		button.addActionListener(e -> action.run());

		if (!"Quit".equals(name)) {
			this.toolBar.add(button);
			this.toolBar.addSeparator();
		} else {
			this.toolBar.add(Box.createGlue()); // this aligns the button to the right
			this.toolBar.addSeparator();
			this.toolBar.add(button);
		}

		return button;
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

	// TODO falta viewAction y regionsAction de momento noAction
	private void noAction() {

	}
	// private void viewAction()
	// private void regionsAction()

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

		if (viewerButton != null)
			viewerButton.setEnabled(!running);

		if (regionsButton != null)
			regionsButton.setEnabled(!running);

		stepsSpinner.setEnabled(!running);
		deltaTimeText.setEnabled(!running);

		stopButton.setEnabled(running);
	}
}
