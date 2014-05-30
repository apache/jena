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

package org.apache.jena.riot.process.normalize;

import static org.apache.jena.atlas.lib.Chars.CH_DOT ;
import static org.apache.jena.atlas.lib.Chars.CH_MINUS ;
import static org.apache.jena.atlas.lib.Chars.CH_PLUS ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

class NormalizeValue2
{
    // faster (??)
    // faster - directly check for correct forms : leading zeros, signs.
    // Place into char[], zap with -1 if unwanted, recollect, rebuild string.
    
    // --- Unfinished
    
    static char NonChar = (char)0 ;
    static char CH_ZERO = '0' ;
    
    private static void stripLeadingPlus(char[] chars)
    {
        if ( chars[0] == CH_PLUS )
            chars[0] = NonChar ;
    }
    
    // Works on decimals and integers as "." is "just" a stopping character.
    private static void stripLeadingZeros(char[] chars)
    {
        // Avoid sign, or zapped sign.
        int idx = 0 ;
        if ( chars[0] == CH_MINUS || chars[0] == NonChar )
            idx = 1 ;
        // BUT not all zeros.
        while( idx < chars.length && chars[idx] == CH_ZERO )
        {
            chars[idx] = NonChar ;
            idx ++ ;
        }
        // All leading zeros - put one back.
        // what about "-.1"?
        if ( idx == chars.length || chars[idx] == '.' )
            chars[idx-1] = CH_ZERO ;
        
    }
    
    // Decimal specific.
    private static void stripTrailingZeros(char[] chars)
    {
        // Find the . (if any)
        int iDot = 0 ;
        for ( ; iDot < chars.length ; iDot++ )
        {
            if ( chars[iDot] == CH_DOT ) 
                break ;
        }
        
        if ( iDot == chars.length )
            // No dot.
            // ??
            {}
        
        int start = 0 ;
        if ( chars[0] == CH_MINUS || chars[0] == NonChar )
            start = 1 ;

        int idx ;
        for ( idx = chars.length-1 ; idx >= start ; idx-- )
        {
            if ( chars[idx] != CH_ZERO )
                break ;
            chars[idx] = NonChar ;
        }
        // Don't remove all trailing zeros if "123." or "000"  
        if ( idx == start || chars[idx] == '.' )
            chars[idx-1] = CH_ZERO ;
    }

    // Rebuild a string but return null for "no change"
    private static String rebuild(char[] chars)
    {
        // i is the read index, j the write index.
        boolean modified = false ;
        int j = 0 ;
        for ( int i = 0 ; i < chars.length ; i++)
        {
            if ( chars[i] == NonChar )
            {
                modified = true ;
                continue ;
            }
            if ( ! modified )
                continue ;
            chars[j] = chars[i] ;
            j++ ;
        }
        if ( ! modified )
            return null ;
        return new String(chars,0,j) ;
    }
    
    // --- Working versions 
    
    static DatatypeHandler dtInteger = new DatatypeHandler() {
        @Override
        public Node handle(Node node, String lexicalForm, RDFDatatype datatype)
        {
            char[] chars = lexicalForm.toCharArray() ;
            if ( chars.length == 0 )
                // Illegal lexical form.
                return node ;
            stripLeadingPlus(chars) ;
            stripLeadingZeros(chars) ;
            String lex2 = rebuild(chars) ;
            if ( lex2 == null )
                return node ;
            return NodeFactory.createLiteral(lex2, null, datatype) ;
        }
    } ;

    static DatatypeHandler dtDecimal = new DatatypeHandler() {
        @Override
        public Node handle(Node node, String lexicalForm, RDFDatatype datatype)
        {
            // Need to force "0."
            char[] chars = lexicalForm.toCharArray() ;
            if ( chars.length == 0 )
                // Illegal lexical form.
                return node ;
            stripLeadingPlus(chars) ;
            stripLeadingZeros(chars) ;
            stripTrailingZeros(chars) ;
            String lex2 = rebuild(chars) ;
            if ( lex2 == null )
                return node ;
            return NodeFactory.createLiteral(lex2, null, datatype) ;
        }
    } ;
}
