package javaserver.core;

import java.io.Serializable;

import org.json.JSONObject;

public class Request implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final int LOGIN = 1;
	public static final int CHAT = 2;
	
	private int type;
	private String data;
	
	public Request(int type, JSONObject data) {
		this.type = type;
		this.data = data.toString();
	}
	
	public Request(int type, String json) {
		this.type = type;
		this.data = json;
	}
	
	public int getType() {
		return type;
	}
	
	public JSONObject getData() {
		return new JSONObject(data);
	}
	
}
