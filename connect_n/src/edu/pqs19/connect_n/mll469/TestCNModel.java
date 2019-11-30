package edu.pqs19.connect_n.mll469;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TestCNModel {
	
	CNPlayer player; 
	CNModel game;
	
	@Before
	public void setup() {
		ArrayList<Integer> dims = new ArrayList<Integer>();
		dims.add(7); // 7 columns 
		dims.add(6); // 6 rows
		game = new CNModel(dims, 4);
		player = new CNPlayer(game);
	}

	@Test
	public void TestAddMultipleListeners() {
		game.addListener(player, "testKey");
		game.addListener(player, "testKey");
	}
	
	@Test
	public void TestAddMultiplePlayers() {
		game.addPlayer(player);
		game.addPlayer(player);
	}
	
	@Test 
	public void TestCheckIsTurn() {
		game.addPlayer(player);
		boolean res = game.checkIsTurn(player);
		assertTrue(res);
		res = game.checkIsTurn(null);
		assertFalse(res);
	}
	

	@Test
	public void TestSendMove() {
		game.addPlayer(player);
		boolean res;
		try {
			res = game.sendMove(0, player, "wrong_key");
			assertFalse(res);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void TestCheckEnd() {
		assertFalse(game.checkIfEndgame());
	}
	
	@Test 
	public void TestCheckWinner() {
		assertEquals(game.anyWinner(game.getGrid(), 2), null);
	}
}
