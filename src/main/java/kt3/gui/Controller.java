package kt3.gui;

import java.awt.CardLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import kt3.NetworkModel;
import kt3.Packet;
import kt3.Packet.Data;
import kt3.events.ConnectionEvent;
import kt3.events.NodeEvent;
import kt3.events.PacketQueueChangeEvent;

@Component
public class Controller {
	
	@Autowired
	private NetworkModel network;
	
	@Value("${kt3.manualupdate}")
	private Boolean manualupdate;
	
	private View view;
	
	@PostConstruct
	private void init() {
		SwingUtilities.invokeLater(() -> {
			view = new View(manualupdate);
			attachListeners();
		});
	}
	
	private void attachListeners() {
		view.getAddNodeButton().addActionListener(l -> {
			String node_name = JOptionPane.showInputDialog("Mazgo pavadinimas");
			network.addNode(node_name, new String[]{});
		});
		view.getInitNodesButton().addActionListener(l -> {
			network.addNode("A", null);
			network.addNode("B", null);
			network.addNode("C", new String[]{"A", "B"});
			network.addNode("D", new String[]{"C"});
			network.addNode("F", null);
			network.addNode("G", null);
			network.addNode("H", new String[]{"A"});
			network.addNode("E", new String[]{"D", "F", "G", "H"});
		});
		if (manualupdate) {
			view.getTickButton().addActionListener(l-> {
				network.sendNextPacket();
			});
			view.getDumpButton().addActionListener(l -> {
				network.fullDump();
			});
		}
	}
	
	private void showCreateConnection(NodeComponent nodecomp) {
		String sel_name = nodecomp.getNode().getName();
		Set<String> neighbours = nodecomp.getNode().getNeighbours();
		String[] other_nodes = network.getNodes().stream()
			.map(n -> n.getName())
			.filter(name -> !name.equals(sel_name))
			.filter(name -> !neighbours.contains(name))
			.toArray(size -> new String[size]);
		JList<String> list = new JList<>(other_nodes);
		
		int rez = JOptionPane.showConfirmDialog(view, new Object[]{"Sujungti su...", list}, "", JOptionPane.OK_CANCEL_OPTION);
		if (rez == JOptionPane.OK_OPTION) {
			list.getSelectedValuesList().forEach(node -> network.addConnection(sel_name, node));
		}
	}
	
	private void showRemoveConnection(NodeComponent nodecomp) {
		String sel_name = nodecomp.getNode().getName();
		JList<String> list = new JList<>(nodecomp.getNode().getNeighbours().stream().toArray(n -> new String[n]));
		int rez = JOptionPane.showConfirmDialog(view, new Object[]{"Atjungti nuo...", list}, "", JOptionPane.OK_CANCEL_OPTION);
		if (rez == JOptionPane.OK_OPTION) {
			list.getSelectedValuesList().forEach(node -> network.breakConnection(sel_name, node));
		}
	}
	
	private void showSendData(NodeComponent nodecomp) {
		JComboBox<String> target_cb = new JComboBox<>(network.getNodes().stream().map(n -> n.getName()).toArray(size -> new String[size]));
		target_cb.setEditable(false);
		JTextField data = new JTextField();
		int rez = JOptionPane.showConfirmDialog(view, new Object[]{"Galinis mazgas", target_cb, "Duomenys", data}, "", JOptionPane.OK_CANCEL_OPTION);
		if (rez == JOptionPane.OK_OPTION) {
			String target = (String)target_cb.getSelectedItem();
			if (target == null) return;
			nodecomp.getNode().sendData(new Data(target, data.getText()));
		}
	}
	
	@EventListener
	private void handleNodeEvent(NodeEvent e) {
		NodeContainer container;
		NodeComponent node;
		switch(e.getType()) {
			case CREATE : 
				node = new NodeComponent(e.getNode());
				node.getConnectButton().addActionListener(l -> {
					showCreateConnection(node);
				});
				node.getDisconnectButton().addActionListener(l -> {
					showRemoveConnection(node);
				});
				node.getSendDataButton().addActionListener(l -> {
					showSendData(node);
				});
				node.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentMoved(ComponentEvent e) {
						view.getNodeContainer().repaint();
					}
				});
				node.getMaximizeButton().addActionListener(l -> {
					CardLayout layout = (CardLayout)node.getContentPane().getLayout();
					layout.show(node.getContentPane(), "details");
					node.pack();
				});
				node.getMinimizeButton().addActionListener(l -> {
					CardLayout layout = (CardLayout)node.getContentPane().getLayout();
					layout.show(node.getContentPane(), "simple");
					node.pack();
				});
				node.addInternalFrameListener(new InternalFrameAdapter() {
					@Override
					public void internalFrameClosed(InternalFrameEvent e) {
						network.removeNode(node.getNode().getName());
					}
				});
				view.getNodeContainer().addNodeComponent(node);
				break;
			case CHANGE : 
				container = view.getNodeContainer();
				node = container.getNodeComponent(e.getNode().getName());
				node.updateView();
				break;
			case DELETE : 
				container = view.getNodeContainer();
				node = container.getNodeComponent(e.getNode().getName());
				node.dispose();
				container.removeNodeComponent(e.getNode().getName());
		}
		view.getNodeContainer().repaint();
	}
	
	@EventListener
	private void handleConnectionEvent(ConnectionEvent e) {
		switch (e.getType()) {
			case CONNECTED : 
				view.getNodeContainer().addConnection(e.getA(), e.getB());
				break;
			case DISCONNECTED:
				view.getNodeContainer().removeConnection(e.getA(), e.getB());
				break;
		}
		view.getNodeContainer().repaint();
	}
	
	@EventListener
	private void handlePacketQueueChangeEvent(PacketQueueChangeEvent e) {
		if (!manualupdate) return;
		DefaultTableModel m = view.getPacketQueueTableModel();
		m.setRowCount(0);
		
		List<Packet> packets = e.getPackets();
		synchronized(packets) {
			for (Packet p : packets) {
				m.addRow(new Object[]{
					p.getType(), 
					p.getFrom(), 
					p.getTo(), 
					p.getType() == Packet.Type.DATA ? p.getData().toString() : null
				});
			}
		}
	}
}
