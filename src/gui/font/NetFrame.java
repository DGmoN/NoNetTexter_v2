package gui.font;

import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;

import io.ioManeger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import quickLinks.gui.ButtonS;
import shorts.guiLinks;

public class NetFrame extends JFrame {
	public static NetFrame MainWindow;

	// GUI ElamentUpdater
	private Thread ObjectUpdater = new Thread() {
		@Override
		public void run() {
			while (true) {
				try {
					sleep(750);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	// GUI Elements : Classes
	private class ContactList extends JTextArea {

	}

	// GUI Elements : Objects
	private JPanel MainPanel;
	private JPanel ClientDataPanel;
	private ButtonS ConnectButton = new ButtonS("Connect", "guiLinks:connect",
			null) {
		public void GatherData() {
			setData(TargetInput.getText());
		}
	};
	private TextField TargetInput = new TextField();

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
		ClientDataPanel = new JPanel();
		ClientDataPanel.setSize(200, 600);
		ClientDataPanel.setLocation(600, 0);
		ClientDataPanel.setVisible(true);
		ClientDataPanel.setBorder(BorderFactory.createBevelBorder(1));
		ClientDataPanel.setLayout(null);
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

		// UserInfo
		UserInfo.setSize(200, 25);
		UserInfo.setLocation(5, 5);
		UserInfo.setVisible(true);
		UserInfo.setText(ioManeger.getThisID() + ":"
				+ ioManeger.getListeningPort());
		ClientDataPanel.add(UserInfo);

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

}
