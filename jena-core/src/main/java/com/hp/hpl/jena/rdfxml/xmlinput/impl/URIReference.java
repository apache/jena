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

/*
 * URIReference.java
 *
 * Created on June 25, 2001, 9:58 PM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import org.xml.sax.SAXParseException;
import org.apache.jena.iri.IRI;

import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.Frame ;

// TODO: not for 2.3 IRI spec conformance

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
