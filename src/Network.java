import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import Tracker.EventTracker;

public class Network extends Thread {
	private static Socket socket;
	private static ServerSocket SSocket;

	private static boolean connected = false;
	private static String Target = null;
	private static int Port;

	private static PrintWriter Writer;
	private static BufferedReader Reader;

	@Override
	public void run() {
		connect();
		boolean requesting;
		String Request_Resev;
		while (true) {
			try {
				if (socket.isConnected()) {
					EventTracker.Write("Connection dropped", 0);
				}
				Request_Resev = Reader.readLine();
				if (Request_Resev.equals(Target + ":GetData")) {

				}

				requesting = true;
				while (requesting) {
					Request_Resev = socket.getInetAddress().getHostAddress()
							+ ":GetData";
					Writer.println(Request_Resev);
					sleep(1000);
					Request_Resev = Reader.readLine();
					if (Request_Resev != socket.getInetAddress()
							.getHostAddress() + ":GetData") {
						requesting = false;
						System.out.println(Request_Resev);
					}
				}
				sleep(500);
			} catch (Exception e) {

			}
		}
	}

	private Network() {
		start();
	}

	private void connect() {
		EventTracker.Write("Opening ports...", 0);
		int Try = 1;
		try {
			SSocket = new ServerSocket(Port);
			SSocket.setSoTimeout(1000);
			System.out.println("Port opend @ " + start.getRunTime());
			if (Target == null)
				Target = "Server";
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (Try < 10) {
			try {
				if (Target != null && Target != "Server") {
					EventTracker.Write("Connecting as client...", 0);
					socket = new Socket(Target, Port);
					EventTracker.Write("Connection made to \""
							+ socket.getInetAddress().getHostName() + "\"", 0);
					break;
				} else {
					EventTracker.Write("Looking for inbound connections",
							(char) 0);
					socket = SSocket.accept();
					EventTracker.Write("Connection made to \""
							+ socket.getInetAddress().getHostAddress(), 0);
					break;
				}
			} catch (Exception e) {
				EventTracker.Write(e.getMessage(), 2);
				System.err.printf("Connection/Creation failure %s:%s. @ %s\n",
						Target, Port, start.getRunTime());
			}

			try {
				sleep(500);
			} catch (InterruptedException e) {

			}
			Try++;
		}
		System.out.println(socket);
		if (socket != null) {
			connected = true;
			try {
				Writer = new PrintWriter(socket.getOutputStream());
				Reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			windows.connectionMade();
			windows.startAutoSync();
			System.out.println("Connected at @ " + start.getRunTime());
		}
	}

	public static void Create(int port) {
		Port = port;
		new Network();
	}

	private static String requestData() throws IOException {
		EventTracker.Write("Requesting data", 0);
		Writer.println("GetData");
		Writer.flush();
		return Reader.readLine();
	}

	public static void Join(String target, int port) {
		Target = target;
		Port = port;
		new Network();
	}
}
