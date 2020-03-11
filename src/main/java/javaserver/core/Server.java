package javaserver.core;

import java.io.IOException;

import javaserver.exceptions.NotInitializedException;

public interface Server extends AutoCloseable {

	void setPort(int port);
	int getPort();
	
	void setMax(int max);
	int getMax();
	
	int getConnectionsSize();
	
	void start() throws NotInitializedException, IOException;
	boolean isRunning();
		
	void setJoinAllowed(boolean isJoinAllowed);
	boolean isJoinAllowed();
	
	void onJoin(Connection connection);
	
	void setRequestHandler(RequestHandler requestHander);
	void broadcast(Response response);
	void connectionClosed(Connection connection);
	
	
	long getCtoTimeMillis();
	void setCtoTimeMillis(long ctoTimeMillis);
	long getCtoPollTimeMillis();
	void setCtoPollTimeMillis(long ctoPollTimeMillis);
	int getJoinCtoTimeMillis();
	void setJoinCtoTimeMillis(int joinCtoTimeMillis);
}
