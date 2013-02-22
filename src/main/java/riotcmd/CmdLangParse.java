/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package riotcmd;

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Properties ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.atlas.lib.SinkCounting ;
import org.apache.jena.atlas.lib.SinkNull ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.out.SinkQuadOutput ;
import org.apache.jena.riot.out.SinkTripleOutput ;
import org.apache.jena.riot.process.inf.InfFactory ;
import org.apache.jena.riot.process.inf.InferenceSetupRDFS ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.apache.log4j.PropertyConfigurator ;
import arq.cmd.CmdException ;
import arq.cmdline.* ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Common framework for running RIOT parsers */
public abstract class CmdLangParse extends CmdGeneral
{
    // We are not a TDB command but still set the logging.
    //static { CmdTDB.setLogging() ; }
    // Module.
    protected ModTime modTime                   = new ModTime() ;
    protected ModLangParse modLangParse         = new ModLangParse() ;
    protected ModSymbol modSymbol               = new ModSymbol() ;
    protected ArgDecl argStrict                 = new ArgDecl(ArgDecl.NoValue, "strict") ;    
    protected InferenceSetupRDFS setup          = null ; 
    
    interface LangHandler {
        String getItemsName() ;
        String getRateName() ;
    }

    static LangHandler langHandlerQuads = new LangHandler()
    {
        @Override
        public String getItemsName()        { return "quads" ; }
        @Override
        public String getRateName()         { return "QPS" ; }
    } ;
    static LangHandler langHandlerTriples = new LangHandler()
    {
        @Override
        public String getItemsName()        { return "triples" ; }
        @Override
        public String getRateName()         { return "TPS" ; }
    } ;
    static LangHandler langHandlerAny = new LangHandler()
    {
        @Override
        public String getItemsName()        { return "tuples" ; }
        @Override
        public String getRateName()         { return "TPS" ; }
    } ;
    
    protected static Map<Lang, LangHandler> dispatch = new HashMap<Lang, LangHandler>() ; 
    static {
        for ( Lang lang : RDFLanguages.getRegisteredLanguages() )
        {
            if ( RDFLanguages.isQuads(lang) )
                dispatch.put(lang, langHandlerQuads) ;
            else
                dispatch.put(lang, langHandlerTriples) ;
        }
    }
    
    protected LangHandler langHandlerOverall = null ;

    // This is the setup for command for their message via the logging in ErrorHandlers
    private static final String log4Jsetup = StrUtils.strjoin("\n"
//                    , "## Plain output to stdout"
//                    , "log4j.appender.riot.plain=org.apache.log4j.ConsoleAppender"
//                    , "log4j.appender.riot.plain.target=System.out"
//                    , "log4j.appender.riot.plain.layout=org.apache.log4j.PatternLayout"
//                    , "log4j.appender.riot.plain.layout.ConversionPattern=%m%n"
                    , "## Plain output to stderr"
                    , "log4j.appender.riot.plainerr=org.apache.log4j.ConsoleAppender"
                    , "log4j.appender.riot.plainerr.target=System.err"
                    , "log4j.appender.riot.plainerr.layout=org.apache.log4j.PatternLayout"
                    , "log4j.appender.riot.plainerr.layout.ConversionPattern=%-5p %m%n"
                    , "## Everything"
                    , "log4j.rootLogger=INFO, riot.plainerr"
                    , "## Parser output"
                    , "log4j.additivity."+SysRIOT.riotLoggerName+"=false"
                    , "log4j.logger."+SysRIOT.riotLoggerName+"=ALL, riot.plainerr "
     ) ;

    /** Reset the logging to be good for command line tools */
    public static void setLogging()
    {
        // Use a plain logger for output. 
        Properties p = new Properties() ;
        InputStream in = new ByteArrayInputStream(StrUtils.asUTF8bytes(log4Jsetup)) ;
        try { p.load(in) ; } catch (IOException ex) {}
        PropertyConfigurator.configure(p) ;
        //LogManager.getLogger(SysRIOT.riotLoggerName).setLevel(Level.ALL) ;
        System.setProperty("log4j.configuration", "set") ;
    }
    
    protected CmdLangParse(String[] argv)
    {
        super(argv) ;
        // As a command, we take control of logging ourselves. 
        setLogging() ;
        
        
        
        super.addModule(modTime) ;
        super.addModule(modLangParse) ;
        super.addModule(modSymbol) ;
        
        super.modVersion.addClass(Jena.class) ;
        super.modVersion.addClass(ARQ.class) ;
        super.modVersion.addClass(RIOT.class) ;
    }

    @Override
    protected String getSummary()
    {
        //return getCommandName()+" [--time] [--check|--noCheck] [--sink] [--base=IRI] [--skip | --stopOnError] file ..." ;
        return getCommandName()+" [--time] [--check|--noCheck] [--sink] [--base=IRI] file ..." ;
    }

    @Override
    protected void processModulesAndArgs()
    { 
        if ( this.contains(argStrict) )
            RIOT.setStrictMode(true) ;
    }

    protected long totalMillis = 0 ; 
    protected long totalTuples = 0 ; 
    
    OutputStream output = System.out ;
    
    @Override
    protected void exec()
    {
        if ( modLangParse.strictMode() )
            RIOT.setStrictMode(true) ; 
        
        if ( modLangParse.getRDFSVocab() != null )
            setup = new InferenceSetupRDFS(modLangParse.getRDFSVocab()) ;
     
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
            parseFile(null, filename, in) ;
            IO.close(in) ;
        }
    }

    public void parseFile(String defaultBaseURI, String filename, InputStream in)
    {   
        String baseURI = modLangParse.getBaseIRI() ;
        if ( baseURI == null )
            baseURI = defaultBaseURI ;
        parseRIOT(baseURI, filename, in) ;
    }
    
    protected abstract Lang selectLang(String filename, Lang dftLang) ;

    protected void parseRIOT(String baseURI, String filename, InputStream in)
    {
        baseURI = SysRIOT.chooseBaseIRI(baseURI, filename) ;
        
        boolean checking = true ;
        if ( modLangParse.explicitChecking() )  checking = true ;
        if ( modLangParse.explicitNoChecking() ) checking = false ;
        
        ErrorHandler errHandler = null ;
        if ( checking )
        {
            if ( modLangParse.stopOnBadTerm() )
                errHandler = ErrorHandlerFactory.errorHandlerStd  ;
            else
                // Try to go on if possible.  This is the default behaviour.
                errHandler = ErrorHandlerFactory.errorHandlerWarn ;
        }
        
        if ( modLangParse.skipOnBadTerm() )
        {
            // TODO skipOnBadterm
        }
        
        Lang lang = selectLang(filename, RDFLanguages.NQUADS) ;  
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
        
        // Make a flag.
        // Input and output subflags.
        // If input is "label, then output using NodeToLabel.createBNodeByLabelRaw() ;
        // else use NodeToLabel.createBNodeByLabel() ;
        // Also, as URI.
        final boolean labelsAsGiven = false ;
        
        SinkCounting<?> sink ;
        LangRIOT parser ;
        
        NodeToLabel labels = SyntaxLabels.createNodeToLabel() ;
        if ( labelsAsGiven )
            labels = NodeToLabel.createBNodeByLabelEncoded() ;
        
        // Uglyness because quads and triples aren't subtype of some Tuple<Node>
        // Replace with StreamRDF all the way through.
        
        if ( RDFLanguages.isTriples(lang) )
        {
            Sink<Triple> s = SinkNull.create() ;
            if ( ! modLangParse.toBitBucket() )
                s = new SinkTripleOutput(output, null, labels) ;
            if ( setup != null )
                s = InfFactory.infTriples(s, setup) ;
            
            SinkCounting<Triple> sink2 = new SinkCounting<Triple>(s) ;
            StreamRDF dest = StreamRDFLib.sinkTriples(sink2) ;
            parser = RiotReader.createParser(in, lang, baseURI, dest) ;
            
            sink = sink2 ;
        }
        else
        {
            Sink <Quad> s = SinkNull.create() ;
            if ( ! modLangParse.toBitBucket() )
                s = new SinkQuadOutput(output, null, labels) ;
            if ( setup != null )
                s = InfFactory.infQuads(s, setup) ;
            
            SinkCounting<Quad> sink2 = new SinkCounting<Quad>(s) ;
            StreamRDF dest = StreamRDFLib.sinkQuads(sink2) ;
            parser = RiotReader.createParser(in, lang, baseURI, dest) ;
            sink = sink2 ;
        }
        
        try
        {
            if ( checking )
            {
                if ( parser.getLang() == RDFLanguages.NTRIPLES ||  parser.getLang() == RDFLanguages.NQUADS )
                    parser.setProfile(RiotLib.profile(baseURI, false, true, errHandler)) ;
                else
                    parser.setProfile(RiotLib.profile(baseURI, true, true, errHandler)) ;
            }
            else
                parser.setProfile(RiotLib.profile(baseURI, false, false, errHandler)) ;
            
            if ( labelsAsGiven )
                parser.getProfile().setLabelToNode(LabelToNode.createUseLabelAsGiven()) ;
            
            modTime.startTimer() ;
            parser.parse() ;
        }
        catch (RiotException ex)
        {
            // Should have handled the exception and logged a message by now.
            //System.err.println("++++"+ex.getMessage()); 
           
            if ( modLangParse.stopOnBadTerm() )
                return ;
        }
        finally {
            IO.close(in) ;
            // Not close - we may write again to the underlying output stream in another call to parse a file.  
            sink.flush() ;
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
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in) ;
        return tokenizer ;
    }
    
    protected void output(String label, long numberTriples, long timeMillis, LangHandler handler)
    {
        double timeSec = timeMillis/1000.0 ;
        
        System.out.flush() ;
        System.err.printf("%s : %,5.2f sec  %,d %s  %,.2f %s\n",
                          label,
                          timeMillis/1000.0, numberTriples,
                          handler.getItemsName(),
                          timeSec == 0 ? 0.0 : numberTriples/timeSec,
                          handler.getRateName()) ;
    }
    
    protected void output(String label)
    {
        System.err.printf("%s : \n", label) ;
    }
}
