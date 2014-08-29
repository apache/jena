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

import java.util.Iterator;

import org.xml.sax.SAXParseException;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIComponents;
import org.apache.jena.iri.Violation;
import org.apache.jena.iri.ViolationCodes;

import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;
import com.hp.hpl.jena.rdfxml.xmlinput.lang.LanguageTag ;
import com.hp.hpl.jena.rdfxml.xmlinput.lang.LanguageTagCodes ;
import com.hp.hpl.jena.rdfxml.xmlinput.lang.LanguageTagSyntaxException ;

public abstract class AbsXMLContext implements ARPErrorNumbers,
        LanguageTagCodes {

    // protected static String truncateXMLBase(String rslt) {
    // if (rslt == null)
    // return null;
    // int hash = rslt.indexOf('#');
    // if (hash != -1) {
    // return rslt.substring(0, hash);
    // }
    // return rslt;
    // }

    protected final String lang;

    protected final Taint langTaint;

    final Taint baseTaint;

    protected final IRI uri;

    protected final AbsXMLContext document;

    protected AbsXMLContext(boolean useDoc, AbsXMLContext document, IRI uri,
            Taint baseT, String lang, Taint langT) {
        // this.base=base;
        this.lang = lang;
        langTaint = langT;
        baseTaint = baseT;
        this.uri = uri;
        this.document = useDoc ? (document == null ? this : document) : null;
    }

    protected static Taint initTaint(XMLHandler h, IRI base)
            throws SAXParseException {
        Taint rslt = new TaintImpl();
        checkURI(h, rslt, base);
        return rslt;
    }

//    protected AbsXMLContext withBase(XMLHandler forErrors, String b)
//            throws SAXParseException {
//        TaintImpl taintB = new TaintImpl();
//        IRI newB = resolveAsURI(forErrors, taintB, b, false);
//        // TO  DO update MALFORMED_CONTEXT
//        if (newB.isVeryBad())
//            return new XMLBaselessContext(forErrors,
//                    ERR_RESOLVING_AGAINST_MALFORMED_BASE, b);
//        return new XMLContext(keepDocument(forErrors), document, newB
//                .create(""), taintB, lang, langTaint);
//    }

    public AbsXMLContext withBase(XMLHandler forErrors, String b)
            throws SAXParseException {
        TaintImpl taintB = new TaintImpl();
        IRI newB = resolveAsURI(forErrors, taintB, b, false);
        if (newB.isRelative())
            return new XMLBaselessContext(forErrors,ERR_RESOLVING_AGAINST_RELATIVE_BASE, newB.create(""));

        if (newB.hasViolation(false))
            return new XMLBaselessContext(forErrors,
                    ERR_RESOLVING_AGAINST_MALFORMED_BASE, newB);
        return new XMLContext(keepDocument(forErrors), document, newB
                .create(""), taintB, lang, langTaint);
    }

    abstract boolean keepDocument(XMLHandler forErrors);

    protected AbsXMLContext withLang(XMLHandler forErrors, String l)
            throws SAXParseException {

        Taint taint = new TaintImpl();
        checkXMLLang(forErrors, taint, l);
        return clone(uri, baseTaint, l, taint);
    }

    abstract AbsXMLContext clone(IRI base, Taint baseT, String l, Taint langT);

    public String getLang(Taint taint) {
        if (langTaint.isTainted())
            taint.taint();
        return lang;
    }

    // protected RDFURIReference getURI(XMLHandler forErrors, Taint taintMe,
    // String relUri) throws SAXParseException {
    // baseUsed(forErrors, taintMe, relUri, null);
    // if (baseTaint.isTainted())
    // taintMe.taint();
    // return uri;
    // }
    final IRI resolveAsURI(XMLHandler forErrors, Taint taintMe, String relUri)
            throws SAXParseException {
        return resolveAsURI(forErrors, taintMe, relUri, true);
    }

    final IRI resolveAsURI(XMLHandler forErrors, Taint taintMe, String relUri,
            boolean checkBaseUse) throws SAXParseException {
        IRI rslt = uri.create(relUri);

        if (checkBaseUse)
            checkBaseUse(forErrors, taintMe, relUri, rslt);

        checkURI(forErrors, taintMe, rslt);

        return rslt;
    }

    abstract void checkBaseUse(XMLHandler forErrors, Taint taintMe,
            String relUri, IRI rslt) throws SAXParseException;

    // abstract void baseUsed(XMLHandler forErrors, Taint taintMe, String
    // relUri,
    // String string) throws SAXParseException;

    protected static void checkURI(XMLHandler forErrors, Taint taintMe, IRI rslt)
            throws SAXParseException {
        if (rslt.hasViolation(false)) {
            Iterator<Violation> it = rslt.violations(false);
            while (it.hasNext()) {
                Violation irie = it.next();
                // if (irie.getViolationCode() ==
                // ViolationCodes.REQUIRED_COMPONENT_MISSING)
                String msg = irie.getShortMessage();
                String uri = rslt.toString();
//                if (msg.endsWith(uri)) {
//                    msg = msg.substring(0, msg.length() - uri.length()) + "<"
//                            + uri + ">";
//                } else {
//                    msg = "<" + uri + "> " + msg;
//                }
                if (irie.getViolationCode() == ViolationCodes.REQUIRED_COMPONENT_MISSING
                        && irie.getComponent() == IRIComponents.SCHEME) {
                    if (!forErrors.allowRelativeURIs())
                        forErrors.warning(taintMe, WARN_RELATIVE_URI,
                                "Relative URIs are not permitted in RDF: specifically <"
                                        + rslt.toString() + ">");

                } else
                    forErrors.warning(taintMe, WARN_MALFORMED_URI, "Bad URI: "
                            + msg);
            }
        }
    }

    public String resolve(XMLHandler forErrors, Taint taintMe, String u)
            throws SAXParseException {
        return resolveAsURI(forErrors, taintMe, u, true).toString();
    }

    private void checkXMLLang(XMLHandler arp, Taint taintMe, String newLang)
            throws SAXParseException {
        if (newLang.equals(""))
            return;
        try {
            LanguageTag tag = new LanguageTag(newLang);
            int tagType = tag.tagType();
            if (tagType == LT_ILLEGAL) {
                arp.warning(taintMe, WARN_BAD_XMLLANG, tag.errorMessage());
            }
            if ((tagType & LT_UNDETERMINED) == LT_UNDETERMINED) {
                arp
                        .warning(taintMe, WARN_BAD_XMLLANG,
                                "Unnecessary use of language tag \"und\" prohibited by RFC3066");
            }
            if ((tagType & LT_IANA_DEPRECATED) == LT_IANA_DEPRECATED) {
                arp.warning(taintMe, WARN_DEPRECATED_XMLLANG,
                        "Use of deprecated language tag \"" + newLang + "\".");
            }
            if ((tagType & LT_PRIVATE_USE) == LT_PRIVATE_USE) {
                arp.warning(taintMe, IGN_PRIVATE_XMLLANG,
                        "Use of (IANA) private language tag \"" + newLang
                                + "\".");
            } else if ((tagType & LT_LOCAL_USE) == LT_LOCAL_USE) {
                arp.warning(taintMe, IGN_PRIVATE_XMLLANG,
                        "Use of (ISO639-2) local use language tag \"" + newLang
                                + "\".");
            } else if ((tagType & LT_EXTRA) == LT_EXTRA) {
                arp.warning(taintMe, IGN_PRIVATE_XMLLANG,
                        "Use of additional private subtags on language \""
                                + newLang + "\".");
            }
        } catch (LanguageTagSyntaxException e) {
            arp.warning(taintMe, WARN_MALFORMED_XMLLANG, e.getMessage());
        }
    }

}
