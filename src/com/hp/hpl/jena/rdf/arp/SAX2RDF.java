/*
 *  (c) Copyright 2004  Hewlett-Packard Development Company, LP
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
 
 * * $Id: SAX2RDF.java,v 1.2 2004-10-06 15:04:39 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
package com.hp.hpl.jena.rdf.arp;
import com.hp.hpl.jena.rdf.model.Model;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.*;
import org.xml.sax.*;

/**
 * Allows connecting an arbitrary source of SAX events with ARP.
 * See <a href="http://javaalmanac.com/egs/javax.xml.transform.sax/Dom2Sax.html">
 * The Java Developer's Almanac</a> for a discussion of how to transform a DOM
 * into a source of SAX events.
 * 
 * 
 * Use pattern, create and initialize one of these,
 * then set it as the content, lexical and error handler
 * for some source of SAX events (e.g. from a parser).
 * When parsing has finished call {@link #finishRDFParsing()}.
 * All the triples have now been processed.
 * 
 * Triples and errors are reported on a different thread.
 * 
 * 
 *  
 * @author Jeremy Carroll
 * */
public class SAX2RDF extends LexicalHandlerImpl implements ContentHandler, LexicalHandler, ErrorHandler{
	/**
	 * Factory method to create a new SAX2RDF.
	 * @return A new SAX2RDF
	 */
	static public SAX2RDF newInstance() { return null; }
    /**
     * Sets the retrieval URL, or the base URI to be 
     * used while parsing.
     * @param base
	 * @return Old value, if any.
     */
	public String setBaseURI(String base){ return null; }
    /**
     * Sets the value of xml:lang, particularly
     * for use when parsing a non-root element within
     * an XML document. In which case the application
     * needs to find this value in the outer context.
     * @param lang
	 * @return Old value, if any.
     */
	public String setLang(String lang){ return null; }
	/**
	 * Sets the triple handler.
	 * Either a triple handler or a model can be used.
	 * @param ah The handler to use.
	 * @see #setModel
	 * @return Old value, if any.
	 * */
	public ARPHandler setTripleHandler(ARPHandler ah){ return null;}
	/**
	 * Sets the Jena Model to load the triples into.
	 * Either a triple handler or a model can be used.
	 * @param m
	 * @see #setTripleHandler
	 * @return Old value, if any.
	 */
	public Model setModel(Model m){ return null; }
	/**
	 * Sets the extended handler, for complex interactions
	 * with the RDF parser.
	 * @param eh The extended handler to use.
	 * @return Old value, if any.
	 */
	public ExtendedHandler setExtendedHandler(ExtendedHandler eh){ return null; }
	

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * <p>The information from this event is not necessary for
     * normal Namespace processing: the SAX XML reader will 
     * automatically replace prefixes for element and attribute
     * names when the <code>http://xml.org/sax/features/namespaces</code>
     * feature is <var>true</var> (the default).</p>
     *
     * <p>There are cases, however, when applications need to
     * use prefixes in character data or in attribute values,
     * where they cannot safely be expanded automatically; the
     * start/endPrefixMapping event supplies the information
     * to the application to expand prefixes in those contexts
     * itself, if necessary.</p>
     *
     * <p>Note that start/endPrefixMapping events are not
     * guaranteed to be properly nested relative to each-other:
     * all startPrefixMapping events will occur before the
     * corresponding {@link #startElement startElement} event, 
     * and all {@link #endPrefixMapping endPrefixMapping}
     * events will occur after the corresponding {@link #endElement
     * endElement} event, but their order is not otherwise 
     * guaranteed.</p>
     *
     * <p>There should never be start/endPrefixMapping events for the
     * "xml" prefix, since it is predeclared and immutable.</p>
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see #endPrefixMapping
     * @see #startElement
     */
    public void startPrefixMapping (String prefix, String uri)
	throws SAXException {
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * <p>See {@link #startPrefixMapping startPrefixMapping} for 
     * details.  This event will always occur after the corresponding 
     * {@link #endElement endElement} event, but the order of 
     * {@link #endPrefixMapping endPrefixMapping} events is not otherwise
     * guaranteed.</p>
     *
     * @param prefix The prefix that was being mapping.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see #startPrefixMapping
     * @see #endElement
     */
    public void endPrefixMapping (String prefix)
	throws SAXException {}
/**
 * This method must be called after the last SAX event.
 *
 */
    public void finishRDFParsing() {}
    
    /**
     * Allow an application to register an error event handler.
     *
     * <p>If the application does not register an error handler, all
     * error events get logged.</p>
     *
     * <p>Applications may register a new or different handler in the
     * middle of a parse, but because of the asynchronicity between the
     * XML parsing and the RDF parsing, the precise behaviour is not
     * well-defined.</p>
     *
     * @param handler The error handler.
     * @exception java.lang.NullPointerException If the handler 
     *            argument is null.
     * @see #getErrorHandler
     */
    public void setErrorHandler (ErrorHandler handler) {}


    /**
     * Return the current error handler.
     *
     * @return The current error handler.
     * @see #setErrorHandler
     */
    public ErrorHandler getErrorHandler () {return null;}
	
}
    
	
