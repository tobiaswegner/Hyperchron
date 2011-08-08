package org.hyperchron.impl;

import java.util.concurrent.atomic.AtomicLong;


public class EntityDescriptor {
	public Tree	tree;
	public String uuid;
	public long entityID; 
	java.util.concurrent.atomic.AtomicLong elementID = new AtomicLong();
	
	public EntityDescriptor (String uuid, long entityID) {
		this.uuid = uuid;
		this.entityID = entityID;
	}
	
	public long getNextElementID () {
		return elementID.getAndIncrement();
	}

	public void setNextElementID (long id) {
		elementID.set(id);
	}
}