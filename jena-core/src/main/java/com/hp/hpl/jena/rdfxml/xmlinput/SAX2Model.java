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

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Use arbitrary SAX input to Jena. See <a
 * href="/documentation/io/sax.html">ARP SAX documentation</a>.
 */
public class SAX2Model extends SAX2RDF {

    /**
     * Factory method to create a new SAX2Model.
     *
     * @param base
     *            The retrieval URL, or the base URI to be used while parsing.
     * @param m
     *            A Jena Model in which to put the triples, this can be null. If
     *            it is null, then use {@link SAX2RDF#getHandlers} or
     *            {@link SAX2RDF#setHandlersWith} to provide a
     *            {@link StatementHandler}, and usually an
     *            {@link org.xml.sax.ErrorHandler}
     * @return A new SAX2Model
     * @throws SAXParseException
     *             On a.error error during setup, maybe malformed base URI
     */
    static public SAX2Model create(String base, Model m)
            throws SAXParseException {
        return new SAX2Model(base, m, "");
    }

    /**
     * Factory method to create a new SAX2Model. This is particularly intended
     * for when parsing a non-root element within an XML document. In which case
     * the application needs to find this value in the outer context.
     * Optionally, namespace prefixes can be passed from the outer context using
     * {@link #startPrefixMapping}.
     *
     * @param base
     *            The retrieval URL, or the base URI to be used while parsing.
     * @param m
     *            A Jena Model in which to put the triples, this can be null. If
     *            it is null, then use {@link SAX2RDF#getHandlers} or
     *            {@link SAX2RDF#setHandlersWith} to provide a
     *            {@link StatementHandler}, and usually an
     *            {@link org.xml.sax.ErrorHandler}
     * @param lang
     *            The current value of <code>xml:lang</code> when parsing
     *            starts, usually "".
     * @return A new SAX2Model
     * @throws SAXParseException
     *             On a.error error during setup, maybe malformed base URI
     */
    static public SAX2Model create(String base, Model m, String lang)
            throws SAXParseException {
        return new SAX2Model(base, m, lang);
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * <p>
     * This is passed to any {@link NamespaceHandler} associated with this
     * parser. It can be called before the initial <code>startElement</code>
     * event, or other events associated with the elements being processed. When
     * building a Jena Model, it is not required to match this with
     * corresponding <code>endPrefixMapping</code> events. Other
     * {@link NamespaceHandler}s may be fussier. When building a Jena Model,
     * the prefix bindings are remembered with the Model, and may be used in
     * some output routines. It is permitted to not call this method for
     * prefixes declared in the outer context, in which case, any output routine
     * will need to use a gensym for such namespaces.
     * </p>
     *
     * @param prefix
     *            The Namespace prefix being declared.
     * @param uri
     *            The Namespace URI the prefix is mapped to.
     *
     */
    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXParseException {
        super.startPrefixMapping(prefix, uri);
    }

    private RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

    final private JenaHandler handler;

    // TODO: deprecate, and make throw MalformedURIException
    /**
     * Constructor, see {@link #create(String, Model, String)} for top-level
     * javadoc.
     *
     * @param base
     * @param m
     * @param lang
     * @throws MalformedURIException
     *             (If base is malformed, and treated as error rather than
     *             warning)
     */
    protected SAX2Model(String base, Model m, String lang) throws SAXParseException
    {
        super(base, lang);
        handler = initHandler(m);
    }

    private JenaHandler initHandler(Model m) {
        if (m==null) return null;
        JenaHandler rslt = new JenaHandler(m, errorHandler);
        rslt.useWith(getHandlers());
        return rslt;

    }
    private boolean closed = false;

    @Override
    public void close() {
        // System.err.println("closing;");
        if (!closed) {
            super.close();
            closed = true;
        }
    }

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
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
        RDFErrorHandler old = this.errorHandler;
        this.errorHandler = errHandler;
        if (handler != null) {
            handler.setErrorHandler(errHandler);
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
     * @see SAX2RDF#getOptions()
     * @see ARPOptions
     */
    public Object setProperty(String str, Object value) throws JenaException {
        Object obj = value;
        if (str.startsWith("http:")) {
            if (str.startsWith(JenaReader.arpPropertiesURL)) {
                str = str.substring(JenaReader.arpPropertiesURLLength);
            }
        }
        return setArpProperty(str, obj);
    }

    private Object setArpProperty(String str, Object v) {
        return JenaReader.processArpOptions(getOptions(), str, v, errorHandler);
    }
}
