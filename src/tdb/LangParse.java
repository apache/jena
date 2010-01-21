/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.InputStream ;

import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdGeneral ;
import arq.cmdline.ModTime ;
import atlas.io.IO ;
import atlas.io.PeekReader ;
import atlas.lib.Sink ;
import atlas.lib.SinkCounting ;
import atlas.lib.SinkNull ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.out.SinkTripleOutput ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerText ;

/** Common framework for running RIOT parsers */
public abstract class LangParse extends CmdGeneral
{

    private ModTime modTime = new ModTime() ;
    private ArgDecl argSink = new ArgDecl(ArgDecl.NoValue, "sink", "null") ;
    private boolean bitbucket = false ; 
    
    protected LangParse(String[] argv)
    {
        super(argv) ;
        super.addModule(modTime) ;
        super.add(argSink) ;
    }


    @Override
    protected String getSummary()
    {
        return getCommandName()+" [--time] file ..." ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        bitbucket = super.contains(argSink) ; 
    }

    private long totalMillis = 0 ; 
    private long totalTriples = 0 ; 
    

    @Override
    protected void exec()
    {
        if ( super.getPositional().isEmpty() )
            parse("-") ;
        else
        {
            for ( String fn : super.getPositional() )
                parse(fn) ;
        }
        if ( super.getPositional().size() > 1 && modTime.timingEnabled() )
            output("Total", totalTriples, totalMillis) ;
    }

    public void parse(String filename)
    {
        InputStream in = null ;
        if ( filename.equals("-") )
            parse("http://base/", "stdin", System.in) ;
        else
        {
            try {
                in = IO.openFile(filename) ;
            } catch (Exception ex)
            {
                System.err.println("Can't open '"+filename+"' "+ex.getMessage()) ;
                return ;
            }
            parse(filename, filename, in) ;
        }
    }

    public void parse(String baseURI, String filename, InputStream in)
    {   
        parseRIOT(baseURI, filename, in) ;
    }

    
    public void parseRIOT(String baseURI, String filename, InputStream in)
    {
        Tokenizer tokenizer = makeTokenizer(in) ;
        
        Sink<Triple> s = new SinkNull<Triple>() ;
        
        if ( ! bitbucket )
            s = new SinkTripleOutput(System.out) ;
        
        SinkCounting<Triple> sink = new SinkCounting<Triple>(s) ;
        
        
        modTime.startTimer() ;
        parseEngine(tokenizer, sink, baseURI) ;
        long x = modTime.endTimer() ;
        long n = sink.getCount() ;

        totalTriples += n ;
        totalMillis += x ;
        
        if ( modTime.timingEnabled() )
            output(filename, n, x) ;
    }

    protected Tokenizer makeTokenizer(InputStream in)
    {
//      PeekInputStream pin = PeekInputStream.make(in) ;
//      Tokenizer tokenizer = new TokenizerBytes(pin) ;
        PeekReader peekReader = PeekReader.makeUTF8(in) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }


    protected abstract void parseEngine(Tokenizer tokenizer, SinkCounting<Triple> sink, String baseURI) ;


    private static void output(String label, long numberTriples, long timeMillis)
    {
        double timeSec = timeMillis/1000.0 ;
        
        System.out.printf(label+" : %,.2f sec  %,d triples  %,.2f TPS\n",
                          timeMillis/1000.0, numberTriples,
                          timeSec == 0 ? 0.0 : numberTriples/timeSec ) ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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