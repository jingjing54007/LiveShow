package com.v2tech.net;

public class ResponsePacket extends Packet {

	private long requestId;

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	
	
}
