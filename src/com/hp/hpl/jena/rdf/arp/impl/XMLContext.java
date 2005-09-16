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
 
 * * $Id: XMLContext.java,v 1.6 2005-09-16 10:40:00 jeremy_carroll Exp $
 
 AUTHOR:  Jeremy J. Carroll
 */
/*
 * XMLContext.java
 *
 * Created on July 10, 2001, 2:35 AM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import com.hp.hpl.jena.iri.*;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagCodes;
import com.hp.hpl.jena.rdf.arp.states.Frame;

import java.net.URISyntaxException;

import org.xml.sax.SAXParseException;

/**
 * 
 * Both the baseURI and the lang may be tainted with errors. They should not be
 * accessed without providing a taint object to propogate such tainting.
 * 
 * @author jjc
 * 
 */
public class XMLContext extends AbsXMLContext implements ARPErrorNumbers,
        LanguageTagCodes {
    // final private String base;

    /**
     * Creates new XMLContext
     * 
     * @throws SAXParseException
     */
    XMLContext(XMLHandler h, String base) throws SAXParseException {

        this(h, h.iriFactory().create(base));
    }

    protected XMLContext(XMLHandler h, RDFURIReference uri, Taint baseT) {
        super(!h.ignoring(IGN_XMLBASE_SIGNIFICANT), null, uri, baseT, "",
                new TaintImpl());
    }

    private XMLContext(XMLHandler h, RDFURIReference baseMaybeWithFrag)
            throws SAXParseException {
        this(h, baseMaybeWithFrag.resolve(""), baseMaybeWithFrag);
    }

    private XMLContext(XMLHandler h, RDFURIReference base,
            RDFURIReference baseMaybeWithFrag) throws SAXParseException {
        this(h, base, initTaint(h, baseMaybeWithFrag));
    }

    XMLContext(boolean b, AbsXMLContext document, RDFURIReference uri,
            Taint baseT, String lang, Taint langT) {
        super(b, document, uri, baseT, lang, langT);
    }

    boolean keepDocument(XMLHandler forErrors) {
        return true;
    }

    boolean isSameAsDocument() {
        return this == document
                || (uri == null ? document.uri == null : uri
                        .equals(document.uri));
    }

    AbsXMLContext clone(RDFURIReference uri, Taint baseT, String lang,
            Taint langT) {
        return new XMLContext(true, document, uri, baseT, lang, langT);
    }

    void baseUsed(XMLHandler forErrors, Taint taintMe, String relUri,
            String resolvedURI) throws SAXParseException {

        if (document == null || relUri.equals(resolvedURI))
            return;
        if (!isSameAsDocument()) {
            String other = document.uri.resolve(relUri).toString();
            if (!other.equals(resolvedURI)) {
                forErrors.warning(taintMe, IGN_XMLBASE_SIGNIFICANT,
                        "Use of attribute xml:base changes interpretation of relative URI: \""
                                + relUri + "\".");
            }
        }
    }

    void checkBaseUse(XMLHandler forErrors, Taint taintMe, String relUri,
            RDFURIReference rslt) throws SAXParseException {
        if (document == null)
            return;

        String resolvedURI = rslt.toString();
        if (relUri.equals(resolvedURI))
            return;
        if (!isSameAsDocument()) {
            String other = document.uri.resolve(relUri).toString();
            if (!other.equals(resolvedURI)) {
                forErrors.warning(taintMe, IGN_XMLBASE_SIGNIFICANT,
                        "Use of attribute xml:base changes interpretation of relative URI: \""
                                + relUri + "\".");
            }
        }

    }

}
