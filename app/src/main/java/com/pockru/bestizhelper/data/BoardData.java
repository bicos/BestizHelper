package com.pockru.bestizhelper.data;

import java.io.Serializable;

public class BoardData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8736404653103144182L;
	
	public String id;
	public String name;
	public String baseUrl;
	public String detailUrl;

	public BoardData(String id, String name, String baseUrl, String detailUrl) {
		super();
		this.id = id;
		this.name = name;
		this.baseUrl = baseUrl;
		this.detailUrl = detailUrl;
	}

}
