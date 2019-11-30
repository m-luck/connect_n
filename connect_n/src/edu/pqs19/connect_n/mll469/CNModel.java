package edu.pqs19.connect_n.mll469;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CNModel {
	
	// Grid

	private ArrayList<ArrayList<CNPiece>> gameGrid; // 2 matrix. Turned CCW 90 degrees for easier indexing (gravity). 
	private int rows; // Second index.
	private int cols; // First index.
	
	
	// Registered Actors
	
	private List<CNListener> subscribers; // watching the game (may be a superset of players)
	private List<CNPlayer> players; // affecting the game
	private List<String> keys; // for usage in notification functions (in which actors vet if sender is trusted)
	private CNListener gaia; // no player
	
	// Flags
	
	private boolean gameStarted; 
	private boolean gameEnded;
	private boolean allowLateJoining;
	private boolean allowDiagonalWins;
	
	
	// Limits
	
	private int subscriberLimit;
	private int playerLimit;
	private int nToWin; // usually 4, undefined if 1 or lower 
	
	
	// Evolving state variables
	
	private LinkedList<CNPlayer> turnQueue; // To check turns
 	private List<String> internal_logger; 
 	private List<String> public_logger; 
 	private CNListener winner;
 	
 	
 	// Security 
 	
 	private String key;
	
 	// Constructor with sizes <rows, cols> and nToWin
	public CNModel(List<Integer> dimSizes, int nToWin) {
	
		Objects.requireNonNull(dimSizes);
		Objects.requireNonNull(nToWin);
		
		this.subscribers = new ArrayList<CNListener>();
		this.players = new ArrayList<CNPlayer>();
		this.keys = new ArrayList<String>();
		this.turnQueue = new LinkedList<CNPlayer>();
		
		this.gaia = null;
		
		this.rows = dimSizes.get(1);
		this.cols = dimSizes.get(0);
		this.gameGrid = this.getFreshGrid();
		
		this.subscriberLimit = 2048; // Set manageable numbers for default limits.
		this.playerLimit = 1024;
		
		this.gameStarted = false;
		this.allowLateJoining = false;
		this.nToWin = nToWin;
		
		this.public_logger = new ArrayList<String>(); 
	 	this.public_logger.add("Ready."); 
		
		keygen();
	}
	
	// Constructor with custom subscriberLimit
	public CNModel(List<Integer> dimSizes, int subscriberLimit, int nToWin) {
		
		Objects.requireNonNull(dimSizes);
		Objects.requireNonNull(playerLimit);
		Objects.requireNonNull(nToWin);
		
		this.subscribers = new ArrayList<CNListener>();
		this.players = new ArrayList<CNPlayer>();
		this.turnQueue = new LinkedList<CNPlayer>();

		this.rows = dimSizes.get(1);
		this.cols = dimSizes.get(0);
		this.gameGrid = this.getFreshGrid();
		
		this.subscriberLimit = subscriberLimit;
		this.playerLimit = subscriberLimit;
		
		this.gameStarted = false;
		this.allowLateJoining = false;
		this.nToWin = nToWin;
		
		keygen();
		
	}
	
	private void keygen() {
		try { // Generate constant key for message passing validation.
			this.key = SecurityLayer.generateSecret();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	
	// Setup 
	
	public int addListener(CNListener listener, String listenerSecret) {
		Objects.requireNonNull(listener);
		Objects.requireNonNull(listenerSecret);
		if (!subscribers.contains(listener) && subscribers.size() < subscriberLimit) {
			try { 
				String keyToAdd = SecurityLayer.mix(this.key, listenerSecret);
				keys.add(keyToAdd); // Blend to make key that subscriber will trust.
				subscribers.add(listener);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return 1;
		}	
		return 0; // Failure to add.
	}
	
	public int addPlayer(CNPlayer player) {
		
		Objects.requireNonNull(player);
		if (!players.contains(player) && (this.allowLateJoining == true || this.gameStarted != true) && (players.size() < playerLimit)) {
			players.add(player);
			turnQueue.add(player); // First come first serve. If joining midway, go to back.
			return 1;
		}
		return 0; // Failure to add. 
	}

	
	private ArrayList<ArrayList<CNPiece>> getFreshGrid() {
		
		ArrayList<ArrayList<CNPiece>> res = new ArrayList<ArrayList<CNPiece>>(); 
		for (int r = 0; r < cols; r++) {
			ArrayList<CNPiece> col = new ArrayList<CNPiece>();
			for (int c = 0; c < rows; c++) {
				col.add(new CNPiece(gaia)); // Fill with natural pieces which do not belong to external players. 
			}
			res.add(col);
		}
		return res;
	}
	
	private boolean startIfAllVoted() {
		
		// Allows for easy one player games if for some reason a user wanted to! 
		
		if (this.players.size() == 0 || this.gameStarted == true) {
			return false; // No players joined yet, should not start game. Or perhaps game is already started, so function should be idempotent.
		}
		
		for (CNPlayer player : this.players) {
			if (!player.hasVoted()) {
				return false; // If there is any player who has not voted to start, short circuit and return false.
			}
		}

		this.gameStarted = true; // Otherwise set the game to start.
		
		for (int i = 0; i < this.subscribers.size(); i++) {
			this.subscribers.get(i).notifiedGameMayHaveStarted(); // Let all subscribers know.
		}
		
		CNPlayer next = turnQueue.peek();
		for (int i = 0; i < this.subscribers.size(); i++) {
			if (next == this.subscribers.get(i)) {
				next.notifiedOther("It's your move.", keys.get(i)); // Let first player turn know.
			}
		}
		
		return true;
	}
	
	public boolean checkIsTurn(CNListener submitter) {
		CNListener next = this.turnQueue.peek();
		if (submitter == next) {
			return true;
		}
		return false; // It's not the player's move
	}
	
	private void cycleTurns() {
		CNPlayer popped = turnQueue.poll();
		turnQueue.add(popped); // Move head to back of line.
		CNPlayer next = turnQueue.peek();
		for (int i = 0; i < this.subscribers.size(); i++) {
			if (next == this.subscribers.get(i)) {
				next.notifiedOther("It's your move.", keys.get(i)); // Let next turn know.
			}
			else {
				subscribers.get(i).notifiedOther("Waiting for next turn...", keys.get(i));
			}
		}
	}
	
	private void updateState(ArrayList<CNPiece>pillar, int depth, CNListener submitter, String moveDescription) {
		pillar.set(depth - 1, new CNPiece(submitter)); // Add a piece.
		this.cycleTurns();
		for (int i = 0; i < this.subscribers.size(); i++) {
			this.subscribers.get(i).notifiedMoveMade(this.gameGrid, moveDescription, keys.get(i)); // Let all subscribers know.
			this.checkIfEndgame();
		}
	}
	
	public boolean sendMove(int col, CNListener submitter, String key) throws NoSuchAlgorithmException {
		Objects.requireNonNull(col);
		Objects.requireNonNull(submitter);
		Objects.requireNonNull(key);
		if (!this.checkIsTurn(submitter) || col > cols - 1) {
			return false; // Move request is invalid because not the submitter's turn, or target col is out of bounds.
		}
		
		if (!keys.contains(SecurityLayer.mix(this.key, key))) {
			return false; // Someone is trying to send a move on behalf of someone else.
		}

		String moveDescription = "Move made.";
		ArrayList<CNPiece> pillar = gameGrid.get(col); // Check the column
		for (int depth = 0; depth <= pillar.size(); depth++) {
			
			if (depth == pillar.size()) { // The bottom has already been reached.
				this.updateState(pillar, depth, submitter, moveDescription); // So add a piece on top
				this.checkIfEndgame(); // Will set game end if true
				return true;
			}
			
			if (pillar.get(depth).getOwner() != this.gaia) { // Drill down until seeing a player unit
				if (depth != 0) { // If there is room for another piece on top
					this.updateState(pillar, depth, submitter, moveDescription); // Then add a piece on top
					this.checkIfEndgame(); // Will set game end if true
					return true;
				}
				break;
			}
		}
		return false; // If none of the conditions were met, this move is invalid.
	}
	
	private void endGame(CNListener winner) {
		this.winner = winner;
		this.gameEnded = true;
		this.gameStarted = false;
		for (int i = 0; i < this.subscribers.size(); i++) {
			this.subscribers.get(i).notifiedGameMayHaveEnded(winner); // Let all subscribers know.
		}
	}
	
	public boolean checkIfEndgame() {
		if (checkIfFull()) {
			endGame(null);
			return true; 
		}
		
		CNListener winner = anyWinner(this.gameGrid, this.nToWin);
		if (winner != null) { // parent function runs after every move so a grid can only have one winning player.
			endGame(winner);
			return true;
		}
		return false;
		
	}
	
	private boolean checkIfFull() {
		for (ArrayList<CNPiece> pillar : gameGrid) {
			if (pillar.get(0).getOwner() == gaia) {
				return false; // There is still space in some of the columns.
			}
		}
		return true;
	}
	
	//------------------------
	// Checking for winners
	//------------------------
	
	public CNListener anyWinner(ArrayList<ArrayList<CNPiece>> grid, int nToWin) {
		Objects.requireNonNull(grid);
		ArrayList<CNListener> potentialWinners = new ArrayList<CNListener>();
		potentialWinners.add(anyWinnerHoriz(grid, nToWin));
		potentialWinners.add(anyWinnerVert(grid, nToWin));
		potentialWinners.add(anyWinnerTopLeftToBottomRight(grid, nToWin));
		potentialWinners.add(anyWinnerTopRightToBottomLeft(grid, nToWin));
		potentialWinners.add(anyWinnerBottomLeftToTopRight(grid, nToWin));
		potentialWinners.add(anyWinnerBottomRightToTopLeft(grid, nToWin));
		for (CNListener potential : potentialWinners) {
			if (potential != null) {
				return potential;
			}
		}
		return null;
	}
	
	private CNListener anyWinnerHoriz(ArrayList<ArrayList<CNPiece>> grid, int nToWin) {
		
		for (int row = 0; row < this.rows; row++) { 
			int max_consec = 1;
			CNListener prev = null;
			for (int col = 0; col < this.cols; col++) { // Horizontal scan
				CNPiece piece = grid.get(col).get(row);
				CNListener color = piece.getOwner();
				if (prev == color && prev != gaia) { // Consecutive and not default piece
					prev = color;
					max_consec++;
					if (max_consec == nToWin) {
						return color; // This satisfies the winning property and this player wins.
					}
				}
				else {
					prev = color;
					max_consec = 1;
				}
				
			}
		}
		return null;
	}
	
	private CNListener anyWinnerVert(ArrayList<ArrayList<CNPiece>> grid, int nToWin) {

		for (ArrayList<CNPiece> pillar : grid) {
			int max_consec = 1;
			CNListener prev = null;
			for (CNPiece piece: pillar) { // Vertical scan
				CNListener color = piece.getOwner();
				if (prev == color && prev != gaia) {
					prev = color;
					max_consec++;
					if (max_consec == nToWin) {
						return color; // This satisfies the winning property and this player wins.
					}
				}
				else {
					prev = color;
					max_consec = 1;
				}
			}
		}
		return null;
	}
	
	private CNListener anyWinnerTopLeftToBottomRight(ArrayList<ArrayList<CNPiece>> grid, int nToWin) {

		for (int col = 0; col < this.rows; col++) {;
			int max_consec = 1;
			CNListener prev = gaia;
			int cur_row = 0;
			int cur_col = col;
			LinkedList<CNPiece> queue = new LinkedList<CNPiece>(); // Search the diagonal from top right to bottom left.
			queue.add(grid.get(cur_col).get(cur_row));
			while (queue.size() > 0) {
				CNPiece piece = queue.poll();
				CNListener color = piece.getOwner();
				if (prev == color && prev != gaia) {
					prev = color;
					max_consec++;
					if (max_consec == nToWin) {
						return color; // This satisfies the winning property and this player wins.
					}
				}
				else {
					prev = color;
					max_consec = 1;
				}
				if (cur_row + 1 < this.rows && cur_col + 1 < this.cols) {
					cur_row++; // One down
					cur_col++; // One right
					// Added the next diagonal over to the traversal (bottom left)
					queue.add(grid.get(cur_col).get(cur_row));
				}
			}			
		}
		return null;
	}
	

	private CNListener anyWinnerBottomLeftToTopRight(ArrayList<ArrayList<CNPiece>> grid, int nToWin) {

		for (int col = 0; col < this.rows; col++) {;
			int max_consec = 1;
			CNListener prev = gaia;
			int cur_row = this.getRowCount() - 1;
			int cur_col = col;
			LinkedList<CNPiece> queue = new LinkedList<CNPiece>(); // Search the diagonal from top right to bottom left.
			queue.add(grid.get(cur_col).get(cur_row));
			while (queue.size() > 0) {
				CNPiece piece = queue.poll();
				CNListener color = piece.getOwner();
				if (prev == color && prev != gaia) {
					prev = color;
					max_consec++;
					if (max_consec == nToWin) {
						return color; // This satisfies the winning property and this player wins.
					}
				}
				else {
					prev = color;
					max_consec = 1;
				}
				if (cur_row - 1 > 0 && cur_col + 1 < this.cols) {
					cur_row--; // One down
					cur_col++; // One right
					// Added the next diagonal over to the traversal (bottom left)
					queue.add(grid.get(cur_col).get(cur_row));
				}
			}			
		}
		return null;
	}

	
	private CNListener anyWinnerTopRightToBottomLeft(ArrayList<ArrayList<CNPiece>> grid, int nToWin) {
	
		for (int col = this.cols - 1; col >= 0; col--) {
			CNListener prev = gaia;
			int max_consec = 1;
			int cur_row = 0;
			int cur_col = col;
			LinkedList<CNPiece> queue = new LinkedList<CNPiece>(); // Search the diagonal from top right to bottom left.
			queue.add(grid.get(cur_col).get(cur_row));
			while (queue.size() > 0) {
				CNPiece piece = queue.poll();
				CNListener color = piece.getOwner();
				if (prev == color && prev != gaia) {
					prev = color;
					max_consec++;
					if (max_consec == nToWin) {
						return color; // This satisfies the winning property and this player wins.
					}
				}
				else {
					prev = color;
					max_consec = 1;
				}
				if (cur_row + 1 < this.rows && cur_col > 0) {
					cur_row++; // One down
					cur_col--; // One left
					// Added the next diagonal over to the traversal (bottom left)
					queue.add(grid.get(cur_col).get(cur_row));
				}
			}			
		}
		return null;
	}
	
	
	private CNListener anyWinnerBottomRightToTopLeft(ArrayList<ArrayList<CNPiece>> grid, int nToWin) {
	
		for (int col = this.cols - 1; col >= 0; col--) {
			CNListener prev = gaia;
			int max_consec = 1;
			int cur_row = this.getRowCount() - 1;
			int cur_col = col;
			LinkedList<CNPiece> queue = new LinkedList<CNPiece>(); // Search the diagonal from top right to bottom left.
			queue.add(grid.get(cur_col).get(cur_row));
			while (queue.size() > 0) {
				CNPiece piece = queue.poll();
				CNListener color = piece.getOwner();
				if (prev == color && prev != gaia) {
					prev = color;
					max_consec++;
					if (max_consec == nToWin) {
						return color; // This satisfies the winning property and this player wins.
					}
				}
				else {
					prev = color;
					max_consec = 1;
				}
				if (cur_row - 1 > 0 && cur_col > 0) {
					cur_row--; // One up
					cur_col--; // One left
					// Added the next diagonal over to the traversal (bottom left)
					queue.add(grid.get(cur_col).get(cur_row));
				}
			}			
		}
		return null;
	}

	
	//------------------- 
	// Some getters
	//-------------------
	
	public boolean isGameStarted() {
		return this.gameStarted;
	}
	
	public boolean isGameEnded() {
		return this.gameEnded;
	}
	
	public int getNToWin() {
		return this.nToWin;
	}
	
	
	public List<String> getGameHistory() {
		// Returns copy of game history.
		List<String> res = new ArrayList<String>();
		res.addAll(this.public_logger);
		return res;
	}

	public ArrayList<ArrayList<CNPiece>> getGrid() {
		// Return copy of grid (prevents write access).
		 ArrayList<ArrayList<CNPiece>> dest = new ArrayList<ArrayList<CNPiece>>();
		    for( List<CNPiece> sublist : this.gameGrid) {
		        List<CNPiece> temp = new ArrayList<CNPiece>();
		        for(CNPiece val: sublist) {
		        	CNPiece newPiece = new CNPiece(val.getOwner());	
		        	temp.add(newPiece);
		        }
		        dest.add((ArrayList<CNPiece>) temp);
		    }
		    return dest;
	}
	
	public int getColCount() {
		return this.cols;
	}
	
	public int getRowCount() {
		return this.rows;
	}
	
	public String getKey() {
		return this.key;
	}
	
	// Generated default methods 

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (allowDiagonalWins ? 1231 : 1237);
		result = prime * result + (allowLateJoining ? 1231 : 1237);
		result = prime * result + cols;
		result = prime * result + ((gaia == null) ? 0 : gaia.hashCode());
		result = prime * result + (gameEnded ? 1231 : 1237);
		result = prime * result + ((gameGrid == null) ? 0 : gameGrid.hashCode());
		result = prime * result + (gameStarted ? 1231 : 1237);
		result = prime * result + ((internal_logger == null) ? 0 : internal_logger.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((keys == null) ? 0 : keys.hashCode());
		result = prime * result + nToWin;
		result = prime * result + playerLimit;
		result = prime * result + ((players == null) ? 0 : players.hashCode());
		result = prime * result + ((public_logger == null) ? 0 : public_logger.hashCode());
		result = prime * result + rows;
		result = prime * result + subscriberLimit;
		result = prime * result + ((subscribers == null) ? 0 : subscribers.hashCode());
		result = prime * result + ((turnQueue == null) ? 0 : turnQueue.hashCode());
		result = prime * result + ((winner == null) ? 0 : winner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CNModel other = (CNModel) obj;
		if (allowDiagonalWins != other.allowDiagonalWins)
			return false;
		if (allowLateJoining != other.allowLateJoining)
			return false;
		if (cols != other.cols)
			return false;
		if (gaia == null) {
			if (other.gaia != null)
				return false;
		} else if (!gaia.equals(other.gaia))
			return false;
		if (gameEnded != other.gameEnded)
			return false;
		if (gameGrid == null) {
			if (other.gameGrid != null)
				return false;
		} else if (!gameGrid.equals(other.gameGrid))
			return false;
		if (gameStarted != other.gameStarted)
			return false;
		if (internal_logger == null) {
			if (other.internal_logger != null)
				return false;
		} else if (!internal_logger.equals(other.internal_logger))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (keys == null) {
			if (other.keys != null)
				return false;
		} else if (!keys.equals(other.keys))
			return false;
		if (nToWin != other.nToWin)
			return false;
		if (playerLimit != other.playerLimit)
			return false;
		if (players == null) {
			if (other.players != null)
				return false;
		} else if (!players.equals(other.players))
			return false;
		if (public_logger == null) {
			if (other.public_logger != null)
				return false;
		} else if (!public_logger.equals(other.public_logger))
			return false;
		if (rows != other.rows)
			return false;
		if (subscriberLimit != other.subscriberLimit)
			return false;
		if (subscribers == null) {
			if (other.subscribers != null)
				return false;
		} else if (!subscribers.equals(other.subscribers))
			return false;
		if (turnQueue == null) {
			if (other.turnQueue != null)
				return false;
		} else if (!turnQueue.equals(other.turnQueue))
			return false;
		if (winner == null) {
			if (other.winner != null)
				return false;
		} else if (!winner.equals(other.winner))
			return false;
		return true;
	}

}
