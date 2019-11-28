package edu.pqs19.connect_n.mll469;

import java.util.Objects;  

public class CNPiece {

	private int owner;
	
	CNPiece(int player) {
		Objects.requireNonNull(player);
		this.owner = player;
	}
	
	@Override
	public String toString() {
		return "[]";
	}
	
	public int hashCode() {
		return 1;
	}
	
	public String getColor() {
		return "nice";
	}
}

