package edu.pqs19.connect_n.mll469;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;

public class CNPlayerView implements CNListener {

	private List<String> pub_logs;
	private List<String> priv_logs;
	private boolean voteYes;
	private secret;
	
	CNModel game;
	
	public CNPlayerView(CNModel game) {
		Objects.requireNonNull(game);
		this.game = game;
		int res = subscribeToGame(this.game);
		if (res == 1) { this.addToPrivLogs("Successfully subscribed to game."); } 
		
		this.secret = this.generateSecret();
	}
	
	private int subscribeToGame(CNModel game) {
		Objects.requireNonNull(game);
		this.game = game;
		this.pub_logs = this.game.getGameHistory();
		this.priv_logs = this.game.getGameHistory();
		return game.addListener(this);
	}
	
	public void addTrustedSender(String hash) {
		
	}
	
	public boolean isTrustedSender() {
		return true;
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
		if (game.isGameStarted()) { // Confirm that the game is the one that sent this message. 
			this.addToPrivLogs(log_string);
		}
	}
	
	public void notifiedGameEnded() {
		String log_string = "Game ended!";
		if (!game.isGameEnded()) { // Confirm that the game is the one that sent this message. 
			this.addToPubLogs(log_string);
		}
	}
	
	public void notifiedMoveMade(String move) {
		if (game.isGameStarted()) { // Confirm that the game is the one that sent this message. 
			this.addToPrivLogs(move);
		}
	}
}
