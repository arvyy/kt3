package kt3.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import kt3.Node;
import kt3.Node.TableEntry;

public class NodeComponent extends JInternalFrame {
	
	private final Node node;
	
	private JTable routingTable;
	private DefaultTableModel tableModel;
	private JButton connect_btn;
	private JButton disconnect_btn;
	private JButton maximize;
	private JButton minimize;
	private JButton sendData_btn;
	private CardLayout layout;
	
	public NodeComponent(Node node) {
		this.node = node;
		tableModel = new DefaultTableModel();
		Object[] columns = {"Tikslas", "Sekantis", "Šuolių", "Sekos nr."};
		tableModel.setColumnIdentifiers(columns);
		
		layout = new PageViewer();
		setLayout(layout);
		
		JPanel simplePanel = new JPanel();
		simplePanel.add(maximize = new JButton("+"));
		simplePanel.add(new JLabel(node.getName()));
		
		add(simplePanel, "simple");
		
		JPanel p;
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new BorderLayout());
		
		detailsPanel.add(minimize = new JButton("-"), BorderLayout.NORTH);
		
		p = new JPanel();
		p.setLayout(new BorderLayout());
		routingTable = new JTable(tableModel);
		p.add(routingTable, BorderLayout.CENTER);
		p.add(routingTable.getTableHeader(), BorderLayout.NORTH);
		detailsPanel.add(p, BorderLayout.CENTER);
		
		
		Box operationsPanel = Box.createVerticalBox();
		
		p = new JPanel();
		p.add(new JLabel("Sujungimai"));
		connect_btn = new JButton("+");
		p.add(connect_btn);
		disconnect_btn = new JButton("-");
		p.add(disconnect_btn);
		operationsPanel.add(p);
		
		p = new JPanel();
		p.add(sendData_btn = new JButton("Siųsti duomenis"));
		operationsPanel.add(p);
		
		detailsPanel.add(operationsPanel, BorderLayout.SOUTH);
		
		add(detailsPanel, "details");
		
		
		
		updateView();
		setClosable(true);
		setTitle(node.getName());
		setVisible(true);
	}
	
	public void updateView() {
		tableModel.getDataVector().clear();
		for (TableEntry t : node.getRoutingTable().values()) {
			tableModel.addRow(new Object[]{
				t.getTargetNode(),
				t.getNextNode(),
				t.getHopCount(),
				t.getDestinationSequence()	
			});
		}
		pack();
	}
	
	public JButton getConnectButton() {
		return connect_btn;
	}
	
	public JButton getDisconnectButton() {
		return disconnect_btn;
	}
	
	public JButton getMinimizeButton() {
		return minimize;
	}
	
	public JButton getMaximizeButton() {
		return maximize;
	}
	
	public JButton getSendDataButton() {
		return sendData_btn;
	}
	
	public Node getNode() {
		return node;
	}
	
	private class PageViewer extends CardLayout {

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Component current = findCurrentComponent(parent);
            if (current != null) {
                Insets insets = parent.getInsets();
                Dimension pref = current.getPreferredSize();
                pref.width += insets.left + insets.right;
                pref.height += insets.top + insets.bottom;
                return pref;
            }
            return super.preferredLayoutSize(parent);
        }

        public Component findCurrentComponent(Container parent) {
            for (Component comp : parent.getComponents()) {
                if (comp.isVisible()) {
                    return comp;
                }
            }
            return null;
        }
    }
}
