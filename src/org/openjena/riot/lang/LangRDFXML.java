/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import java.io.IOException ;
import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.Lang ;
import org.openjena.riot.checker.CheckerLiterals ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.system.RiotLib ;
import org.xml.sax.SAXException ;
import org.xml.sax.SAXParseException ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.arp.ALiteral ;
import com.hp.hpl.jena.rdf.arp.ARP ;
import com.hp.hpl.jena.rdf.arp.AResource ;
import com.hp.hpl.jena.rdf.arp.NamespaceHandler ;
import com.hp.hpl.jena.rdf.arp.ParseException ;
import com.hp.hpl.jena.rdf.arp.StatementHandler ;
import com.hp.hpl.jena.rdf.arp.impl.ARPSaxErrorHandler ;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler ;

public class LangRDFXML implements LangRIOT
{
    // This is not a full member of the RIOT suite because it needs to work
    // with Xerces and already carries out it's own error handling and output
    
    private ARP arp = new ARP() ;
    private long count = 0 ;
    
    private InputStream input = null ;
    private String xmlBase ;
    private String filename ;
    private Sink<Triple> sink ;
    private ParserProfile profile ;             // Warning - we don't use all of this.
    
    public ParserProfile getProfile()
    {
        return profile ;
    }

    public void setProfile(ParserProfile profile)
    { this.profile = profile ; }

    public static LangRDFXML create(InputStream in, String xmlBase, String filename, ErrorHandler errorHandler, Sink<Triple> sink)
    {
        return new LangRDFXML(in, xmlBase, filename, errorHandler, sink) ;
    }
    
    public static LangRDFXML create(String xmlBase, String filename, ErrorHandler errorHandler, Sink<Triple> sink)
    {
        return create(IO.openFile(filename), xmlBase, filename, errorHandler, sink) ;
    }
    
    private LangRDFXML(InputStream in, String xmlBase, String filename, ErrorHandler errorHandler, Sink<Triple> sink)
    {
        this.input = in ;
        this.xmlBase = xmlBase ;
        this.filename = filename ;
        this.sink = sink ;
        this.profile = RiotLib.profile(getLang(), xmlBase, errorHandler) ;
    }
    
    //@Override
    public Lang getLang()   { return Lang.RDFXML ; }

    
    public void parse()
    {   
        // Hacked out of ARP because of all the "private" methods
        count = 0 ;
        HandlerSink rslt = new HandlerSink(sink, getProfile().getHandler()) ;
        arp.getHandlers().setStatementHandler(rslt);
        arp.getHandlers().setErrorHandler(rslt) ;
        arp.getHandlers().setNamespaceHandler(rslt) ;
        
        try {
            arp.load(input, xmlBase);
        } catch (IOException e) {
            getProfile().getHandler().error(filename + ": " + ParseException.formatMessage(e), -1 , -1) ;
        } catch (SAXParseException e) {
            // already reported.
        } catch (SAXException sax) {
            getProfile().getHandler().error(filename + ": " + ParseException.formatMessage(sax), -1 , -1) ;
        }
        sink.flush() ;
    }

    
    private static class HandlerSink extends ARPSaxErrorHandler implements StatementHandler, NamespaceHandler
    {
        private Sink<Triple> sink ;
        private ErrorHandler errHandler ;
        private CheckerLiterals checker ;

        HandlerSink(Sink<Triple> sink, ErrorHandler errHandler)
        {
            super(new ErrorHandlerBridge(errHandler)) ;
            this.sink = sink ;
            this.errHandler = errHandler ;
            this.checker = new CheckerLiterals(errHandler) ;
        }
        
        public void statement(AResource subj, AResource pred, AResource obj)
        { sink.send(convert(subj, pred, obj)); }

        public void statement(AResource subj, AResource pred, ALiteral lit)
        { sink.send(convert(subj, pred, lit)) ; }

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
        
        public void endPrefixMapping(String prefix)
        {}

        public void startPrefixMapping(String prefix, String uri)
        {}
    }
    
    private static class ErrorHandlerBridge implements RDFErrorHandler
    {
        private ErrorHandler errorHandler ;

        ErrorHandlerBridge(ErrorHandler hander)
        {
            this.errorHandler = hander ;
        }
        
        public void warning(Exception e)        { errorHandler.warning(e.getMessage(), -1, -1) ; }
        public void error(Exception e)          { errorHandler.error(e.getMessage(), -1, -1) ; }
        public void fatalError(Exception e)     { errorHandler.fatal(e.getMessage(), -1, -1) ; }
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