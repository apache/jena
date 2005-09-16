/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.net.URISyntaxException;
import java.util.Collection;

import com.hp.hpl.jena.iri.*;
import com.hp.hpl.jena.rdf.arp.MalformedURIException;

public class BadIRI extends AbsIRI {
    final String iri;
    final IRIException exception1, exception2;
    public BadIRI(IRIException e1, IRIException e2, IRIFactory f, String s) {
        super(f);
        iri = s;
        exception1 = e1;
        exception2 = e2;
    }
    public BadIRI(IRIException e1, IRIException e2, AbsIRI p, String s) {
        super(p);
        iri = s;
        exception1 = e1;
        exception2 = e2;
    }
    public boolean isAbsolute() {
        return false;
    }
    public boolean isOpaque() {
        return false;
    }
    public boolean isRelative() {
        return false;
    }
    public boolean isRDFURIReference() {
        return false;
    }
    public boolean isIRI() {
        return false;
    }
    public boolean isJavaNetURI() {
        return false;
    }
    public boolean isVeryBad() {
        return true;
    }
    public boolean isXSanyURI() {
        return false;
    }
    public RDFURIReference resolve(RDFURIReference rel) {
        return ((AbsIRI)rel).reparent(this);
    }
    public RDFURIReference resolve(String uri) {
        return ((AbsIRI)factory.create(uri)).reparent(this);
    }
    RDFURIReference resolveAgainst(JavaURIWrapper base) {
        try {
        return new JavaURIWrapper(base,
                base.iri.resolve(iri));
        }
        catch (IllegalArgumentException e) {
            IRIException irie1 = null;
            try {
                irie1 = factory.wrapJavaNetException((URISyntaxException)e.getCause());
                return
                new XercesURIWrapper(irie1,
                        factory,base.toString()).resolve(this);
            } catch (MalformedURIException mue) {
                return new BadIRI(irie1,factory.wrapXercesException(mue),
                        this,iri);
            }
        }
    }
    boolean addExceptions(int level, Collection here) {
        addException(exception1,level,here);
        addException(exception2,level,here);
        return true;
    }
    public RDFURIReference reparent(AbsIRI badIRI) {
        return new BadIRI(exception1,exception2,badIRI,iri);
    }

    public boolean isURIinASCII() {
        return false;
    }

    public String toString() {
        return iri;
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
 
