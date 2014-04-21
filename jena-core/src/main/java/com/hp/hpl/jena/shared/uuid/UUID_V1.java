/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
