package gui.font;

import java.awt.Panel;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class NetFrame extends JFrame {
	public static NetFrame MainWindow;

	// GUI Elements : Classes
	private class ContactList extends JTextArea {

	}

	// GUI Elements : Objects
	private JPanel MainPanel;
	private JPanel ClientDataPanel;

	public NetFrame() {
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
		this.add(MainPanel);
	}

	public static void main(String[] args) {
		new NetFrame();
	}

}
