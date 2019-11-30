package edu.pqs19.connect_n.mll469;

import java.util.ArrayList;

public class CNApp {
	public static void main(String[] args) {
		
		ArrayList<Integer> dims = new ArrayList<Integer>();
		dims.add(7); // 7 columns 
		dims.add(6); // 6 rows
		
		CNModel game = new CNModel(dims, 4);
		
		boolean humanvhuman = false;
		boolean humanvai = false;
		boolean humanvais = true;
		
		CNPlayer human = new CNPlayer(game);
		
		ArrayList<CNComputer> AIs = new ArrayList<CNComputer>();
		
		if (humanvhuman) {
			CNPlayer human2 = new CNPlayer(game);
		}
		else if (humanvai) {
			CNComputer ai = new CNComputer(game, true);
			AIs.add(ai);
		}
		else if (humanvais) {
			CNComputer AI1 = new CNComputer(game, true);
			CNComputer AI2 = new CNComputer(game, true);
			AIs.add((CNComputer) AI1);
			AIs.add((CNComputer) AI2);
		}

		else { // Be sure to add AIs to AI list.
			
	
		}
		
		for (CNComputer ai : AIs) {
			ai.moveIfTurn();
		}
		
	}
}
