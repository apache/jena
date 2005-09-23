/*
 (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: XMLBaselessContext.java,v 1.2 2005-09-23 07:51:49 jeremy_carroll Exp $
 */
package com.hp.hpl.jena.rdf.arp.impl;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.RDFURIReference;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;

/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 * 
 */
public class XMLBaselessContext extends AbsXMLContext implements ARPErrorNumbers {

    final int errno;

    final String errmsg;

    public XMLBaselessContext(XMLHandler f, int eno) {
      this(f,eno,f.sameDocRef());
    }
    XMLBaselessContext(XMLHandler f, int eno, String baseURI) {
        this(f,eno,f.iriFactory().create(baseURI).resolve(""));
    }
    XMLBaselessContext(XMLHandler f, int eno, RDFURIReference baseURI) {
        super(true, null, baseURI, 
                new TaintImpl(), "",
                new TaintImpl());
        errno = eno;
        switch (errno) {
        case ERR_RESOLVING_URI_AGAINST_NULL_BASE:
            errmsg = "Base URI is null, but there are relative URIs to resolve.";
            break;
        case WARN_RESOLVING_URI_AGAINST_EMPTY_BASE:
            errmsg = "Base URI is \"\", relative URIs left as relative.";
            break;
        case ERR_RESOLVING_AGAINST_MALFORMED_BASE:
            errmsg = "Resolving against bad URI <"+baseURI+">";
            break;
        case ERR_RESOLVING_AGAINST_RELATIVE_BASE:
            errmsg = "Resolving against relative URI <"+baseURI+">";
            break;
            default:
                throw new IllegalArgumentException("Unknown error code: "+eno);
        }
    }

    private XMLBaselessContext(AbsXMLContext document, RDFURIReference uri,
            Taint baseT, String lang, Taint langT, XMLBaselessContext parent) {
        super(true, document, uri, baseT, lang, langT);
        errno = parent.errno;
        errmsg = parent.errmsg;
    }

    AbsXMLContext clone(RDFURIReference u, Taint baseT, String lng,
            Taint langT) {
        return new XMLBaselessContext(document, u, baseT, lng, langT, this);
    }

    public AbsXMLContext withBase(XMLHandler forErrors, String b)
            throws SAXParseException {
        TaintImpl taintB = new TaintImpl();
        RDFURIReference newB = resolveAsURI(forErrors, taintB, b, false);
        if (newB.isVeryBad())
            return new XMLBaselessContext(forErrors,ERR_RESOLVING_AGAINST_MALFORMED_BASE,b);
        if (newB.isRelative() )
            return new XMLBaselessContext(forErrors,errno,newB.resolve(""));
        return new XMLContext(keepDocument(forErrors), document, newB
                .resolve(""), taintB, lang, langTaint);
    }

    boolean keepDocument(XMLHandler forErrors) {
        return !forErrors.ignoring(IGN_XMLBASE_SIGNIFICANT);
    }

    boolean isSameAsDocument() {
        return this == document;
    }

    void baseUsed(XMLHandler forErrors, Taint taintMe, String relUri,
            String string) throws SAXParseException {
        forErrors.warning(taintMe, errno, errmsg + ": <" + relUri + ">");

    }
    void checkBaseUse(XMLHandler forErrors, Taint taintMe, String relUri, RDFURIReference rslt) throws SAXParseException {
       if (!rslt.isAbsolute())
        forErrors.warning(taintMe, errno, errmsg + ": <" + relUri + ">");
       
    }

}

/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP All
 * rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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