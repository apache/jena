/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.impl;

import org.xml.sax.ErrorHandler;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.ARPHandlers;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.ExtendedHandler;
import com.hp.hpl.jena.rdf.arp.NamespaceHandler;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

/**
 * Used for configuring user code to
 * respond to statements and other events
 * detected in input file.
 * @author Jeremy J. Carroll
 *
 */
public class ARPHandlersImpl extends ARPHandlers implements Cloneable  {
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

                /**
                 * Copies this object.
                 * @return A copy of this object.
                 */
	public ARPHandlersImpl copy() {
		try {
          return (ARPHandlersImpl)clone();
		}
		catch (CloneNotSupportedException e){
			throw new java.lang.RuntimeException(e);
		}
	}

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPHandlersI#setExtendedHandler(com.hp.hpl.jena.rdf.arp.ExtendedHandler)
     */
	public ExtendedHandler setExtendedHandler(ExtendedHandler sh) {
		ExtendedHandler old = scopeHandler;
		scopeHandler = sh;
		return old;
	}

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPHandlersI#setNamespaceHandler(com.hp.hpl.jena.rdf.arp.NamespaceHandler)
     */
	public NamespaceHandler setNamespaceHandler(NamespaceHandler sh) {
		NamespaceHandler old = nameHandler;
		nameHandler = sh;
		return old;
	}

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPHandlersI#setStatementHandler(com.hp.hpl.jena.rdf.arp.StatementHandler)
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

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPHandlersI#setErrorHandler(org.xml.sax.ErrorHandler)
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
 
