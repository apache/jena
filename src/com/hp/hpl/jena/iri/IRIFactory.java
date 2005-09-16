/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri;

import java.net.URISyntaxException;

import com.hp.hpl.jena.iri.impl.*;
import com.hp.hpl.jena.rdf.arp.MalformedURIException;

import org.xml.sax.Locator;

public class IRIFactory implements IRIConformanceLevels {
    final Locator locator;
    static IRIFactory d = new IRIFactory();
    
    static public IRIFactory defaultFactory() {
        return d;
    }

    public IRIFactory() {
        this(new Locator(){

            public String getPublicId() {
                return null;
            }
            public String getSystemId() {
                return null;
            }
            public int getLineNumber() {
                return -1;
            }
            public int getColumnNumber() {
                return -1;
            }});
    }

    public IRIFactory(Locator l) {
        locator = l;
    }

    public RDFURIReference create(String s) {
        if (s == null)
            return new NullIRI(this);
        if (s.equals(""))
            return new EmptyIRI(this);
        try {
            return new JavaURIWrapper(this,s);
        } catch (URISyntaxException jnue) {
            IRIException irie1 = wrapJavaNetException(jnue);
            try {
                return new XercesURIWrapper(irie1,this,s);
            } catch (MalformedURIException xue) {
                IRIException irie2 = wrapXercesException(xue);
                return new BadIRI(irie1,irie2,this,s);
            }
        }

    }


    // TODO: pull up into impl package
    /**
     * Not part of API.
     * @param xue
     * @return
     */
    public IRIException wrapXercesException(MalformedURIException xue) {
        return new IRIException(RDF_URI_Reference|Absolute_URI|
                // TODO: (not hard) relative XML Schema anyURIs have this exception ...
                XML_Schema_anyURI,this,xue);
    }

    public IRIException wrapJavaNetException(URISyntaxException jnue) {
        return new IRIException(Java_Net_URI|IRI|
             (isAlsoBadRDFURIRef(jnue)?(RDF_URI_Reference|Absolute_URI|XML_Schema_anyURI):0),
             this,jnue);
    }
    static private boolean isAlsoBadRDFURIRef(URISyntaxException jnue) {
        // TODO: (hard) gross oversimplification ...
        // TODO: (hard) java.net.URI code looks too severe for IRI spec ...
        // maybe I need a punycode wrapper of java.net.URI
        return !jnue.getReason().startsWith("Illegal character");
    }

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

