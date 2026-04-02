package com.mw.layout.audit.model.listener;

import java.io.Serializable;

public class CollectionModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long ctCollectionId;
	private String name;
	private boolean production;
	
	public long getCtCollectionId() {
		return ctCollectionId;
	}
	public void setCtCollectionId(long ctCollectionId) {
		this.ctCollectionId = ctCollectionId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isProduction() {
		return production;
	}
	public void setProduction(boolean production) {
		this.production = production;
	}
	
	public CollectionModel(long ctCollectionId, String name, boolean production) {
		super();
		this.ctCollectionId = ctCollectionId;
		this.name = name;
		this.production = production;
	}
}