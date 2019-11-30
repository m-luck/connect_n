package edu.pqs19.connect_n.mll469;

import static org.junit.Assert.assertNotEquals;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;

public class TestSecurityLayer {

	@Test
	public void TestGenerateSecret() throws NoSuchAlgorithmException {
		assertNotEquals(SecurityLayer.generateSecret(), null);
	}
	
	@Test
	public void TestMixStrings() throws NoSuchAlgorithmException {
		String res = SecurityLayer.mix("shattered", "windows");
		assertNotEquals(res, null);
	}
}
