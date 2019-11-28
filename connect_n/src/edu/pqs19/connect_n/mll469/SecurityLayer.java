package edu.pqs19.connect_n.mll469;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityLayer {

	public String generateSecret() {
		long value=123L;
		String hash = String.format("%016x", value);
		MessageDigest md = MessageDigest.getInstance("SHA-256");    
        byte[] byteHash = md.digest(hash.getBytes(StandardCharsets.UTF_8));  
	    BigInteger number = new BigInteger(1, byteHash);  
        // Convert message digest into hex value  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
        // Pad with leading zeros 
        while (hexString.length() < 32)  
        {  
            hexString.insert(0, '0');  
        }  
        return hexString.toString(); 
	}
	
}
