/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.InputStream ;
import java.io.PrintStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkCounting ;
import org.openjena.atlas.lib.SinkNull ;
import org.openjena.atlas.logging.Log ;

import tdb.cmdline.ModLangParse ;
import arq.cmdline.CmdGeneral ;
import arq.cmdline.ModTime ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.ErrorHandlerLib ;
import com.hp.hpl.jena.riot.RiotException ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerFactory ;
import com.hp.hpl.jena.tdb.TDB ;

/** Common framework for running RIOT parsers */
public abstract class LangParse<X> extends CmdGeneral
{
    static { Log.setLog4j() ; }
    // Module.
    protected ModTime modTime                 = new ModTime() ;
    protected ModLangParse modLangParse       = new ModLangParse() ;
    
    protected LangParse(String[] argv)
    {
        super(argv) ;
        super.addModule(modTime) ;
        super.addModule(modLangParse) ;
        
        super.modVersion.addClass(Jena.class) ;
        super.modVersion.addClass(ARQ.class) ;
        super.modVersion.addClass(TDB.class) ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" [--time] [--check|--noCheck] [--sink] [--skip | --stopOnError] file ..." ;
    }

    @Override
    protected void processModulesAndArgs()
    { }

    private long totalMillis = 0 ; 
    private long totalTriples = 0 ; 
    

    @Override
    protected void exec()
    {
        try {
            if ( super.getPositional().isEmpty() )
                parse("-") ;
            else
            {
                for ( String fn : super.getPositional() )
                {
                    try {
                        parse(fn) ;
                    } catch (RiotException ex)
                    {
                        if ( ! modLangParse.stopOnBadTerm() )
                        {
                            // otherwise the checker sent the error message  
                            System.err.println(ex.getMessage()) ;
                            //ex.printStackTrace(System.err) ;
                        }
                        return ;
                    }
                }
            }
        } finally {
            System.err.flush() ;
            System.out.flush() ;
            if ( super.getPositional().size() > 1 && modTime.timingEnabled() )
                output("Total", totalTriples, totalMillis) ;
        }
        
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
        
        Sink<X> s = new SinkNull<X>() ;
        
        if ( ! modLangParse.toBitBucket() )
            s =  makePrintSink(System.out) ;
        
        SinkCounting<X> sink = new SinkCounting<X>(s) ;
        
        modTime.startTimer() ;
        
        Checker checker = null ;
        if ( modLangParse.checking() )
        {
            // Skip on bad terms is done include the N-Tuples parser
            // Not available for Turtle etc.
            if ( modLangParse.stopOnBadTerm() )
                checker = new Checker(ErrorHandlerLib.errorHandlerStd)  ;
            else
                checker = new Checker(ErrorHandlerLib.errorHandlerWarn) ;
        }
        
        try
        {
            parseEngine(tokenizer, baseURI, sink, checker, modLangParse.skipOnBadTerm()) ;
        }
        finally {
            tokenizer.close() ;
            s.close() ;
        }
        
        long x = modTime.endTimer() ;
        long n = sink.getCount() ;

        totalTriples += n ;
        totalMillis += x ;
        
        if ( modTime.timingEnabled() )
            output(filename, n, x) ;
    }

    protected Tokenizer makeTokenizer(InputStream in)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(in) ;
        return tokenizer ;
    }


    protected abstract void parseEngine(Tokenizer tokens,
                                        String baseIRI,
                                        Sink<X> sink,
                                        Checker checker,
                                        boolean skipOnBadTerms) ;
                                        
    protected abstract Sink<X> makePrintSink(PrintStream out) ;
    

    private static void output(String label, long numberTriples, long timeMillis)
    {
        double timeSec = timeMillis/1000.0 ;
        
        System.out.printf(label+" : %,5.2f sec  %,d triples  %,.2f TPS\n",
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