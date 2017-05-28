package kt3;

import java.util.List;

import kt3.Node.TableEntry;

public class Packet {
	private String from, to;
	public enum Type {
		TABLE_UPDATE,
		DATA
	}
	private Type type;
	private List<TableEntry> update;
	private Data data;
	
	public Packet(String from, String to, List<TableEntry> update) {
		this.from = from;
		this.to = to;
		type = Type.TABLE_UPDATE;
		this.update = update;
	}
	
	public Packet(String from, String to, Data data) {
		this.from = from;
		this.to = to;
		type = Type.DATA;
		this.data = data;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public Type getType() {
		return type;
	}

	public List<TableEntry> getUpdate() {
		return update;
	}

	public Data getData() {
		return data;
	}
	
	public static class Data {
		public final String destination;
		public final Object data;
		public Data(String dest, Object data) {
			this.destination = dest;
			this.data = data;
		}
	}
	
}