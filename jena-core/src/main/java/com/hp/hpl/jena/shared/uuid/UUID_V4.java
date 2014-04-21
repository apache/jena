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
import java.security.* ;

/** Random number based UUIDs
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

    @Override
    public int getVersion() { return _getVersion(bitsMostSignificant, bitsLeastSignificant) ; }
    @Override
    public int getVariant() { return _getVariant(bitsMostSignificant, bitsLeastSignificant) ; }
}
