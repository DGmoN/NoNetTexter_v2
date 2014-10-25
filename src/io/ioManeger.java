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
import Database.database;
import Formating.Strings;
import Tracker.EventTracker;

public abstract class ioManeger extends Thread {

	private static String thisID;

	public static boolean ShowStreamText = false;

	private static final byte START_OF_STRING = (byte) 0x8A;
	private static final byte END = (byte) 0xFF;

	private static class Connection { // Connection is the port master, manages
										// outgoing and incoming bytes

		protected EventTracker myTracker;
		public boolean markedForRemoval = false;
		boolean DataReady = false;
		byte[] sendBuffer = null;

		private Thread Deamon = new Thread() {
			@Override
			public void run() {
				int strike = 0;
				long now, timeTaken = -1;
				byte[] buffer = new byte[16];
				if (isOutgoingConection)
					NetFrame.MainWindow.addText("[INFO] Connected to: " + Name);
				else
					NetFrame.MainWindow
							.addText("[INFO] " + Name + " Connected");
				myTracker.Write("Starting Deamon for\"" + getName() + "\"", 0);
				while (true) {
					now = System.currentTimeMillis();
					if (ShowStreamText)
						myTracker.Write("Cycle time = " + timeTaken, 0);
					if (strike == 5) {
						markedForRemoval = true;
						NetFrame.Cleanable = true;
						return;
					}
					try {
						if (DataReady) {
							ToTarget.write(sendBuffer);
							ToTarget.flush();
							DataReady = false;
							sendBuffer = null;
						} else {
							ToTarget.write(new byte[] { 0x00 });
							ToTarget.flush();
						}
						FromTarget.read(buffer);
						if (ShowStreamText)
							myTracker.Write("Read: \t" + buffer.length, 0);
						manageIncomingData(buffer);
						buffer = new byte[16];
						strike = 0;
					} catch (IOException e1) {
						myTracker.Write(Name + " : Failure Strike #"
								+ (strike++) + "\n" + e1.getLocalizedMessage(),
								2);
					}
					try {
						if (strike != 0) {
							sleep(500);
						} else
							sleep(150);

					} catch (InterruptedException e) {

					}
					timeTaken = System.currentTimeMillis() - now;
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

			myTracker = EventTracker.init("logs/Connections/" + Name,
					this.getClass());
			ToTarget.write(new byte[] { (byte) ID });
			byte[] d = new byte[1];
			FromTarget.read(d);
			externalID = (char) d[0];
			System.out.printf(Name + " : Internal ID: %s\t External ID: %s\n",
					(int) ID, (int) externalID);
			Deamon.start();

		}

		private int handaling = -1;
		String temp = "";

		private void manageIncomingData(byte[] a) {
			myTracker.Write("DATA : " + ByteConventions.bytesToHexes(a), 0);

			if (a[0] == START_OF_STRING) {
				handaling = 0;
			}
			switch (handaling) {
			case -1:
				break;
			case 0:
				myTracker.Write("Its a string!", 0);
				for (int x = 0; x < a.length; x++) {
					if (a[x] == END) {
						NetFrame.MainWindow.addText(Name + ": " + temp);
						temp = "";
						myTracker.Write(temp, 0);
						handaling = -1;
						break;
					}
					if (a[x] != START_OF_STRING)
						temp += (char) a[x];
				}
				break;

			}
		}

		public void addDataForSend(int mode, byte[] data) {
			sendBuffer = new byte[data.length + 2];
			int x = 0;
			switch (mode) {
			case 0:
				sendBuffer[x++] = START_OF_STRING;
				break;
			}
			for (byte s : data) {
				sendBuffer[(x++)] = s;
			}
			sendBuffer[x] = END;
			DataReady = true;
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
							isOutgoing_ = false;
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

	public static void sendString(String a) {
		System.out.println("Sending: " + a);
		sendData(0, ByteConventions.getCharStringAsBytes(a));
	}

	private static void sendData(int startMatker, byte[] data) {
		String[] targets = NetFrame.MainWindow.getSelectedList();
		if (targets[0] == null) {
			NetFrame.MainWindow.addText("[ERROR] No Targets Selected");
			System.out.println("No Targets Selected");
			return;
		}
		NetFrame.MainWindow.addText("Me: "
				+ ByteConventions.byteSequenceToStrings(data));
		TT.Write("Sending : " + startMatker, 0);
		TT.Write("Data : " + ByteConventions.bytesToHexes(data), 0);
		TT.Write("To: " + Formating.Strings.combine("\t", 1, targets), 0);
		System.out.println("To: \n\n"
				+ Formating.Strings.combine("\t", 1, targets));
		for (Connection s : Connections) {
			for (String a : targets) {
				if (s.Name.equals(a))
					s.addDataForSend(startMatker, data);
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
				NetFrame.MainWindow.addText("[INFO] " + s.Name
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
			TT.Write("Creating connection to: " + a.getInetAddress(), 0);
			Connections.add(new Connection(a, isOutgoing_));
			TT.Write("Connection made! :-)", 0);
		} catch (IOException e) {
			TT.Write(e);
		}
	}
}
