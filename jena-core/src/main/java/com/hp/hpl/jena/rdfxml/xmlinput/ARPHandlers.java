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

import org.xml.sax.ErrorHandler;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.DefaultErrorHandler ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.XMLHandler ;


/**
 * The interface to set the various handlers on ARP. User defined
 * implementations of this interface are not supported. This is a class rather
 * than an interface to have better backward compatibilitiy with earlier
 * versions, however constructing instances of this class is deprecated.
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
