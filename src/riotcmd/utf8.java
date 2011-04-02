/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package riotcmd;

import java.io.IOException ;
import java.io.InputStream ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.InputStreamBuffered ;
import org.openjena.atlas.io.InStreamUTF8 ;

public class utf8
{
    /** Simple program to help hunt down bad UTF-8 encoded characters */
    public static void main(String[] args)
    {
        long INIT_LINE = 1 ; 
        long INIT_COL = 1 ; 

        if ( args.length == 0 )
            args = new String[] {"-"} ;
        
        String label = "" ;
        for ( String fn : args )
        {
            if ( args.length > 1 )
                label = fn+": " ;
            InputStream in = IO.openFile(fn) ;
            in = new InputStreamBuffered(in) ;
            
            long charCount = 0 ;
            long lineNum = INIT_LINE ;
            long colNum = INIT_COL ;
            
            try {
                InStreamUTF8 utf8 = new InStreamUTF8(in) ;
                for (;;) 
                {
                    int ch = utf8.read() ;
                    if ( ch == -1 )
                        break ;
                    charCount++ ;
                    if (ch == '\n')
                    {
                        lineNum++;
                        colNum = INIT_COL ;
                    } 
                    else
                        colNum++;
                    if ( ! Character.isDefined(ch) )
                        throw new AtlasException(String.format("No such codepoint: 0x%04X", ch)) ;
                }
                System.out.printf("%s: chars = %d , lines = %d\n", fn, charCount, lineNum) ;
            } catch (AtlasException ex)
            {
                System.out.printf(label+"[line=%d, col=%d] %s\n", lineNum, colNum, ex.getMessage()) ;
            }
            catch (IOException ex)
            {
                ex.printStackTrace(System.err) ;
            }
        }
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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