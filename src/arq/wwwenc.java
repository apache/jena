/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.io.UnsupportedEncodingException ;

import org.openjena.atlas.lib.StrUtils ;


public class wwwenc
{
    /* http://en.wikipedia.org/wiki/Percent-encoding
     * Reserved characters after percent-encoding 
     *   !    *   "   '   (   )   ;   :   @   &   =   +   $   ,   /   ?   %   #   [   ]
     *   %21  %2A %22 %27 %28 %29 %3B %3A %40 %26 %3D %2B %24 %2C %2F %3F %25 %23 %5B %5D
     * These loose any reserved meaning if encoded.
     *   
     * Other common, but unreserved, characters after percent-encoding 
     *   <   >   ~   .   {   }   |   \   -   `   _   ^
     *   %3C %3E %7E %2E %7B %7D %7C %5C %2D %60 %5F %5E
     * 
     * Unreserved characters treated equivalent to their unencoded form.  
     *   
     *   
     */
    public static void main(String...args) throws UnsupportedEncodingException
    {
        // Reserved characters + space
        char reserved[] = 
            {' ',
             '\n','\t',
             '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
             '=', '+', '$', ',', '/', '?', '%', '#', '[', ']'} ;
        
        char[] other = {'<', '>', '~', '.', '{', '}', '|', '\\', '-', '`', '_', '^'} ;        
        
        for ( String x : args)
        {
            // Not URLEncoder which does www-form-encoding.
            String y = StrUtils.encodeHex(x, '%', reserved) ;
            System.out.println(y) ;
            
//            String s2 = URLEncoder.encode(s, "utf-8") ;
//            System.out.println(s2) ;

        }
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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