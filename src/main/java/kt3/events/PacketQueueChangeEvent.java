package kt3.events;

import java.util.List;

import kt3.Packet;

public class PacketQueueChangeEvent {
	
	private List<Packet> packets;

	public PacketQueueChangeEvent(List<Packet> packets) {
		this.packets = packets;
	}
	
	public List<Packet> getPackets() {
		return packets;
	}
	
}
