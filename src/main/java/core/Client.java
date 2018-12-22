package core;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class Client implements AutoCloseable {

	private String lastHost;
	private int lastPort;
	private String lastLoginData;
	
	private Socket server;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isClosed;
	
	public void connect(String host, int port, String loginData) throws UnknownHostException, IOException {
		server = new Socket(host, port);
		out = new ObjectOutputStream(server.getOutputStream());
		out.flush();
		Thread thread = inSetterThread(Thread.currentThread());
		thread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Successful in initialization
		}
		if (in == null) {
			close();
			throw new IOException("Unable to join");
		}
		listenThread();
		sendRequest(new Request(Request.LOGIN, loginData));
		lastHost = host;
		lastPort = port;
		lastLoginData = loginData;
	}
	
	private Thread inSetterThread(Thread toInterrupt) {
		return new Thread(() -> {
			try {
				in = new ObjectInputStream(server.getInputStream());
				toInterrupt.interrupt();
			} catch (IOException e) {
				if (!"Software caused connection abort: recv failed".equals(e.getMessage()) &&
					!"Socket closed".equals(e.getMessage())) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void sendRequest(Request request) throws IOException {
		if (server.isClosed()) {
			throw new IOException("Connection Timed Out");
		}
		out.writeObject(request);
		out.flush();
	}
	
	public void reconnect() throws UnknownHostException, IOException {
		close();
		isClosed = false;
		connect(lastHost, lastPort, lastLoginData);
	}
	
	protected abstract void gotResponse(Response response);
	
	private void listenThread() {
		new Thread(() -> {
			while(!isClosed) {
				try {
					gotResponse((Response) in.readObject());
				} catch (EOFException e) {
					close();
				} catch (IOException e) {
					if (!(isClosed && "Socket closed".equals(e.getMessage()))) {
						e.printStackTrace();
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	@Override
	public void close() {
		isClosed = true;
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
