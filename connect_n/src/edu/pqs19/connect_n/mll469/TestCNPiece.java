package edu.pqs19.connect_n.mll469;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TestCNPiece {
	
	CNListener player; 
	
	@Before
	public void setup() {
		ArrayList<Integer> dims = new ArrayList<Integer>();
		dims.add(7); // 7 columns 
		dims.add(6); // 6 rows
		CNModel game = new CNModel(dims, 4);
		player = new CNPlayer(game);
	}

	@Test
	public void TestGetOwner() {
		assertEquals(new CNPiece(player).getOwner(), player);
	}
}
