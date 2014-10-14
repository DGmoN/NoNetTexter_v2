package io;

/*	Thought: Client starts normaly -> socket listening port = 5555
 * 			 Client starts with argument x -> socket listening port = x
 * 
 * */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import Tracker.EventTracker;

public abstract class ioManeger extends Thread {

	private static String thisID;

	private static class Connection { // Connection is the port master, manages
										// outgoing and incoming bytes

		private static int nextID = 0;

		public final int ConnectionID;

		public final boolean isOutgoingConection;

		private final InputStream FromTarget;
		private final OutputStream ToTarget;

		private final Socket _socket;

		public Connection(Socket s, boolean a) throws IOException {
			_socket = s;
			isOutgoingConection = a;
			FromTarget = s.getInputStream();
			ToTarget = s.getOutputStream();
			ConnectionID = nextID++;
		}
	}

	private static int PORT; // Local port ID

	// only for outgoing connection
	private static boolean isOutgoing_ = false;
	private static String Target;
	private static int targetPort;
	//
	private static EventTracker TT = EventTracker.init("logs/IOLogs", // IO log
																		// writer
			ioManeger.class);

	// All active Connections(Might want to make a cleanup for this)
	private static ArrayList<Connection> Connections = new ArrayList<Connection>();

	private static Socket _Socket;
	private static ServerSocket SSocket;

	private static final Thread ListenerDeamon = new Thread() {
		@Override
		public void run() {
			resetSockets();
			TT.Write("Starting daemon on port: " + SSocket.getLocalPort(), 0);
			while (true) {
				try {
					if (isOutgoing_) {

						TT.Write("Trying to connect to: " + Target
								+ "  |\tPort: " + targetPort, 0);
						_Socket = new Socket(Target, targetPort);
						isOutgoing_ = false;
					} else {
						_Socket = SSocket.accept();
					}
					createConnection(_Socket, false);
					resetSockets();
				} catch (SocketTimeoutException e) {

				} catch (UnknownHostException d) {
					TT.Write(d.getLocalizedMessage(), 2);
					isOutgoing_ = false;
					System.err.println("No sutch host");
					resetSockets();
				} catch (IOException e) {
					TT.Write(e);
				}
			}
		}

		private void resetSockets() {
			try {
				if (_Socket != null) {
					_Socket.close();
				}
				if (SSocket != null) {
					SSocket.close();
				}
				SSocket = null;
				_Socket = null;
				SSocket = new ServerSocket(PORT);
				SSocket.setSoTimeout(1000);
			} catch (Exception e) {
				TT.Write(e);
			}
		}
	};

	public static void init(int port) {
		TT.Write("Starting IOManager...", 0);
		try {
			thisID = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			TT.Write(e);
		}
		PORT = port;
		ListenerDeamon.setDaemon(true);
		ListenerDeamon.start();

	}

	public static void ConnectTo(String target, int port) {
		Target = target;
		targetPort = port;
		isOutgoing_ = true;
	}

	public static int getListeningPort() {
		return PORT;
	}

	public static String getThisID() {
		return thisID;
	}

	private static void createConnection(Socket a, boolean isOutgoing) {
		try {
			TT.Write("Creating connection to: " + a.getInetAddress(), 0);
			Connections.add(new Connection(a, isOutgoing));
			TT.Write("Connection made! :-)", 0);
		} catch (IOException e) {
			TT.Write(e);
		}
	}

	public static boolean isOutgoingMade() {
		for (Connection s : Connections) {
			if (s.isOutgoingConection)
				return true;
		}
		return false;
	}

}
