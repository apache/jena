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

import java.io.OutputStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.process.inf.InfFactory;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDF2;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.SyntaxLabels;

import arq.cmd.CmdException;

import com.hp.hpl.jena.sparql.util.Utils;

/**
 * It's a command line tool for direct and scalable transforming from CSV to the formatted RDF syntax (i.e. N-Triples), 
 * with no intermediary Graph or PropertyTable.
 * 
 * It reuses the parsing functions from CmdLangParse and sinks the triples into the destination output file.
 *
 */
public class csv2rdf extends CmdLangParse{
	
	protected ModDest modDest = new ModDest() ;
	protected OutputStream destOut;

    public static void main(String... argv)
    {
        new csv2rdf(argv).mainRun() ;
    }    
    
    protected csv2rdf(String[] argv)
    {
        super(argv) ;
        super.addModule(modDest) ;
        
    }
	
	@Override
	protected Lang selectLang(String filename, ContentType contentType,
			Lang dftLang) {
		return RDFLanguages.CSV; 
	}

	@Override
	protected String getCommandName() {
		return Utils.classShortName(csv2rdf.class) ;
	}
	
    @Override
    protected String getSummary()
    {
        return getCommandName()+" --dest=outputFile inputFile ..." ;
    }

	// override the original CmdLangParse.parseRIOT()
    protected void parseRIOT(String baseURI, String filename, TypedInputStream in)
    {
    	
    	String dest = modDest.getDest();
    	LocatorOupputFile l = new LocatorOupputFile();
    	destOut = l.open(dest);
    	
    	if (destOut == null){
            System.err.println("Can't write to destination output file: '"+dest+"' ") ;
            return ;
    	}
    	
        // I ti s shame we effectively duplicate deciding thelnaguage but we want to control the
        // pasrer at a deep level (in validation, we want line numbers get into error message)
        // This code predates RDFDataMgr.
        
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
        
        NodeToLabel labels = SyntaxLabels.createNodeToLabel() ;
        if ( labelsAsGiven )
            labels = NodeToLabel.createBNodeByLabelEncoded() ;
        
        StreamRDF s = StreamRDFLib.sinkNull() ;
        if ( ! modLangParse.toBitBucket() )
            s = StreamRDFLib.writer(output) ;
        
        // add dest output
        if ( destOut != null)
        	s = new StreamRDF2(s,  StreamRDFLib.writer(destOut));
        
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
            reader.read(in, baseURI, ct, sink, null) ;
        } catch (RiotException ex) {
            // Should have handled the exception and logged a message by now.
            // System.err.println("++++"+ex.getMessage());

            if ( modLangParse.stopOnBadTerm() )
                return ;
        } finally {
            // Not close - we may write again to the underlying output stream in another call to parse a file.  
            sink.finish() ;
            IO.close(in) ;
        }
        long x = modTime.endTimer() ;
        long n = sink.countTriples()+sink.countQuads() ;

        if ( modTime.timingEnabled() )
            output(filename, n, x, handler) ;
        
        totalMillis += x ;
        totalTuples += n ;
    }
}
