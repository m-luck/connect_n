package edu.pqs19.connect_n.mll469;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TestCNComputer {
	
	CNModel game;
	@Before 
	public void setup() {
		ArrayList<Integer> dims = new ArrayList<Integer>();
		dims.add(7); // 7 columns 
		dims.add(6); // 6 rows
		game=new CNModel(dims, 4);
	}
	
	@Test
	public void TestNotifiedMoveMade() {
		CNComputer ai = new CNComputer(game);
		ai.notifiedMoveMade(game.getGrid(), "move", "wrong key");
	}
	
	@Test
	public void TestMoveIfTurn() {
		CNComputer ai = new CNComputer(game);
		ai.moveIfTurn();
	}
}
