/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * (c) Copyright 2003, Plugged In Software 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
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
 * 
 * $Id: XMLHandler.java,v 1.2 2005-08-01 15:54:51 jeremy_carroll Exp $
 * 
 * AUTHOR: Jeremy J. Carroll
 */
/*
 * ARPFilter.java
 * 
 * Created on June 21, 2001, 10:01 PM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import java.io.InterruptedIOException;
import com.hp.hpl.jena.shared.wg.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.ARPHandlers;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.ParseException;
import com.hp.hpl.jena.rdf.arp.StatementHandler;
import com.hp.hpl.jena.rdf.arp.states.Frame;
import com.hp.hpl.jena.rdf.arp.states.FrameI;
import com.hp.hpl.jena.rdf.arp.states.WantRDFFrame;



/**
 * This class converts SAX events into a stream of encapsulated events suitable
 * for the RDF parser. In effect, this is the RDF lexer. updates by kers to
 * handle exporting namespace prefix maps.
 * 
 * @author jjc
 */
public class XMLHandler extends LexicalHandlerImpl implements ARPErrorNumbers, Names {


    boolean encodingProblems = false;

    protected Map idsUsed = new HashMap();
  
    public void triple(ANode s, ANode p, ANode o) {
//        System.out.println(s + " " + p + " " + o + " .");
        StatementHandler stmt = handlers.getStatementHandler();
        AResourceInternal subj = (AResourceInternal)s;
        AResourceInternal pred = (AResourceInternal)p;
        subj.setHasBeenUsed();
        if (o instanceof AResource) {
            AResourceInternal obj = (AResourceInternal)o;
            obj.setHasBeenUsed();
            stmt.statement(subj,pred,obj);
        } else
            stmt.statement(subj,pred,(ALiteral)o);
    }
    // This is the current frame.
    FrameI frame;


    public void startPrefixMapping(String prefix, String uri) throws SAXParseException {
        checkNamespaceURI(uri);
        handlers.getNamespaceHandler().startPrefixMapping(prefix,uri);
    }

    public void endPrefixMapping(String prefix) {
         handlers.getNamespaceHandler().endPrefixMapping(prefix);
    }

    public Locator getLocator() {
        return locator;
    }

    Locator locator;

  
    // TODO: where do these names belong?

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    static final private boolean DEBUG = false;
    public void startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXException {
        if (Thread.interrupted())
            throw new WrappedException(new InterruptedIOException());
        FrameI oldFrame = frame;
        frame =
        frame.startElement(uri,localName,rawName,atts);
        if (DEBUG)
        System.err.println("<"+rawName + "> :: "+getSimpleName(oldFrame.getClass()) + " --> " + getSimpleName(frame.getClass()));
    }

    public void endElement(String uri, String localName, String rawName)
            throws SAXException {
        frame.endElement();
        frame = frame.getParent();
        if (DEBUG)
        System.err.println("</"+rawName+"> :: <--" + getSimpleName(frame.getClass()) );
    }    
    static public String getSimpleName(Class c) {
        String rslt[] = c.getName().split("\\.");
        return rslt[rslt.length-1];
    }
    public void characters(char ch[], int start, int length)
            throws SAXException {
        frame.characters(ch,start,length);
    }

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
    
    public void comment(char[] ch, int start, int length)
            throws SAXParseException {
        frame.comment(ch,start,length);
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        frame.processingInstruction(target,data);
    }

    public void warning(int id, String msg)  throws SAXParseException {
        if (options.getErrorMode()[id] != EM_IGNORE)
            warning(id,location(),msg);
    }

    void warning(int id, Location loc, String msg)  throws SAXParseException {
        if (options.getErrorMode()[id] != EM_IGNORE)
            warning(id,new ParseException(id,loc,msg){
                private static final long serialVersionUID = 1990910846204964756L;
            });
    }

    void generalError(int id, Exception e) throws SAXParseException {
        Location where = new Location(locator);
        //   System.err.println(e.getMessage());
        warning(id,new ParseException(id,where,e));

    }
    void warning(int id, SAXParseException e) throws SAXParseException {
        try {
            switch (options.getErrorMode()[id]) {
                case EM_IGNORE :
                    break;
                case EM_WARNING :
                    handlers.getErrorHandler().warning(e);
                    break;
                case EM_ERROR :
                    handlers.getErrorHandler().error(e);
                    break;
                case EM_FATAL :
                    handlers.getErrorHandler().fatalError(e);
                    // If we get here,  we shouldn't go on
                    // throw an error into Jena.
                    throw new JumpUpTheStackException();
            }
        } 
        catch (SAXParseException xx) {
            throw xx;
        }
        catch (SAXException ee) {
            throw new WrappedException(ee);
        }
        if ( e instanceof ParseException 
                && ((ParseException)e).promoteMe)
            throw e;
    }
    
    public void error(SAXParseException e) throws SAXParseException {
        warning(ERR_SAX_ERROR, e);
    }
    public void warning(SAXParseException e) throws SAXParseException {
        warning(WARN_SAX_WARNING, e);
    }
    public void fatalError(SAXParseException e) throws SAXException {
        warning(ERR_SAX_FATAL_ERROR, e);
        // If we get here,  we shouldn't go on
        // throw an error into Jena.
        throw new JumpUpTheStackException();
        
    }
    


    /**
     * @param v
     */
    public void endLocalScope(ANode v) {
        if (handlers.getExtendedHandler() != ARPHandlersImpl.nullScopeHandler) {
                ARPResource bn = (ARPResource) v;
              if (!bn.getHasBeenUsed())
                return;
                if (bn.hasNodeID()) {
                    // save for later end scope
                    if ( handlers.getExtendedHandler().discardNodesWithNodeID())
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
        return options.getErrorMode()[eCode]==EM_IGNORE;
    }

    protected XMLContext initialContext(String base, String lang) throws SAXParseException {
        return initialContextWithBase(base).withLang(lang);
    }

    private XMLContext initialContextWithBase(String base) throws SAXParseException {
        try {
            if (base == null) {
                // TODO: base warnings
                warning(IGN_NO_BASE_URI_SPECIFIED,
                        "Base URI not specified for input file; local URI references will be in error.");

                return new XMLNullContext(this,
                        ERR_RESOLVING_URI_AGAINST_NULL_BASE);

            } else if (base.equals("")) {
                warning(IGN_NO_BASE_URI_SPECIFIED,
                        "Base URI specified as \"\"; local URI references will not be resolved.");
                return new XMLNullContext(this,
                        WARN_RESOLVING_URI_AGAINST_EMPTY_BASE);
            } else {
                return new XMLContext(base);
            }
        } catch (URISyntaxException e) {
            // TODO: bad base what here?
            return null;
        }
    }


    private ARPOptionsImpl options = new ARPOptionsImpl();
    private ARPHandlersImpl handlers = new ARPHandlersImpl();
    
    StatementHandler getStatementHandler() {
        return handlers.getStatementHandler();
    }
    public ARPHandlers getHandlers() {
        return handlers;
    }

    public ARPOptionsImpl getOptions() {
        return options;
    }
    public void setOptionsWith(ARPOptionsImpl newOpts) {
        options = newOpts.copy();
    }
    public void setHandlersWith(ARPHandlersImpl newHh){
        handlers = newHh.copy();
    }   
    private Map nodeIdUserData;
    
    public void initParse(String base,String lang)  throws SAXParseException 
    {
        nodeIdUserData = new HashMap();
        idsUsed = new HashMap();
        // String base = input.getSystemId();
        // TODO: embedding option or not?
        // TODO: first frame
        if (getOptions().getEmbedding())
            frame = new WantRDFFrame(this, initialContext(base,lang));
        else
            // TODO: following line is wrong
            frame = new WantRDFFrame(this, initialContext(base,lang));
        
    }
    
    /**
     * This method must be always be called after parsing,
     * e.g. in a finally block.
     *
     */
    void afterParse() {
        while (frame!=null) {
            frame.abort();
            frame = frame.getParent();
        }
//        endRDF();
        endBnodeScope();
    }

    void endBnodeScope() {
        if ( handlers.getExtendedHandler() != ARPHandlersImpl.nullScopeHandler ) {
            Iterator it = nodeIdUserData.keySet().iterator();
            while (it.hasNext()) {
                String nodeId = (String)it.next();
                ARPResource bn = new ARPResource(this,nodeId);
                handlers.getExtendedHandler().endBNodeScope(bn);
            }
        }
    }
    
    
    public Location location() {
        return new Location(locator);
    }

    private void checkNamespaceURI(String uri) throws SAXParseException {
        ((Frame)frame).checkEncoding(uri);
        if (uri.length() != 0)
            try {
                URI u = URI.create(uri);
                if (!u.isAbsolute()) {
                    warning(
                            WARN_RELATIVE_NAMESPACE_URI_DEPRECATED,
                      "The namespace URI: <"+uri+"> is relative. Such use has been deprecated by the W3C, and may result in RDF interoperability failures. Use an absolute namespace URI."      
                            );
                }
                if (!u.toASCIIString().equals(u.toString()))
                        warning(
                                WARN_BAD_NAMESPACE_URI,
                     "Non-ascii characters in a namespace URI may not be completely portable: <"+
                     u.toString()+">. Resulting RDF URI references are legal.");
                
                if (uri.startsWith(rdfns) && !uri.equals(rdfns))
                    warning(
                        WARN_BAD_RDF_NAMESPACE_URI,
                        "Namespace URI ref <"
                            + uri
                            + "> may not be used in RDF/XML.");
                if (uri.startsWith(xmlns) && !uri.equals(xmlns))
                    warning(
                        WARN_BAD_XML_NAMESPACE_URI,
                        "Namespace URI ref <"
                            + uri
                            + "> may not be used in RDF/XML.");
//            } catch (URISyntaxException m) {
                // TODO: make this a bit cleaner here:
            } catch (Exception m) {
                                warning(
                    WARN_BAD_NAMESPACE_URI,
                    "Illegal URI in xmlns declaration: " + uri);
            }
    }

}
