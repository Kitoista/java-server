package core;

import java.io.Serializable;

import org.json.JSONObject;

public class Response implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final int PING = 1;
	public static final int BROADCAST = 2;
	public static final int AUTH = 3;
	
	private int type;
	private String data;

	public Response(int type, JSONObject data) {
		this.type = type;
		this.data = data.toString();
	}
	
	public Response(int type, String json) {
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
