/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.shared.uuid;

import java.util.* ;
import java.security.* ;

/** Random number based UUIDs
 * @author		Andy Seaborne
 * @version 	$Id: UUID_V4.java,v 1.2 2005-09-19 11:19:34 andy_seaborne Exp $
 */
class UUID_V4 extends UUID
{
	// Implementation should be compatible with JXTA UUIDs
	// Constants
    static final int versionHere = 4 ;	      // Version 4: random number
    static final int variantHere = 2 ;		  // DCE/IETF varient
    
	static Random random = null ;
	
	// Constructors : call UUID.create
	
	UUID_V4()
	{
		super(versionHere, variantHere);
		init();
		byte b[] = new byte[128 / 8];

		random.nextBytes(b);

		// Insert version and variant.
		// Version: Octet6: high 4 bits are 1000
		b[6] &= 0x0F;	// Clear 4 bits
		b[6] |= (versionHere<<4) ;	// Set
		
		// Variant: Octet8: 2 MSB are 10
		b[8] &= 0x3F;	// Clear 2 bits
		b[8] |= (variantHere)<<6 ;	// Set DCE/IETF variant
		
		// Ensure the multicast bit is 1
		// JXTA does this but it looses a bit of randomness.
		// It stops classes with network id 
		//b[10] &= 0x7f;	// Clear one bit
		//b[10] |= (1<<7) ; 	// Set one bit

		// Generate string.
		StringBuffer sb = new StringBuffer(40) ;
		String tmp = UUID.stringify(b) ;
		
		sb.append(tmp.substring(0,8)) ;
		sb.append('-') ;
		sb.append(tmp.substring(8,12)) ;
		sb.append('-') ;
		sb.append(tmp.substring(12,16)) ;
		sb.append('-') ;
		sb.append(tmp.substring(16,20)) ;
		sb.append('-') ;
		sb.append(tmp.substring(20)) ;
		uuid = sb.toString() ;
	}
    
    UUID_V4(String s)
    {
       	super(versionHere, variantHere) ;	
       	    	
    	if ( s.equals(nilStr) )
    	{
    		uuid = nilStr ;
    		return ;
    	}
    	// Get version and variant.  And test.
    	try {
	    	int var  = Integer.parseInt(s.substring(19,20),16) ;
	    	var = var >> 2 ;
	    	if ( var != variantHere )
		    	throw new FormatException("UUID variant is not 2: "+var+" in "+s) ;
		    	
			String tmp = s.substring(14,15) ;
			int version = Integer.parseInt(tmp,HEX) ;		    	
		    	
			// Version
	        int ver = Integer.parseInt(s.substring(14,15),16) ;
	        if ( ver != versionHere )
	            throw new FormatException("UUID version is strange: "+ver+" in "+s) ;
    	}
		catch (NumberFormatException e)
        { throw new FormatException("UUID has unknown variant or version: "+s) ; }
        
    	uuid = s ;
    }

    static boolean initialized = false ;
	static boolean warningSent = false ;
	
    static public void init()
    {
        if ( !initialized )
        {
            reset() ;
            initialized = true ;
        }
    }

    static public void uninit() { initialized = false ; }
    
    public static void reset() 
    {
        random = null;

        if (useSecureRandom)
        {
            try
            {
                SecureRandom sRandom = SecureRandom.getInstance("SHA1PRNG");
                random = sRandom;
                // First call - can be SLOW - depends on version of Java.  OK 1.4.2
            }
            catch (NoSuchAlgorithmException ex)
            {
                if (!warningSent)
                {
                    System.err.println("No secure random generator.");
                    warningSent = true;
                }
            }
        }
        
        byte[] seed = makeSeed() ;
        
        if ( random == null )
        {
            // dreadful.
            random = new Random() ;
            long l = 0; 
            for (int i = 0; i < 8; i++)
                l = (l << 8) | (seed[i] & 0xff);
            random = new Random() ;
            random.setSeed(l) ;
        }
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
