/*
 *  (c) Copyright 2001  Hewlett-Packard Development Company, LP
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
 
 * * $Id: StanfordImpl.java,v 1.2 2003-08-27 13:05:52 andy_seaborne Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/

/*
 * StanfordImpl.java
 *
 * Created on 28 July 2001, 00:06
 */

package com.hp.hpl.jena.rdf.arp;

import org.w3c.rdf.syntax.*;
import org.w3c.rdf.model.*;
import org.xml.sax.*;

/**
 * An implementation of Sergey Melnik's Stanford API, used by SiRPAC.
 * Note: this interface is provided for backwards compatibility
 * and does not give access to the whole range of ARP features.
 * @author  Jeremy Carroll
 */
public class StanfordImpl implements org.w3c.rdf.syntax.RDFParser {
    private ARP arp;
    /** Creates an RDFParser. 
     */
    public StanfordImpl() {
        arp = new ARP();
    }

    ErrorHandler errorHandler = new DefaultErrorHandler();
    /**
     * Parse from the given SAX/XML input source.
     * @param source The RDF/XML document to parse.
     * @param consumer For callbacks from the parser.
     */
    synchronized public void parse(InputSource source,final RDFConsumer consumer) throws SAXException {
        arp.setStatementHandler(new StanfordStatementHandler(consumer) );
        try {
           arp.load(source);
        }
        catch (java.io.IOException ioe) {
            throw new SAXException(ioe);
        }
    }
    
    /**
     * Set the error handler.
     */
    public void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
        arp.setErrorHandler(handler);
    }
    
    private class StanfordStatementHandler implements StatementHandler {
        RDFConsumer consumer;
        NodeFactory nodeFactory;
        boolean started = false;
        StanfordStatementHandler(RDFConsumer cc) throws SAXException {
            consumer = cc;
            try {
                nodeFactory = cc.getNodeFactory();
            }
            catch ( ModelException ee ) {
                throw new SAXException(ee);
            }
            }
        void end() throws SAXException {
            if ( started )
                try {
                consumer.endModel();
                }
                catch ( ModelException ee ) {
                throw new SAXException(ee);
                }
            started = false;
        }
        private Resource translate(AResource ar) throws ModelException {
            Resource rslt = (Resource)ar.getUserData();
            if ( rslt != null )
                return rslt;
            if ( ar.isAnonymous() ) {
                rslt = nodeFactory.createUniqueResource();
            } else {
                rslt = nodeFactory.createResource(ar.getURI());
            }
            ar.setUserData(rslt);
            return rslt;
        }
        private Literal translate(ALiteral al) throws ModelException {
            return nodeFactory.createLiteral(al.toString());
        }
        void addStatement( Resource s, Resource p, RDFNode n ) throws ModelException {
            if (!started) {
                consumer.startModel();
                started = true;
            }
            consumer.addStatement(nodeFactory.createStatement(s,p,n));
        }
        public void statement(AResource subj, AResource pred, AResource obj ) {
            try {
                
                addStatement(translate(subj),translate(pred),translate(obj));
            }
            catch (ModelException e) {
                throw new WrappedException(e);
            }
        }
        public void statement(AResource subj, AResource pred, ALiteral lit ) {
            try {
                addStatement(translate(subj),translate(pred),translate(lit));
            }
            catch (ModelException e) { 
                throw new WrappedException(e);
            }
        }
    }
    
}
        