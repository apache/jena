/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import org.xml.sax.ErrorHandler;

/**
 * Used for configuring user code to
 * respond to statements and other events
 * detected in input file.
 * @author Jeremy J. Carroll
 *
 */
public class ARPHandlers implements Cloneable {
	private ErrorHandler errorHandler = new DefaultErrorHandler();
	private ExtendedHandler scopeHandler = nullScopeHandler;
	private StatementHandler statementHandler = new StatementHandler() {
			public void statement(AResource s, AResource p, AResource o) {
			}
			public void statement(AResource s, AResource p, ALiteral o) {
			}
		};
	private NamespaceHandler nameHandler = new NamespaceHandler() {
			
					public void startPrefixMapping(String prefix, String uri) {
						
					}
			
					public void endPrefixMapping(String prefix) {
						
					}
				};

	ARPHandlers copy() {
		try {
          return (ARPHandlers)clone();
		}
		catch (CloneNotSupportedException e){
			throw new java.lang.RuntimeException(e);
		}
	}

	/** Sets the ExtendedHandler that provides the callback mechanism
		 * for bnodes as they leave scope, and for the start and end of rdf:RDF
		 * elements.
	 * <p>
	 * See note about large files in class documentation.
		 * @param sh The handler to use.
		 * @return The old handler.
		 */
	public ExtendedHandler setExtendedHandler(ExtendedHandler sh) {
		ExtendedHandler old = scopeHandler;
		scopeHandler = sh;
		return old;
	}

	/** Sets the NamespaceHandler that provides the callback mechanism
	* for XML namespace declarations.
	* @param sh The handler to use.
	* @return The old handler.
	*/
	public NamespaceHandler setNamespaceHandler(NamespaceHandler sh) {
		NamespaceHandler old = nameHandler;
		nameHandler = sh;
		return old;
	}

	/** Sets the StatementHandler that provides the callback mechanism
	 * for each triple in the file.
	 * @param sh The statement handler to use.
	 * @return The old statement handler.
	 */
	public StatementHandler setStatementHandler(StatementHandler sh) {
		StatementHandler old = statementHandler;
		statementHandler = sh;
		return old;
	}

	final static ExtendedHandler nullScopeHandler = new ExtendedHandler() {
	
			public void endBNodeScope(AResource bnode) {
			}
	
			public void startRDF() {
			}
	
			public void endRDF() {
			}
	
			public boolean discardNodesWithNodeID() {
				return true;
			}
		};

	/** Sets the error handler, for both XML and RDF parse errors.
	 * XML errors are reported by Xerces, as instances of
	 * SAXParseException;
	 * the RDF errors are reported from ARP as instances of
	 * ParseException.
	 * Code that needs to distingusih between them
	 * may look like:
	 * <pre>
	 *   void error( SAXParseException e ) throws SAXException {
	 *     if ( e instanceof com.hp.hpl.jena.rdf.arp.ParseException ) {
	 *          ...
	 *     } else {
	 *          ...
	 *     }
	 *   }
	 * </pre>
	 * <p>
	 * See the ARP documentation for ErrorHandler for details of
	 * the ErrorHandler semantics (in particular how to upgrade a warning to
	 * an error, and an error to a fatalError).
	 * </p>
	 * <p>
	 * The Xerces/SAX documentation for ErrorHandler is available on the web.
	 * </p>
	 *
	 * @param eh The error handler to use.
	 * @return The previous error handler.
	 */
	public ErrorHandler setErrorHandler(ErrorHandler eh) {
	    ErrorHandler old = errorHandler;
		errorHandler = eh;
	    return old;
	}

	/**
	 * @return Returns the error handler.
	 */
	ErrorHandler getErrorHandler() {
		return errorHandler;
	}
	/**
	 * @return Returns the namespace handler.
	 */
	NamespaceHandler getNamespaceHandler() {
		return nameHandler;
	}
	/**
	 * @return Returns the extended handler.
	 */
	ExtendedHandler getExtendedHandler() {
		return scopeHandler;
	}
	/**
	 * @return Returns the extended handler.
	 */
	StatementHandler getStatementHandler() {
		return statementHandler;
	}
	

}


/*
 *  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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
 
