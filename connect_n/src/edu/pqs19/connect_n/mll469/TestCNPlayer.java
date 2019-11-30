package edu.pqs19.connect_n.mll469;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TestCNPlayer {
	
	CNModel game;
	CNPlayer player;
	
	@Before
	public void setup() {
		ArrayList<Integer> dims = new ArrayList<Integer>();
		dims.add(7); // 7 columns 
		dims.add(6); // 6 rows
		
		game = new CNModel(dims, 4);
		player = new CNPlayer(game);
	}
	
	@Test
	public void TestNotifiedGameMayHaveStarted() {
		player.notifiedGameMayHaveStarted();
	}
	
	@Test
	public void TestNotifiedGameMayHaveEnded() {
			player.notifiedGameMayHaveEnded(null);
	}
	
	@Test
	public void notifiedMoveMade() {
		player.notifiedMoveMade(game.getGrid(), "none", "wrong key");
	}
	
	@Test
	public void TestNotifiedOther() {
		player.notifiedOther("message", "wrong key");
	}
	
}
