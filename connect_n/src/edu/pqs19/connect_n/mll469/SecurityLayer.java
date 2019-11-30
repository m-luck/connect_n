// This class is responsible for making sure notifications sent to listeners are indeed from trusted entities (i.e. games which the listener has willingly called through private functions in the past).

package edu.pqs19.connect_n.mll469;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class SecurityLayer {
	
	public static String generateSecret() throws NoSuchAlgorithmException {
		
		long value=new Random().nextLong();;
		String hash = String.format("%016x", value);
		
		return  getHexString(hash);
	}
	
	public static String mix(String gameKey, String actorKey) throws NoSuchAlgorithmException {
		
		String hash = gameKey.concat(actorKey);
		
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


	private static String getHexString(String hash) throws NoSuchAlgorithmException {
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
