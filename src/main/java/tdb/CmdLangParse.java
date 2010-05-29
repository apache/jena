/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkCounting ;
import org.openjena.atlas.lib.SinkNull ;
import org.openjena.riot.Checker ;
import org.openjena.riot.ErrorHandlerLib ;
import org.openjena.riot.IRIResolver ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRDFXML ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.out.SinkQuadOutput ;
import org.openjena.riot.out.SinkTripleOutput ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;
import tdb.cmdline.CmdTDB ;
import tdb.cmdline.ModLangParse ;
import arq.cmd.CmdException ;
import arq.cmdline.CmdGeneral ;
import arq.cmdline.ModTime ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.TDB ;

/** Common framework for running RIOT parsers */
public abstract class CmdLangParse extends CmdGeneral
{
    // We are not a TDB command but still set the logging.
    static { CmdTDB.setLogging() ; }
    // Module.
    protected ModTime modTime                 = new ModTime() ;
    protected ModLangParse modLangParse       = new ModLangParse() ;
    
    interface LangHandler {
        String getItemsName() ;
        String getRateName() ;
    }

    static LangHandler langHandlerQuads = new LangHandler()
    {
        public String getItemsName()        { return "quads" ; }
        public String getRateName()         { return "QPS" ; }
    } ;
    static LangHandler langHandlerTriples = new LangHandler()
    {
        public String getItemsName()        { return "triples" ; }
        public String getRateName()         { return "TPS" ; }
    } ;
    static LangHandler langHandlerAny = new LangHandler()
    {
        public String getItemsName()        { return "tuples" ; }
        public String getRateName()         { return "TPS" ; }
    } ;
    
    protected static Map<Lang, LangHandler> dispatch = new HashMap<Lang, LangHandler>() ; 
    static {
        
        for ( Lang lang : Lang.values())
        {
            if ( lang.isQuads() )
                dispatch.put(lang, langHandlerQuads) ;
            else
                dispatch.put(lang, langHandlerTriples) ;
        }
    }
    
    protected LangHandler langHandlerOverall = null ;
    
    protected CmdLangParse(String[] argv)
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
        return getCommandName()+" [--time] [--check|--noCheck] [--sink] [--base=IRI] [--skip | --stopOnError] file ..." ;
    }

    @Override
    protected void processModulesAndArgs()
    { }

    protected long totalMillis = 0 ; 
    protected long totalTuples = 0 ; 
    

    @Override
    protected void exec()
    {
        try {
            if ( super.getPositional().isEmpty() )
                parseFile("-") ;
            else
            {
                for ( String fn : super.getPositional() )
                    parseFile(fn) ;
            }
        } finally {
            System.err.flush() ;
            System.out.flush() ;
            if ( super.getPositional().size() > 1 && modTime.timingEnabled() )
                output("Total", totalTuples, totalMillis, langHandlerOverall) ;
        }
    }

    public void parseFile(String filename)
    {
        InputStream in = null ;
        if ( filename.equals("-") )
            parseFile("http://base/", "stdin", System.in) ;
        else
        {
            try {
                in = IO.openFile(filename) ;
            } catch (Exception ex)
            {
                System.err.println("Can't open '"+filename+"' "+ex.getMessage()) ;
                return ;
            }
            parseFile(filename, filename, in) ;
            IO.close(in) ;
        }
    }

    public void parseFile(String defaultBaseURI, String filename, InputStream in)
    {   
        String baseURI = modLangParse.getBaseIRI() ;
        if ( baseURI == null )
            baseURI = defaultBaseURI ;
        // Make absolute
        baseURI = IRIResolver.resolveGlobalAsString(baseURI) ;
        parseRIOT(baseURI, filename, in) ;
    }
    
    protected abstract Lang selectLang(String filename, Lang nquads) ;

    protected void parseRIOT(String baseURI, String filename, InputStream in)
    {
        Checker checker = null ;
        if ( modLangParse.checking() )
        {
            if ( modLangParse.stopOnBadTerm() )
                checker = new Checker(ErrorHandlerLib.errorHandlerStd)  ;
            else
                checker = new Checker(ErrorHandlerLib.errorHandlerWarn) ;
        }
        
        Lang lang = selectLang(filename, Lang.NQUADS) ;  
        LangHandler handler = dispatch.get(lang) ;
        if ( handler == null )
            throw new CmdException("Undefined language: "+lang) ; 
        
        // If multiple files, choose the overall labels. 
        if ( langHandlerOverall == null )
            langHandlerOverall = handler ;
        else
        {
            if ( langHandlerOverall != langHandlerAny )
            {
                if ( langHandlerOverall != handler )
                    langHandlerOverall = langHandlerAny ;
            }
        }
        
        SinkCounting<?> sink ;
        LangRIOT parser ;
        
        // Uglyness because quads and triples aren't subtype of some Tuple<Node>
        // That would change a lot (Triples came several years before Quads). 
        if ( lang.isTriples() )
        {
            Sink <Triple> s = SinkNull.create() ;
            if ( ! modLangParse.toBitBucket() )
                s = new SinkTripleOutput(System.out) ;
            SinkCounting<Triple> sink2 = new SinkCounting<Triple>(s) ;
            if ( lang.equals(Lang.RDFXML) )
                // Adapter round ARP RDF/XML reader.
                parser = LangRDFXML.create(in, baseURI, filename, checker, sink2) ;
            else
                parser = RiotReader.createParserTriples(in, lang, baseURI, sink2) ;
            sink = sink2 ;
        }
        else
        {
            Sink <Quad> s = SinkNull.create() ;
            if ( ! modLangParse.toBitBucket() )
                s = new SinkQuadOutput(System.out) ;
            SinkCounting<Quad> sink2 = new SinkCounting<Quad>(s) ;
            parser = RiotReader.createParserQuads(in, lang, baseURI, sink2) ;
            sink = sink2 ;
        }
        
        modTime.startTimer() ;
        try
        {
            parser.parse() ;
        }
        catch (RiotException ex)
        {
            if ( modLangParse.stopOnBadTerm() )
                return ;
        }
        finally {
            IO.close(in) ;
            sink.close() ;
        }
        long x = modTime.endTimer() ;
        long n = sink.getCount() ;
        

        if ( modTime.timingEnabled() )
            output(filename, n, x, handler) ;
        
        totalMillis += x ;
        totalTuples += n ;
    }
    
    protected Tokenizer makeTokenizer(InputStream in)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(in) ;
        return tokenizer ;
    }
    
    protected void output(String label, long numberTriples, long timeMillis, LangHandler handler)
    {
        double timeSec = timeMillis/1000.0 ;
        
        System.out.printf("%s : %,5.2f sec  %,d %s  %,.2f %s\n",
                          label,
                          timeMillis/1000.0, numberTriples,
                          handler.getItemsName(),
                          timeSec == 0 ? 0.0 : numberTriples/timeSec,
                          handler.getRateName()) ;
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