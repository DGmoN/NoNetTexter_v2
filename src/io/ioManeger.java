package io;

/*	Thought: Client starts normaly -> socket listening port = 5555
 * 			 Client starts with argument x -> socket listening port = x
 * 
 * */
import gui.front.NetFrame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import DataTypes.ByteConventions;
import Tracker.EventTracker;

public abstract class ioManeger extends Thread {

	private static String thisID;

	private static class Connection { // Connection is the port master, manages
										// outgoing and incoming bytes

		private Thread Deamon = new Thread() {
			@Override
			public void run() {
				byte[] buffer = new byte[8];
				while (true) {

					/*
					 * try { ToTarget.write(new byte[] { (byte) 0xFF, 0x00,
					 * 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }); ToTarget.flush();
					 * } catch (IOException e1) { e1.printStackTrace(); }
					 */

					try {
						if (FromTarget.available() == 8)
							;
						System.out
								.println("Read: \t" + FromTarget.read(buffer));
						manageIncomingData(buffer);
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					try {
						sleep(150);
					} catch (InterruptedException e) {

					}
				}
			}
		};

		public final boolean isOutgoingConection;

		private static char TID = 0;
		final char ID, externalID;
		private final InputStream FromTarget;
		private final OutputStream ToTarget;

		private final Socket _socket;

		private final String Name;

		public Connection(Socket s, boolean a) throws IOException {
			Deamon.setDaemon(true);

			ID = TID++;
			_socket = s;
			isOutgoingConection = a;
			FromTarget = s.getInputStream();
			ToTarget = s.getOutputStream();
			Name = _socket.getInetAddress().getHostName();
			ToTarget.write(new byte[] { (byte) ID });
			byte[] d = new byte[1];
			FromTarget.read(d);
			externalID = (char) d[0];
			System.out.printf("Internal ID: %s\t External ID: %s\n", (int) ID,
					(int) externalID);
			Deamon.start();
		}

		private void manageIncomingData(byte[] a) {
			System.out.println(PORT + ":" + ByteConventions.bytesToHexes(a));
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
			boolean makeConnection = true;
			TT.Write("Starting daemon on port: " + SSocket.getLocalPort(), 0);
			while (true) {
				try {
					if (isOutgoing_) {
						TT.Write("Trying to connect to: " + Target
								+ "  \tPort: " + targetPort, 0);
						_Socket = new Socket(Target, targetPort);
						isOutgoing_ = false;
					} else {
						_Socket = SSocket.accept();
					}
					for (Connection s : Connections) {
						if (_Socket
								.getInetAddress()
								.getHostAddress()
								.equals(s._socket.getInetAddress()
										.getHostAddress())) {
							makeConnection = false;
						}

					}
					if (makeConnection) {
						createConnection(_Socket);
					} else {
						System.err.printf(
								"A connection is already made with: %s\n",
								_Socket.getInetAddress().getHostAddress());
					}
					NetFrame.MainWindow.updateListOfContacts();
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

	public static int getConnectedCount() {
		return Connections.size();
	}

	public static String[] getConnectedNames() {
		String[] ret = new String[Connections.size()];
		int x = 0;
		for (Connection s : Connections) {
			ret[x++] = s.Name;
		}
		return ret;
	}

	public static String getThisID() {
		return thisID;
	}

	private static void createConnection(Socket a) {
		try {
			TT.Write("Creating connection to: " + a.getInetAddress(), 0);
			Connections.add(new Connection(a, isOutgoing_));
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
