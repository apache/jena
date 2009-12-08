/*
 * (c) Copyright 2009 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package perf;

import java.io.Reader ;

import atlas.io.IO ;
import atlas.io.PeekReader ;
import atlas.lib.SinkCounting ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.lang.LangNTriples ;
import com.hp.hpl.jena.riot.lang.LangRIOT ;
import com.hp.hpl.jena.riot.lang.LangTurtle ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerText ;
import com.hp.hpl.jena.sparql.util.Timer ;

public class Performance
{
    static public void tokenizer(String filename)
    {
        Reader r = IO.openFileUTF8(filename) ;
        PeekReader pr = PeekReader.make(r) ;
        timeTokenizer("tokenizer("+filename+")", pr) ;
    }

    static public void tokenizerAscii(String filename)
    {
        Reader r = IO.openFileASCII(filename) ;
        PeekReader pr = PeekReader.make(r) ;
        timeTokenizer("tokenizerAscii("+filename+")", pr) ;
    }

    static public void timeTokenizer(String message, PeekReader peekReader) 
    {
        if ( message != null )
            System.out.println(message) ;
        
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        long count = 0 ;

        for ( ; tokenizer.hasNext() ; )
        {
            count++ ;
            tokenizer.next() ;
        }
        
        long time = timer.endTimer() ;
        System.out.printf("Tokens: %,d\n", count) ;
        System.out.printf("Time:   %.2fs\n", time/1000.0) ;
        System.out.printf("Tokens per second: %,.02f\n", count/( time/1000.0)) ;
    }
    
    static public void ntriples(String filename)
    {
        System.out.println("N-Triples("+filename+")") ;
        Reader r = IO.openFileUTF8(filename) ;
        PeekReader peekReader = PeekReader.make(r) ;

        SinkCounting<Triple> sink = new SinkCounting<Triple>() ; 
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        LangNTriples parser = new LangNTriples(tokenizer, sink) ;

        parser.setChecker(null) ;
        
        Timer timer = new Timer() ;
        timer.startTimer() ;

        parser.parse();
        sink.close() ;

        long time = timer.endTimer() ;
        System.out.printf("Triples: %,d\n", sink.count) ;
        System.out.printf("Time:    %.2fs\n", time/1000.0) ;
        System.out.printf("Triples per second: %,.02f\n", sink.count/( time/1000.0)) ;
    }
    
    static public void turtle(String filename)
    {
        System.out.println("Turtle("+filename+")") ;
        Reader r = IO.openFileUTF8(filename) ;
        PeekReader peekReader = PeekReader.make(r) ;

        SinkCounting<Triple> sink = new SinkCounting<Triple>() ; 
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        LangRIOT parser = new LangTurtle(null, tokenizer, sink) ;

        parser.setChecker(null) ;
        
        Timer timer = new Timer() ;
        timer.startTimer() ;

        parser.parse();
        sink.close() ;

        long time = timer.endTimer() ;
        System.out.printf("Triples: %,d\n", sink.count) ;
        System.out.printf("Time:    %.2fs\n", time/1000.0) ;
        System.out.printf("Triples per second: %,.02f\n", sink.count/( time/1000.0)) ;
    }
}

/*
 * (c) Copyright 2009 Talis Information Ltd
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