/*
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.shared.uuid;

import java.util.* ;
import java.security.* ;

/*
Version 1:
60 bits of time
48 bits of nodeId
12 bits of clock sequence
2  bits variant
4  bits version

   0                   1                   2                   3
   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                          time_low                             |   8 hex digits
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |       time_mid                |         time_hi_and_version   |   4-4
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |clk_seq_hi_res |  clk_seq_low  |         node (0-1)            |   4-
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                         node (2-5)                            |   12
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
*/

// http://www.opengroup.org/onlinepubs/009629399/apdxa.htm



/** Timebased UUIDs.
 * 
 * @author		Andy Seaborne
 * @version 	$Id: UUID_V1.java,v 1.1 2005-09-07 17:26:22 andy_seaborne Exp $
 */


class UUID_V1 extends UUID
{
	// Constants
    static final int versionHere = 1 ;	      // Version 1: time-based. This is one character hex string.
    static final int variantHere = 2 ;		  // DCE varient

	// The timestamp is the time and the increment.
    long timestamp ;

	// Generator variables.
    static long gregorianTime = 0 ;
    static final long UUIDS_PER_TICK = 100 ;
    static long uuids_this_tick = UUIDS_PER_TICK+1 ;	// Force reset first time.

    // The 48 bit "address" for this machine.
    static String address = null ;

	// Lock object
    private static Object mutex = new Object() ;
    
	// Time control
    private static long lastTime = 0 ;
    private static long DELAY = 10 ; // Milliseconds

	static boolean initialized = false ;

	// Constructors : call UUID.create to get a UUID. 
    UUID_V1()
    {
    	super(versionHere, variantHere) ;
		init() ;
    	
        synchronized(mutex)
        {
            if ( uuids_this_tick >= UUIDS_PER_TICK )
                setTime() ;
            timestamp = gregorianTime + uuids_this_tick ;
            uuids_this_tick++ ;
        }
        generateString() ;
    }

    UUID_V1(String s) throws FormatException
    {
    	super(versionHere, variantHere) ;
    	
    	if ( s.equals(nilStr) )
    	{
    		uuid = nilStr ;
    		return ;
    	}
    	
        // Canonical: this works in conjunction with .equals
        s = s.toLowerCase() ;

        if ( s.startsWith("urn:") )
            s = s.substring(4) ;
        if ( s.startsWith("uuid:") )
            s = s.substring(5) ;

        // 8-4-4-4-12 = 32+4 dashes = 36 chars
        // 0 to 7       Time low
        // 8 is dash
        // 9 to 12      Time mid
        // 13 is dash
        // 14 to 17     Variant and time high
        // 18 is dash
        // 19 to 22     Version and ClockSeqHigh and ClockSeqLow
        // 23 is dash
        // 24 to 35     Node ID

        if ( s.length() != 36 )
            throw new FormatException("UUID string is not 36 chars long: it's "+s.length()+" ["+s+"]") ;

        if ( s.charAt(8)  != '-' && s.charAt(13) != '-' && s.charAt(18) != '-' && s.charAt(23) != '-' )
            throw new FormatException("String does not have dashes in the right places: "+s) ;

	    // The UUID broken up into parts.
	    
        String timeLow            = extract(s, 0, 8) ;
        String timeMid            = extract(s, 9, 13) ;
	    String verStr             = extract(s, 14, 15) ;
        String timeHigh           = extract(s, 15, 18) ;
        String clockSeqHighRes    = extract(s, 19, 21) ;
        String clockSeqLow        = extract(s, 21, 23) ;
        String address            = extract(s, 24, 36) ;

        try {
            // Extract the variant.
            int var  = Integer.parseInt(clockSeqHighRes.substring(0,1),16) ;
            // top 3 bits of the nibble.
            var = var >> 2 ;
            if ( var != variantHere )
                throw new FormatException("UUID variant is not 2: "+var+" in "+s) ;
            // Version
            int ver = Integer.parseInt(verStr,16) ;
            if ( ver != versionHere )
                throw new FormatException("UUID version is strange: "+ver+" in "+s) ;
        } catch (NumberFormatException e)
        { throw new FormatException("UUID has unknown variant or version: "+s) ; }

		// Put it all back together again
        uuid = timeLow + "-" +
	           timeMid + "-" +
	           version+timeHigh + "-" +
	           clockSeqHighRes + clockSeqLow + "-" +
	           address ;

        // Test against the canonical input string
        if ( ! s.equals(uuid) )
        {
            System.out.println("Input2:  "+s);
            System.out.println("Output: "+uuid);
            throw new FormatException("UUID string did not regenerate: "+s) ;
        }
    }
	

    public boolean equals(UUID u)
    {
        return uuid.equals(u.uuid) ;
    }

	public static void init()
	{
		if (!initialized)
		{
			reset();
			initialized = true;
		}
	}
	
    public static void uninit() { initialized = false ; }

    /** (Re)set the network id (a random number) and the timstamp */

	static public void reset()
	{
        setNetworkId() ;
        setTime() ;
	}

	// ------------------------------------------------------------------------
    // ---- Work functions
    
    private void generateString()
    {
        // 8-4-4-4-12
        // time(low/4) - 8 chars
        // time(mid/2) - 4 chars
        // version (4 bits) and time hi - 1+3chars of time.
        // variant (2 bits) and clock seq_hi = 4 chars
        // clock_seq_low
        // Node
        String gt = Long.toHexString(timestamp) ;
        // Make sure its long enough.
        // 60bits, in 4 bit nibbles.
        while(gt.length()<(60/4))
            gt = "0"+gt ;

        // Should be 15.
        if ( gt.length() != 15 )
        	throw new RuntimeException("UUID error: Gregorian time is not 15 hex digits long") ;

        final int len = 15 ;

        String timeLow = gt.substring(len-8) ;
        String timeMid = gt.substring(len-12,len-8) ;
        // Loose 4 (high) bits of time for version
        String timeHigh = gt.substring(0,len-12) ;
        String versionStr = Integer.toString(version) ;

		// Clock sequence number is zero.
		// Thgis could be used for restarts and clocks that jump backwards.
        // Variant and clockHigh = 0
        String clockSeqHighRes = Integer.toHexString(variantHere<<6 + 0) ;
        // clockSeqLow is alway zero 
        String clockSeqLow = "00" ;

		uuid = timeLow + "-" +
	           timeMid + "-" +
	           versionStr+timeHigh + "-" +
	           clockSeqHighRes + clockSeqLow + "-" +
	           address ;
    }

    private static void setTime()
    {
        long time = 0 ;
        
        // Wait for a clock tick.
        synchronized (mutex) {
            if ( lastTime == 0 )
                lastTime = System.currentTimeMillis();

            boolean done = false;
            while (!done) {
                time = System.currentTimeMillis();
                if (time < lastTime+DELAY) {
                    // pause for a while to wait for time to change
                    try {
                        Thread.sleep(DELAY);
                    } catch (java.lang.InterruptedException e) {} // ignore exception
                    continue;
                } else {
                    done = true;
                }
            }
        }

        // We claim this tick just passed!  1 UUID per 100ns so ...
        //UUIDS_PER_TICK = (time-lastTime)*10 ;
        lastTime = time;
        uuids_this_tick = 0 ;

        // Convert to the UUID base time (00:00:00.00, 15 October 1582)
        // That's the date of the Gregorian calendar reforms
        // See the text quoted for the number.
        // Java base time is is January 1, 1970.

        gregorianTime = time*10 + 0x01B21DD213814000L ;
    }

	// ----------------------------------------------------
	// Initial seeding
	
	private static boolean noRandWarningSent = false ;
	
	private static void setNetworkId()
	{
		byte[] seed = makeSeed() ;
		
		// Either use the bottom 48bits from the seed or
		// use the secure random number generator.

   		Random random = null ;

		if ( useSecureRandom )
		{
	    	try {
		    	SecureRandom sRandom = SecureRandom.getInstance("SHA1PRNG");
		    	// This is in addition to, not instead of, the built-in seeding.
		    	sRandom.setSeed(seed) ;
		    	random = sRandom ;
				// First call - can be SLOW - depends on version of Java.  OK 1.4.2
	    	} catch (NoSuchAlgorithmException ex)
	    	{
				if (!noRandWarningSent)
				{
					System.err.println("No secure random generator.");
                    noRandWarningSent = true;
				}
	    	}
		}
		
		byte b[] = new byte[48/8] ;
		
		if ( random == null )
		{
			// We could use the seed with java.util.Random but 
			// instead we just take 48 bits from the seed.
			
			//long l = 0; 
			//for (int i = 0; i < 8; i++)
			//	l = (l << 8) | (seed[i] & 0xff);
			//random = new Random() ;
			//random.setSeed(l) ;

			int i = 0 ;
			for ( ; i < Math.min(seed.length, b.length) ; i++ )
					b[i] = seed[i] ;

			for ( ; i < b.length ; i++ )
				b[i] = 0 ;
		}
		else
			random.nextBytes(b) ;
		 
		// Got the pseudo address.  Make sure it is a group address.

	    b[0] |= 1<<7 ;
	    address = UUID.stringify(b) ;
        // 12 zeros.
        //         123456789012
        address = "000000000000"+address ;
        address = address.substring(address.length()-12) ;

        // *Ensure* high bit is set : this is the multicast bit so we
        // then do not clash with any real network node.

        String nibble = address.substring(0,1) ;
        int i = Integer.parseInt(nibble,16) ;

        if ( (i & 0x8 ) == 0 )
        {
			System.err.println("High bit of address is not one: "+address) ;
			// Fix.
            address = Integer.toHexString(i)+address.substring(1) ;
        }
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
