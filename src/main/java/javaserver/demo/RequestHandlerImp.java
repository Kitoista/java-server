package javaserver.demo;

import java.io.IOException;

import org.json.JSONObject;

import javaserver.core.Connection;
import javaserver.core.Request;
import javaserver.core.RequestHandler;
import javaserver.core.Response;
import javaserver.core.Server;

public class RequestHandlerImp implements RequestHandler {

	@Override
	public void serveRequest(Server server, Connection connection, Request request) {
		JSONObject data = request.getData();
		switch (request.getType()) {
		case Request.LOGIN : serveLoginRequest(server, connection, data); break;
		case 2 : serveChatRequest(server, connection, data); break;
		}
	}
	
	protected void serveLoginRequest(Server server, Connection connection, JSONObject data) {
		System.out.println("LoginRequest");
		if (data.has("login") && data.getBoolean("login") == true) {
			server.onJoin(connection);
			connection.setLoggedIn(true);
			try {
				connection.sendResponse(new Response(Response.AUTH, "{shallYouPass: true}"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				connection.sendResponse(new Response(Response.AUTH, "{shallYouPass: false}"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			connection.close();
		}
	}
	
	protected void serveChatRequest(Server server, Connection connection, JSONObject data) {
		System.out.println("ChatRequest from " + connection.getIp());
		System.out.println("data: " + data);
		try {
			connection.sendResponse(new Response(Response.PING, "{success: true, yourDataWas: "  + data + "}"));
		} catch (IOException e) {
			if (!"The client closed the socket".equals(e.getMessage())) {
				e.printStackTrace();
			}
		}
	}

}
