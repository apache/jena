/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package riotcmd;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.sparql.util.Timer ;

public class CmdTokens
{
    
    public static void tokens(final boolean print, final boolean timing, String...args)
    {
        // Turn the node cache off.
        //com.hp.hpl.jena.graph.Node.cache(false) ;

        if ( args.length == 0 )
            args = new String[] {"-"} ;

        String arg = args[0] ;

        if ( arg.equals("--help") || arg.equals("-help") || arg.equals("-h") || arg.equals("--h") ) 
        {
            System.err.println("Usage: stdin | FILE ...") ;
            System.exit(1) ;
        }
        for ( String filename : args )
        {
            InputStream in = IO.openFile(args[0]) ;
            Tokenizer tokenize = TokenizerFactory.makeTokenizerUTF8(in) ;
            Timer timer = new Timer() ;
            long count = 0 ; 
            timer.startTimer() ;
            for ( ; tokenize.hasNext() ; )
            {
                Token t = tokenize.next() ;
                if ( print )
                    System.out.println(t) ;
                count++ ;
            }
            tokenize.close();
            long millis = timer.endTimer() ;
            if ( timing )
            {
                if ( millis == 0 )
                    System.out.printf("Tokens=%,d : Time=0.00s\n", count) ;
                else
                {
                    double seconds = millis/1000.0 ;
                    System.out.printf("Tokens=%,d : Time=%,.2fs : Rate=%,.2f\n", count, seconds, count/seconds) ;
                }
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