package kt3.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

public class NodeContainer extends JPanel{
	
	private Map<String, NodeComponent> nodeComponents;
	
	private Set<Set<String>> connections;
	
	public NodeContainer() {
		nodeComponents = new HashMap<>();
		connections = new HashSet<>();
		setLayout(null);
		setFocusable(true);
	}
	
	public void addNodeComponent(NodeComponent node) {
		nodeComponents.put(node.getNode().getName(), node);
		add(node);
	}
	
	public void removeNodeComponent(String name) {
		remove(nodeComponents.get(name));
		nodeComponents.remove(name);
	}
		
	public void addConnection(String a, String b) {
		HashSet<String> set = new HashSet<>();
		set.add(a);
		set.add(b);
		connections.add(set);
	}
	
	public void removeConnection(String a, String b) {
		HashSet<String> set = new HashSet<>();
		set.add(a);
		set.add(b);
		connections.remove(set);
	}
	
	public NodeComponent getNodeComponent(String name) {
		return nodeComponents.get(name);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (Set<String> c : connections) {
			Iterator<String> i = c.iterator();
			NodeComponent a = nodeComponents.get(i.next());
			NodeComponent b = nodeComponents.get(i.next());
			g.setColor(Color.BLACK);
			g.drawLine(a.getX() + a.getWidth()/2, a.getY() + a.getHeight() / 2, 
					b.getX() + b.getWidth()/2, b.getY() + b.getHeight() / 2);
		}
	}


	
	
}
