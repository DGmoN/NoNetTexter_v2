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

import DataTypes.ByteConventions;
import Tracker.EventTracker;

public abstract class ioManeger extends Thread {

	private static String thisID;

	private static class Connection { // Connection is the port master, manages
										// outgoing and incoming bytes

		public boolean markedForRemoval = false;
		private Thread Deamon = new Thread() {
			@Override
			public void run() {
				int strike = 0;
				byte[] buffer = new byte[16];
				byte[] sendBuffer = null;
				while (true) {
					if (strike == 5) {
						markedForRemoval = true;
						NetFrame.Cleanable = true;
						return;
					}
					try {
						if (sendBuffer != null) {
							ToTarget.write(sendBuffer);
							ToTarget.flush();
							sendBuffer = null;
						}

						TT.Write("Read: \t" + FromTarget.read(buffer), 0);
						manageIncomingData(buffer);
						strike = 0;
					} catch (IOException e1) {
						TT.Write(Name + " : Failure Strike #" + (strike++), 2);
					}
					try {
						if (strike != 0) {
							sleep(500);
						} else
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
			System.out.printf(Name + " : Internal ID: %s\t External ID: %s\n",
					(int) ID, (int) externalID);
			Deamon.start();
		}

		private void manageIncomingData(byte[] a) {
			TT.Write("DATA : " + ByteConventions.bytesToHexes(a), 0);

		}

		public String getName() {
			return Name;
		}

		public void kill() {
			try {
				TT.Write("Closing socket: " + getName(), 1);
				_socket.close();
			} catch (IOException e) {
				TT.Write(e);
			}
		}
	}

	private static int PORT; // Local port ID

	// only for outgoing connection
	private static boolean isOutgoing_ = false;
	private static boolean cleaning;
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
				if (!cleaning) {
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
				SSocket = new ServerSocket(PORT);
				SSocket.setSoTimeout(1000);
			} catch (Exception e) {
				TT.Write(e);
			}
		}
	};

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
			}
		}
		TT.Write(toBeRemoved.size() + " : Removed", 1);
		Connections.removeAll((toBeRemoved));
		cleaning = false;
	}

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
			ret[x++] = s.getName();
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
