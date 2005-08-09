/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.util.Collection;

import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.RDFURIReference;

/**
 * The IRI "", i.e. a same doc ref.
 * Misimplemented in java.net.URI, hence this class
 * with a work-around in resolveAgainst.
 * @author Jeremy J. Carroll
 *
 */
public class EmptyIRI extends AbsIRI {

    final IRIException exception;
    
    public EmptyIRI(IRIFactory f) {
        this(f,"The URI \"\" is a same document reference, and is not absolute, and cannot be resolved against.");
    }
    
    EmptyIRI(IRIFactory f,String msg) {
        super(f);
        exception = new IRIException(Absolute_URI,f,msg);
    }

    public EmptyIRI(IRIException e, AbsIRI p) {
        super(p);
        exception = e;
    }

    RDFURIReference resolveAgainst(JavaURIWrapper base) {
        if (base.iri.getFragment()==null)
            return base;
        String uri = base.toString();
        int hash = uri.indexOf('#');
        if (hash==-1)
            throw new IllegalArgumentException("Shouldn't happen.");
        return ((AbsIRI)factory.create(uri.substring(0,hash))).reparent(base);
    }

    public boolean isAbsolute() {
        return false;
    }

    public boolean isOpaque() {
        return false;
    }

    public boolean isRelative() {
        return true;
    }

    public boolean isRDFURIReference() {
        return false;
    }

    public boolean isIRI() {
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
        // TODO: isRelative or isAbsolute as test?
        return 
          rel.isRelative()?
           ((AbsIRI)rel).reparent(this):
                   rel;
    }

    public RDFURIReference resolve(String uri) {
        return resolve(factory.create(uri));
    }

    boolean addExceptions(int level, Collection here) {
        addException(exception,level,here);
        return true;
    }

    public RDFURIReference reparent(AbsIRI p) {
        return new EmptyIRI(exception,p);
    }

    public String toString() {
        return "";
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
 
