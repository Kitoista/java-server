package core;

public interface RequestHandler {

	void serveRequest(Server server, Connection connection, Request request);
	
}
