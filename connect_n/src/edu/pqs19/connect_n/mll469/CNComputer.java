package edu.pqs19.connect_n.mll469;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CNComputer extends CNPlayer {
	
	private String secret;
	private boolean adversarial;
	
	public CNComputer(CNModel game) {
		super(game);
		
		try {
			this.secret = SecurityLayer.generateSecret(); // Security key for message passing.
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		try {
			game.addListener(this, secret);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.adversarial = false;
	}
	
	public CNComputer(CNModel game, boolean adversarial) {
		super(game);
		
		this.adversarial = adversarial; 
		
		try {
			this.secret = SecurityLayer.generateSecret(); // Security key for message passing.
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		try {
			game.addListener(this, secret);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notifiedMoveMade(ArrayList<ArrayList<CNPiece>> grid, String move, String key) {
		if (this.isTrustedSender(key)) { // Confirm that the game is the one that sent this message, for integrity. 
			this.drawGrid(midPanel, grid, game.getColCount(), game.getRowCount());
			this.moveIfTurn();
		}
	}
	
	public void moveIfTurn() {
		if (this.game.checkIsTurn(this) && !this.game.isGameEnded()) { // An opportunity to move.
			int goodMove = simulateWin();
			if (goodMove != -1) {
				actuallyMove(goodMove);
			}
			else {
				int tries = 0;
				boolean success = false;
				while (success == false && tries < this.game.getColCount() * this.game.getColCount() ) {
					Random r = new Random();
					int choice = r.nextInt((this.game.getColCount() - 1) + 1);
					success = actuallyMove(choice); // Make a random move that works.
					tries++;
				}
			}
			
			dramaticPause();
		}
	}
	
	private int simulateWin() {
		int temp = -1;
		for (int i = 0; i < this.game.getColCount(); i++) {
			ArrayList<ArrayList<CNPiece>> simulationGrid = this.game.getGrid();
			simulateMove(simulationGrid, i, this);
			CNListener check = this.game.anyWinner(simulationGrid, this.game.getNToWin()); 
			if (check == this) {
				return i;
			}
			else {
				check = this.game.anyWinner(simulationGrid, this.game.getNToWin() - 1); // Gets closer.
				if (check == this) {
					return i;
				}
			}
		}
		return temp;
	}
	
	private boolean actuallyMove(int i) {
		try {
			return this.game.sendMove(i, this, this.secret);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean simulateMove(ArrayList<ArrayList<CNPiece>> grid, int col, CNListener submitter) {
	
		ArrayList<CNPiece> pillar = grid.get(col); // Check the column
		for (int depth = 0; depth <= pillar.size(); depth++) {
			
			if (depth == pillar.size()) { // The bottom has already been reached.
				this.updateState(pillar, depth, submitter); // So add a piece on top
				return true;
			}
			
			if (pillar.get(depth).getOwner() != null) { // Drill down until seeing a player unit
				if (depth != 0) { // If there is room for another piece on top
					this.updateState(pillar, depth, submitter); // Then add a piece on top
					return true;
				}
				break;
			}
		}
		return false; // If none of the conditions were met, this move is invalid.
	}
	
	private void updateState(ArrayList<CNPiece>pillar, int depth, CNListener submitter) {
		pillar.set(depth - 1, new CNPiece(submitter)); // Add a piece.
	}
	
	private void dramaticPause() {
		try { 
			TimeUnit.MILLISECONDS.sleep(200); // Pause a bit for drama.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	


}
