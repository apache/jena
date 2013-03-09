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

package org.apache.jena.riot.out;

import java.io.IOException ;
import java.io.Writer ;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.lib.BitsInt ;
import org.apache.jena.atlas.lib.Chars ;

public class OutputUtils
{
    /** Print the number x in width hex chars.  x must fit */
    public static void printHex(StringBuilder out, int x, int width)
    {
        for ( int i = width-1 ; i >= 0 ; i-- )
            x = oneHex(out, x, i) ;
    }

    /** Print one hex digit of the number */
    public static int oneHex(StringBuilder out, int x, int i)
    {
        int y = BitsInt.unpack(x, 4*i, 4*i+4) ;
        char charHex = Chars.hexDigitsUC[y] ;
        out.append(charHex) ; 
        return BitsInt.clear(x, 4*i, 4*i+4) ;
    }
    
    /** Print the number x in width hex chars.  x must fit */
    public static void printHex(Writer out, int x, int width)
    {
        for ( int i = width-1 ; i >= 0 ; i-- )
            x = oneHex(out, x, i) ;
    }

    /** Print one hex digit of the number */
    public static int oneHex(Writer out, int x, int i)
    {
        int y = BitsInt.unpack(x, 4*i, 4*i+4) ;
        char charHex = Chars.hexDigitsUC[y] ;
        try { out.write(charHex) ; } catch (IOException ex) {} 
        return BitsInt.clear(x, 4*i, 4*i+4) ;
    }

    /** Print the number x in width hex chars.  x must fit */
    public static void printHex(AWriter out, int x, int width)
    {
        for ( int i = width-1 ; i >= 0 ; i-- )
            x = oneHex(out, x, i) ;
    }

    /** Print one hex digit of the number */
    public static int oneHex(AWriter out, int x, int i)
    {
        int y = BitsInt.unpack(x, 4*i, 4*i+4) ;
        char charHex = Chars.hexDigitsUC[y] ;
        out.print(charHex) ; 
        return BitsInt.clear(x, 4*i, 4*i+4) ;
    }
    
}
