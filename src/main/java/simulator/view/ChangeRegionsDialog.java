package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.EcoSysObserver;
import simulator.model.animal.AnimalInfo;
import simulator.model.region.MapInfo;
import simulator.model.region.RegionInfo;

@SuppressWarnings("serial")
class ChangeRegionsDialog extends JDialog implements EcoSysObserver {

	private DefaultComboBoxModel<String> regionsModel;
	private DefaultComboBoxModel<String> fromRowModel;
	private DefaultComboBoxModel<String> toRowModel;
	private DefaultComboBoxModel<String> fromColModel;
	private DefaultComboBoxModel<String> toColModel;

	private DefaultTableModel dataTableModel;
	private Controller ctrl;
	private List<JSONObject> regionsInfo;

	private String[] headers = { "Key", "Value", "Description" };

	@SuppressWarnings("unused") // Unused para el status (o borrar status)
	private int status;
	private JComboBox<String> regionsCombo;
	private JComboBox<String> fromRowCombo;
	private JComboBox<String> toRowCombo;
	private JComboBox<String> fromColCombo;
	private JComboBox<String> toColCombo;

	ChangeRegionsDialog(Controller ctrl) {
		super((Frame) null, true);
		this.ctrl = ctrl;
		initGUI();
		this.ctrl.addObserver(this);
	}

	private void initGUI() {
		setTitle("Change Regions");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);

		this.regionsInfo = Main.regionsFactory.getInfo(); // se usa en createControlsPanel - no borrar

		mainPanel.add(createHelpPanel());
		mainPanel.add(createTablePanel());
		mainPanel.add(createControlsPanel());
		mainPanel.add(createButtonsPanel());

		if (this.regionsModel.getSize() > 0) {
			this.regionsCombo.setSelectedIndex(0);
			updateRegionDataTable(0);
		}

		setPreferredSize(new Dimension(700, 400)); // puedes usar otro tamaño
		pack();
		setResizable(false);
		setVisible(false);
	}

	public void open(Frame parent) {
		setLocation(parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
				parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		pack();
		setVisible(true);
	}

	private JPanel createHelpPanel() {
		JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JLabel helpLabel = new JLabel("<html><div style='text-align:center;'>"
				+ "Select a region type, the rows/cols interval, and provide values for the parameters in the Value column<br>"
				+ "(default values are used for parameters with no value)." + "</div></html>");

		helpPanel.add(helpLabel);

		return helpPanel;
	}

	private JPanel createTablePanel() {
		JPanel tablePanel = new JPanel(new BorderLayout());

		this.dataTableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}
		};
		this.dataTableModel.setColumnIdentifiers(this.headers);

		JTable table = new JTable(this.dataTableModel);
		table.setFillsViewportHeight(true);

		tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

		return tablePanel;
	}

	private JPanel createControlsPanel() {
		JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		this.regionsModel = new DefaultComboBoxModel<>();
		for (JSONObject info : this.regionsInfo) {
			this.regionsModel.addElement(info.getString("type"));
		}

		this.regionsCombo = new JComboBox<>(this.regionsModel);
		this.regionsCombo.addActionListener(e -> updateRegionDataTable(this.regionsCombo.getSelectedIndex()));

		controlsPanel.add(new JLabel("Region type:"));
		controlsPanel.add(this.regionsCombo);

		this.fromRowModel = new DefaultComboBoxModel<>();
		this.toRowModel = new DefaultComboBoxModel<>();

		this.fromRowCombo = new JComboBox<>(this.fromRowModel);
		this.toRowCombo = new JComboBox<>(this.toRowModel);

		controlsPanel.add(new JLabel("Row from/to:"));
		controlsPanel.add(this.fromRowCombo);
		controlsPanel.add(this.toRowCombo);

		this.fromColModel = new DefaultComboBoxModel<>();
		this.toColModel = new DefaultComboBoxModel<>();

		this.fromColCombo = new JComboBox<>(this.fromColModel);
		this.toColCombo = new JComboBox<>(this.toColModel);

		controlsPanel.add(new JLabel("Column from/to:"));
		controlsPanel.add(this.fromColCombo);
		controlsPanel.add(this.toColCombo);

		return controlsPanel;
	}

	private JPanel createButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton okButton = new JButton("OK");
		okButton.addActionListener(e -> okAction());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> cancelAction());

		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		return buttonsPanel;
	}

	private void updateRegionDataTable(int index) {
		this.dataTableModel.setRowCount(0);

		if (index < 0 || index >= this.regionsInfo.size()) {
			return;
		}

		JSONObject info = this.regionsInfo.get(index);
		JSONObject data = info.getJSONObject("data");

		for (String key : data.keySet()) {
			this.dataTableModel.addRow(new Object[] { key, "", data.get(key).toString() });
		}
	}

	private void cancelAction() {
		this.status = 0;
		setVisible(false);
	}

	private void okAction() {
		try {
			JSONObject regionData = new JSONObject();

			for (int i = 0; i < this.dataTableModel.getRowCount(); i++) {
				Object keyObj = this.dataTableModel.getValueAt(i, 0);
				Object valueObj = this.dataTableModel.getValueAt(i, 1);

				String key = keyObj.toString();
				String value = valueObj == null ? "" : valueObj.toString().trim();

				if (!value.isEmpty()) {
					regionData.put(key, value);
				}
			}

			int index = this.regionsCombo.getSelectedIndex();
			String regionType = this.regionsInfo.get(index).getString("type");

			int rowFrom = Integer.parseInt(this.fromRowCombo.getSelectedItem().toString());
			int rowTo = Integer.parseInt(this.toRowCombo.getSelectedItem().toString());
			int colFrom = Integer.parseInt(this.fromColCombo.getSelectedItem().toString());
			int colTo = Integer.parseInt(this.toColCombo.getSelectedItem().toString());

			JSONObject spec = new JSONObject();
			spec.put("type", regionType);
			spec.put("data", regionData);

			JSONObject region = new JSONObject();
			region.put("row", new JSONArray().put(rowFrom).put(rowTo));
			region.put("col", new JSONArray().put(colFrom).put(colTo));
			region.put("spec", spec);

			JSONObject json = new JSONObject();
			json.put("regions", new JSONArray().put(region));

			this.ctrl.setRegions(json);

			this.status = 1;
			setVisible(false);

		} catch (Exception e) {
			ViewUtils.showErrorMsg(this, e.getMessage());
		}
	}

	private void updateCoords(MapInfo map) {
		this.fromRowModel.removeAllElements();
		this.toRowModel.removeAllElements();
		this.fromColModel.removeAllElements();
		this.toColModel.removeAllElements();

		for (int i = 0; i < map.getRows(); i++) {
			String s = Integer.toString(i);
			this.fromRowModel.addElement(s);
			this.toRowModel.addElement(s);
		}

		for (int i = 0; i < map.getCols(); i++) {
			String s = Integer.toString(i);
			this.fromColModel.addElement(s);
			this.toColModel.addElement(s);
		}
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateCoords(map);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateCoords(map);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {

	}
}