package com.pockru.network;

import java.util.Map;

import org.apache.http.client.entity.UrlEncodedFormEntity;

public class RequestInfo {
	private String url = "";
	private String params = "";
	private String encoding = "";

	private Map<String, String> requestProperty;
	private UrlEncodedFormEntity entity;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Map<String, String> getRequestProperty() {
		return requestProperty;
	}

	public void setRequestProperty(Map<String, String> requestProperty) {
		this.requestProperty = requestProperty;
	}

	public void setStartPeriod(String timonPeriod) {
		// TODO Auto-generated method stub

	}

	public UrlEncodedFormEntity getEntity() {
		return entity;
	}

	public void setEntity(UrlEncodedFormEntity entity) {
		this.entity = entity;
	}

}
