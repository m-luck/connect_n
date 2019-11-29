package edu.pqs19.connect_n.mll469;

import java.util.ArrayList;

public class CNApp {
	public static void main(String[] args) {
		
		ArrayList<Integer> dims = new ArrayList<Integer>();
		dims.add(7); // 7 columns 
		dims.add(6); // 6 rows
		
		CNModel game = new CNModel(dims, 4);
		
		new CNPlayer(game);
		new CNPlayer(game);
		
	}
}
