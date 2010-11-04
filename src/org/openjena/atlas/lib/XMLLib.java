/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

/** Operations in somewhay related to XML */
public class XMLLib
{
    /** Trim the XML whitespace characters strictly needed for whitespace facet collapse.
     * This <b>not</b> full whitespace facet collapse, which also requires processing of
     * internal spaces.  Because none of the datatypes that have whitespace facet
     * collapse and have values we extract can legally contain internal whitespace,
     * we just need to trim the string.
     * 
     *  Java String.trim removes any characters less than 0x20. 
     */
    static String WScollapse(String string)
    {
        int len = string.length();
        if ( len == 0 )
            return string ;
        
        if ( (string.charAt(0) > 0x20) && (string.charAt(len-1) > 0x20) )
            return string ;

        int idx1 = 0 ;
        for ( ; idx1 < len ; idx1++ )
        {
            char ch = string.charAt(idx1) ;
            if ( ! testForWS(ch) )
                break ;
        }
        int idx2 = len-1 ;
        for ( ; idx2 > idx1 ; idx2-- )
        {
            char ch = string.charAt(idx2) ;
            if ( ! testForWS(ch) )
                break ;
        }
        return string.substring(idx1, idx2+1) ;
    }

    private static boolean testForWS(char ch)
    {
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' ; 
    }
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