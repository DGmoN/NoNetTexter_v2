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
import java.util.Collection;
import java.util.Collections;

import web.HtmlSection;
import DataTypes.ByteConventions;
import Database.database;
import Formating.Strings;
import Tracker.EventTracker;

public abstract class ioManeger extends Thread {

	private static String thisID;

	private static int PORT; // Local port ID

	// only for outgoing connection
	private static boolean isOutgoing_ = false;
	private static boolean cleaning;
	private static String Target;
	private static int targetPort;
	//
	private static EventTracker TT;// IO log writer

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
				if (!cleaning) {
					try {
						if (isOutgoing_) {
							TT.Write("Trying to connect to: " + Target
									+ "  \tPort: " + targetPort, 0);
							NetFrame.MainWindow.addText("Connectiong to: "
									+ Target);
							_Socket = new Socket(Target, targetPort);
						} else {
							_Socket = SSocket.accept();
						}
						for (Connection s : Connections) {
							if (_Socket
									.getInetAddress()
									.getHostAddress()
									.equals(s.get_socket().getInetAddress()
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
				} else {
					try {
						sleep(750);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		private void resetSockets() {
			try {
				_Socket = null;
			} catch (Exception e) {
				TT.Write(e);
			}
			try {
				SSocket = new ServerSocket(PORT);
			} catch (Exception e) {
				TT.Write(e);
			}
			try {
				SSocket.setSoTimeout(1000);
			} catch (Exception e) {
				TT.Write(e);
			}

		}
	};

	public static Connection[] getConnections() {
		Connection[] ret = new Connection[Connections.size()];
		return Connections.toArray(ret);
	}

	public static void sendData(int Matker, byte[] data) {
		String[] targets = NetFrame.MainWindow.getSelectedList();
		if (targets[0] == null) {
			NetFrame.MainWindow.addText("[ERROR] No Targets Selected");
			System.out.println("No Targets Selected");
			return;
		}
		NetFrame.MainWindow.addText("Me: "
				+ ByteConventions.byteSequenceToStrings(data));
		TT.Write("Sending : " + Matker, 0);
		TT.Write("Data : " + ByteConventions.bytesToHexes(data), 0);
		TT.Write("To: " + Formating.Strings.combine("\t", 1, targets), 0);
		System.out.println("To: \n"
				+ Formating.Strings.combine("\t", 1, targets));
		for (Connection s : Connections) {
			for (String a : targets) {
				if (s.getName().equals(a))
					s.addDataForSend(Matker, data);
			}

		}
	}

	/*
	 * This is meant to remove all the dead connections, ID's will not be reset
	 * unfortunately
	 */
	public static void cleanup() {
		cleaning = true;
		TT.Write("Cleaning!", 1);
		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 1500) {

		}
		ArrayList<Connection> toBeRemoved = new ArrayList<Connection>();
		for (Connection s : Connections) {
			if (s.markedForRemoval) {
				toBeRemoved.add(s);
				NetFrame.MainWindow.addText("[INFO] " + s.getName()
						+ " has disconnected");
			}
		}
		TT.Write(toBeRemoved.size() + " : Removed", 1);
		Connections.removeAll((toBeRemoved));
		cleaning = false;
	}

	public static void init(int port) {
		TT = EventTracker.init("Logs/IOLogs.txt", ioManeger.class);
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
			ret[x++] = s.getName();
		}
		return ret;
	}

	public static String getThisID() {
		return thisID;
	}

	private static void createConnection(Socket a) {
		try {
			TT.Write("Creating connection to: " + a.getInetAddress()
					+ " : is outgoing -> " + isOutgoing_, 0);
			Connections.add(new Connection(a, isOutgoing_));
			isOutgoing_ = false;
			TT.Write("Connection made! :-)", 0);
		} catch (IOException e) {
			TT.Write(e);
		}
	}
}
