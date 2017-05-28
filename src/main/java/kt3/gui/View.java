package kt3.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class View extends JFrame {

	private NodeContainer nodeContainer;
	
	private JButton addNodeButton, dumpButton, tickButton, initNodesButton;
	
	private DefaultTableModel tableModel;

	public View(boolean manualupdate) {
		nodeContainer = new NodeContainer();
		setLayout(new BorderLayout());
		
		add(nodeContainer, BorderLayout.CENTER);
		
		JPanel topPanel = new JPanel();
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 5, 5);
		topPanel.setLayout(flowLayout);
		topPanel.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		topPanel.add(addNodeButton = new JButton("+ Pridėti mazgą"));
		if (manualupdate) {
			topPanel.add(dumpButton = new JButton("Siųsti lenteles"));
			topPanel.add(tickButton = new JButton("Siųsti vieną paketą"));
		}
		topPanel.add(initNodesButton = new JButton("Pradiniai mazgai"));
		add(topPanel, BorderLayout.NORTH);
		
		if (manualupdate) {
			tableModel = new DefaultTableModel();
			tableModel.setColumnIdentifiers(new String[]{
				"Tipas",
				"Iš",
				"Į",
				"Duomenys"
			});
			JTable packet_queue = new JTable(tableModel);
			add(new JScrollPane(packet_queue), BorderLayout.EAST);
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public NodeContainer getNodeContainer() {
		return nodeContainer;
	}
	
	public JButton getAddNodeButton() {
		return addNodeButton;
	}
	
	public JButton getDumpButton() {
		return dumpButton;
	}
	
	public JButton getTickButton() {
		return tickButton;
	}
	
	public JButton getInitNodesButton() {
		return initNodesButton;
	}
	
	public DefaultTableModel getPacketQueueTableModel() {
		return tableModel;
	}
	
}
