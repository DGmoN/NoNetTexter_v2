import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.annotation.Target;

import javax.swing.JButton;
import javax.swing.JFrame;

import Tracker.EventTracker;

public class windows {

	public static JFrame Window = new JFrame();
	private static JButton Server = new JButton("Create"),
			Client = new JButton("Join"), Connect = new JButton("Connect"),
			Create = new JButton("Start!");
	public static TextArea Input = new TextArea();
	private static TextArea Output = new TextArea();

	private static TextField Target = new TextField();

	private static long delta = 0;

	private static ActionListener AL = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Join"))
				config(1);
			if (e.getActionCommand().equals("Create"))
				config(2);
			if (e.getActionCommand().equals("Connect")) {
				Network.Join(getTarget(Target.getText()),
						getPort(Target.getText()));
				Target.setEnabled(false);
				Connect.setEnabled(false);
			}
			if (e.getActionCommand().equals("Start!")) {
				Network.Create(getPort(Target.getText()));
				Target.setEnabled(false);
				Connect.setEnabled(false);
			}
		}
	};

	public static boolean SyncPending = false;

	private static Thread Ticker = new Thread() {
		@Override
		public void run() {
			while (true) {
				if (SyncPending) {
					if (System.currentTimeMillis() - delta > 500) {

					}
				} else
					try {
						
						sleep(100);
					} catch (Exception e) {
						EventTracker.Write(e.getMessage(), 3);
					}
			}
		}
	};

	private static KeyListener KL = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent e) {
			delta = System.currentTimeMillis();
			SyncPending = true;
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent e) {

		}
	};

	public windows() {
		EventTracker.Write("Loading UI", (char) 0);
		Window.setLayout(null);
		Window.setResizable(false);
		Window.setDefaultCloseOperation(Window.EXIT_ON_CLOSE);

		Server.setSize(100, 100);
		Server.setVisible(false);
		Server.setLocation(0, 0);

		Client.setSize(100, 100);
		Client.setVisible(false);
		Client.setLocation(100, 0);

		Client.addActionListener(AL);
		Server.addActionListener(AL);

		Input.setSize(395, 100);
		Input.setLocation(0, 0);
		Input.setVisible(false);
		Input.addKeyListener(KL);

		Output.setSize(395, 100);
		Output.setLocation(0, 100);
		Output.setVisible(false);
		Output.setEditable(false);

		Target.setSize(300, 27);
		Target.setLocation(0, 200);
		Target.setVisible(false);

		Connect.setSize(95, 27);
		Connect.setLocation(301, 200);
		Connect.setVisible(false);
		Connect.addActionListener(AL);

		Window.add(Connect);
		Window.add(Target);
		Window.add(Input);
		Window.add(Output);
		Window.add(Client);
		Window.add(Server);

		config(0);
		EventTracker.Write("Loaded UI", (char) 0);
	}

	public static void config(int mode) {
		Window.setVisible(false);
		Client.setVisible(false);
		Server.setVisible(false);
		Input.setVisible(false);
		Output.setVisible(false);

		switch (mode) {
		case 0:
			Window.setSize(206, 123);
			Client.setVisible(true);
			Server.setVisible(true);
			Window.setTitle("Select an option.");
			Window.setVisible(true);
			break;

		case 1:
			Window.setSize(400, 255);
			Target.setVisible(true);
			Connect.setVisible(true);
			Input.setVisible(true);
			Output.setVisible(true);
			Window.setTitle("Disconected");
			Window.setVisible(true);
			break;

		case 2:
			Window.setSize(400, 255);
			Target.setVisible(true);
			Connect.setVisible(true);
			Connect.setText("Start!");
			Connect.setActionCommand("Start!");
			Input.setVisible(true);
			Output.setVisible(true);
			Window.setTitle("Disconected");
			Window.setVisible(true);
			break;
		}
	}

	private static int getPort(String IP_PORT) {
		EventTracker.Write("Extrapolating Port for \"" + IP_PORT + "\"",
				(char) 0);
		boolean Counts = false;
		String temp = "";
		for (char a : IP_PORT.toCharArray()) {
			if (a == ':') {
				Counts = true;
			} else if (Counts)
				temp += a;
		}
		try {
			EventTracker
					.Write("Attempting to parse \"" + temp + "\"", (char) 0);
			return Integer.parseInt(temp);
		} catch (Exception e) {
			EventTracker.Write("Parsing Failed", (char) 3);
			return 0;
		}
	}

	public static void startAutoSync() {
		EventTracker.Write("Starting AutoSync", (char) 0);
		Ticker.start();
	}

	public static void connectionMade() {
		Window.setTitle("Conected");
	}

	private static String getTarget(String IP_PORT) {
		EventTracker.Write("Extrapolating target for \"" + IP_PORT + "\"",
				(char) 0);
		String temp = "";
		for (char a : IP_PORT.toCharArray()) {
			if (a == ':') {
				break;
			}
			temp += a;
		}
		EventTracker.Write("Target read as \"" + temp + "\"", 0);
		return temp;
	}
}
