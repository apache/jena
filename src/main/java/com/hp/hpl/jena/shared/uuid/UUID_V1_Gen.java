/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.shared.uuid;

import com.hp.hpl.jena.shared.uuid.JenaUUID.FormatException;

/* RFC 4122  "A Universally Unique IDentifier (UUID) URN Namespace"
   ftp://ftp.rfc-editor.org/in-notes/rfc4122.txt
   Originally: http://www.opengroup.org/onlinepubs/009629399/apdxa.htm
 
Version 1, variant 2: timebased:
60 bits of time
48 bits of nodeId
12 bits of clock sequence
2  bits variant
4  bits version

laid out as:

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

Java5 introduced java.util.UUID but it does not include an timebased generator.
It provides an API to manipulate timebased UUIDs, and also factory methods for
vversion 4 (random) and version 3 (name based/MD5) or version 5 (name based/SHA1).
Version 2 is DCE Security version, with embedded POSIX UIDs.

Most significant long:

0xFFFFFFFF00000000 time_low
0x00000000FFFF0000 time_mid
0x000000000000F000 version
0x0000000000000FFF time_hi

Least significant long:

0xC000000000000000 variant     C = 1100 (base 2) 
0x3FFF000000000000 clock_seq   3 = 0011 (base 2)
0x0000FFFFFFFFFFFF node

Note on variant: despite the javadoc document, the variant is defined as the
top 3 bits of octet 8.  But the low bit is a "don't care" in this variant
and is used by the clock. 

*/

/* Java6: get MAC address:
NetworkInterface ni = NetworkInterface.getByInetAddress(address)
*/

/** Generator for timebased UUIDs (version 1, variant 2)
 * 
 * @author		Andy Seaborne
 * @version 	$Id: UUID_V1_Gen.java,v 1.2 2011-05-05 20:48:51 andy_seaborne Exp $
 */


public class UUID_V1_Gen implements UUIDFactory
{
	// Constants
    static final int versionHere = 1 ;	      // Version 1: time-based. This is one character hex string.
    static final int variantHere = 2 ;		  // DCE varient
    
    static final long maskTimeLow    = 0xFFFFFFFF00000000L ;
    static final long maskTimeMid    = 0x00000000FFFF0000L ;
    static final long maskTimeHigh   = 0x0000000000000FFFL ;
    static final long maskVersion    = 0x000000000000F000L ;
        
    static final long maskVariant    = 0xC000000000000000L ;
    static final long maskClockSeq   = 0x3FFF000000000000L ;
    static final long maskNode       = 0x0000FFFFFFFFFFFFL ;
    
	// Generator variables.
    long gregorianTime = 0 ;
    static final long UUIDS_PER_TICK = 100 ;
    long uuids_this_tick = UUIDS_PER_TICK+1 ;	// Force reset first time.
    
    // Genrator initial state
    int clockSeq = 0 ;
    private static final int CLOCK_BITS = 8 ; 
    long node = 0 ;

	// Time control
    private long lastTime = 0 ;
    private long DELAY = 10 ; // Milliseconds

 
    public UUID_V1_Gen()
    { reset() ; }

    /** (Re)set the network id (a random number) and the timstamp */
    
    @Override
    public void reset()
    {
        setInitialState() ;
        setTime() ;
    }

    @Override
    public JenaUUID generate()
    { return generateV1() ; }
        
    public UUID_V1 generateV1()
    {
        
        long timestamp = 0 ;
        synchronized(this)
        {
            if ( uuids_this_tick >= UUIDS_PER_TICK )
                setTime() ;
            timestamp = gregorianTime + uuids_this_tick ;
            uuids_this_tick++ ;
        }
        return generate(timestamp) ;
    }
    
    /*  8-4-4-4-12 = 32+4 dashes = 36 chars

    UUID                   = <time_low> "-" <time_mid> "-"
                             <time_high_and_version> "-"
                             <variant_and_sequence> "-"
                              <node>
    time_low               = 4*<hexOctet>
    time_mid               = 2*<hexOctet>
    time_high_and_version  = 2*<hexOctet>
    variant_and_sequence   = 2*<hexOctet>
    node                   = 6*<hexOctet>
    hexOctet               = <hexDigit><hexDigit>
     */

    
    @Override
    public JenaUUID parse(String s) throws FormatException
    {
        s = s.toLowerCase() ;

        if ( s.length() != 36 )
            throw new FormatException("UUID string is not 36 chars long: it's "+s.length()+" ["+s+"]") ;

        if ( s.charAt(8)  != '-' && s.charAt(13) != '-' && s.charAt(18) != '-' && s.charAt(23) != '-' )
            throw new FormatException("String does not have dashes in the right places: "+s) ;

        UUID_V1 u = parse$(s) ;
        if ( u.getVersion() != versionHere )
            throw new FormatException("Wrong version (Expected: "+versionHere+"Got: "+u.getVersion()+"): "+s) ;
        if ( u.getVariant() != variantHere )
            throw new FormatException("Wrong version (Expected: "+variantHere+"Got: "+u.getVariant()+"): "+s) ;
        return u ;
    }
    
    static UUID_V1 parse$(String s)
    {
        // The UUID broken up into parts.
        //       00000000-0000-0000-0000-000000000000
        //       ^        ^    ^    ^    ^           
        // Byte: 0        4    6    8    10
        // Char: 0        9    14   19   24  including hyphens
        int x = (int)Bits.unpack(s, 19, 23) ;
        int variant = (x>>>14) ;
        int clockSeq = x & 0x3FFF ;

        long timeHigh = Bits.unpack(s, 15, 18) ;
        long timeMid  = Bits.unpack(s, 9, 13) ;
        long timeLow  = Bits.unpack(s, 0, 8) ;

        long node = Bits.unpack(s, 24, 36) ;
        int version = (int)Bits.unpack(s, 14, 15) ;
        return generate(version, variant, timeHigh, timeMid, timeLow, clockSeq, node) ;
    }
    
    // Easier to have parse and unparse code close together 
    public static String unparse(UUID_V1 uuid)
    {
        int _variant = uuid.getVariant() ;  // (int)((UUID_V1_Gen.maskVariant & uuid.bitsLower)>>>62) ;
        int _version = uuid.getVersion() ;  // (int)((UUID_V1_Gen.maskVersion & uuid.bitsUpper)>>>12) ;

        long timeHigh = uuid.getTimeHigh() ;
        long timeMid =  uuid.getTimeMid() ;
        long timeLow =  uuid.getTimeLow() ;

        long node = uuid.getNode() ;
        long clockSeq = uuid.getClockSequence() ;

        StringBuffer sBuff = new StringBuffer() ;
        JenaUUID.toHex(sBuff, timeLow, 4) ;
        sBuff.append('-') ;
        JenaUUID.toHex(sBuff, timeMid, 2) ;
        sBuff.append('-') ;
        JenaUUID.toHex(sBuff, _version << 12 | timeHigh, 2) ;
        sBuff.append('-') ;
        JenaUUID.toHex(sBuff, (long)_variant << 14 | clockSeq, 2) ;
        sBuff.append('-') ;
        JenaUUID.toHex(sBuff, node, 6) ;
        return sBuff.toString() ;
    }

    private UUID_V1 generate(long timestamp)
    {
        return generate(versionHere, variantHere, timestamp, clockSeq, node) ;
    }

    // Testing.
    public static UUID_V1 generate(int version, int variant, long timestamp, long clockSeq, long node)
    {
        long timeHigh = timestamp>>>(60-12) ;            // Top 12 bits of 60 bit number.
        long timeMid  = (timestamp>>>32)&0xFFFFL ;       // 16 bits, bits 32-47
        long timeLow = timestamp & 0xFFFFFFFFL ;         // Low 32 bits

        return generate(version, variant, timeHigh, timeMid, timeLow, clockSeq, node) ;
    }

    private static UUID_V1 generate(int version, int variant, long timeHigh, long timeMid, long timeLow, long clockSeq, long node)
    {
        long mostSigBits = (timeLow<<32) | (timeMid<<16) | (versionHere<<12) |  timeHigh ;
        long leastSigBits = (long)variantHere<<62 | (clockSeq << 48) | node ;
        return new UUID_V1(mostSigBits, leastSigBits) ;
    }

	private void setTime()
    {
        long time = 0 ;
        
        // Wait for a clock tick.
        synchronized (this) {
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

    private void setInitialState()
	{
        long random = LibUUID.makeRandom().nextLong() ;

        node = Bits.unpack(random, 0, 47);      // Low 48bits, except groups address bit
        node = Bits.set(node, 47) ;             // Set group address bit
        
        // Can also set the clock sequence number to increase the randomness.
        // Use up to 13 bits for the clock (actually, it's 14 bits as 
        // strays into the variant).
        // We use less to get a characteristic "-80??-" in the string
        
        clockSeq = 0 ;
        if ( CLOCK_BITS != 0 )
            clockSeq = (int)Bits.unpack(random, 48, (48+CLOCK_BITS)) ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
