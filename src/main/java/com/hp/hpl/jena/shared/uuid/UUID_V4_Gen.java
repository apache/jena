/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.shared.uuid;

import java.util.* ;

import com.hp.hpl.jena.shared.uuid.JenaUUID.FormatException;


/** Generator for for random number based UUIDs (version 2, variant 4)
 * @author		Andy Seaborne
 * @version 	$Id: UUID_V4_Gen.java,v 1.1 2009-06-29 08:55:40 castagna Exp $
 */
public class UUID_V4_Gen implements UUIDFactory
{
	// Implementation should be compatible with JXTA UUIDs

    static final int versionHere = 4 ;	      // Version 4: random number
    static final int variantHere = JenaUUID.Var_Std ;
    
	Random random = null ;
    
    public UUID_V4_Gen() {}
    
	@Override
    public JenaUUID generate()
    { return generateV4() ; }
    
    public UUID_V4 generateV4()
    {
        init() ;
        long mostSigBits = random.nextLong() ;
        long leastSigBits = random.nextLong() ;
        mostSigBits = Bits.pack(mostSigBits, versionHere, 12, 16) ;
        leastSigBits = Bits.pack(leastSigBits, variantHere, 62, 64) ;
        return new UUID_V4(mostSigBits, leastSigBits) ;
    }
    
    
	@Override
    public JenaUUID parse(String s)
    { return parseV4(s) ; }
    
    public UUID_V4 parseV4(String s)
    {
        s = s.toLowerCase() ;

        if ( s.length() != 36 )
            throw new FormatException("UUID string is not 36 chars long: it's "+s.length()+" ["+s+"]") ;

        if ( s.charAt(8)  != '-' && s.charAt(13) != '-' && s.charAt(18) != '-' && s.charAt(23) != '-' )
            throw new FormatException("String does not have dashes in the right places: "+s) ;

        UUID_V4 u = parse$(s) ;
        if ( u.getVersion() != versionHere )
            throw new FormatException("Wrong version (Expected: "+versionHere+"Got: "+u.getVersion()+"): "+s) ;
        if ( u.getVariant() != variantHere )
            throw new FormatException("Wrong version (Expected: "+variantHere+"Got: "+u.getVariant()+"): "+s) ;
        return u ;
    }
    
    static UUID_V4 parse$(String s)
    {
        // The UUID broken up into parts.
        //       00000000-0000-0000-0000-000000000000
        //       ^        ^    ^    ^    ^           
        // Byte: 0        4    6    8    10
        // Char: 0        9    14   19   24  including hyphens
        long mostSigBits = Bits.unpack(s, 0, 8) ;
        // Skip -
        mostSigBits = mostSigBits << 16 | Bits.unpack(s, 9, 13) ;
        // Skip -
        mostSigBits = mostSigBits << 16 | Bits.unpack(s, 14, 18) ;
        
        long leastSigBits = Bits.unpack(s, 19, 23) ;
        leastSigBits = leastSigBits<<48 | Bits.unpack(s, 24, 36) ;
        return new UUID_V4(mostSigBits, leastSigBits) ;
    }
    
    public static String unparse(UUID_V4 uuid)
    {
        StringBuffer sb = new StringBuffer(36) ;
        JenaUUID.toHex(sb, Bits.unpack(uuid.bitsMostSignificant,  32, 64), 4) ;
        sb.append('-') ;
        JenaUUID.toHex(sb, Bits.unpack(uuid.bitsMostSignificant,  16, 32), 2) ;
        sb.append('-') ;
        JenaUUID.toHex(sb, Bits.unpack(uuid.bitsMostSignificant,  0,  16), 2) ;
        sb.append('-') ;
        JenaUUID.toHex(sb, Bits.unpack(uuid.bitsLeastSignificant, 48, 64), 2) ;
        sb.append('-') ;
        JenaUUID.toHex(sb, Bits.unpack(uuid.bitsLeastSignificant, 0,  48), 6) ;
        return sb.toString() ;
    }
    
    private void init()
    {
        if ( random == null )
            reset() ;
    }

    @Override
    public void reset() 
    {
        random =  LibUUID.makeRandom() ; 
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
