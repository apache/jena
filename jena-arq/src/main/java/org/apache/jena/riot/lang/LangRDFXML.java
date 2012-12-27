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

package org.apache.jena.riot.lang;

import static com.hp.hpl.jena.rdf.arp.ARPErrorNumbers.EM_FATAL ;
import static com.hp.hpl.jena.rdf.arp.ARPErrorNumbers.WARN_MALFORMED_XMLLANG ;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.checker.CheckerLiterals ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.SinkRDF ;
import org.xml.sax.SAXException ;
import org.xml.sax.SAXParseException ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.arp.* ;
import com.hp.hpl.jena.rdf.arp.impl.ARPSaxErrorHandler ;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler ;

public class LangRDFXML implements LangRIOT
{
    // This is not a full member of the RIOT suite because it needs to work
    // with Xerces and already carries out it's own error handling and output
    
    private ARP arp = new ARP() ;
    
    private InputStream input = null ;
    private Reader reader = null ;
    private String xmlBase ;
    private String filename ;
    private SinkRDF sink ;
    private ParserProfile profile ;             // Warning - we don't use all of this.
    
    @Override
    public ParserProfile getProfile()
    {
        return profile ;
    }

    @Override
    public void setProfile(ParserProfile profile)
    { this.profile = profile ; }

    public static LangRDFXML create(InputStream in, String xmlBase, String filename, ErrorHandler errorHandler, SinkRDF sink)
    {
        return new LangRDFXML(in, xmlBase, filename, errorHandler, sink) ;
    }
    
    @Deprecated
    public static LangRDFXML create(Reader reader, String xmlBase, String filename, ErrorHandler errorHandler, SinkRDF sink)
    {
        return new LangRDFXML(reader, xmlBase, filename, errorHandler, sink) ;
    }
    

    public static LangRDFXML create(String xmlBase, String filename, ErrorHandler errorHandler, SinkRDF sink)
    {
        return create(IO.openFile(filename), xmlBase, filename, errorHandler, sink) ;
    }
    
    private LangRDFXML(Reader reader, String xmlBase, String filename, ErrorHandler errorHandler, SinkRDF sink)
    {
        this.reader = reader ;
        this.xmlBase = xmlBase ;
        this.filename = filename ;
        this.sink = sink ;
        this.profile = RiotLib.profile(getLang(), xmlBase, errorHandler) ;
    }
    
    private LangRDFXML(InputStream in, String xmlBase, String filename, ErrorHandler errorHandler, SinkRDF sink)
    {
        this.input = in ;
        this.xmlBase = xmlBase ;
        this.filename = filename ;
        this.sink = sink ;
        this.profile = RiotLib.profile(getLang(), xmlBase, errorHandler) ;
    }
    
    @Override
    public Lang getLang()   { return RDFLanguages.RDFXML ; }

    public static boolean RiotUniformCompatibility = false ;
    // Warnings in ARP that should be errors to be compatible with
    // non-XML-based languages.  e.g. language tags should be
    // syntactically valid.
    private static int[] additionalErrors = new int[] {
        WARN_MALFORMED_XMLLANG
        //, WARN_STRING_NOT_NORMAL_FORM_C
    } ;
    
    @Override
    public void parse()
    {   
        // Hacked out of ARP because of all the "private" methods
        sink.start() ;
        HandlerSink rslt = new HandlerSink(sink, getProfile().getHandler()) ;
        arp.getHandlers().setStatementHandler(rslt);
        arp.getHandlers().setErrorHandler(rslt) ;
        arp.getHandlers().setNamespaceHandler(rslt) ;
        
        if ( RiotUniformCompatibility )
        {
            ARPOptions options = arp.getOptions() ;
            // Convert some warnings to errors for compatible behaviour for all parsers. 
            for ( int code : additionalErrors )
                options.setErrorMode(code, EM_FATAL) ;
            arp.setOptionsWith(options) ;
        }
        
        try {
            if ( reader != null )
                arp.load(reader, xmlBase);
            else
                arp.load(input, xmlBase);
        } catch (IOException e) {
            getProfile().getHandler().error(filename + ": " + ParseException.formatMessage(e), -1 , -1) ;
        } catch (SAXParseException e) {
            // already reported.
        } catch (SAXException sax) {
            getProfile().getHandler().error(filename + ": " + ParseException.formatMessage(sax), -1 , -1) ;
        }
        sink.finish() ;
    }

    
    private static class HandlerSink extends ARPSaxErrorHandler implements StatementHandler, NamespaceHandler
    {
        private SinkRDF output ;
        private ErrorHandler errHandler ;
        private CheckerLiterals checker ;

        HandlerSink(SinkRDF output, ErrorHandler errHandler)
        {
            super(new ErrorHandlerBridge(errHandler)) ;
            this.output = output ;
            this.errHandler = errHandler ;
            this.checker = new CheckerLiterals(errHandler) ;
        }
        
        @Override
        public void statement(AResource subj, AResource pred, AResource obj)
        { output.triple(convert(subj, pred, obj)); }

        @Override
        public void statement(AResource subj, AResource pred, ALiteral lit)
        { output.triple(convert(subj, pred, lit)) ; }

        // From JenaReader
        private static Node convert(ALiteral lit) {
            String dtURI = lit.getDatatypeURI();
            if (dtURI == null)
                return Node.createLiteral(lit.toString(), lit.getLang(), false);

            if (lit.isWellFormedXML()) {
                return Node.createLiteral(lit.toString(), null, true);
            }

            RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtURI);
            return Node.createLiteral(lit.toString(), null, dt);

        }

        private static Node convert(AResource r) {
            if (!r.isAnonymous())
                return Node.createURI(r.getURI());

            // String id = r.getAnonymousID();
            Node rr = (Node) r.getUserData();
            if (rr == null) {
                rr = Node.createAnon();
                r.setUserData(rr);
            }
            return rr;

        }

        private Triple convert(AResource s, AResource p, AResource o)
        {
            return Triple.create(convert(s), convert(p), convert(o));
        }

        private Triple convert(AResource s, AResource p, ALiteral o)
        {
            Node object = convert(o) ;
            checker.check(object, -1, -1) ;
            return Triple.create(convert(s), convert(p), object);
        }
        
        @Override
        public void startPrefixMapping(String prefix, String uri)
        {
            output.prefix(prefix, uri) ;
        }
        
        @Override
        public void endPrefixMapping(String prefix)
        {}

    }
    
    private static class ErrorHandlerBridge implements RDFErrorHandler
    {
        private ErrorHandler errorHandler ;

        ErrorHandlerBridge(ErrorHandler hander)
        {
            this.errorHandler = hander ;
        }
        
        @Override
        public void warning(Exception e)        { errorHandler.warning(e.getMessage(), -1, -1) ; }
        @Override
        public void error(Exception e)          { errorHandler.error(e.getMessage(), -1, -1) ; }
        @Override
        public void fatalError(Exception e)     { errorHandler.fatal(e.getMessage(), -1, -1) ; }
    }
}
