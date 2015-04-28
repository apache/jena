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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.AtlasException ;

/** Working in hex ... */
public class Hex
{
    // No checking, fixed width.
    public static int formatUnsignedLongHex(final byte[] b, final int start, final long value, final int width)
    {
        // Insert from low value end to high value end.
        int idx = start+width-1 ;
        int w = width ;
        long x = value ;
        
        while ( w > 0 )
        {
            int d = (int)(x & 0xF) ;
            x = x>>>4 ; // Unsigned shift.
            byte ch = Bytes.hexDigitsUC[d] ; 
            b[idx] = ch ;
            w-- ;
            idx-- ;

            if ( x == 0 )
                break ;
        }

        if ( x != 0 )
            throw new AtlasException("formatUnsignedLongHex: overflow") ;

        while ( w > 0 )
        {
            b[idx] = '0' ;
            idx-- ;
            w-- ;
        }
        return width ;
    }
    
    // No checking, fixed width.
    public static long getLong(byte[] arr, int idx)
    {
        long x = 0 ;
        for ( int i = 0 ; i < 16 ; i++ )
        {
            byte c = arr[idx] ;
            int v = hexByteToInt(c) ;
            x = x << 4 | v ;  
            idx++ ; 
        }
        return x ;
    }
    
    public static int hexByteToInt(int c)
    {
        if ( '0' <= c && c <= '9' )   
            return c-'0' ;
        else if ( 'A' <= c && c <= 'F' )
            return c-'A'+10 ;
        else if ( 'a' <= c && c <= 'f' )
            return c-'a'+10 ;
        else
            throw new IllegalArgumentException("Bad index char : "+c) ;
    }
    
    /** Return the value of the hex digit, or the marker value if not a hex digit.*/
    public static int hexByteToInt(int c, int marker)
    {
        if ( '0' <= c && c <= '9' )   
            return c-'0' ;
        else if ( 'A' <= c && c <= 'F' )
            return c-'A'+10 ;
        else if ( 'a' <= c && c <= 'f' )
            return c-'a'+10 ;
        else
            return marker ;
    }
}
