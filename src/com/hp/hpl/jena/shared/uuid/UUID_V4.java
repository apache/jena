/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.shared.uuid;

import java.util.* ;
import java.security.* ;

/** Random number based UUIDs
 * @author		Andy Seaborne
 * @version 	$Id: UUID_V4.java,v 1.6 2007-01-02 11:51:49 andy_seaborne Exp $
 */
public class UUID_V4 extends JenaUUID
{
	// Implementation should be compatible with JXTA UUIDs
	// Constants
    public static final int version = 4 ;	      // Version 4: random number
    public static final int variant = JenaUUID.Var_Std ;
    
	static Random random = null ;
    
    long bitsMostSignificant = 0 ;  // Bytes 0 to 7
    long bitsLeastSignificant = 0 ; // Bytes 8 to 15

	UUID_V4(long mostSigBits, long leastSigBits)
	{
        if ( ! check(mostSigBits, leastSigBits) )
            throw new IllegalArgumentException("Funny bits") ;
        this.bitsMostSignificant = mostSigBits ;
        this.bitsLeastSignificant = leastSigBits ;
    }
    
    public long getMostSignificantBits() { return bitsMostSignificant ; }
    public long getLeastSignificantBits() { return bitsLeastSignificant ; }

    private boolean check(long mostSigBits, long leastSigBits)
    { 
        int _variant = _getVariant(mostSigBits, leastSigBits) ; 
        int _version = _getVersion(mostSigBits, leastSigBits) ;
        
        if ( _variant != variant) return false ;
        if ( _version != version) return false ;
        return true ;
    }
    
    public String toString()
    {
        return UUID_V4_Gen.unparse(this) ;
    }

    public int hashCode() { return (int) Bits.unpack(bitsMostSignificant, 32, 64) ; }
    
    public boolean equals(Object other)
    {
        if ( ! (other instanceof UUID_V4 ) )
            return false ;
        UUID_V4 u = (UUID_V4)other ;
        return this.bitsMostSignificant  == u.bitsMostSignificant &&
               this.bitsLeastSignificant == u.bitsLeastSignificant ; 
    }
    
    static boolean initialized = false ;
    
	
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
        random =  new SecureRandom() ; // SecureRandom.getInstance("SHA1PRNG"); 
        
        byte[] seed = LibUUID.makeSeed() ;
        
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

    public int getVersion() { return _getVersion(bitsMostSignificant, bitsLeastSignificant) ; }
    public int getVariant() { return _getVariant(bitsMostSignificant, bitsLeastSignificant) ; }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
