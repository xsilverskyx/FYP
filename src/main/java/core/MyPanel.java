package core;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import core.MyEvent;
import core.MyEventListener;

public class MyPanel extends JPanel implements MyEventListener {
	JTextArea	textArea;
	JScrollPane	scrollPane;

	/* (non-Javadoc)
	 * 
	 * @see core.MyEventListener#onRecvMyEvent(core.MyEvent) */
	public void onRecvMyEvent(MyEvent event) {
		this.updateStatus(event.text);
	}

	private void updateStatus(final String status) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				textArea.append(status + "\n");
				textArea.setCaretPosition(textArea.getDocument().getLength());
			}
		});
	}

	public MyPanel() {
		super();
		textArea = new JTextArea(30, 70);
		textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(scrollPane);
	}
}