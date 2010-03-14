/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;

import java.io.IOException;
import java.io.Writer;

import org.openjena.atlas.lib.BitsInt ;
import org.openjena.atlas.lib.Chars ;


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
        char charHex = Chars.hexDigits[y] ;
        out.append(charHex) ; 
        return BitsInt.clear(x, 4*i, 4*i+4) ;
    }
    
    /** Print the number x in width hex chars.  x must fit */
    public static void printHex(Writer out, int x, int width)
    {
        for ( int i = width-1 ; i >= 0 ; i-- )
            x = oneHex(out, x, i) ;
    }

    /** Print one hex digit of the numer */
    public static int oneHex(Writer out, int x, int i)
    {
        int y = BitsInt.unpack(x, 4*i, 4*i+4) ;
        char charHex = Chars.hexDigits[y] ;
        try { out.write(charHex) ; } catch (IOException ex) {} 
        return BitsInt.clear(x, 4*i, 4*i+4) ;
    }

    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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