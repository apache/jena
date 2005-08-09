/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 
 * * $Id: URIReference.java,v 1.3 2005-08-09 03:30:20 jeremy_carroll Exp $
 
 AUTHOR:  Jeremy J. Carroll
 */
/*
 * URIReference.java
 *
 * Created on June 25, 2001, 9:58 PM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import java.net.URISyntaxException;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.RDFURIReference;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.ParseException;
import com.hp.hpl.jena.rdf.arp.states.Frame;

;

// TODO: IRI spec conformance

/**
 * 
 * @author jjc
 * 
 */
public class URIReference implements AResourceInternal, ARPErrorNumbers {

    /** Creates new URIReference */
    final private String uri;

    // URIReference(Location l, XMLContext ctxt,String uri) throws
    // URISyntaxException, ParseException {
    //        
    // // this.uri = new URI(ctxt.getURI(),URIref.encode(uri));
    // this.uri = ctxt.resolve(l, uri);
    // }
    private URIReference(String uri) {
        // this.uri = new URI(URIref.encode(uri));
        this.uri = uri;
        if (uri==null)
            throw new NullPointerException();
    }

//    URIReference() {
//        uri = null;
//    }

    public String toString() {
        return uri;
    }

    public boolean isAnonymous() {
        return false;
    }

    public String getAnonymousID() {
        return null;
    }

    public String getURI() {
        return uri;
    }

    public Object getUserData() {
        throw new IllegalStateException(
                "User data only supported on blank nodes");
    }

    public void setUserData(Object d) {
        throw new IllegalStateException(
                "User data only supported on blank nodes");
    }

    /**
     * Does not compare userData field, only URI.
     */
    public boolean equals(Object o) {
        return o != null && (o instanceof URIReference)
                && uri.equals(((URIReference) o).uri);
    }

    public int hashCode() {
        return uri.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.rdf.arp.AResource#hasNodeID()
     */
    public boolean hasNodeID() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.rdf.arp.AResourceInternal#setHasBeenUsed()
     */
    public void setHasBeenUsed() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.rdf.arp.AResourceInternal#getHasBeenUsed()
     */
    public boolean getHasBeenUsed() {
        throw new UnsupportedOperationException("Internal error");
    }

    /**
     * 
     * @param f
     *            A frame for error reporting. XMLContext of frame is ignored.
     * @param x
     *            The XML context for the base URI
     * @param name
     *            The local name
     * @return The resulting URI
     * @throws ParseException
     */
    public static URIReference fromID(Frame f, XMLContext x, String name)
            throws SAXParseException {
        // Other errors are checked for by the AttributeLexer
        return resolve(f,x,"#"+name);
    }

    /**
     * 
     * @param f
     *            A frame for error reporting. XMLContext of frame is ignored.
     * @param x
     *            The XML context for the base URI
     * @param uri
     *            Input string, may be relative etc.
     * @return The resolved URI
     * @throws ParseException
     */
    public static URIReference resolve(Frame f, XMLContext ctxt, String uri)
            throws SAXParseException {
        f.checkEncoding(uri);
        RDFURIReference iri = ctxt.resolveAsURI(uri);
        XMLHandler arp = f.arp;
        if (uri.indexOf(':') == -1) {
            if ((!arp.ignoring(IGN_XMLBASE_SIGNIFICANT))
                    && !ctxt.isSameAsDocument()) {
                    String other = ctxt.getDocument().resolve(uri);
                if (!other.equals(iri.toString())) {
                    f.warning(IGN_XMLBASE_SIGNIFICANT,
                            "Use of attribute xml:base changes interpretation of relative URI: \""
                                    + uri + "\".");
                }
            }
        }
        arp.checkBadURI(iri);
        return new URIReference(iri.toString());
    }

    public static URIReference fromQName(Frame f, String ns, String local)
            throws SAXParseException {
        f.checkEncoding(local);
        // TODO: move some of the check upwards ...
        RDFURIReference iri = f.arp.iriFactory().create(ns+local);
        f.checkBadURI(iri);
        return new URIReference(ns + local);
    }

    public static URIReference createNoChecks(String uri) {
        return new URIReference(uri);
    }
}
