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
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.irix.IRIs;
import org.apache.jena.rdf.model.RDFErrorHandler ;
import org.apache.jena.rdfxml.xmlinput0.* ;
import org.apache.jena.rdfxml.xmlinput0.impl.ARPSaxErrorHandler ;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.Checker;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;
import org.xml.sax.SAXException ;
import org.xml.sax.SAXParseException ;

/** RDF/XML.
 * <p>
 * <b>LEGACY</b>
 * <p>
 * Uses xmlinput0 - uses the version of ARP from jena 4.7.0 before the conversion to IRIx.
 *
 * @see <a href="http://www.w3.org/TR/rdf-syntax-grammar/">http://www.w3.org/TR/rdf-syntax-grammar/</a>
 */
@SuppressWarnings("deprecation")
class ReaderRIOTRDFXML0 implements ReaderRIOT
{
    public static ReaderRIOTFactory factory = (Lang language, ParserProfile parserProfile) ->
            // Ignore the provided ParserProfile
            // ARP predates RIOT and does many things internally already.
            // This includes IRI resolution.
            new ReaderRIOTRDFXML0(parserProfile.getErrorHandler())
            ;

    private ARP0 arp = new ARP0() ;

    private InputStream input = null ;
    private Reader reader = null ;
    private String xmlBase ;
    private String filename ;
    private StreamRDF sink ;
    private ErrorHandler errorHandler;

    private Context context;

    public ReaderRIOTRDFXML0(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        this.input = in ;
        this.xmlBase = baseURI_RDFXML(baseURI) ;
        this.filename = baseURI ;
        this.sink = output ;
        this.context = context;
        parse();
    }

    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        this.reader = reader ;
        this.xmlBase = baseURI_RDFXML(baseURI) ;
        this.filename = baseURI ;
        this.sink = output ;
        this.context = context;
        parse();
    }

    // RDF 1.1 is based on URIs/IRIs, where space are not allowed.
    // RDF 1.0 (and RDF/XML) was based on "RDF URI References" which did allow spaces.

    // Use with TDB requires this to be "true" - it is set by InitTDB.
    public static boolean RiotUniformCompatibility = true ;
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

    public void parse() {
        // Hacked out of ARP because of all the "private" methods
        // JenaReader has reset the options since new ARP() was called.
        sink.start() ;
        HandlerSink rslt = new HandlerSink(sink, errorHandler) ;
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
        private StreamRDF       output ;
        private ErrorHandler    riotErrorHandler ;

        HandlerSink(StreamRDF output, ErrorHandler errHandler) {
            super(new ErrorHandlerBridge(errHandler)) ;
            this.output = output ;
            this.riotErrorHandler = errHandler ;
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
                return NodeFactory.createLiteral(lit.toString(), lit.getLang());

            if (lit.isWellFormedXML()) {
                return NodeFactory.createLiteral(lit.toString(), null, true);
            }

            RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtURI);
            return NodeFactory.createLiteral(lit.toString(), dt);
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
                return NodeFactory.createURI(uriStr);
            }

            // String id = r.getAnonymousID();
            Node rr = (Node) r.getUserData();
            if (rr == null) {
                rr = NodeFactory.createBlankNode();
                r.setUserData(rr);
            }
            return rr;
        }

        private Triple convert(AResource s, AResource p, AResource o) {
            return Triple.create(convert(s), convert(p), convert(o)) ;
        }

        private Triple convert(AResource s, AResource p, ALiteral o) {
            Node literal = convert(o) ;
            Checker.checkLiteral(literal, riotErrorHandler, -1, -1);
            return Triple.create(convert(s), convert(p), literal) ;
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
