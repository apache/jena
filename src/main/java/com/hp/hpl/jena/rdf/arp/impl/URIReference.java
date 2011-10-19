/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 
 * * $Id: URIReference.java,v 1.1 2009-06-29 08:55:38 castagna Exp $
 
 AUTHOR:  Jeremy J. Carroll
 */
/*
 * URIReference.java
 *
 * Created on June 25, 2001, 9:58 PM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.states.Frame;


// TODO: not for 2.3 IRI spec conformance

/**
 * 
 * @author jjc
 * 
 */
public class URIReference extends TaintImpl implements AResourceInternal, ARPErrorNumbers {

    /** Creates new URIReference */
    final private String uri;

    // URIReference(Location l, AbsXMLContext ctxt,String uri) throws
    // URISyntaxException, ParseException {
    //        
    // // this.uri = new URI(ctxt.getURI(),URIref.encode(uri));
    // this.uri = ctxt.resolve(l, uri);
    // }
    protected URIReference(String uri) {
        // this.uri = new URI(URIref.encode(uri));
        this.uri = uri;
        if (uri==null)
            throw new NullPointerException();
    }

//    URIReference() {
//        uri = null;
//    }

    @Override
    public String toString() {
        return uri;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public String getAnonymousID() {
        return null;
    }

    @Override
    public String getURI() {
        return uri;
    }

    @Override
    public Object getUserData() {
        throw new IllegalStateException(
                "User data only supported on blank nodes");
    }

    @Override
    public void setUserData(Object d) {
        throw new IllegalStateException(
                "User data only supported on blank nodes");
    }

    /**
     * Does not compare userData field, only URI.
     */
    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof URIReference)
                && uri.equals(((URIReference) o).uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.rdf.arp.AResource#hasNodeID()
     */
    @Override
    public boolean hasNodeID() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.rdf.arp.AResourceInternal#setHasBeenUsed()
     */
    @Override
    public void setHasBeenUsed() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.rdf.arp.AResourceInternal#getHasBeenUsed()
     */
    @Override
    public boolean getHasBeenUsed() {
        throw new UnsupportedOperationException("Internal error");
    }

    /**
     * 
     * @param f
     *            A frame for error reporting. AbsXMLContext of frame is ignored.
     * @param x
     *            The XML context for the base URI
     * @param name
     *            The local name
     * @return The resulting URI
     * @throws SAXParseException
     */
    public static URIReference fromID(Frame f, AbsXMLContext x, String name)
            throws SAXParseException {
        // Other errors are checked for by the AttributeLexer
        URIReference rslt = resolve(f,x,"#"+name);
        f.checkIdSymbol(rslt,x,name);
        return rslt;
        
    }

    /**
     * 
     * @param f
     *            A frame for error reporting. AbsXMLContext of frame is ignored.
     * @param ctxt
     *            The XML context for the base URI
     * @param uri
     *            Input string, may be relative etc.
     * @return The resolved URI
     * @throws SAXParseException
     */
    public static URIReference resolve(Frame f, AbsXMLContext ctxt, String uri)
            throws SAXParseException {

        Taint taintMe = new TaintImpl();
        IRI iri = ctxt.resolveAsURI(f.arp,taintMe,uri);
        f.checkEncoding(taintMe,uri);

        URIReference rslt = new URIReference(iri.toString());
        if (taintMe.isTainted())
            rslt.taint();
        return rslt;
    }

    public static URIReference fromQName(Frame f, String ns, String local)
            throws SAXParseException {
        URIReference rslt = new URIReference(ns + local);
        f.checkEncoding(rslt,local);
        // TODO: not for 2.3 move some of the check upwards ...
        IRI iri = f.arp.iriFactory().create(ns+local);
        AbsXMLContext.checkURI(f.arp,rslt,iri);
        return rslt;
    }

    public static URIReference createNoChecks(String uri) {
        return new UntaintableURIReference(uri);
    }
}
