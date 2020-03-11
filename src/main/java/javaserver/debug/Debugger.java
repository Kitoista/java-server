package javaserver.debug;

public class Debugger {
	
	public static void debugMessage(Object obj) {
		debugMessage(obj.toString());
	}
	
	public static void debugMessage(String msg) {
		if ("true".equals(System.getenv("java-server.isDebugMode"))) {
			System.out.println("-- java-server: " + msg);
		}
	}
}
