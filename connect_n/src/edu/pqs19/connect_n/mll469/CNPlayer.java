package edu.pqs19.connect_n.mll469;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.SpinnerNumberModel;

public class CNPlayer implements CNListener {

	private List<String> pub_logs;
	private List<String> priv_logs;
	
	private boolean voteYes = true;
	private String secret;
	private final Color color = new Color((int)(Math.random() * 0x1000000)); // Color assignment
	
	private JTextArea textArea = new JTextArea();
	private GridLayout arena; 
	protected JPanel midPanel;
	private JButton button = new JButton("SEND MOVE");
	private JSpinner num_spin;
	private SpinnerNumberModel col_field;
	
	
	final CNModel game;
	
	public CNPlayer(CNModel game)  {
		Objects.requireNonNull(game);

		this.game = game;
		
		try {
			this.secret = SecurityLayer.generateSecret(); // Security key for message passing.
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		this.priv_logs = new ArrayList<String>();
		int resListen = 0;
		try {
			resListen = subscribeToGame();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Implicitly adds game as trusted entity, starts listening.
		int resPlay = this.game.addPlayer(this); // Register as a player.
		if (resListen == 1) { this.addToScreen("Successfully subscribed to game."); } 
		if (resPlay == 1) { this.addToScreen("Successfully entered game as player."); }
		else { this.addToScreen("Could not enter as player."); }

		setupGUI();
	}
	
	private void sendMove(int col) {
		if (col == -1) {
			col = this.col_field.getNumber().intValue();
		}
		boolean res = false;
		try {
			res = this.game.sendMove(col, this, this.secret);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (res == false) {
			addToScreen("Not valid. Probably not your turn or space is filled.");
		}
	}
	
	protected void addToScreen(String line) {
		if (line != null) {
			textArea.setText("");
			textArea.append(line);
			textArea.append("\n");
			this.addToPrivLogs(line);
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}

	protected void drawGrid(JPanel midPanel, ArrayList<ArrayList<CNPiece>> grid, int cols, int rows) {
		
		midPanel.removeAll();
		for (int row = 0; row < rows; row++) { 
			for (int col = 0; col < cols; col++) { // Horizontal scan
					CNPiece piece = grid.get(col).get(row);
					JTextArea text = new JTextArea();
					Color pieceColor = Color.YELLOW;
					if (piece.getOwner() != null) {
						pieceColor = piece.getOwner().getColor();
					}
					midPanel.add(text);
					midPanel.setPreferredSize(new Dimension(500, 500));
					text.setBackground(pieceColor);
					text.append("|");
				}
		}
		
		JButton colbutt;
		for (int col = 0; col < cols; col++) { // Horizontal scan
			colbutt = null;
			colbutt = new JButton(Integer.toString(col));
			Integer target_col = col;
			colbutt.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					sendMove(target_col);
				}
			});
			midPanel.add(colbutt);
			
		}

	}
			
	private int subscribeToGame() throws NoSuchAlgorithmException {
		this.pub_logs = this.game.getGameHistory();
		List<String> gameHistory = this.game.getGameHistory();
		for (String line : gameHistory) {
			addToScreen(line);
		}
		return this.game.addListener(this, this.secret);
	}
	
	protected boolean isTrustedSender(String mix) {
		try {
			if (SecurityLayer.mix(this.game.getKey(), this.secret).contentEquals(mix)) {
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
	
	public Color getColor() {
		return this.color;
	}
	
	private void addToPrivLogs(String s) {
		priv_logs.add(s);
	}

//	private void addToPubLogs(String s) {
//		pub_logs.add(s);
//		priv_logs.add(s);
//	}


	// --------------------------------------------------------
	// Push Notifications
	// --------------------------------------------------------
	
	public void notifiedGameMayHaveStarted() {
		String log_string = "Game started!";
		if (game.isGameStarted()) { // Confirm that the game is in this state, no matter who sent the message. 
			this.addToScreen(log_string);
		}
	}
	
	public void notifiedGameMayHaveEnded(CNListener winner) {
		String log_string = "Game ended!";
		if (winner != null) { log_string = log_string.concat(winner.toString());}
		if (game.isGameEnded()) { // Confirm that the game is in this state, no matter who sent the message. 
			this.addToScreen(log_string);
		}
	}
	
	public void notifiedMoveMade(ArrayList<ArrayList<CNPiece>> grid, String move, String key) {
		if (this.isTrustedSender(key)) { // Confirm that the game is the one that sent this message, for integrity. 
			this.drawGrid(midPanel, grid, game.getColCount(), game.getRowCount());
		}
	}
	
	public void notifiedOther(String msg, String key) {
		if (this.isTrustedSender(key)) { // Confirm that the game is the one that sent this message, for integrity. 
			this.addToScreen(msg);
		}
	}
	
	private void setupGUI() {
		arena = new GridLayout(game.getRowCount() + 1, game.getColCount());
		col_field = new SpinnerNumberModel(0, 0, game.getColCount() - 1, 1);
		
		num_spin = new JSpinner(col_field);
		
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(num_spin, BorderLayout.CENTER);
		bottomPanel.add(button, BorderLayout.WEST);
		
		JPanel topPanel = new JPanel();
		textArea.setLineWrap(true);
		textArea.setBackground(this.color);
		topPanel.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(textArea);
		
		topPanel.setMaximumSize(new Dimension(204,300));
		pane.setMaximumSize(new Dimension(204,300));
		textArea.setMaximumSize(new Dimension(204,300));
		
		topPanel.add(pane, BorderLayout.NORTH);
		
		midPanel = new JPanel();
		midPanel.setLayout(arena);
	
		panel.add(midPanel, BorderLayout.CENTER);
		bottomPanel.add(topPanel, BorderLayout.NORTH);	
		panel.add(bottomPanel, BorderLayout.SOUTH);
		
		
		frame.getContentPane().add(panel);
		
		frame.setSize(200,200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.drawGrid(midPanel, game.getGrid(), game.getColCount(), game.getRowCount());
		frame.setVisible(true);

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendMove(-1);
			}
		});	
	}
	
	// Generated methods

//	@Override
//	public int hashCode() {
//		return Objects.hash(arena, button, col_field, color, game, midPanel, num_spin, priv_logs, pub_logs, secret,
//				textArea, voteYes);
//	}

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (obj == null) {
//			return false;
//		}
//		if (getClass() != obj.getClass()) {
//			return false;
//		}
//		CNPlayer other = (CNPlayer) obj;
//		return Objects.equals(arena, other.arena) && Objects.equals(button, other.button)
//				&& Objects.equals(col_field, other.col_field) && Objects.equals(color, other.color)
//				&& Objects.equals(game, other.game) && Objects.equals(midPanel, other.midPanel)
//				&& Objects.equals(num_spin, other.num_spin) && Objects.equals(priv_logs, other.priv_logs)
//				&& Objects.equals(pub_logs, other.pub_logs) && Objects.equals(secret, other.secret)
//				&& Objects.equals(textArea, other.textArea) && voteYes == other.voteYes;
//	}
//
//	@Override
//	public String toString() {
//		return "CNPlayer [" + (pub_logs != null ? "pub_logs=" + pub_logs + ", " : "")
//				+ (priv_logs != null ? "priv_logs=" + priv_logs + ", " : "") + "voteYes=" + voteYes + ", "
//				+ (secret != null ? "secret=" + secret + ", " : "") + (color != null ? "color=" + color + ", " : "")
//				+ (textArea != null ? "textArea=" + textArea + ", " : "")
//				+ (arena != null ? "arena=" + arena + ", " : "")
//				+ (midPanel != null ? "midPanel=" + midPanel + ", " : "")
//				+ (button != null ? "button=" + button + ", " : "")
//				+ (num_spin != null ? "num_spin=" + num_spin + ", " : "")
//				+ (col_field != null ? "col_field=" + col_field + ", " : "") + (game != null ? "game=" + game : "")
//				+ "]";
//	}
//	
}
