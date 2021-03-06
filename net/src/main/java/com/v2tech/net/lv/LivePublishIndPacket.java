package com.v2tech.net.lv;

import com.v2tech.net.pkt.IndicationPacket;


public class LivePublishIndPacket extends IndicationPacket {
	
	public long vid;
	public long lid;
	public long uid;
	public double lat;
	public double lng;
	public long v2uid;
	public String pwd;
	public OptType ot;
	
	public LivePublishIndPacket() {
		super();
	}
	
	
	public LivePublishIndPacket(long vid, long uid, long lid, double lat, double lng, String pwd) {
		super();
		this.vid = vid;
		this.uid = uid;
		this.lid = lid;
		this.lat = lat;
		this.lng = lng;
		this.pwd = pwd;
	}

	

	
	
	
	public enum OptType{
		PUBLISH,UPDATE;
	}
	
	
	

	

	
	

}
