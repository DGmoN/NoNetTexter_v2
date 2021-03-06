package shorts;

import java.awt.Color;

import DataTypes.ByteConventions;
import gui.front.NetFrame;
import io.ioManeger;
import quickLinks.QuickLinks;

public class guiLinks extends QuickLinks {

	public guiLinks() {
		super("guiLinks");
	}

	public void connect(String a) {
		if (a.length() < 1) {
			System.err.println("No target spesified");
			return;
		}
		String port = null, target;
		if (a.contains(":")) {
			port = a.substring(a.indexOf(":") + 1, a.length());
			target = a.substring(0, a.indexOf(":"));
		} else
			target = a;

		if (port == null)
			ioManeger.ConnectTo(target, 5555);
		else
			ioManeger.ConnectTo(target, Integer.parseInt(port));
	}

	public void sendString(String a) {
		ioManeger.sendData(0, a.getBytes());

	}
}
