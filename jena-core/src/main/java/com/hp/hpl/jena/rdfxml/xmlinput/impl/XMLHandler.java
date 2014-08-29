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

/* This file includes contributions by:
 * (c) Copyright 2003, Plugged In Software 
 * See end of file for BSD-style license.
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import java.net.MalformedURLException ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.xml.sax.Attributes ;
import org.xml.sax.Locator ;
import org.xml.sax.SAXException ;
import org.xml.sax.SAXParseException ;

import com.hp.hpl.jena.rdfxml.xmlinput.* ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.Frame ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.FrameI ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.LookingForRDF ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.StartStateRDForDescription ;

/**
 * This class converts SAX events into a stream of encapsulated events suitable
 * for the RDF parser. In effect, this is the RDF lexer. updates by kers to
 * handle exporting namespace prefix maps.
 */
public class XMLHandler extends LexicalHandlerImpl implements ARPErrorNumbers,
        Names {

    boolean encodingProblems = false;

    protected Map<IRI, Map<String,ARPLocation>> idsUsed = new HashMap<>();
    protected int idsUsedCount = 0;

    public XMLHandler() {}
    
    public void triple(ANode s, ANode p, ANode o) {
        StatementHandler stmt;
        boolean bad=s.isTainted() || p.isTainted() || o.isTainted();
        if (bad) {
            stmt = badStatementHandler;
        } else {
            stmt = handlers.getStatementHandler();
        }
        AResourceInternal subj = (AResourceInternal) s;
        AResourceInternal pred = (AResourceInternal) p;
        if (!bad)
            subj.setHasBeenUsed();
        if (o instanceof AResource) {
            AResourceInternal obj = (AResourceInternal) o;
            if (!bad) obj.setHasBeenUsed();
            stmt.statement(subj, pred, obj);
        } else
            stmt.statement(subj, pred, (ALiteral) o);
    }

    // This is the current frame.
    FrameI frame;

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXParseException {
        checkNamespaceURI(uri);
        handlers.getNamespaceHandler().startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) {
        handlers.getNamespaceHandler().endPrefixMapping(prefix);
    }

    public Locator getLocator() {
        return locator;
    }

    Locator locator;

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    static final private boolean DEBUG = false;

    @Override
    public void startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXException {
        if (Thread.interrupted())
            warning(null, ERR_INTERRUPTED, "Interrupt detected.");
        FrameI oldFrame = frame;
        frame = frame.startElement(uri, localName, rawName, atts);
        if (DEBUG)
            System.err.println("<" + rawName + "> :: "
                    + getSimpleName(oldFrame.getClass()) + " --> "
                    + getSimpleName(frame.getClass()));
    }

    @Override
    public void endElement(String uri, String localName, String rawName)
            throws SAXException {
        frame.endElement();
        frame = frame.getParent();
        frame.afterChild();
        if (DEBUG)
            System.err.println("</" + rawName + "> :: <--"
                    + getSimpleName(frame.getClass()));
    }

    static public String getSimpleName(Class< ? extends FrameI> c) {
        String rslt[] = c.getName().split("\\.");
        return rslt[rslt.length - 1];
    }

    @Override
    public void characters(char ch[], int start, int length)
            throws SAXException {
        frame.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException { // Never called.
        characters(ch, start, length);
    }

    void setUserData(String nodeId, Object v) {
        nodeIdUserData.put(nodeId, v);
    }

    Object getUserData(String nodeId) {
        return nodeIdUserData.get(nodeId);
    }

    @Override
    public void comment(char[] ch, int start, int length)
            throws SAXParseException {
        frame.comment(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        frame.processingInstruction(target, data);
    }

    public void warning(Taint taintMe,int id, String msg) throws SAXParseException {
        if (options.getErrorMode(id) != EM_IGNORE)
            warning(taintMe,id, location(), msg);
    }

    void warning(Taint taintMe, int id, ARPLocation loc, String msg) throws SAXParseException {
        if (options.getErrorMode(id) != EM_IGNORE)
            warning(taintMe, id, new ParseException(id, loc, msg) {
                private static final long serialVersionUID = 1990910846204964756L;
            });
    }

    void generalError( int id, Exception e) throws SAXParseException {
        ARPLocation where = new ARPLocation(locator);
        // System.err.println(e.getMessage());
        warning(null, id, new ParseException(id, where, e));

    }

    void warning(Taint taintMe, int id, SAXParseException e) throws SAXParseException {
        try {
            switch (options.getErrorMode(id)) {
            case EM_IGNORE:
                break;
            case EM_WARNING:
                handlers.getErrorHandler().warning(e);
                break;
            case EM_ERROR:
                if (taintMe != null)
                    taintMe.taint();
                handlers.getErrorHandler().error(e);
                break;
            case EM_FATAL:
                handlers.getErrorHandler().fatalError(e);
                break;
            }
        } catch (SAXParseException xx) {
            throw xx;
        } catch (SAXException ee) {
            throw new WrappedException(ee);
        }
        if (e instanceof ParseException && ((ParseException) e).isPromoted())
            throw e;
        if (options.getErrorMode(id) == EM_FATAL) {
            // If we get here, we shouldn't go on
            // throw an error into Jena.
            throw new FatalParsingErrorException();

        }
    }

    @Override
    public void error(SAXParseException e) throws SAXParseException {
        warning(null,ERR_SAX_ERROR, e);
    }

    @Override
    public void warning(SAXParseException e) throws SAXParseException {
        warning(null,WARN_SAX_WARNING, e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        warning(null,ERR_SAX_FATAL_ERROR, e);
        // If we get here, we shouldn't go on
        // throw an error into Jena.
        throw new FatalParsingErrorException();

    }

    /**
     * @param v
     */
    public void endLocalScope(ANode v) {
        if (handlers.getExtendedHandler() != nullScopeHandler) {
            ARPResource bn = (ARPResource) v;
            if (!bn.getHasBeenUsed())
                return;
            if (bn.hasNodeID()) {
                // save for later end scope
                if (handlers.getExtendedHandler().discardNodesWithNodeID())
                    return;
                String bnodeID = bn.nodeID;
                if (!nodeIdUserData.containsKey(bnodeID))
                    nodeIdUserData.put(bnodeID, null);
            } else {
                handlers.getExtendedHandler().endBNodeScope(bn);
            }
        }
    }

    public void endRDF() {
        handlers.getExtendedHandler().endRDF();
    }

    public void startRDF() {
        handlers.getExtendedHandler().startRDF();
    }

    boolean ignoring(int eCode) {
        return options.getErrorMode(eCode) == EM_IGNORE;
    }
    
    public boolean isError(int eCode) {
        return options.getErrorMode(eCode) == EM_ERROR;
    }

    protected AbsXMLContext initialContext(String base, String lang)
            throws SAXParseException {
        return initialContextWithBase(base).withLang(this,lang);
    }

    private boolean allowRelativeReferences = false;
    
    private AbsXMLContext initialContextWithBase(String base) throws SAXParseException {
        allowRelativeReferences = false;
            if (base == null) {
                warning(null,IGN_NO_BASE_URI_SPECIFIED,
                        "Base URI not specified for input file; local URI references will be in error.");

                return new XMLBaselessContext(this,
                        ERR_RESOLVING_URI_AGAINST_NULL_BASE);

            } else if (base.equals("")) {
                allowRelativeReferences = true;
                warning(null,IGN_NO_BASE_URI_SPECIFIED,
                        "Base URI specified as \"\"; local URI references will not be resolved.");
                return new XMLBaselessContext(this,
                        WARN_RESOLVING_URI_AGAINST_EMPTY_BASE);
            } else {
//                if (base.toLowerCase().startsWith("file:")
//                    && base.length()>5
//                    && base.charAt(5) != '/'
//                ) {
//                    System.err.print(base);
//                    try {
//                        base = new File(base.substring(5)).toURL().toString();
//                        if (base.length()<=6
//                                || base.charAt(6)!= '/')
//                            base = "file://"+base.substring(5);
//                    } catch (MalformedURLException e) {
//                        // ignore, just leave it alone.
//                    }
//                    System.err.println(" ==> "+base);
//                    
//                }
                return new XMLBaselessContext(this,
                        ERR_RESOLVING_AGAINST_RELATIVE_BASE).withBase(this,base);
            }
    }
    /*
    private XMLContext initialContextWithBasex(String base)
            throws SAXParseException {
        XMLContext rslt = new XMLContext(this, base);
        RDFURIReference b = rslt.getURI();
        if (base == null) {
            warning(null,IGN_NO_BASE_URI_SPECIFIED,
                    "Base URI not specified for input file; local URI references will be in error.");

        } else if (base.equals("")) {
            warning(null,IGN_NO_BASE_URI_SPECIFIED,
                    "Base URI specified as \"\"; local URI references will not be resolved.");

        } else {
            checkBadURI(null,b);
            // Warnings on bad base.

            // if (b.isVeryBad()||b.isRelative()) {
            // return
        }

        return rslt;
    }
    */

    private ARPOptions options = ARPOptions.createNewOptions() ;
    private ARPHandlers handlers = ARPHandlers.createNewHandlers() ;

    StatementHandler getStatementHandler() {
        return handlers.getStatementHandler();
    }

    public ARPHandlers getHandlers() {
        return handlers;
    }

    public ARPOptions getOptions() {
        return options;
    }

    public void setOptionsWith(ARPOptions newOpts) {
           options = newOpts.copy();
        
    }

    public void setHandlersWith(ARPHandlers newHh) {
        handlers = ARPHandlers.createNewHandlers() ;
        handlers.setErrorHandler(newHh.getErrorHandler());
        handlers.setExtendedHandler(newHh.getExtendedHandler());
        handlers.setNamespaceHandler(newHh.getNamespaceHandler());
        handlers.setStatementHandler(newHh.getStatementHandler());
       
    }

    private Map<String, Object> nodeIdUserData;

    public void initParse(String base, String lang) throws SAXParseException {
        nodeIdUserData = new HashMap<>();
        idsUsed = 
        	ignoring(WARN_REDEFINITION_OF_ID)?
        			null:
        	        new HashMap<IRI, Map<String,ARPLocation>>();
        idsUsedCount = 0;
        if (options.getEmbedding())
            frame = new LookingForRDF(this, initialContext(base, lang));
        else
            frame = new StartStateRDForDescription(this, initialContext(base,
                    lang));

    }

    /**
     * This method must be always be called after parsing, e.g. in a finally
     * block.
     * 
     */
    void afterParse() {
        while (frame != null) {
            frame.abort();
            frame = frame.getParent();
        }
        // endRDF();
        endBnodeScope();
        idsUsed = null;
    }

    void endBnodeScope() {
        if (handlers.getExtendedHandler() != nullScopeHandler) {
            for ( String nodeId : nodeIdUserData.keySet() )
            {
                ARPResource bn = new ARPResource( this, nodeId );
                handlers.getExtendedHandler().endBNodeScope( bn );
            }
        }
    }

    public ARPLocation location() {
        return new ARPLocation(locator);
    }

    private IRIFactory factory = null ;

    IRIFactory iriFactory() {
        if (factory == null) {
            factory = options.getIRIFactory() ;
            if ( factory == null )
                factory = ARPOptions.getIRIFactoryGlobal() ;
        }
        return factory;
    }

    private void checkNamespaceURI(String uri) throws SAXParseException {
        ((Frame) frame).checkEncoding(null,uri);
        if (uri.length() != 0)
             {
                IRI u = iriFactory().create(uri);
//                if (u.isVeryBad()) {
//                    warning(null,
//                            WARN_BAD_NAMESPACE_URI,
//                            "The namespace URI: <"
//                                    + uri
//                                    + "> is not well formed.");
//                    return;
//                 
//                }
                if (!u.isAbsolute()) {
                    warning(null,
                            WARN_RELATIVE_NAMESPACE_URI_DEPRECATED,
                            "The namespace URI: <"
                                    + uri
                                    + "> is relative. Such use has been deprecated by the W3C, and may result in RDF interoperability failures. Use an absolute namespace URI.");
                }
                try {
                    if (!u.toASCIIString().equals(u.toString()))
                        warning(null,
                                WARN_BAD_NAMESPACE_URI,
                                "Non-ascii characters in a namespace URI may not be completely portable: <"
                                        + u.toString()
                                        + ">. Resulting RDF URI references are legal.");
                } catch (MalformedURLException e) {
                    warning(null,
                            WARN_BAD_NAMESPACE_URI,
                            "toAscii failed for namespace URI: <"
                                    + u.toString()
                                    + ">. " + e.getMessage());
              } 

                if (uri.startsWith(rdfns) && !uri.equals(rdfns))
                    warning(null,WARN_BAD_RDF_NAMESPACE_URI, "Namespace URI ref <"
                            + uri + "> may not be used in RDF/XML.");
                if (uri.startsWith(xmlns) && !uri.equals(xmlns))
                    warning(null,WARN_BAD_XML_NAMESPACE_URI, "Namespace URI ref <"
                            + uri + "> may not be used in RDF/XML.");
             }   
    }

    public boolean allowRelativeURIs() {
        return allowRelativeReferences;
    }
    private IRI sameDocRef;
    public IRI sameDocRef() {
        if (sameDocRef==null){
            sameDocRef = iriFactory().create("");
        }
        return sameDocRef;
    }

    private StatementHandler badStatementHandler = nullStatementHandler;
    
    public void setBadStatementHandler(StatementHandler sh) {
        badStatementHandler = sh;
    }

    final public static StatementHandler nullStatementHandler =
    new StatementHandler() {
        @Override
        public void statement(AResource s, AResource p, AResource o) {
        }
        @Override
        public void statement(AResource s, AResource p, ALiteral o) {
        }
    };
    final public static ExtendedHandler nullScopeHandler = new ExtendedHandler() {
        
        @Override
        public void endBNodeScope(AResource bnode) {
        }

        @Override
        public void startRDF() {
        }

        @Override
        public void endRDF() {
        }

        @Override
        public boolean discardNodesWithNodeID() {
            return true;
        }
    };
}

/*
 *  (c) Copyright 2003, Plugged In Software 
 *
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
 */
