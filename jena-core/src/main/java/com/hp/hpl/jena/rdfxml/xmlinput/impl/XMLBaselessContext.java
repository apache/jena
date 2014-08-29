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

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import org.xml.sax.SAXParseException;
import org.apache.jena.iri.IRI;

import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;

public class XMLBaselessContext extends AbsXMLContext implements ARPErrorNumbers {

    final int errno;

    final String errmsg;

    public XMLBaselessContext(XMLHandler f, int eno) {
      this(f,eno,f.sameDocRef());
    }
//    XMLBaselessContext(XMLHandler f, int eno, String baseURI) {
//        this(f,eno,f.iriFactory().create(baseURI).create(""));
//    }
    XMLBaselessContext(XMLHandler f, int eno, IRI baseURI) {
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

    private XMLBaselessContext(AbsXMLContext document, IRI uri,
            Taint baseT, String lang, Taint langT, XMLBaselessContext parent) {
        super(true, document, uri, baseT, lang, langT);
        errno = parent.errno;
        errmsg = parent.errmsg;
    }

    @Override
    AbsXMLContext clone(IRI u, Taint baseT, String lng,
            Taint langT) {
        return new XMLBaselessContext(document, u, baseT, lng, langT, this);
    }

    @Override
    public AbsXMLContext withBase(XMLHandler forErrors, String b)
            throws SAXParseException {
        TaintImpl taintB = new TaintImpl();
        IRI newB = resolveAsURI(forErrors, taintB, b, false);
        if (newB.isRelative() )
            return new XMLBaselessContext(forErrors,errno,newB.create(""));
        
        if (newB.hasViolation(false))
            return new XMLBaselessContext(forErrors,ERR_RESOLVING_AGAINST_MALFORMED_BASE,newB);
        return new XMLContext(keepDocument(forErrors), document, newB
                .create(""), taintB, lang, langTaint);
    }

    @Override
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
    @Override
    void checkBaseUse(XMLHandler forErrors, Taint taintMe, String relUri, IRI rslt) throws SAXParseException {

        String resolvedURI = rslt.toString();
        if (relUri.equals(resolvedURI) && rslt.isAbsolute())
            return;
        
        forErrors.warning(taintMe, errno, errmsg + ": <" + relUri + ">");
       
    }

}
