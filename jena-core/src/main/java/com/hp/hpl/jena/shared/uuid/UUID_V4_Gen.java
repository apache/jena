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

import java.util.* ;

import com.hp.hpl.jena.shared.uuid.JenaUUID.UUIDFormatException;


/** Generator for for random number based UUIDs (version 2, variant 4)
 */
public class UUID_V4_Gen implements UUIDFactory
{
	// Implementation should be compatible with JXTA UUIDs

    static final int versionHere = 4 ;	      // Version 4: random number
    static final int variantHere = JenaUUID.Var_Std ;
    
	private Random random = null ;
    
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
        s = s.toLowerCase(Locale.ENGLISH) ;

        if ( s.length() != 36 )
            throw new UUIDFormatException("UUID string is not 36 chars long: it's "+s.length()+" ["+s+"]") ;

        if ( s.charAt(8)  != '-' && s.charAt(13) != '-' && s.charAt(18) != '-' && s.charAt(23) != '-' )
            throw new UUIDFormatException("String does not have dashes in the right places: "+s) ;

        UUID_V4 u = parse$(s) ;
        if ( u.getVersion() != versionHere )
            throw new UUIDFormatException("Wrong version (Expected: "+versionHere+"Got: "+u.getVersion()+"): "+s) ;
        if ( u.getVariant() != variantHere )
            throw new UUIDFormatException("Wrong version (Expected: "+variantHere+"Got: "+u.getVariant()+"): "+s) ;
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
