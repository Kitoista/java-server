package javaserver.imp;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javaserver.core.Connection;
import javaserver.core.Request;
import javaserver.core.RequestHandler;
import javaserver.core.Response;
import javaserver.core.Server;
import javaserver.debug.Debugger;
import javaserver.exceptions.NotInitializedException;

public class ServerImp implements Server {
	
	private int port = 19969;
	private int max = 10;
	private long ctoTimeMillis = 3 * 1000;
	private long ctoPollTimeMillis = 100;
	private int joinCtoTimeMillis = 1000;
	private boolean isInitialized;
	private boolean isRunning;
	private boolean isJoinAllowed = true;
	private RequestHandler requestHandler;
	
	private ServerSocket server;
	private boolean isClosed;
	
	private Collection<Connection> connections = new HashSet<>();
	private Map<Connection, Long> cto = new HashMap<>();
	
	private Collection<Thread> threads = new HashSet<>();
	
	public ServerImp() {
	}

	@Override
	public void start() throws NotInitializedException, IOException {
		if (!isInitialized) {
			if (requestHandler == null) {
				throw new NotInitializedException("RequestHandler is null");
			}
			Debugger.debugMessage("starting server...");
			server = new ServerSocket(port);
			Debugger.debugMessage("server started");
			server.setSoTimeout(joinCtoTimeMillis);
			startThread();
			ctoThread();
			isInitialized = true;
		}
		isRunning = true;
	}
	
	@Override
	public void broadcast(Response response) {
		for (Connection connection : connections) {
			if (connection.isLoggedIn()) {
				try {
					connection.sendResponse(response);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void connectionClosed(Connection connection) {
		connections.remove(connection);
		cto.remove(connection);
		onClose(connection);
	}
	
	@Override
	public void close() {
		Debugger.debugMessage("closing server...");
		isClosed = true;
		for (Thread thread : threads) {
			thread.interrupt();
		}
		for (Connection connection : connections) {
			connection.close();
		}
		try {
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
		}
		Debugger.debugMessage("server closed");
	}
	
	
	// threads
	

	private void startThread() {
		Debugger.debugMessage("start thread started");
		Thread thread = new Thread(() -> {
			while(!isClosed) {
				if (!isJoinAllowed && max < connections.size()) {
					try {
						Thread.sleep(joinCtoTimeMillis);
					} catch (InterruptedException e) {
					}
				} else {
					try {
						Socket socket = server.accept();
						Debugger.debugMessage("socket appeared with ip: " + socket.getInetAddress().getHostAddress());
						if (isJoinAllowed && max > connections.size()) {
							Connection connection = new Connection(socket, this);
							Debugger.debugMessage("connection built");
							connections.add(connection);
							cto.put(connection, System.currentTimeMillis());
							listenerThread(connection);
						} else {
							socket.close();
						}
					} catch (SocketTimeoutException e) {
					} catch (IOException e) {
						if (!"Socket operation on nonsocket: configureBlocking".equals(e.getMessage())) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		threads.add(thread);
		thread.start();
	}
	
	private void listenerThread(Connection connection) {
		Debugger.debugMessage("new listener thread started for: " + connection.getIp());
		Thread thread = new Thread(() -> {
			while(!isClosed && connections.contains(connection)) {
				try {
					Request request = connection.readRequest();
					Debugger.debugMessage("request arrived from " + connection.getIp());
					requestHandler.serveRequest(this, connection, request);
				} catch (EOFException e) {
					connection.close();
				} catch (IOException e) {
					if ("Software caused connection abort: recv failed".equals(e.getMessage()) ||
						"socket closed".equals(e.getMessage().toLowerCase())) {
						connection.close();
					} else {
						e.printStackTrace();
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		threads.add(thread);
	}
	
	private void ctoThread() {
		Debugger.debugMessage("cto thread started");
		Thread thread = new Thread(() -> {
			while(!isClosed) {
				Set<Connection> toRemove = new HashSet<>();
				for (Connection connection : cto.keySet()) {
					long lastRequestTime = cto.get(connection);
					if (System.currentTimeMillis() - lastRequestTime > ctoTimeMillis) {
						Debugger.debugMessage("connection timed out for " + connection.getIp());
						toRemove.add(connection);
					}
				}
				for (Connection connection : toRemove) {
					connection.close();
				}
				try {
					Thread.sleep(ctoPollTimeMillis);
				} catch (InterruptedException e) {
				}
			}
		});
		threads.add(thread);
		thread.start();
	}
	
	
	// callbacks
	
	
	@Override
	public void onJoin(Connection connection) {
		Debugger.debugMessage("socket logged in: " + connection.getIp());
	}
	
	public void onClose(Connection connection) {
	}
	
	
	// getters setters marker XXX
	
	@Override
	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public int getPort() {
		return port;
	}
	@Override
	public void setMax(int max) {
		this.max = max;
	}

	@Override
	public int getMax() {
		return max;
	}
	@Override
	public boolean isRunning() {
		return isRunning;
	}
	@Override
	public void setRequestHandler(RequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}
	@Override
	public void setJoinAllowed(boolean isJoinAllowed) {
		this.isJoinAllowed = isJoinAllowed;
		
	}
	@Override
	public boolean isJoinAllowed() {
		return isJoinAllowed;
	}

	@Override
	public int getConnectionsSize() {
		return connections.size();
	}
	public long getCtoTimeMillis() {
		return ctoTimeMillis;
	}
	public void setCtoTimeMillis(long ctoTimeMillis) {
		this.ctoTimeMillis = ctoTimeMillis;
	}
	public long getCtoPollTimeMillis() {
		return ctoPollTimeMillis;
	}
	public void setCtoPollTimeMillis(long ctoPollTimeMillis) {
		this.ctoPollTimeMillis = ctoPollTimeMillis;
	}
	public int getJoinCtoTimeMillis() {
		return joinCtoTimeMillis;
	}
	public void setJoinCtoTimeMillis(int joinCtoTimeMillis) {
		this.joinCtoTimeMillis = joinCtoTimeMillis;
	}
}
