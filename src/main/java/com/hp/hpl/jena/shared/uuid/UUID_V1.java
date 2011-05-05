/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.shared.uuid;

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

/* java.util.UUID
0xFFFFFFFF00000000 time_low
0x00000000FFFF0000 time_mid
0x000000000000F000 version
0x0000000000000FFF time_hi

The least significant long consists of the following unsigned fields:

0xC000000000000000 variant
0x3FFF000000000000 clock_seq
0x0000FFFFFFFFFFFF node
*/

/** Timebased UUIDs. */


public class UUID_V1 extends JenaUUID
{
    // Constants
    public static final int version = 1 ;           // Version 1: time-based.
    public static final int variant = JenaUUID.Var_Std ;          // DCE varient

    // The only state-per-object
    long bitsMostSignificant ;
    long bitsLeastSignificant ;
    
    UUID_V1(long mostSigBits, long leastSigBits)
    {
        if ( ! check(mostSigBits, leastSigBits) )
        {
            check(mostSigBits, leastSigBits) ;
            throw new IllegalArgumentException("Funny bits") ;
        }
        bitsMostSignificant = mostSigBits ;
        bitsLeastSignificant = leastSigBits;
    }
    
    @Override
    public long getMostSignificantBits() { return bitsMostSignificant ; }
    @Override
    public long getLeastSignificantBits() { return bitsLeastSignificant ; }
    
    private boolean check(long mostSigBits, long leastSigBits)
    { 
        int _variant = _getVariant(mostSigBits, leastSigBits) ; 
        int _version = _getVersion(mostSigBits, leastSigBits) ;
        
        if ( _variant != variant) return false ;
        if ( _version != version) return false ;
        return true ;
    }
    
    @Override
    public String toString()
    { return UUID_V1_Gen.unparse(this) ; } 

    // Time low - which includes the incremental count. 
    @Override
    public int hashCode() { return (int) Bits.unpack(bitsMostSignificant, 32, 64) ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof UUID_V1 ) )
            return false ;
        UUID_V1 x = (UUID_V1)other ;
        return this.bitsMostSignificant == x.bitsMostSignificant &&  this.bitsLeastSignificant == x.bitsLeastSignificant ;
    }

    // Accessors
    
    long getTimeHigh()  { return Bits.unpack(bitsMostSignificant, 0,  12) ; } // ( uuid.bitsUpper & UUID_V1_Gen.maskTimeHigh ) ;
    long getTimeMid()   { return Bits.unpack(bitsMostSignificant, 16, 32) ; } // ( uuid.bitsUpper & UUID_V1_Gen.maskTimeMid ) >>> 16 ;
    long getTimeLow()   { return Bits.unpack(bitsMostSignificant, 32, 64) ; } // ( uuid.bitsUpper & UUID_V1_Gen.maskTimeLow ) >>> 32;
    
    public long getTimestamp()
    { 
        return getTimeLow() | getTimeMid()<<32 | getTimeHigh()<<48 ;
    }
    
    public long getClockSequence()
    {
        return Bits.unpack(bitsLeastSignificant, 48, 62) ;
    }
    public long getNode() { return Bits.unpack(bitsLeastSignificant, 0, 48) ; }
    
    @Override
    public int getVersion() { return super._getVersion(bitsMostSignificant, bitsLeastSignificant) ; }

    @Override
    public int getVariant() { return super._getVariant(bitsMostSignificant, bitsLeastSignificant) ; }
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
