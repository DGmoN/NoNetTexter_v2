package gui.front;

import io.ioManeger;

import java.awt.Label;
import java.awt.TextField;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import quickLinks.gui.ButtonS;
import quickLinks.gui.KeyEdit;
import shorts.guiLinks;

public class NetFrame extends JFrame {
	public static NetFrame MainWindow;
	public static boolean Cleanable = false;

	// GUI ElamentUpdater
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
					sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

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
			String[] names = ioManeger.getConnectedNames();
			Boxes = new ClientCheckBox[names.length];
			int x = 0;
			for (String a : names) {
				Boxes[x] = new ClientCheckBox(a);
				Boxes[x].setLocation(5, (10 * x) + (x * Boxes[x].getHeight()));
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

	private class ClientCheckBox extends JCheckBox {
		final String Target;

		public ClientCheckBox(String name) {
			Target = name;
			this.setToolTipText("Select/Deselect: " + Target);
			this.setText(Target);
			this.setSize(180, 25);
		}
	}

	// GUI Elements : Objects
	private JPanel MainPanel;
	private ClientPannel ClientDataPanel;

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

	private KeyEdit TextInput = new KeyEdit(new String[] { "guiLinks:send" },
			new byte[] { (byte) 0x0A }) {

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

		ObjectUpdater.start();
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			ioManeger.init(Integer.parseInt(args[0]));
		} else {
			ioManeger.init(5555);
		}

		new guiLinks();
		new NetFrame();
	}

	public void updateListOfContacts() {
		ClientDataPanel.update();
	}

}
