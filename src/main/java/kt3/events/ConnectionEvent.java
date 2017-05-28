package kt3.events;

public class ConnectionEvent {
	
	public enum Type {
		CONNECTED,
		DISCONNECTED
	}
	
	private Type type;
	private String a, b;
	
	public ConnectionEvent(Type t, String a, String b) {
		this.type = t;
		this.a = a;
		this.b = b;
	}
	
	public Type getType() {
		return type;
	}
	public String getA() {
		return a;
	}
	public String getB() {
		return b;
	}
	
}
