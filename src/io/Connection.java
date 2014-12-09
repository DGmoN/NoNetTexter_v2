package io;

import gui.front.NetFrame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import crypto.Encryption;
import DataTypes.ByteConventions;
import Tracker.EventTracker;

public class Connection { // Connection is the port master, manages
	// outgoing and incoming bytes

	public static boolean ShowStreamText = false;

	private static final int STRING = 0;
	private static final int MAIN_KEY = 1;

	static final byte[] MARKERS_START = new byte[] { (byte) 0x01, (byte) 0x02,
			(byte) 0x03 };

	static final byte[] MARKERS_SECTION_END = new byte[] { (byte) 0x11,
			(byte) 0x12, (byte) 0x13 };

	static final byte[] MARKERS_CLOSE = new byte[] { (byte) 0x21, (byte) 0x22,
			(byte) 0x23 };

	private byte[] SessionKey;

	private class dataPacket {

		final int intervalSize;
		byteStack Data;

		public dataPacket(int intervalS) {
			intervalSize = intervalS;
			Data = new byteStack();
		}

		public void clear() {
			Data.clear();
		}

		public void create(byte[] data) {
			Data = new byteStack(data);
		}

		public byte[] getLine(int Type) {
			int x = 0;
			byte[] ret = new byte[intervalSize + 2];
			ret[0] = MARKERS_START[Type];
			while (x < intervalSize) {
				ret[x + 1] = Data.get();
				x++;
				if (Data.empty())
					break;
			}
			if (Data.empty()) {
				ret[x + 1] = MARKERS_CLOSE[Type];
			} else
				ret[x + 1] = MARKERS_SECTION_END[Type];
			return ret;
		}

		public boolean empty() {
			return Data.empty();
		}
	}

	private class byteStack {
		private byte[] Data = new byte[0];
		int size = 0;

		public byteStack(byte... e) {
			if (e.length > Data.length) {
				Data = e;
				size = e.length - 1;
				
			}
		}

		public void add(byte[] line) {
			for (byte s : line) {
				add(s);
			}
		}

		public void add(byte e) { // add object to top of the stack, stack limit
									// undefiend
			byte[] NewData = new byte[++size];
			for (int x = 0; x < size - 1; x++) {
				NewData[x] = Data[x];
			}
			NewData[size - 1] = e;
			Data = NewData;

		}

		// abcdefghijklmnopqrstuvwxyz
		public byte get() {
			byte ret;
			byte[] NewData = new byte[--size];
			for (int x = 0; x < size; x++) {
				NewData[x] = Data[x];
			}
			ret = Data[size];
			Data = NewData;

			return ret;
		}

		public byte[] clear() {
			int startSize = size;
			byte[] ret = new byte[size];
			for (int x = 0; x < startSize; x++) {
				ret[x] = get();
			}
			size = 0;
			Data = new byte[0];
			return ret;
		}

		public void filp() {
			Data = ByteConventions.flipArr(Data);
		}

		public boolean empty() {
			return size == 0;
		}
	}

	public long getLatency() {
		return timeTaken;
	}

	private Thread Deamon = new Thread() {
		@Override
		public void run() {
			int strike = 0;
			if (isOutgoingConection)
				NetFrame.MainWindow.addText("[INFO] Connected to: " + Name);
			else
				NetFrame.MainWindow.addText("[INFO] " + Name + " Connected");
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
					write();
					read();

					strike = 0;
				} catch (IOException e1) {
					myTracker.Write(Name + " : Failure Strike #" + (strike++)
							+ "\n" + e1.getLocalizedMessage(), 2);
					myTracker.Write(e1);
				}
				timeTaken = System.currentTimeMillis() - now;
				try {
					if (strike != 0) {
						sleep(500);
					} else
						sleep(150);

				} catch (InterruptedException e) {

				}

			}
		}

		public void read() throws IOException {
			byte[] buffer = new byte[255];
			FromTarget.read(buffer);
			if (ShowStreamText) {
				myTracker.Write("Read: \t" + buffer.length, 0);
			}
			manageIncomingData(buffer);
		}

		public void write() throws IOException {
			if (DataReady) {
				System.out.println("Writing buffer...");
				int x = 0;
				byte[] temp;
				while (!sendBuffer.empty()) {
					temp = sendBuffer.getLine(sendType);
					System.out.println("Writing line : "
							+ ByteConventions.bytesToHexes(temp));
					ToTarget.write(temp);
					x++;
				}
				DataReady = false;
				System.out.println("Wrote " + x + " lines!");
			} else
				ToTarget.write((byte) 0x00);
			ToTarget.flush();
		}
	};

	protected EventTracker myTracker;
	public boolean markedForRemoval = false;
	boolean DataReady = false;
	dataPacket sendBuffer = new dataPacket(130);
	byteStack MiscStack = new byteStack();
	int sendType;

	long now, timeTaken = -1;

	public final boolean isOutgoingConection;
	private final InputStream FromTarget;
	private final OutputStream ToTarget;

	private final Socket _socket;

	private final String Name;

	public Connection(Socket s, boolean a) throws IOException {
		Deamon.setDaemon(true);
		_socket = s;
		isOutgoingConection = a;
		FromTarget = s.getInputStream();
		ToTarget = s.getOutputStream();
		Name = get_socket().getInetAddress().getHostName();
		myTracker = EventTracker.init("logs/Connections/" + Name,
				this.getClass());
		updateSessionKey();
		Deamon.start();
	}

	public void updateSessionKey() {
		if (!isOutgoingConection) {
			SessionKey = Encryption.genPGPSessionKey();
			addDataForSend(1, SessionKey);
		}
	}

	private void manageIncomingData(byte[] a) {
		int handaling = 0;
		myTracker.Write("DATA : " + ByteConventions.bytesToHexes(a), 0);
		for (byte s : MARKERS_START) {
			if (s == a[0])
				break;
			handaling++;
		}
		if (handaling < MARKERS_START.length - 1) {
			System.out.println("Handaling " + handaling + " : "
					+ ByteConventions.byteToHex(a[0]));
			for (int x = 1; x < a.length; x++) {
				if (a[x] == MARKERS_CLOSE[handaling] && a[x] == getLastByte(a)) {
					System.out.println("End of packet found");
					break;
				} else if (a[x] == MARKERS_SECTION_END[handaling]
						&& a[x] == getLastByte(a)) {
					System.out.println("End of packet section");
					break;
				} else {
					MiscStack.add(a[x]);
				}
			}
		}

		switch (handaling) {
		case 0:
			printStringStack();
			break;
		case 1:
			MiscStack.filp();
			SessionKey = MiscStack.clear();
			System.out.println("Session key : "
					+ ByteConventions.bytesToHexes(SessionKey));
			break;
		}
	}

	private byte getLastByte(byte[] a) {
		for (int x = 0; x < a.length; x++) {
			if (a[x] == (byte) 0x00) {
				return a[x - 1];
			}
		}
		return a[a.length - 1];
	}

	String temp = "";

	public void printStringStack() {
		temp = new String(MiscStack.clear());
		NetFrame.MainWindow.addText(Name + ": " + temp);
		temp = "";
		myTracker.Write(temp, 0);
	}

	public void addDataForSend(int mode, byte[] data) {
		sendBuffer.Data.add(data);
		DataReady = true;
		sendType = mode;
	}

	public String getName() {
		return Name;
	}

	public void kill() {
		try {
			myTracker.Write("Closing socket: " + getName(), 1);
			get_socket().close();
		} catch (IOException e) {
			myTracker.Write(e);
		}
	}

	public Socket get_socket() {
		return _socket;
	}
}
