package kt3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import kt3.events.NodeEvent;
import kt3.events.NodeEvent.Type;

@Component
@Scope("prototype")
public class Node {
	
	@Autowired
	private NetworkModel network;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	private String name;
	
	private Map<String, TableEntry> routingTable;
	
	private Set<String> neighbours;
	
	@PostConstruct
	private void init() {
		neighbours = new HashSet<>();
		routingTable= new HashMap<>();
	}
	
	public void onConnectionAdded(String other) {
		neighbours.add(other);
		network.sendTableUpdate(name, other, new ArrayList<>(routingTable.values()));
	}
	
	public void onConnectionBroken(String other) {
		neighbours.remove(other);
		List<TableEntry> update = new ArrayList<>();
		for (TableEntry e : routingTable.values()) {
			if (e.nextNode.equals(other)) {
				e.destinationSequence++;
				e.hopCount = Integer.MAX_VALUE;
				update.add(e);
			}
		}
		sendTableUpdate(update);
	}
	
	public void sendFullUpdate() {
		TableEntry me = routingTable.get(name);
		me.destinationSequence++;
		eventPublisher.publishEvent(new NodeEvent(Type.CHANGE, this));
		sendTableUpdate(new ArrayList<>(routingTable.values()));
	}
	
	public void sendTableUpdate(List<TableEntry> entries) {
		for (String neighbour : neighbours) {
			network.sendTableUpdate(name, neighbour, entries);
		}
	}
	
	public void receiveTableUpdate(String source, List<TableEntry> entries) {
		List<TableEntry> update_rows = new ArrayList<>();
		List<TableEntry> adjust_entries = entries.stream()
				.map(e -> new TableEntry(e))
				.peek(e -> {
					if (e.getTargetNode().equals(name)) {
						e.hopCount = 0;
						e.nextNode = name;
					} else {
						if (e.hopCount != Integer.MAX_VALUE)
							e.hopCount++; 
						e.nextNode = source;
					}
				})
				.collect(Collectors.toList());
		for (TableEntry e : adjust_entries) {

			if (!routingTable.containsKey(e.targetNode)) {
				update_rows.add(e);
				continue;
			}
			if (e.destinationSequence > routingTable.get(e.targetNode).destinationSequence) {
				update_rows.add(e);
				continue;
			}
			if (e.destinationSequence == routingTable.get(e.targetNode).destinationSequence && e.hopCount < routingTable.get(e.targetNode).hopCount) {
				update_rows.add(e);
				continue;
			}
		}
		for (TableEntry e : update_rows) {
			routingTable.put(e.targetNode, e);
		}
		if (!update_rows.isEmpty()) {
			eventPublisher.publishEvent(new NodeEvent(Type.CHANGE, this));
			for (String neighbour : neighbours) {
				if (!neighbour.equals(source)) {
					network.sendTableUpdate(name, neighbour, update_rows);
				}
			}
		}
	}
	
	public void sendData(Packet.Data data) {
		if (!routingTable.containsKey(data.destination)) {
			System.out.println("Paketas " + data.data.toString() + " prarastas mazge " + name + ".");
		} else {
			String next = routingTable.get(data.destination).nextNode;
			network.sendData(name, next, data);
		}
	}
	
	public void receiveData(Packet.Data data) {
		if (data.destination == name) {
			System.out.println("Gautas paketas " + data.data.toString() + " mazge " + name + ". Galutinis mazgas.");
		} else {
			sendData(data);
			System.out.println("Gautas paketas " + data.data.toString() + " mazge " + name + ". Persiunčiamas toliau.");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, TableEntry> getRoutingTable() {
		return routingTable;
	}
	
	public Set<String> getNeighbours() {
		return neighbours;
	}

	public void setName(String name) {
		if (this.name != null) return;
		this.name = name;
		TableEntry e = new TableEntry();
		e.targetNode = name;
		e.hopCount = 0;
		e.nextNode = name;
		e.destinationSequence = network.createDestinationSequence();
		this.routingTable.put(name, e);
	}

	public static class TableEntry {
		private String targetNode;
		private String nextNode;
		private int hopCount;
		private int destinationSequence;
		
		public TableEntry() {}
		
		public TableEntry(TableEntry e) {
			targetNode = e.targetNode;
			nextNode = e.nextNode;
			hopCount = e.hopCount;
			destinationSequence = e.destinationSequence;
		}	
		
		public String getTargetNode() {return targetNode; }
		public String getNextNode() { return nextNode; }
		public Integer getHopCount() { return hopCount; }
		public Integer getDestinationSequence() { return destinationSequence; }
	}
}
