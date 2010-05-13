/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.InputStream ;
import java.io.PrintStream ;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkCounting ;
import org.openjena.atlas.lib.SinkNull ;

import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.ErrorHandlerLib ;
import com.hp.hpl.jena.riot.RiotException ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;

/** Parser framework extension for a fixed language.  Temporary. */
abstract class LangParseFixed<X> extends LangParse
{
    protected LangParseFixed(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected void parseRIOT(String baseURI, String filename, InputStream in)
    {
        Tokenizer tokenizer = makeTokenizer(in) ;
        
        Sink<X> s = new SinkNull<X>() ;
        
        if ( ! modLangParse.toBitBucket() )
            s = makeOutputSink(System.out) ;
        
        SinkCounting<X> sink = new SinkCounting<X>(s) ;
        
        Checker checker = null ;
        if ( modLangParse.checking() )
        {
            if ( modLangParse.stopOnBadTerm() )
                checker = new Checker(ErrorHandlerLib.errorHandlerStd)  ;
            else
                checker = new Checker(ErrorHandlerLib.errorHandlerWarn) ;
        }
        
        modTime.startTimer() ;
        try
        {
            parseEngine(tokenizer, baseURI, sink, checker, modLangParse.skipOnBadTerm()) ;
        }
        catch (RiotException ex)
        {
            if ( modLangParse.stopOnBadTerm() )
                return ;
        }
        finally {
            tokenizer.close() ;
            s.close() ;
        }
        long x = modTime.endTimer() ;
        long n = sink.getCount() ;

        totalTuples += n ;
        totalMillis += x ;

        if ( modTime.timingEnabled() )
            output(filename, n, x) ;
    }

    protected abstract void parseEngine(Tokenizer tokens,
                                        String baseIRI,
                                        Sink<X> sink,
                                        Checker checker,
                                        boolean skipOnBadTerms) ;
                                        
    protected abstract Sink<X> makeOutputSink(PrintStream out) ;
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