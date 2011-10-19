/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import org.xml.sax.ErrorHandler;

import com.hp.hpl.jena.rdf.arp.impl.DefaultErrorHandler;
import com.hp.hpl.jena.rdf.arp.impl.XMLHandler;


/**
 * The interface to set the various handlers on ARP. User defined
 * implementations of this interface are not supported. This is a class rather
 * than an interface to have better backward compatibilitiy with earlier
 * versions, however constructing instances of this class is deprecated.
 * 
 * @author Jeremy J. Carroll
 * 
 */
public class ARPHandlers {
    
    /**
     * Do not use this constructor.
     * An example of not using this constructor is as follows.
     * <br/>
     * Deprecated usage:
     * <br/>
     * <pre>
        ARP arp = new ARP();
        ARPHandlers handlers = new ARPHandlers();
        handlers.setStatementHandler(new MyStatementHandler());
     </pre>
     <br/>
     * Preferred code:
     * <br/>
     * <pre>
        ARP arp = new ARP();
        ARPHandlers handlers = arp.getHandlers();
        handlers.setStatementHandler(new MyStatementHandler());
     </pre>
     */
    //*@ deprecated Use {@link ARPConfig#getHandlers()}
    private ARPHandlers() {}
    
    /** Internal use only */
    public static ARPHandlers createNewHandlers() { return new ARPHandlers() ; }

    private ErrorHandler errorHandler = new DefaultErrorHandler();

    private ExtendedHandler scopeHandler = XMLHandler.nullScopeHandler;

    private StatementHandler statementHandler = XMLHandler.nullStatementHandler;

    private NamespaceHandler nameHandler = new NamespaceHandler() {

        @Override
        public void startPrefixMapping(String prefix, String uri) {

        }

        @Override
        public void endPrefixMapping(String prefix) {

        }
    };

    /**
     * Sets the ExtendedHandler that provides the callback mechanism for bnodes
     * as they leave scope, and for the start and end of rdf:RDF elements.
     * <p>
     * See note about large files in {@link ARP} class documentation.
     * 
     * @param sh
     *            The handler to use.
     * @return The old handler.
     */
    public ExtendedHandler setExtendedHandler(ExtendedHandler sh) {
        ExtendedHandler old = scopeHandler;
        scopeHandler = sh;
        return old;
    }

    /**
     * Sets the NamespaceHandler that provides the callback mechanism for XML
     * namespace declarations.
     * 
     * @param sh
     *            The handler to use.
     * @return The old handler.
     */
    public NamespaceHandler setNamespaceHandler(NamespaceHandler sh) {
        NamespaceHandler old = nameHandler;
        nameHandler = sh;
        return old;
    }

    /**
     * Sets the StatementHandler that provides the callback mechanism for each
     * triple in the file.
     * 
     * @param sh
     *            The statement handler to use.
     * @return The old statement handler.
     */
    public StatementHandler setStatementHandler(StatementHandler sh) {
        StatementHandler old = statementHandler;
        statementHandler = sh;
        return old;
    }

    /**
     * Sets the error handler, for both XML and RDF parse errors. XML errors are
     * reported by Xerces, as instances of SAXParseException; the RDF errors are
     * reported from ARP as instances of ParseException. Code that needs to
     * distingusih between them may look like:
     * 
     * <pre>
     *    void error( SAXParseException e ) throws SAXException {
     *      if ( e instanceof com.hp.hpl.jena.rdf.arp.ParseException ) {
     *           ...
     *      } else {
     *           ...
     *      }
     *    }
     * </pre>
     * 
     * <p>
     * See the ARP documentation for ErrorHandler for details of the
     * ErrorHandler semantics (in particular how to upgrade a warning to an
     * error, and an error to a.errorError).
     * </p>
     * <p>
     * The Xerces/SAX documentation for ErrorHandler is available on the web.
     * </p>
     * 
     * @param eh
     *            The error handler to use.
     * @return The previous error handler.
     */
    public ErrorHandler setErrorHandler(ErrorHandler eh) {
        ErrorHandler old = errorHandler;
        errorHandler = eh;
        return old;
    }

    /**
     * Gets the current error handler.
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Gets the current namespace handler.
     */
    public NamespaceHandler getNamespaceHandler() {
        return nameHandler;
    }

    /**
     * Gets the current extended handler.
     */
    public ExtendedHandler getExtendedHandler() {
        return scopeHandler;
    }

    /**
     * Gets the current statement handler.
     */
    public StatementHandler getStatementHandler() {
        return statementHandler;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

