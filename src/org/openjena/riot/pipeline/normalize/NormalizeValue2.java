/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.pipeline.normalize;

import static org.openjena.atlas.lib.Chars.CH_DOT ;
import static org.openjena.atlas.lib.Chars.CH_MINUS ;
import static org.openjena.atlas.lib.Chars.CH_PLUS ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;

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
            ;
        
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
            return Node.createLiteral(lex2, null, datatype) ;
        }
    } ;

    static DatatypeHandler dtDecimal = new DatatypeHandler() {
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
            return Node.createLiteral(lex2, null, datatype) ;
        }
    } ;
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
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