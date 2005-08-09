/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.net.*;
import java.util.Collection;

import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.RDFURIReference;

public class JavaURIWrapper extends AbsIRI {
    final URI iri;

    final JavaURIWrapper forResolution;

    public JavaURIWrapper(IRIFactory f, String s) throws URISyntaxException {
        this(f, new URI(s));
    }

    public JavaURIWrapper(AbsIRI p, String s) throws URISyntaxException {
        this(p, new URI(s));
    }

    public JavaURIWrapper(IRIFactory f, URI uri) {
        super(f);
        iri = uri;
        forResolution = nullPathBugWorkaround();
    }

    public JavaURIWrapper(AbsIRI p, URI uri) {
        super(p);
        iri = uri;
        forResolution = nullPathBugWorkaround();
    }

    JavaURIWrapper nullPathBugWorkaround() {
        try {
//            if (iri.toString().equals("http://example.org"))
//                System.err.println("bug");
            // Work around java.net.URI bug
            // resolving "file" against "http://example.org"
            // does not insert extra "/" as required,
            // do it here.
            return ((iri.getRawPath() == null 
                     || iri.getRawPath().equals(""))
                     && (!iri.toString().endsWith("/"))
                    && (!iri.isOpaque())) ? (parent != null ? new JavaURIWrapper(
                    parent, iri.toString() + "/")
                    : new JavaURIWrapper(factory, iri.toString() + "/"))
                    : this;
        } catch (URISyntaxException e) {
            IllegalStateException ise = new IllegalStateException(
                    "Shouldn't be possible.");
            ise.initCause(e);
            throw ise;
        }
    }

    public boolean isAbsolute() {
        return iri.isAbsolute();
    }

    public boolean isOpaque() {
        return iri.isOpaque();
    }

    public boolean isRelative() {
        return !iri.isAbsolute();
    }

    public boolean isRDFURIReference() {
        return isAbsolute();
    }

    public boolean isIRI() {
        // TODO: IRI conformance - bidi ??? others ???
        return true;
    }
    

    public boolean isJavaNetURI() {
        return true;
    }

    public boolean isVeryBad() {
        return false;
    }

    public boolean isXSanyURI() {
        return true;
    }

    public RDFURIReference resolve(RDFURIReference rel) {
        // TODO: clean up here sameDocReference
        // Use this for sameDoc references
        return ((AbsIRI) rel).resolveAgainst(
                (rel.getPath()==null||rel.getPath().equals(""))?this:forResolution);
    }

    public RDFURIReference resolve(String uri) {
        return resolve(factory.create(uri));
    }

    RDFURIReference resolveAgainst(JavaURIWrapper base) {
        return isAbsolute() ? this : 
            base.isOpaque() && iri.getRawSchemeSpecificPart().equals("") ?
                    factory.create(base.toString()+iri.toString()) :
            base.isAbsolute() ? 
                    new JavaURIWrapper(
                factory, base.iri.resolve(iri)) : new JavaURIWrapper(base,
                base.iri.resolve(iri));
    }

    boolean addExceptions(int level, Collection here) {
        if ((level & Absolute_URI)!=0 && !isAbsolute()) {
            // TODO: add an exception here.
            return true;
        }
        return false;
    }

    public RDFURIReference reparent(AbsIRI p) {
        return new JavaURIWrapper(p,iri);
    }

    public String getUserinfo() {
        return iri.getRawUserInfo();
    }

    public int getPort() {
        return iri.getPort();
    }

    public String getPath() {
        return iri.getRawPath();
    }

    public String getQuery() {
        return iri.getRawQuery();
    }

    public String getFragment() {
        return iri.getRawFragment();
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

//    public String toASCIIString() {
//        
//        return iri.toASCIIString();
//    }
}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
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
 */

