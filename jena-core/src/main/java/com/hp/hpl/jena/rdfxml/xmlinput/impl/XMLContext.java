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
 * XMLContext.java
 *
 * Created on July 10, 2001, 2:35 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import org.xml.sax.SAXParseException;
import org.apache.jena.iri.IRI;

import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;
import com.hp.hpl.jena.rdfxml.xmlinput.lang.LanguageTagCodes ;

/**
 * 
 * Both the baseURI and the lang may be tainted with errors. They should not be
 * accessed without providing a taint object to propogate such tainting.
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

    protected XMLContext(XMLHandler h, IRI uri, Taint baseT) {
        super(!h.ignoring(IGN_XMLBASE_SIGNIFICANT), null, uri, baseT, "",
                new TaintImpl());
    }

    private XMLContext(XMLHandler h, IRI baseMaybeWithFrag)
            throws SAXParseException {
        this(h, baseMaybeWithFrag.create(""), baseMaybeWithFrag);
    }

    private XMLContext(XMLHandler h, IRI base,
            IRI baseMaybeWithFrag) throws SAXParseException {
        this(h, base, initTaint(h, baseMaybeWithFrag));
    }

    XMLContext(boolean b, AbsXMLContext document, IRI uri,
            Taint baseT, String lang, Taint langT) {
        super(b, document, uri, baseT, lang, langT);
    }

    @Override
    boolean keepDocument(XMLHandler forErrors) {
        return true;
    }

    boolean isSameAsDocument() {
        return this == document
                || (uri == null ? document.uri == null : uri
                        .equals(document.uri));
    }

    @Override
    AbsXMLContext clone(IRI u, Taint baseT, String lng,
            Taint langT) {
        return new XMLContext(true, document, u, baseT, lng, langT);
    }

    void baseUsed(XMLHandler forErrors, Taint taintMe, String relUri,
            String resolvedURI) throws SAXParseException {

        if (document == null || relUri.equals(resolvedURI))
            return;
        if (!isSameAsDocument()) {
            String other = document.uri.create(relUri).toString();
            if (!other.equals(resolvedURI)) {
                forErrors.warning(taintMe, IGN_XMLBASE_SIGNIFICANT,
                        "Use of attribute xml:base changes interpretation of relative URI: \""
                                + relUri + "\".");
            }
        }
    }

    @Override
    void checkBaseUse(XMLHandler forErrors, Taint taintMe, String relUri,
            IRI rslt) throws SAXParseException {
        if (document == null)
            return;

        String resolvedURI = rslt.toString();
        if (relUri.equals(resolvedURI))
            return;
        if (!isSameAsDocument()) {
            String other = document.uri.create(relUri).toString();
            if (!other.equals(resolvedURI)) {
                forErrors.warning(taintMe, IGN_XMLBASE_SIGNIFICANT,
                        "Use of attribute xml:base changes interpretation of relative URI: \""
                                + relUri + "\".");
            }
        }

    }

}
