package com.v2tech.vo.group;


public class OrgGroup extends Group {
	
	public OrgGroup(long mGId, String mName) {
		super(mGId, GroupType.ORG, mName, null, null);
	}
	
	
	@Override
	public String toXml() {
		return "";
	}
}
