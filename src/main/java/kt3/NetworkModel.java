package kt3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kt3.Node.TableEntry;
import kt3.Packet.Type;
import kt3.events.ConnectionEvent;
import kt3.events.NodeEvent;
import kt3.events.PacketQueueChangeEvent;

@Component
public class NetworkModel {
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Value("${kt3.manualupdate}")
	private Boolean manualUpdate;
	
	private Map<String, Node> nodes;
	private Map<String, Set<String>> connections;
	private List<Packet> packetQueue;
	
	private int dest_seq_last;
	
	public NetworkModel() {
		nodes = new HashMap<>();
		connections = new HashMap<>();
		packetQueue = Collections.synchronizedList(new LinkedList<Packet>());
		
		dest_seq_last = 0;
	}
	
	public int createDestinationSequence() {
		return 100 * ++dest_seq_last + 1;
	}
	
	@Scheduled(fixedRateString="${kt3.tickrate}")
	private void scheduledTick() {
		if (!manualUpdate)
			sendNextPacket();
	}
	
	public void sendNextPacket() {
		if (packetQueue.isEmpty()) return;
		Packet p = packetQueue.remove(0);
		if (connectionLive(p.getFrom(), p.getTo())) {
			if (p.getType() == Type.TABLE_UPDATE) {
				getNode(p.getTo()).receiveTableUpdate(p.getFrom(), p.getUpdate());
			} else if (p.getType() == Type.DATA) {
				getNode(p.getTo()).receiveData(p.getData());
			}
		} else {
			System.out.println("Paketas prarastas tarp " + p.getFrom() + " ir " + p.getTo());
		}
		eventPublisher.publishEvent(new PacketQueueChangeEvent(packetQueue));
	}	
	
	@Scheduled(fixedRateString="${kt3.dumprate}")
	private void scheduledDump() {
		if (!manualUpdate)
			fullDump();
	}
	
	public void fullDump() {
		nodes.values().forEach(node -> node.sendFullUpdate());
	}
	
	public void addNode(String name, String[] neighbours) {
		if (!nodes.containsKey(name)) {
			Node n = context.getBean(Node.class);
			n.setName(name);
			nodes.put(name, n);
			if (neighbours != null)
				Arrays.stream(neighbours).forEach(neighbour -> addConnection(name, neighbour));
			eventPublisher.publishEvent(new NodeEvent(NodeEvent.Type.CREATE, n));
		} else {
			System.err.println("Nodas " + name + " jau egzistuoja. ");
		}
	}
	
	public void removeNode(String name) {
		Set<String> neighbours = connections.get(name);
		if (neighbours != null) 
			while (!neighbours.isEmpty()) {
				breakConnection(name, neighbours.iterator().next());
			}	
		nodes.remove(name);
	}
	
	public void breakConnection(String n1, String n2) {
		connections.get(n1).remove(n2);
		connections.get(n2).remove(n1);
		getNode(n1).onConnectionBroken(n2);
		getNode(n2).onConnectionBroken(n1);
		eventPublisher.publishEvent(new ConnectionEvent(ConnectionEvent.Type.DISCONNECTED, n1, n2));
	}
	
	public void addConnection(String n1, String n2) {
		if (!nodes.containsKey(n1) || !nodes.containsKey(n2)) return;
		if (connections.get(n1) == null || !connections.get(n1).contains(n2)) {
			if (connections.get(n1) == null) connections.put(n1, new HashSet<>());
			connections.get(n1).add(n2);
			if (connections.get(n2) == null) connections.put(n2, new HashSet<>());
			connections.get(n2).add(n1);
			nodes.get(n1).onConnectionAdded(n2);
			nodes.get(n2).onConnectionAdded(n1);
			eventPublisher.publishEvent(new ConnectionEvent(ConnectionEvent.Type.CONNECTED, n1, n2));
		}
	}
	
	public void sendTableUpdate(String node_from, String node_to, List<TableEntry> update) {
		packetQueue.add(new Packet(node_from, node_to, update));
		eventPublisher.publishEvent(new PacketQueueChangeEvent(packetQueue));
	}
	
	public void sendData(String node_from, String node_to, Packet.Data data) {
		packetQueue.add(new Packet(node_from, node_to, data));
		eventPublisher.publishEvent(new PacketQueueChangeEvent(packetQueue));
	}
	
	public boolean connectionLive(String n1, String n2) {
		return connections.get(n1) != null && connections.get(n1).contains(n2);
	}
	
	public Node getNode(String name) {
		return nodes.get(name);
	}
	
	public Collection<Node> getNodes() {
		return nodes.values();
	}
}
