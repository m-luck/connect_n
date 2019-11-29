package edu.pqs19.connect_n.mll469;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class CNPlayer implements CNListener {

	private List<String> pub_logs;
	private List<String> priv_logs;
	private boolean voteYes;
	private String secret;
	
	private JTextArea textArea = new JTextArea();
	private GridLayout arena; 
	private JButton button = new JButton("SEND MOVE");
	private JSpinner num_spin;
	private SpinnerNumberModel col_field;
	
	
	CNModel game;
	
	public CNPlayer(CNModel game) {
		Objects.requireNonNull(game);

		this.game = game;
		
		try {
			this.secret = SecurityLayer.generateSecret(); // Security key for message passing.
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		
		this.priv_logs = new ArrayList<String>();
		int resListen = subscribeToGame(); // Implicitly adds game as trusted entity, starts listening.
		int resPlay = this.game.addPlayer(this); // Register as a player.
		if (resListen == 1) { this.addToScreen("Successfully subscribed to game."); } 
		if (resPlay == 1) { this.addToScreen("Successfully entered game as player."); }
		else { this.addToScreen("Could not enter as player."); }
		

		arena = new GridLayout(game.getRowCount(), game.getColCount());
		col_field = new SpinnerNumberModel(0, 0, game.getColCount(), 1);
		num_spin = new JSpinner(col_field);
		
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(num_spin, BorderLayout.CENTER);
		bottomPanel.add(button, BorderLayout.WEST);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
		
		JPanel midPanel = new JPanel();
		midPanel.setLayout(arena);
		midPanel.add(new JTextArea());
		
		
		panel.add(bottomPanel, BorderLayout.SOUTH);
		panel.add(midPanel, BorderLayout.CENTER);
		panel.add(topPanel, BorderLayout.NORTH);
		
		frame.getContentPane().add(panel);
		
		frame.setSize(200,200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendMove();
			}
		});

	}
	
	private void sendMove() {
		int col = this.col_field.getNumber().intValue();
		this.addToScreen("Sent move");
		this.game.sendMove(col, this, this.secret);
	}
	
	private void addToScreen(String line) {
		if (line != null) {
			textArea.append(line);
			textArea.append("\n");
			this.addToPrivLogs(line);
		}
	}

//	private void drawGrid(ArrayList<ArrayList<CNPiece>> grid, int cols, int rows) {
//		
//		arena = new GridLayout(rows,cols);
//		for (int row = 0; row < rows; row++) { 
//			for (int col = 0; col < cols; col++) { // Horizontal scan
//				CNPiece piece = grid.get(col).get(row);
//			
//	}
//	
	private int subscribeToGame() {
		this.pub_logs = this.game.getGameHistory();
		List<String> gameHistory = this.game.getGameHistory();
		for (String line : gameHistory) {
			addToScreen(line);
		}
		return this.game.addListener(this, this.secret);
	}
	
	private boolean isTrustedSender(String mix) {
		try {
			if (mix == SecurityLayer.mix(this.game.getKey(), this.secret)) {
				return true;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean hasVoted() {
		return this.voteYes;
	}
	
	public List<String> getPublicLogs() {
		return pub_logs;
	}
	
	private void addToPrivLogs(String s) {
		priv_logs.add(s);
	}

	private void addToPubLogs(String s) {
		pub_logs.add(s);
		priv_logs.add(s);
	}


	// --------------------------------------------------------
	// Push Notifications
	// --------------------------------------------------------
	
	public void notifiedGameMayHaveStarted() {
		String log_string = "Game started!";
		if (game.isGameStarted()) { // Confirm that the game is in this state, no matter who sent the message. 
			this.addToPrivLogs(log_string);
		}
	}
	
	public void notifiedGameMayHaveEnded() {
		String log_string = "Game ended!";
		if (game.isGameEnded()) { // Confirm that the game is in this state, no matter who sent the message. 
			this.addToPubLogs(log_string);
		}
	}
	
	public void notifiedMoveMade(ArrayList<ArrayList<CNPiece>> grid, String move, String key) {
		if (this.isTrustedSender(key)) { // Confirm that the game is the one that sent this message, for integrity. 
			this.addToPrivLogs(move);
		}
	}
	
	
}
