/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import org.xml.sax.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;

/**
 * @author Jeremy J. Carroll
 *
 */
public class SAX2Model extends SAX2RDF {
	/**
	 * Factory method to create a new SAX2RDF.
	 * @param base The retrieval URL, or the base URI to be 
     * used while parsing.
     * @param m A Jena Model in which to put the triples,
     * this can be null. If it is null, then use
     * {@link #getHandlers} or {@link #setHandlersWith} to provide
     * a {@link StatementHandler}, and usually an {@link org.xml.sax.ErrorHandler}
	 * @return A new SAX2RDF
	 * @throws MalformedURIException
	 */
	static public SAX2Model newInstance(String base, Model m) throws MalformedURIException { 
		return new SAX2Model(base,m,""); 
	}
	/**
	 * Factory method to create a new SAX2RDF.
	 * This is particularly
     * intended for when parsing a non-root element within
     * an XML document. In which case the application
     * needs to find this value in the outer context.
     * Optionally, namespace prefixes can be passed from the
     * outer context using {@link #startPrefixMapping}.
	 * @param base The retrieval URL, or the base URI to be 
     * used while parsing.
     * @param m A Jena Model in which to put the triples,
     * this can be null. If it is null, then use
     * {@link #getHandlers} or {@link #setHandlersWith} to provide
     * a {@link StatementHandler}, and usually an {@link org.xml.sax.ErrorHandler}
	 * @param lang The current value of <code>xml:lang</code> when parsing starts, usually "".
	 * @return A new SAX2RDF
	 * @throws MalformedURIException
	 */
	static public SAX2Model newInstance(String base, Model m, String lang) throws MalformedURIException { 
		return new SAX2Model(base,m,lang); 
	}    
	/**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     *<p>This is passed to any {@link NamespaceHandler} associated
     *with this parser.
     *It can be called before the initial 
     *{@link #startElement} event, or other events associated
     *with the elements being processed.
     *When building a Jena Model, it is not required to match this
     *with corresponding {@link #endPrefixMapping} events.
     *Other {@link NamespaceHandler}s may be fussier.
     *When building a Jena Model, the prefix bindings are
     *remembered with the Model, and may be used in some
     *output routines. It is permitted to not call this method
     *for prefixes declared in the outer context, in which case,
     *any output routine will need to use a gensym for such 
     *namespaces.
     *</p>
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * 
     */
    public void startPrefixMapping (String prefix, String uri)
	 { super.startPrefixMapping(prefix,uri);
    }

	private RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

	final private JenaHandler handler;

    private SAX2Model(String base, Model m, String lang) throws MalformedURIException {
    	super(base,lang);
    	handler = new JenaHandler(m,errorHandler);
    	handler.useWith(getHandlers());
    }
private boolean closed = false;
    public void close() throws SAXException{
    //	System.err.println("closing;");
    	if (!closed) {
    	super.close();
    	handler.bulkUpdate();
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
	 * This method is untested.
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
	 * <td>{@link ARP#setDefaultErrorMode}<br>
	 * {@link ARP#setLaxErrorMode}<br>
	 * {@link ARP#setStrictErrorMode}<br>
	 * {@link ARP#setStrictErrorMode(int)}<br>
	 * </td>
	 * <td>String</td>
	 * <td><CODE>default</CODE><br>
	 * <CODE>lax</CODE><br>
	 * <CODE>strict</CODE><br>
	 * <CODE>strict-ignore</CODE><br>
	 * <CODE>strict-warning</CODE><br>
	 * <CODE>strict-error</CODE><br>
	 * <CODE>strict-fatal</CODE><br>
	 * </td>
	 * </tr>
	 * <tr BGCOLOR="white" CLASS="TableRowColor">
	 * <td><CODE>embedding</CODE></td>
	 * <td>{@link ARP#setEmbedding}</td>
	 * <td>String or Boolean</td>
	 * <td><CODE>true</CODE> or <CODE>false</CODE></td>
	 * </tr>
	 * <tr BGCOLOR="white" CLASS="TableRowColor">
	 * <td><code>ERR_&lt;XXX&gt;</code><br>
	 * <code>WARN_&lt;XXX&gt;</code><br>
	 * <code>IGN_&lt;XXX&gt;</code></td>
	 * <td>{@link ARPErrorNumbers}<br>
	 * Any of the error condition numbers listed. <br>
	 * {@link ARP#setErrorMode(int, int)}</td>
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
	public Object setProperty(String str, Object value) throws JenaException {
		Object obj = value;
		if (str.startsWith("http:")) {
			if (str.startsWith(JenaReader.arpPropertiesURL)) {
				str = str.substring(JenaReader.arpPropertiesURLLength);				
			}	
		}
		return setArpProperty(str, obj);
	}
	private Object setArpProperty( String str, Object v) {
		return JenaReader.setArpProperty(getOptions(),str,v,errorHandler);
	}
}


/*
 *  (c) Copyright 2004 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 
