package gui;

import io.ioManeger;
import io.ioManeger.Connection;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

public class ClientCheckBox extends JCheckBox {
	private final Connection Target;

	Button CloseConnection = new Button("");

	public ClientCheckBox(ioManeger.Connection con) {
		Target = con;
		this.setToolTipText("Select/Deselect: " + getTarget().getName());

		this.setText(getTarget().getName());
		this.setSize(180, 23);
		this.setLayout(null);
		this.setBorder(BorderFactory.createBevelBorder(1));
		prepareGui();
	}

	private void prepareGui() {
		CloseConnection.setSize(5, 5);
		CloseConnection.setVisible(true);
		CloseConnection.setLocation(this.getWidth()-5, this.getHeight()-5);
		CloseConnection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Target.close();
			}
		});
		this.add(CloseConnection);
	}

	public void undateLatency() {
		this.setText(getTarget().getName() + " : " + getTarget().getLatency()
				+ "ms");
	}

	public Connection getTarget() {
		return Target;
	}
}