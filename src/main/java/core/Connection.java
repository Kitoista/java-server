package core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Connection implements AutoCloseable {
	
	private String ip;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Server server;
	private boolean isLoggedIn;
	private boolean isClosed;
	
	public Connection(Socket socket, Server server) throws IOException {
		this.socket = socket;
		this.server = server;
		ip = socket.getInetAddress().getHostAddress();
		out = new ObjectOutputStream(socket.getOutputStream());
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
			throw new IOException("The client side didn't open an ObjectOutputStream");
		}
	}
	
	private Thread inSetterThread(Thread toInterrupt) {
		return new Thread(() -> {
			try {
				in = new ObjectInputStream(socket.getInputStream());
				toInterrupt.interrupt();
			} catch (IOException e) {
				if (!"Socket closed".equals(e.getMessage())) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void close() {
		if (isClosed) {
			return;
		}
		isClosed = true;
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
		if (server != null) {
			server.connectionClosed(this);
		}
	}
	
	public String getIp() {
		return ip;
	}
	
	public void sendResponse(Response response) throws IOException {
		try {
			if (out != null && socket != null && !socket.isClosed()) {
				out.writeObject(response);
				out.flush();
			}
		} catch (SocketException e) {
			if ("Software caused connection abort: socket write error".equals(e.getMessage())) {
				close();
				throw new SocketException("The client closed the socket");
			}
			throw e;
		}
	}
	
	public Request readRequest() throws ClassNotFoundException, IOException {
		return (Request) in.readObject();
	}
	
	
	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	
}
