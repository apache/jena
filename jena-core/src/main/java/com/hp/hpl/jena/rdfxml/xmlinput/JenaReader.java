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

package com.hp.hpl.jena.rdfxml.xmlinput;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale ;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.* ;

import org.apache.jena.iri.IRIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.RDFXMLParser ;
import com.hp.hpl.jena.shared.DoesNotExistException;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.UnknownPropertyException;
import com.hp.hpl.jena.shared.WrappedIOException;

/**
 * Interface between Jena and ARP.
 */
public class JenaReader implements RDFReader, ARPErrorNumbers {
    
    static private final String saxFeaturesURL = "http://xml.org/sax/features/";

    static private final String saxPropertiesURL = "http://xml.org/sax/properties/";

    static private final String apacheFeaturesURL = "http://apache.org/xml/features/";

    static private final String apachePropertiesURL = "http://apache.org/xml/properties/";

    static final String arpPropertiesURL = "http://jena.hpl.hp.com/arp/properties/";

    static final int arpPropertiesURLLength = arpPropertiesURL.length();

    /**
     * Creates new JenaReader
     */
    public JenaReader() {
        arpf = RDFXMLParser.create();
    }

    final private RDFXMLParser arpf;

    private Model model;

    /**
     * Reads from url, using url as base, adding triples to model. 
     * Uses content negotiation to ask for application/rdf+xml, if available.
     * 
     * @param m
     *            A model to add triples to.
     * @param url
     *            The URL of the RDF/XML document.
     */
    @Override
    public void read(Model m, String url) throws JenaException {
        try {
            URLConnection conn = new URL(url).openConnection();
			conn.setRequestProperty("accept", "application/rdf+xml, application/xml; q=0.8, text/xml; q=0.7, application/rss+xml; q=0.3, */*; q=0.2");
            String encoding = conn.getContentEncoding();
            if (encoding == null)
                read(m, conn.getInputStream(), url);
            else
                read(m, new InputStreamReader(conn.getInputStream(),
                        encoding), url);
        } catch (FileNotFoundException e) {
            throw new DoesNotExistException(url);
        } catch (IOException e) {
            throw new JenaException(e);
        }
    }

    private static Node convert(ALiteral lit) {
        String dtURI = lit.getDatatypeURI();
        if (dtURI == null)
            return NodeFactory.createLiteral(lit.toString(), lit.getLang(), false);

        if (lit.isWellFormedXML()) {
            return NodeFactory.createLiteral(lit.toString(), null, true);
        }

        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtURI);
        return NodeFactory.createLiteral(lit.toString(), null, dt);

    }

    private static Node convert(AResource r) {
        if (!r.isAnonymous())
            return NodeFactory.createURI(r.getURI());

        // String id = r.getAnonymousID();
        Node rr = (Node) r.getUserData();
        if (rr == null) {
            rr = NodeFactory.createAnon();
            r.setUserData(rr);
        }
        return rr;

    }

    static Triple convert(AResource s, AResource p, AResource o) {
        return Triple.create(convert(s), convert(p), convert(o));
    }

    static Triple convert(AResource s, AResource p, ALiteral o) {
        return Triple.create(convert(s), convert(p), convert(o));
    }

    /**
     * Reads from reader, using base URI xmlbase, adding triples to model. If
     * xmlbase is "" then relative URIs may be added to model.
     * 
     * @param m
     *            A model to add triples to.
     * @param reader
     *            The RDF/XML document.
     * @param xmlBase
     *            The base URI of the document or "".
     */
    private void read(Model m, InputSource inputS, String xmlBase)
            throws JenaException {
        model = m;
        read(model.getGraph(), inputS, xmlBase, model);
    }

    private JenaHandler handler;

    synchronized private void read(final Graph g, InputSource inputS,
            String xmlBase, Model m) {

        try {
            g.getEventManager().notifyEvent(g, GraphEvents.startRead);
            inputS.setSystemId(xmlBase);
            handler = new JenaHandler(g, m, errorHandler);
            handler.useWith(arpf.getHandlers());
            arpf.parse(inputS, xmlBase);
        } catch (IOException e) {
            throw new WrappedIOException(e);
        } catch (SAXException e) {
            throw new JenaException(e);
        } finally {
            g.getEventManager().notifyEvent(g, GraphEvents.finishRead);
            handler = null;
        }
    }

    /**
     * Reads from reader, using base URI xmlbase, adding triples to model. If
     * xmlbase is "" then relative URIs may be added to model.
     * 
     * @param m
     *            A model to add triples to.
     * @param reader
     *            The RDF/XML document.
     * @param xmlBase
     *            The base URI of the document or "".
     */
    @Override
    public void read(final Model m, Reader reader, String xmlBase)
            throws JenaException {
        read(m, new InputSource(reader), xmlBase);
    }

    /**
     * Reads from reader, using base URI xmlbase, adding triples to graph. If
     * xmlbase is "" then relative URIs may be added to graph.
     * 
     * @param g
     *            A graph to add triples to.
     * @param reader
     *            The RDF/XML document.
     * @param xmlBase
     *            The base URI of the document or "".
     */
    public void read(Graph g, Reader reader, String xmlBase)
            throws JenaException {
        read(g, new InputSource(reader), xmlBase, null);
    }

    /**
     * Reads from inputStream, using base URI xmlbase, adding triples to model.
     * If xmlbase is "" then relative URIs may be added to model.
     * 
     * @param m
     *            A model to add triples to.
     * @param in
     *            The RDF/XML document stream.
     * @param xmlBase
     *            The base URI of the document or "".
     */
    @Override
    public void read(final Model m, InputStream in, String xmlBase)
            throws JenaException {
        read(m, new InputSource(in), xmlBase);
    }

    /**
     * Reads from inputStream, using base URI xmlbase, adding triples to graph.
     * If xmlbase is "" then relative URIs may be added to graph.
     * 
     * @param g
     *            A graph to add triples to.
     * @param in
     *            The RDF/XML document stream.
     * @param xmlBase
     *            The base URI of the document or "".
     */
    public void read(Graph g, InputStream in, String xmlBase) {
        read(g, new InputSource(in), xmlBase, null);
    }

    private RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

    /**
     * Change the error handler.
     * <p>
     * Note that errors of class {@link ParseException}can be promoted using
     * the {@link ParseException#promote}method. See ARP documentation for
     * {@link org.xml.sax.ErrorHandler}for the details of error promotion.
     * 
     * @param errHandler
     *            The new error handler.
     * @return The old error handler.
     */
    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
        RDFErrorHandler old = this.errorHandler;
        this.errorHandler = errHandler;
        JenaHandler h = handler;
        if (h != null) {
            h.setErrorHandler(errHandler);
        }
        return old;
    }

    /**
     * 
     * Change a property of the RDF or XML parser.
     * <p>
     * I do not believe that many of the XML features or properties are in fact
     * useful for ARP users. The ARP properties allow fine-grained control over
     * error reporting.
     * <p>
     * This interface can be used to set and get:
     * <dl>
     * <dt>SAX2 features</dt>
     * <dd>See <a href="http://xml.apache.org/xerces-j/features.html">Xerces
     * features </a>. Value should be given as a String "true" or "false" or a
     * Boolean.</dd>
     * <dt>SAX2 properties</dt>
     * <dd>See <a href="http://xml.apache.org/xerces-j/properties.html">Xerces
     * properties </a>.</dd>
     * <dt>Xerces features</dt>
     * <dd>See <a href="http://xml.apache.org/xerces-j/features.html">Xerces
     * features </a>. Value should be given as a String "true" or "false" or a
     * Boolean.</dd>
     * <dt>Xerces properties</dt>
     * <dd>See <a href="http://xml.apache.org/xerces-j/properties.html">Xerces
     * properties </a>.</dd>
     * <dt>ARP properties</dt>
     * <dd>These are referred to either by their property name, (see below) or
     * by an absolute URL of the form
     * <code>http://jena.hpl.hp.com/arp/properties/&lt;PropertyName&gt;</code>.
     * The value should be a String, an Integer or a Boolean depending on the
     * property. <br>
     * ARP property names and string values are case insensitive. <br>
     * <TABLE BORDER="1" CELLPADDING="3" CELLSPACING="0">
     * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
     * <TD COLSPAN=4><FONT SIZE="+2"> <B>ARP Properties </B> </FONT></TD>
     * </TR>
     * <tr BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor">
     * <th>Property Name</th>
     * <th>Description</th>
     * <th>Value class</th>
     * <th>Legal Values</th>
     * </tr>
     * <tr BGCOLOR="white" CLASS="TableRowColor">
     * <td><CODE>error-mode</CODE></td>
     * <td>{@link ARPOptions#setDefaultErrorMode}<br>
     * {@link ARPOptions#setLaxErrorMode}<br>
     * {@link ARPOptions#setStrictErrorMode()}<br>
     * {@link ARPOptions#setStrictErrorMode(int)}<br>
     * </td>
     * <td>String</td>
     * <td><CODE>default</CODE><br>
     * <CODE>lax</CODE><br>
     * <CODE>strict</CODE><br>
     * <CODE>strict-ignore</CODE><br>
     * <CODE>strict-warning</CODE><br>
     * <CODE>strict-error</CODE><br>
     * <CODE>strict.error</CODE><br>
     * </td>
     * </tr>
     * <tr BGCOLOR="white" CLASS="TableRowColor">
     * <td><CODE>embedding</CODE></td>
     * <td>{@link ARPOptions#setEmbedding}</td>
     * <td>String or Boolean</td>
     * <td><CODE>true</CODE> or <CODE>false</CODE></td>
     * </tr>
     * <tr BGCOLOR="white" CLASS="TableRowColor">
     * <td><code>ERR_&lt;XXX&gt;</code><br>
     * <code>WARN_&lt;XXX&gt;</code><br>
     * <code>IGN_&lt;XXX&gt;</code></td>
     * <td>{@link ARPErrorNumbers}<br>
     * Any of the error condition numbers listed. <br>
     * {@link ARPOptions#setErrorMode(int, int)}</td>
     * <td>String or Integer</td>
     * <td>{@link ARPErrorNumbers#EM_IGNORE EM_IGNORE}<br>
     * {@link ARPErrorNumbers#EM_WARNING EM_WARNING}<br>
     * {@link ARPErrorNumbers#EM_ERROR EM_ERROR}<br>
     * {@link ARPErrorNumbers#EM_FATAL EM_FATAL}<br>
     * </td>
     * </tr>
     * </table></dd>
     * </dl>
     * 
     * @param str
     *            The property to set.
     * @param value
     *            The new value; values of class String will be converted into
     *            appropriate classes. Values of class Boolean or Integer will
     *            be used for appropriate properties.
     * @throws JenaException
     *             For bad values.
     * @return The old value, or null if none, or old value is inaccesible.
     */
    @Override
    public Object setProperty(String str, Object value) throws JenaException {
        Object obj = value;
        if (str.startsWith("http:")) {
            if (str.startsWith(arpPropertiesURL)) {
                return setArpProperty(str.substring(arpPropertiesURLLength),
                        obj);
            }
            if (str.startsWith(saxPropertiesURL)
                    || str.startsWith(apachePropertiesURL)) {
                Object old;
                try {
                    old = arpf.getSAXParser().getProperty(str);
                } catch (SAXNotSupportedException ns) {
                    old = null;
                } catch (SAXNotRecognizedException nr) {
                    errorHandler.error(new UnknownPropertyException(str));
                    return null;
                }
                try {
                    arpf.getSAXParser().setProperty(str, obj);
                } catch (SAXNotSupportedException ns) {
                    errorHandler.error(new JenaException(ns));
                } catch (SAXNotRecognizedException nr) {
                    errorHandler.error(new UnknownPropertyException(str));
                    return null;
                }
                return old;
            }

            if (str.startsWith(saxFeaturesURL)
                    || str.startsWith(apacheFeaturesURL)) {
                Boolean old;
                try {
                    old = arpf.getSAXParser().getFeature( str );
                } catch (SAXNotSupportedException ns) {
                    old = null;
                } catch (SAXNotRecognizedException nr) {
                    errorHandler.error(new UnknownPropertyException(str));
                    return null;
                }
                try {
                    arpf.getSAXParser().setFeature(str,
                            ((Boolean) obj).booleanValue());
                } catch (SAXNotSupportedException ns) {
                    errorHandler.error(new JenaException(ns));
                } catch (SAXNotRecognizedException nr) {
                    errorHandler.error(new UnknownPropertyException(str));
                    return null;
                } catch (ClassCastException cc) {
                    errorHandler.error(new JenaException(
                            new SAXNotSupportedException("Feature: '" + str
                                    + "' can only have a boolean value.")));
                }
                return old;
            }
        }
        return setArpProperty(str, obj);
    }

    private Object setArpProperty(String str, Object v) {
        return processArpOptions(getOptions(), str, v, errorHandler);
    }

	public ARPOptions getOptions() {
		return arpf.getOptions();
	}

	public void setOptionsWith(ARPOptions opts) {
		arpf.setOptionsWith(opts);
	}

    /**
     * Supported properties: 
     * error-mode (String) default, lax, strict,
     * strict-ignore, strict-warning, strict-error, strict.error <br/> 
     * embedding (String/Boolean) true, false<br/>
     * ERR_* (String/Integer) em_warning, em.error, em_ignore, em_error<br/>
     * IGN_* ditto<br/>
     * WARN_* ditto<br/>
     * iri-rules (String), "Jena", "IRI", "strict", "lax"
     */
    static Object processArpOptions(ARPOptions options, String str, Object v,
            RDFErrorHandler eh) {
        // ARPOptions options = arpf.getOptions();
        str = str.toUpperCase();
        if (v == null)
            v = "";
        if (v instanceof String) {
            v = ((String) v).toUpperCase(Locale.ENGLISH);
        }
        if (str.equals("ERROR-MODE")) {
            if (v instanceof String) {
                String val = (String) v;
                if (val.equals("LAX")) {
                    options.setLaxErrorMode();
                    return null;
                }
                if (val.equals("DEFAULT")) {
                    options.setDefaultErrorMode();
                    return null;
                }
                if (val.equals("STRICT")) {
                    options.setStrictErrorMode();
                    return null;
                }
                if (val.equals("STRICT-WARNING")) {
                    options.setStrictErrorMode(EM_WARNING);
                    return null;
                }
                if (val.equals("STRICT-FATAL")) {
                    options.setStrictErrorMode(EM_FATAL);
                    return null;
                }
                if (val.equals("STRICT-IGNORE")) {
                    options.setStrictErrorMode(EM_IGNORE);
                    return null;
                }
                if (val.equals("STRICT-ERROR")) {
                    options.setStrictErrorMode(EM_ERROR);
                    return null;
                }
            }
            eh.error(new IllegalArgumentException(
                                                  "Property \"ERROR-MODE\" takes the following values: "
                                                  + "\"default\", \"lax\", \"strict\", \"strict-ignore\", \"strict-warning\", \"strict-error\", \"strict.error\".")) ;
            return null;
        }
        if (str.equals("EMBEDDING")) {
            if (v instanceof String) {
                v = Boolean.valueOf((String) v);
            }
            if ((v instanceof Boolean))
                return options.setEmbedding( ( (Boolean) v ).booleanValue() );

            // Illegal value.
            eh.error(new IllegalArgumentException(
                    "Property \"EMBEDDING\" requires a boolean value."));
            boolean old = options.setEmbedding(false);
            options.setEmbedding(old);
            return old;

        }
        if (str.startsWith("ERR_") || str.startsWith("IGN_")
                || str.startsWith("WARN_")) {
            int cond = ParseException.errorCode(str);
            if (cond == -1) {
                // error, see end of function.
            } else {
                if (v instanceof String) {
                    if (!((String) v).startsWith("EM_")) {
                        // error, see below.
                    } else {
                        int val = ParseException.errorCode((String) v);
                        if (val == -1) {
                            // error, see below.
                        } else {
                            int rslt = options.setErrorMode(cond, val);
                            return rslt;
                        }
                    }
                } else if (v instanceof Integer) {
                    int val = ((Integer) v).intValue();
                    switch (val) {
                    case EM_IGNORE:
                    case EM_WARNING:
                    case EM_ERROR:
                    case EM_FATAL:
                        int rslt = options.setErrorMode(cond, val);
                        return rslt;
                    default:
                    // error, see below.
                    }
                }
                // Illegal value.
                eh.error(new IllegalArgumentException("Property \"" + str
                        + "\" cannot have value: " + v.toString()));
                int old = options.setErrorMode(cond, EM_ERROR);
                options.setErrorMode(cond, old);
                return old;
            }
        }
        
        if ( str.equals("IRI-RULES") )
        {
            IRIFactory old = options.getIRIFactory() ;
            if ( v.equals("STRICT") )   { options.setIRIFactory(IRIFactory.semanticWebImplementation()) ; }
            else if ( v.equals("IRI") ) { options.setIRIFactory(IRIFactory.iriImplementation()) ; }
            else if ( v.equals("LAX") ) { options.setIRIFactory(IRIFactory.jenaImplementation()) ; }
            else
                eh.error(new IllegalArgumentException(
                "Property \"IRI-RULES\" requires one of 'STRICT', 'IRI' or 'LAX'"));
            return old ;
        }
        
        eh.error(new UnknownPropertyException(str));
        return null;
    }

}
