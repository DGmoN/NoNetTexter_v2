package gui.front;

import gui.ClientCheckBox;
import io.Connection;
import io.Encryption;
import io.ioManeger;

import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import quickLinks.gui.ButtonS;
import quickLinks.gui.KeyEdit;
import shorts.guiLinks;
import Formating.Strings;
import Formating.Strings.LINES;

public class NetFrame extends JFrame {
	public static NetFrame MainWindow;
	public static boolean Cleanable = false;

	// GUI ElamentUpdater : makes the thingys that need to change change
	private Thread ObjectUpdater = new Thread() {
		@Override
		public void run() {
			int x = 0;
			while (true) {
				try {
					if (Cleanable) {
						ioManeger.cleanup();
						Cleanable = false;
					}
					if (ConnectedList.Boxes.length != ioManeger
							.getConnectedCount()) {
						ClientDataPanel.update();
					}

					for (ClientCheckBox s : ConnectedList.Boxes) {
						s.undateLatency();
					}

					sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private static Color[] colors = new Color[] { Color.black, Color.blue,
			Color.cyan, Color.darkGray, Color.gray, Color.green,
			Color.lightGray, Color.magenta, Color.orange, Color.pink,
			Color.red, Color.yellow };

	// GUI Elements : Classes
	private class ContactList extends JPanel {
		private ClientCheckBox[] Boxes = new ClientCheckBox[0];

		public ContactList() {
			this.setSize(180, 400);
			this.setLocation(5, 35);
			this.setVisible(true);
			this.setBorder(BorderFactory.createBevelBorder(1));
			this.setLayout(null);
		}

		public void updateText() {
			if (Boxes != null)
				for (ClientCheckBox a : Boxes) {
					this.remove(a);
				}
			Connection[] con = ioManeger.getConnections();
			Boxes = new ClientCheckBox[con.length];
			int x = 0;
			for (Connection a : con) {
				Boxes[x] = new ClientCheckBox(a);
				Boxes[x].setLocation(5, (15 * x) + (x * Boxes[x].getHeight()));
				Boxes[x].setVisible(true);
				this.add(Boxes[x]);
				x++;
			}
			this.repaint();
		}
	}

	private class ClientPannel extends JPanel {

		public ClientPannel() {
			this.setSize(200, 600);
			this.setLocation(600, 0);
			this.setVisible(true);
			this.setBorder(BorderFactory.createBevelBorder(1));
			this.setLayout(null);
			// ConnectedList
			this.add(ConnectedList);

			// UserInfo
			UserInfo.setSize(200, 25);
			UserInfo.setLocation(5, 5);
			UserInfo.setVisible(true);
			UserInfo.setText(ioManeger.getThisID() + ":"
					+ ioManeger.getListeningPort());
			this.add(UserInfo);
		}

		public void update() {
			ConnectedList.updateText();
		}
	}

	private class TextDisplay extends JTextPane {

		LINES lines = new LINES();

		private int firstVisableIndex = 0;

		public TextDisplay() {
			this.setSize(590, 530);
			this.setLocation(5, 5);
			this.setVisible(true);
			this.setEditable(false);
			this.setFont(new Font("Verdana", Font.BOLD, 10));
		}

		public void add(String a) {
			lines.add(a);
			update();
		}

		private void update() {
			String[] tempArr = lines.getAllLines();
			String temp = "";
			firstVisableIndex = tempArr.length - 33;
			for (int x = 0; x < tempArr.length; x++) {
				if (x >= firstVisableIndex)
					temp += tempArr[x] + "\n";
			}
			this.setText(temp);
		}
	}

	// End

	// GUI Elements : Objects
	private JPanel MainPanel;
	private ClientPannel ClientDataPanel;

	private TextDisplay Text = new TextDisplay();

	ContactList ConnectedList = new ContactList();

	private ButtonS ConnectButton = new ButtonS("Connect", "guiLinks:connect",
			null) {
		public void GatherData() {
			setData(TargetInput.getText());
		}
	};

	private KeyEdit TargetInput = new KeyEdit(
			new String[] { "guiLinks:connect" }, new byte[] { (byte) 0x0A }) {

		public Object[] GatherData(int s) {
			return new Object[] { TargetInput.getText() };
		}
	};

	private KeyEdit TextInput = new KeyEdit(
			new String[] { "guiLinks:sendString" }, new byte[] { (byte) 0x0A }) {

		public Object[] GatherData(int s) {
			String a = TextInput.getText();
			TextInput.setText("");
			return new Object[] { a };
		}
	};

	private Label UserInfo = new Label();

	public NetFrame() {

		ObjectUpdater.setDaemon(true);
		if (MainWindow == null)
			MainWindow = this;
		else {
			System.out
					.println("MainWindowAwlready created, destroy befor reinitializing the window");
			return;
		}
		this.setSize(800, 600);
		this.setAlwaysOnTop(true);
		this.setResizable(false);
		this.setVisible(true);
		this.setLayout(null);
		this.setTitle("NooooNetTexter");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// GUI init

		// MainPanel
		MainPanel = new JPanel();
		MainPanel.setSize(600, 600);
		MainPanel.setLocation(0, 0);
		MainPanel.setVisible(true);
		MainPanel.setBorder(BorderFactory.createBevelBorder(1));
		MainPanel.setLayout(null);
		this.add(MainPanel);

		// ClientDataPanel
		ClientDataPanel = new ClientPannel();
		this.add(ClientDataPanel);

		// ConnectButton
		ConnectButton.setSize(70, 25);
		ConnectButton.setLocation(5, 540);
		ConnectButton.setVisible(true);
		MainPanel.add(ConnectButton);

		// TargetInput
		TargetInput.setSize(100, 25);
		TargetInput.setLocation(80, 540);
		TargetInput.setVisible(true);
		MainPanel.add(TargetInput);

		// TextInput
		TextInput.setSize(400, 25);
		TextInput.setLocation(185, 540);
		TextInput.setVisible(true);
		MainPanel.add(TextInput);

		// Text
		MainPanel.add(Text);

		ObjectUpdater.start();
	}

	public static void main(String[] args) {
		int port = 5555;
		System.setProperty("Write", "false");
		for (String a : args) {
			if (a.equals("-l")) {
				System.setProperty("Write", "true");
			}
			try {
				port = Integer.parseInt(a);
			} catch (Exception e) {

			}
		}
		ioManeger.init(port);

		new guiLinks();
		new NetFrame();
	}

	public void updateListOfContacts() {
		ClientDataPanel.update();
	}

	public String[] getSelectedList() {
		String[] ret = new String[ConnectedList.Boxes.length];
		int x = 0;
		for (ClientCheckBox a : ConnectedList.Boxes) {
			if (a.isSelected())
				ret[x++] = a.getTarget().getName();
		}
		return ret;
	}

	public void addText(String a) {
		Text.add(Strings.getTime(':') + " | " + a);

	}

	private void appendToPane(JTextPane tp, String msg, Color c) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Foreground, c);

		aset = sc.addAttribute(aset, StyleConstants.FontFamily,
				"Lucida Console");
		aset = sc.addAttribute(aset, StyleConstants.Alignment,
				StyleConstants.ALIGN_JUSTIFIED);

		int len = tp.getDocument().getLength();
		tp.setCaretPosition(len);
		tp.setCharacterAttributes(aset, false);
		tp.replaceSelection(msg);
	}
}
