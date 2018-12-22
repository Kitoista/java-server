package demo;

import java.io.IOException;

import core.Client;
import core.Request;
import core.RequestHandler;
import core.Response;
import core.Server;
import exceptions.NotInitializedException;
import imp.ServerImp;

public class Main {

	public static void main(String[] args) {
		
		Environment.setEnv();
		
		Server server = new ServerImp();
		RequestHandler rh = new RequestHandlerImp();
		server.setRequestHandler(rh);
		try {
			server.start();
		} catch (NotInitializedException | IOException e1) {
			e1.printStackTrace();
		}
		
		Client client = new Client() {
			@Override
			protected void gotResponse(Response response) {
				System.out.println("I got the following response: " + response.getData());
			}
		};
		
		Client client2 = new Client() {
			@Override
			protected void gotResponse(Response response) {
				System.out.println("I got the following epic response: " + response.getData());
			}
		};
		
		
		sleep(1 * 1000);
		
		try {
			client.connect("127.0.0.1", 19969, "{login: true}");
			sleep(1 * 1000);
			client.sendRequest(new Request(Request.CHAT, "{message: \"First joiner, first message\"}"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		sleep(2 * 1000);

		try {
			client2.connect("127.0.0.1", 19969, "{login: false}");
			sleep(1 * 1000);
			client2.sendRequest(new Request(Request.CHAT, "{message: \"Second joiner, first message\"}"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			client.sendRequest(new Request(Request.CHAT, "{message: \"First joiner, second message\"}"));
		} catch (IOException e) {
			e.printStackTrace();
			if ("Connection Timed Out".equals(e.getMessage())) {
				try {
					client.reconnect();
					client.sendRequest(new Request(Request.CHAT, "{message: \"First joiner, third message\"}"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		sleep(2 * 1000);

		server.broadcast(new Response(Response.PING, "{ data: \"Broadcast message\" }"));
		
		sleep(2 * 1000);
		
		
		client.close();
		client2.close();
		
		
		try {
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		sleep(1 * 1000);
		
	}
	
	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
