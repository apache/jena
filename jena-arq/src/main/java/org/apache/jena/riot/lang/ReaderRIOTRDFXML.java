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

import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;
import java.util.Map;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.TypeMapper ;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.irix.IRIs;
import org.apache.jena.rdf.model.RDFErrorHandler ;
import org.apache.jena.rdfxml.xmlinput1.*;
import org.apache.jena.rdfxml.xmlinput1.impl.ARPSaxErrorHandler;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.util.Context;
import org.xml.sax.SAXException ;
import org.xml.sax.SAXParseException ;

/** RDF/XML.
 *
 * @see <a href="http://www.w3.org/TR/rdf-syntax-grammar/">http://www.w3.org/TR/rdf-syntax-grammar/</a>
 */
public class ReaderRIOTRDFXML implements ReaderRIOT
{
    public static ReaderRIOTFactory factory = (Lang language, ParserProfile parserProfile) -> {
        // Ignore the provided ParserProfile
        // ARP predates RIOT and does many things internally already.
        return new ReaderRIOTRDFXML(parserProfile);
    };

    private final ParserProfile parserProfile;
    private final ErrorHandler errorHandler;


    public ReaderRIOTRDFXML(ParserProfile parserProfile) {
        this.parserProfile = parserProfile;
        this.errorHandler = parserProfile.getErrorHandler();
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        parse(in, null, baseURI, ct, output, context);
    }

    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        parse(null, reader, baseURI, ct, output, context);
    }

    // RDF 1.1 is based on URIs/IRIs, where space are not allowed.
    // RDF 1.0 (and RDF/XML) was based on "RDF URI References" which did allow spaces.

    // Use with TDB requires this to be "true" - it is set by InitTDB.
    public static final boolean RiotUniformCompatibility = true ;

    // Warnings in ARP that should be errors to be compatible with
    // non-XML-based languages.  e.g. language tags should be
    // syntactically valid.
    private static int[] additionalErrors = new int[] {
        ARPErrorNumbers.WARN_MALFORMED_XMLLANG
        //, ARPErrorNumbers.WARN_MALFORMED_URI
        //, ARPErrorNumbers.WARN_STRING_NOT_NORMAL_FORM_C
    } ;

    // Special case of space in URI is handled in HandlerSink (below).
    // This is instead of ARPErrorNumbers.WARN_MALFORMED_URI in additionalErrors[].
    // which causes a WARN (from ARP, with line+column numbers) then a ERROR from RIOT.
    // It's a pragmatic compromise.
    private static boolean errorForSpaceInURI = true;

    // Extracted from org.apache.jena.rdfxml.xmlinput.JenaReader
    private void oneProperty(ARPOptions options, String pName, Object value) {
        if (! pName.startsWith("ERR_") && ! pName.startsWith("IGN_") && ! pName.startsWith("WARN_"))
            return ;
        int cond = ParseException.errorCode(pName);
        if (cond == -1)
            throw new RiotException("No such ARP property: '"+pName+"'");
        int val;
        if (value instanceof String) {
            if (!((String) value).startsWith("EM_"))
                throw new RiotException("Value for ARP property does not start EM_: '"+pName+"' = '"+value+"'" );
            val = ParseException.errorCode((String) value);
            if (val == -1 )
                throw new RiotException("Illegal value for ARP property: '"+pName+"' = '"+value+"'" );
        } else if (value instanceof Integer) {
            val = ((Integer) value).intValue();
            switch (val) {
                case ARPErrorNumbers.EM_IGNORE:
                case ARPErrorNumbers.EM_WARNING:
                case ARPErrorNumbers.EM_ERROR:
                case ARPErrorNumbers.EM_FATAL:
                    break;
                default:
                    throw new RiotException("Illegal value for ARP property: '"+pName+"' = '"+value+"'" );
            }
        } else {
            throw new RiotException("Property \"" + pName + "\" cannot have value: " + value.toString());
        }
        options.setErrorMode(cond, val);
    }

    @SuppressWarnings("deprecation")
    private void parse(InputStream input, Reader reader, String xmlBase, ContentType ct, StreamRDF sink, Context context) {
        // One of input and reader is null.
        boolean legacySwitch = context.isTrue(RIOT.symRDFXML0);
        if ( legacySwitch ) {
            ReaderRIOTRDFXML0 other = new ReaderRIOTRDFXML0(parserProfile.getErrorHandler());
            other.parse(input, reader, xmlBase, ct, sink, context);
            return;
        }

        // Hacked out of ARP because of all the "private" methods
        // JenaReader has reset the options since new ARP() was called.
        sink.start() ;
        HandlerSink rslt = new HandlerSink(sink, parserProfile) ;

        ARP arp = new ARP();
        arp.getHandlers().setStatementHandler(rslt) ;
        arp.getHandlers().setErrorHandler(rslt) ;
        arp.getHandlers().setNamespaceHandler(rslt) ;

        // ARPOptions.
        ARPOptions arpOptions = arp.getOptions() ;
        if ( RiotUniformCompatibility ) {
            // Convert some warnings to errors for compatible behaviour for all parsers.
            for ( int code : additionalErrors )
                arpOptions.setErrorMode(code, ARPErrorNumbers.EM_ERROR) ;
        }

        if ( context != null ) {
            Map<String, Object> properties = null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> p = (Map<String, Object>)(context.get(SysRIOT.sysRdfReaderProperties)) ;
                properties = p;
            } catch(Throwable ex) {
                Log.warn(this, "Problem accessing the RDF/XML reader properties: properties ignored", ex);
            }
            if ( properties != null )
                properties.forEach((k,v) -> oneProperty(arpOptions, k, v)) ;
        }
        arp.setOptionsWith(arpOptions) ;

        String filename = xmlBase;

        try {
            if ( reader != null )
                arp.load(reader, xmlBase) ;
            else
                arp.load(input, xmlBase) ;
        }
        catch (IOException e) {
            errorHandler.error(filename + ": " + ParseException.formatMessage(e), -1, -1) ;
        }
        catch (SAXParseException e) {
            // already reported.
        }
        catch (SAXException sax) {
            errorHandler.error(filename + ": " + ParseException.formatMessage(sax), -1, -1) ;
        }
        sink.finish() ;
    }

    /** Sort out the base URI for RDF/XML parsing. */
    private static String baseURI_RDFXML(String baseIRI) {
        if ( baseIRI == null )
            return IRIs.getBaseStr();
        // RDFParserBuidler resolved the baseIRI
        return baseIRI;
    }

    private static class HandlerSink extends ARPSaxErrorHandler implements StatementHandler, NamespaceHandler {
        private final StreamRDF       output ;
        private final ParserProfile parserProfile;
        private final ErrorHandler riotErrorHandler;
        private final FactoryRDF   termFactory;

        HandlerSink(StreamRDF output, ParserProfile parserProfile) {
            super(new ErrorHandlerBridge(parserProfile.getErrorHandler())) ;
            this.output = output ;
            this.parserProfile = parserProfile;
            this.riotErrorHandler = parserProfile.getErrorHandler();
            this.termFactory = parserProfile.getFactorRDF();
        }

        @Override
        public void statement(AResource subj, AResource pred, AResource obj)
        { output.triple(convert(subj, pred, obj)); }

        @Override
        public void statement(AResource subj, AResource pred, ALiteral lit)
        { output.triple(convert(subj, pred, lit)) ; }

        // Should be called by RDFXMLReader.
        private Node convert(ALiteral lit) {
            String dtURI = lit.getDatatypeURI();
            if (dtURI == null)
                return parserProfile.createLangLiteral(lit.toString(), lit.getLang(), -1, -1);

            if (lit.isWellFormedXML()) {
                return parserProfile.createTypedLiteral(lit.toString(), XMLLiteralType.theXMLLiteralType, -1, -1);
            }

            RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtURI);
            return parserProfile.createTypedLiteral(lit.toString(), dt, -1, -1);
        }

        private Node convert(AResource r) {
            if (!r.isAnonymous()) {
                // URI.
                String uriStr = r.getURI() ;
                if ( errorForSpaceInURI ) {
                    // Special check for spaces in a URI.
                    // Convert to an error like TokernizerText.
                    if ( uriStr.contains(" ") ) {
                        int i = uriStr.indexOf(' ');
                        String s = uriStr.substring(0,i);
                        String msg = String.format("Bad character in IRI (space): <%s[space]...>", s);
                        riotErrorHandler.error(msg, -1, -1);
                        throw new RiotParseException(msg, -1, -1);
                    }
                }
                return termFactory.createURI(uriStr);
            }

            // String id = r.getAnonymousID();
            Node rr = (Node) r.getUserData();
            if (rr == null) {
                rr = termFactory.createBlankNode();
                r.setUserData(rr);
            }
            return rr;
        }

        private Triple convert(AResource s, AResource p, AResource o) {
            return Triple.create(convert(s), convert(p), convert(o)) ;
        }

        private Triple convert(AResource s, AResource p, ALiteral o) {
            return Triple.create(convert(s), convert(p), convert(o)) ;
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) {
            output.prefix(prefix, uri) ;
        }

        @Override
        public void endPrefixMapping(String prefix) {}
    }

    private static class ErrorHandlerBridge implements RDFErrorHandler {
        private ErrorHandler errorHandler ;

        ErrorHandlerBridge(ErrorHandler hander) {
            this.errorHandler = hander ;
        }

        @Override
        public void warning(Exception e) {
            Pair<Integer, Integer> p = getLineCol(e) ;
            errorHandler.warning(e.getMessage(), p.getLeft(), p.getRight()) ;
        }

        @Override
        public void error(Exception e) {
            Pair<Integer, Integer> p = getLineCol(e) ;
            errorHandler.error(e.getMessage(), p.getLeft(), p.getRight()) ;
        }

        @Override
        public void fatalError(Exception e) {
            Pair<Integer, Integer> p = getLineCol(e) ;
            errorHandler.fatal(e.getMessage(), p.getLeft(), p.getRight()) ;
        }

        private static Pair<Integer, Integer> getLineCol(Exception e) {
            if ( e instanceof SAXParseException ) {
                SAXParseException esax = (SAXParseException)e ;
                return Pair.create(esax.getLineNumber(), esax.getColumnNumber()) ;
            } else {
                return Pair.create(-1, -1) ;
            }
        }
    }
}
