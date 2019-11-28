package edu.pqs19.connect_n.mll469;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CNModel {

	private List<Object> gameGrid; // N dimensional matrix.
	private int gridDimensionCount; // e.g. 2 for 2D
	private List<Integer> dimensionSizes; // e.g. [7, 6] for a 2D 7 row, 6 column grid
	
	private List<CNListener> subscribers; // watching the game
	private List<CNPlayerView> players; // affecting the game
	
	private boolean gameStarted; 
	private boolean gameEnded;
	private boolean allowLateJoining;
	
	private int subscriberLimit;
	private int playerLimit;
	
	
	private List<CNPlayerView> turnQueue;
 	private List<String> internal_logger;
 	private CNListener public_logger;
	
	public CNModel(int dimensions, List<Integer> dimSizes) {
		
		Objects.requireNonNull(dimensions);
		Objects.requireNonNull(dimSizes);
		
		if (dimensions > 2 || dimensions < 1) { // Only support 1 or 2 dimensions for now.
			throw new IllegalArgumentException();
		}
		
		this.subscribers = new ArrayList<CNListener>();
		this.players = new ArrayList<CNPlayerView>();
		this.turnQueue = new LinkedList<CNPlayerView>();
		
		this.gridDimensionCount = dimensions;
		this.dimensionSizes = dimSizes;
		this.gameGrid = this.getFreshGrid();
		
		this.subscriberLimit = 2048;
		this.playerLimit = 1024;
		this.gameStarted = false;
		this.allowLateJoining = false;
	}
	
	public CNModel(int dimensions, int dimSizes, int playerLimit) {
		
		Objects.requireNonNull(dimensions);
		Objects.requireNonNull(dimSizes);
		Objects.requireNonNull(playerLimit);
		
		if (dimensions > 2 || dimensions < 1) { // Only support 1 or 2 dimensions for now.
			throw new IllegalArgumentException();
		}
		
		this.subscribers = new ArrayList<CNListener>();
		this.players = new ArrayList<CNPlayerView>();
		this.turnQueue = new LinkedList<CNPlayerView>();
		
		this.gridDimensionCount = dimensions;
		this.subscriberLimit = playerLimit;
		this.playerLimit = playerLimit;
		
		this.gameGrid = this.getFreshGrid();
	}
	
	public int addListener(CNListener listener, String listenerSecret) {
		Objects.requireNonNull(listener);
		if (subscribers.size() < subscriberLimit) {
			subscribers.add(listener);
			listener.addTrustedSender(this.secret, listenerSecret);
			return 1;
		}	
		return 0; // Failure to add.
	}
	
	public int addPlayer(CNPlayerView player) {
		if ((this.allowLateJoining == true || this.gameStarted != true) && (players.size() < playerLimit)) {
			players.add(player);
			return 1;
		}
		return 0; // Failure to add. 
	}

	private List<Object> generateSpace(int dimensional){
		Objects.requireNonNull(dimensional);
		List<Object> res = new ArrayList<Object>();
		
		if (dimensional == 1) { // Make a simple row [_, _, _, ..., _, _]
			for (int cell = 0; cell < this.dimensionSizes.get(dimensional); cell++) {
				res.add(new CNPiece(-1)); // Owned by player -1 (gaia/system)
			}
		}
		
		else {
			
			// Recursive multiply spaces together. O(dimensionCount) stack space. 
			for (int cell = 0; cell < this.dimensionSizes.get(dimensional); cell++) {
				res.add(generateSpace(dimensional - 1)); // Make an n-d space (at least 2D
			}
		}
		
		return res;
	}
	
	private List<Object> getFreshGrid() {
		return generateSpace(this.gridDimensionCount);
	}
	
	public boolean startIfAllVoted() {
		
		// Allows for easy one player games if for some reason a user wanted to! 
		
		if (this.players.size() == 0 || this.gameStarted == true) {
			return false; // No players joined yet, should not start game. Or, game already started, so function should be idempotent.
		}
		
		for (CNPlayerView player : this.players) {
			if (!player.hasVoted()) {
				return false; // If there is any player who has not voted to start, short circuit and return false.
			}
		}

		this.gameStarted = true; // Otherwise set the game to start.
		
		for (CNListener subscriber : this.subscribers) {
			subscriber.notifiedGameMayHaveStarted(); // Let all subscribers know.
		}
		
		return true;
	}
	
	public boolean isGameStarted() {
		return this.gameStarted;
	}
	
	public boolean isGameEnded() {
		return this.gameEnded;
	}
	
	
	public List<String> getGameHistory() {
		return this.public_logger.getPublicLogs();
	}

	public List<Object> getGrid() {
		return this.gameGrid;
	}
}
