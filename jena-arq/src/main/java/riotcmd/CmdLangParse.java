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

import java.io.InputStream ;
import java.io.OutputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.process.inf.InfFactory ;
import org.apache.jena.riot.process.inf.InferenceSetupRDFS ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import arq.cmd.CmdException ;
import arq.cmdline.* ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.query.ARQ ;

/** Common framework for running RIOT parsers */
public abstract class CmdLangParse extends CmdGeneral
{
    protected ModTime modTime                   = new ModTime() ;
    protected ModLangParse modLangParse         = new ModLangParse() ;
    protected ModLangOutput modLangOutput       = new ModLangOutput() ;
    protected ModSymbol modSymbol               = new ModSymbol() ;
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
    
    protected LangHandler langHandlerOverall = null ;

    protected CmdLangParse(String[] argv)
    {
        super(argv) ;
        
        super.addModule(modSymbol) ;
        super.addModule(modTime) ;
        super.addModule(modLangOutput) ;
        super.addModule(modLangParse) ;
        
        super.modVersion.addClass(Jena.class) ;
        super.modVersion.addClass(ARQ.class) ;
        super.modVersion.addClass(RIOT.class) ;
        
        
    }

    @Override
    protected String getSummary()
    {
        //return getCommandName()+" [--time] [--check|--noCheck] [--sink] [--base=IRI] [--skip | --stopOnError] file ..." ;
        return getCommandName()+" [--time] [--check|--noCheck] [--sink] [--base=IRI] [--out=FORMAT] file ..." ;
    }

    protected long totalMillis = 0 ; 
    protected long totalTuples = 0 ; 
    
    OutputStream output = System.out ;
    StreamRDF outputStream = null ;
    

    @Override
    protected void processModulesAndArgs() {}
    
    @Override
    protected void exec()
    {
        if ( modLangParse.strictMode() )
            RIOT.setStrictMode(true) ; 
        
        if ( modLangParse.getRDFSVocab() != null )
            setup = new InferenceSetupRDFS(modLangParse.getRDFSVocab()) ;
     
        outputStream = createSink() ;
        
        try {
            if ( super.getPositional().isEmpty() )
                parseFile("-") ;
            else
            {
                boolean b = super.getPositional().size() > 1 ;
                for ( String fn : super.getPositional() )
                {
                    if ( b && ! super.isQuiet() )
                        SysRIOT.getLogger().info("File: "+fn) ;
                    parseFile(fn) ;
                }
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
        TypedInputStream in = null ;
        if ( filename.equals("-") ) {
            in = new TypedInputStream(System.in) ;
            parseFile("http://base/", "stdin", in) ;
        } else {
            try {
                in = RDFDataMgr.open(filename) ;
            } catch (Exception ex) {
                System.err.println("Can't open '"+filename+"' "+ex.getMessage()) ;
                return ;
            }
            parseFile(null, filename, in) ;
            IO.close(in) ;
        }
    }

    public void parseFile(String defaultBaseURI, String filename, TypedInputStream in)
    {   
        String baseURI = modLangParse.getBaseIRI() ;
        if ( baseURI == null )
            baseURI = defaultBaseURI ;
        parseRIOT(baseURI, filename, in) ;
    }
    
    protected abstract Lang selectLang(String filename, ContentType contentType, Lang dftLang  ) ;

    protected void parseRIOT(String baseURI, String filename, TypedInputStream in)
    {
        ContentType ct = in.getMediaType() ;
        
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
        
        Lang lang = selectLang(filename, ct, RDFLanguages.NQUADS) ;  
        LangHandler handler = null ;
        if ( RDFLanguages.isQuads(lang) )
            handler = langHandlerQuads ;
        else if ( RDFLanguages.isTriples(lang) )
            handler = langHandlerTriples ;
        else 
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

        NodeToLabel labels = SyntaxLabels.createNodeToLabel() ;
        if ( labelsAsGiven )
            labels = NodeToLabel.createBNodeByLabelEncoded() ;
        
        StreamRDF s = outputStream ; 
        if ( setup != null )
            s = InfFactory.inf(s, setup) ;
        StreamRDFCounting sink = StreamRDFLib.count(s) ;
        s = null ;
        
        ReaderRIOT reader = RDFDataMgr.createReader(lang) ;
        try {
            if ( checking ) {
                if ( lang == RDFLanguages.NTRIPLES || lang == RDFLanguages.NQUADS )
                    reader.setParserProfile(RiotLib.profile(baseURI, false, true, errHandler)) ;
                else
                    reader.setParserProfile(RiotLib.profile(baseURI, true, true, errHandler)) ;
            } else
                reader.setParserProfile(RiotLib.profile(baseURI, false, false, errHandler)) ;

            if ( labelsAsGiven )
                reader.getParserProfile().setLabelToNode(LabelToNode.createUseLabelAsGiven()) ;
            modTime.startTimer() ;
            sink.start() ;
            reader.read(in, baseURI, ct, sink, null) ;
            sink.finish() ;
        } catch (RiotException ex) {
            // Should have handled the exception and logged a message by now.
            // System.err.println("++++"+ex.getMessage());

            if ( modLangParse.stopOnBadTerm() )
                return ;
        } finally {
            // Not close the output - we may write again to the underlying output stream in another call to parse a file.  
            IO.close(in) ;
        }
        long x = modTime.endTimer() ;
        long n = sink.countTriples()+sink.countQuads() ;

        if ( modTime.timingEnabled() )
            output(filename, n, x, handler) ;
        
        totalMillis += x ;
        totalTuples += n ;
    }
    
    protected StreamRDF createSink() {
        if ( modLangParse.toBitBucket() )
            return StreamRDFLib.sinkNull() ;
        RDFFormat fmt = modLangOutput.getOutputFormat() ;
        return StreamRDFWriter.getWriterStream(System.out, fmt) ;
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
