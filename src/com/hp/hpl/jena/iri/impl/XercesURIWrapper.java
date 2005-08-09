/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.util.Collection;

import com.hp.hpl.jena.iri.*;
import com.hp.hpl.jena.rdf.arp.MalformedURIException;

public class XercesURIWrapper extends AbsIRI {
    final XercesURI iri;
    final IRIException exception;
 
    public XercesURIWrapper(IRIException e,IRIFactory f, String s) throws MalformedURIException {
        this(e,f,new XercesURI(s));
    }

    public XercesURIWrapper(IRIException e,IRIFactory factory, XercesURI uri) {
        super(factory);
        iri = uri;
        exception = e;
    }

    private XercesURIWrapper(IRIException e, AbsIRI p, XercesURI uri) {
        super(p);
        iri = uri;
        exception = e;
    }

    public boolean isAbsolute() {
        return true;
    } 

    public boolean isOpaque() {
        return !iri.isGenericURI();
    }

    public boolean isRelative() {
        return false;
    }

    public boolean isRDFURIReference() {
        return true;
    }

    /**
     * This always returns false, because
     * the factory always prefers the java.net.URI
     */
    public boolean isIRI() {
        return false;
    }

    /**
     * This always returns false, because
     * the factory always prefers the java.net.URI
     */
    public boolean isJavaNetURI() {
        return false;
    }


    public boolean isVeryBad() {
        return false;
    }

    
    public boolean isXSanyURI() {
        // TODO xsAnyURI Auto-generated method stub
        // look for spaces ....
        return true;
    }

    public RDFURIReference resolve(RDFURIReference rel) {
        return resolve(rel.toString());
    }

    public RDFURIReference resolve(String uri) {
        IRIException newException = exception;
        try {
            XercesURI resolved = new XercesURI(iri,uri);
            
            RDFURIReference redo = factory.create(resolved.toString());
            if (redo.isJavaNetURI())
                return redo;
            if (redo instanceof XercesURIWrapper)
                newException = ((XercesURIWrapper)redo).exception;
            return new XercesURIWrapper(
               newException,
                    factory,resolved);
        } catch (MalformedURIException xue) {
            return new BadIRI(
                    newException,
                    factory.wrapXercesException(xue),
                    factory,uri);
        }
    }

    RDFURIReference resolveAgainst(JavaURIWrapper base) {
        return this;
    }

    boolean addExceptions(int level, Collection here) {
       addException(exception,level,here);
       return true;
    }

    public RDFURIReference reparent(AbsIRI p) {
        return new XercesURIWrapper(exception,p,iri);
    }

    public String getUserinfo() {
        return iri.getUserinfo();
    }

    public int getPort() {
        return iri.getPort();
    }

    public String getPath() {
        return iri.getPath();
    }

    public String getQuery() {
        return iri.getQueryString();
    }

    public String getFragment() {
        return iri.getFragment();
    }

    public String getHost() {
        return iri.getHost();
    }

    public String getScheme() {
        return iri.getScheme();
    }

    public String toString() {
        return iri.toString();
    }

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 
