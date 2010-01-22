/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.InputStream ;

import atlas.io.CharStream ;
import atlas.io.InputStreamBuffered ;
import atlas.io.PeekInputStream ;
import atlas.io.PeekReader ;
import atlas.io.StreamASCII ;
import atlas.lib.SinkCounting ;
import atlas.logging.Log ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.lang.LangNTriples ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerBytes ;
import com.hp.hpl.jena.riot.tokens.TokenizerText ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class ntriples extends LangParse
{
    /** Run the N-triples parser - and produce N-triples */
    public static void main(String... argv)
    {
        Log.setLog4j() ;
        new ntriples(argv).mainRun() ;
    }        

    protected ntriples(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected Tokenizer makeTokenizer(InputStream in)
    {
        if ( false )
        {
            // Hardwired byte parser.
            // This is the fastest way but uses a cheat for bytes to chars
            // About 20% faster.
            PeekInputStream pin = PeekInputStream.make(in) ;
            // This cheats. 
            Tokenizer tokenizer = new TokenizerBytes(pin) ;
            return tokenizer ;
        }
        if ( false )
        {
            // ASCII
            InputStream in2 = new InputStreamBuffered(in) ;
            CharStream cs = new StreamASCII(in2) ;
            PeekReader peekReader = PeekReader.make(cs) ;
            Tokenizer tokenizer = new TokenizerText(peekReader) ;
            return tokenizer ;
        }
        PeekReader peekReader = PeekReader.makeUTF8(in) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }

    
    @Override
    protected String getCommandName()
    {
        return Utils.classShortName(ntriples.class) ;
    }

    @Override
    protected void parseEngine(Tokenizer tokenizer, SinkCounting<Triple> sink, String baseURI)
    {
        LangNTriples parser = new LangNTriples(tokenizer, sink) ;
        Checker checker = new Checker(null) ;
        parser.setChecker(checker) ;
        parser.parse();
        sink.close() ;
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