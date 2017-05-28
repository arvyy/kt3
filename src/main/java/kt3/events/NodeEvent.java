package kt3.events;

import kt3.Node;

public class NodeEvent {
	
	public enum Type {
		CREATE,
		CHANGE, 
		DELETE
	}
	
	private Type type;
	private Node node;
	
	public NodeEvent(Type type, Node node) {
		this.type = type;
		this.node = node;
	}
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	
}
