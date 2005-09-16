/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.impl;

import java.util.Iterator;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.*;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTag;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagCodes;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagSyntaxException;

public abstract class AbsXMLContext implements ARPErrorNumbers,
        LanguageTagCodes {

//    protected static String truncateXMLBase(String rslt) {
//        if (rslt == null)
//            return null;
//        int hash = rslt.indexOf('#');
//        if (hash != -1) {
//            return rslt.substring(0, hash);
//        }
//        return rslt;
//    }

    protected final String lang;

    private final Taint langTaint;

    final Taint baseTaint;

    protected final RDFURIReference uri;

    protected final AbsXMLContext document;

    protected AbsXMLContext(boolean useDoc, AbsXMLContext document,
            RDFURIReference uri, Taint baseT, String lang, Taint langT) {
        // this.base=base;
        this.lang = lang;
        langTaint = langT;
        baseTaint = baseT;
        this.uri = uri;
        this.document = useDoc ? (document == null ? this : document) : null;
    }

    protected static Taint initTaint(XMLHandler h, RDFURIReference base)
            throws SAXParseException {
        Taint rslt = new TaintImpl();
        checkURI(h, rslt, base);
        return rslt;
    }

    protected XMLContext withBase(XMLHandler forErrors, String b)
            throws SAXParseException {
        TaintImpl taintB = new TaintImpl();
        RDFURIReference newB = resolveAsURI(forErrors, taintB, b);
        return new XMLContext(keepDocument(forErrors), document, newB.resolve(""), taintB,
                lang, langTaint);
    }

    abstract boolean keepDocument(XMLHandler forErrors);

    protected AbsXMLContext withLang(XMLHandler forErrors, String l)
            throws SAXParseException {

        Taint taint = new TaintImpl();
        checkXMLLang(forErrors, taint, lang);
        return clone(uri, baseTaint, l, taint);
    }

    abstract AbsXMLContext clone(RDFURIReference base, Taint baseT, String l,
            Taint langT);

    public String getLang(Taint taint) {
        if (langTaint.isTainted())
            taint.taint();
        return lang;
    }

    protected RDFURIReference getURI(XMLHandler forErrors, Taint taintMe,
            String relUri) throws SAXParseException {
        baseUsed(forErrors, taintMe, relUri, null);
        if (baseTaint.isTainted())
            taintMe.taint();
        return uri;
    }

    protected RDFURIReference resolveAsURI(XMLHandler forErrors, Taint taintMe,
            String relUri) throws SAXParseException {
        RDFURIReference rslt = uri.resolve(relUri);

        checkBaseUse(forErrors, taintMe, relUri, rslt);

        checkURI(forErrors, taintMe, rslt);
        return rslt;
    }

    private void checkBaseUse(XMLHandler forErrors, Taint taintMe,
            String relUri, RDFURIReference rslt) throws SAXParseException {
        if (!(document == null || rslt.toString().equals(relUri))) {
            baseUsed(forErrors, taintMe, relUri, rslt.toString());
        }
    }

    abstract void baseUsed(XMLHandler forErrors, Taint taintMe, String relUri,
            String string) throws SAXParseException;

    protected static void checkURI(XMLHandler forErrors, Taint taintMe,
            RDFURIReference rslt) throws SAXParseException {
        boolean errorReported = false;
        if (!rslt.isRDFURIReference()) {
            if (rslt.isVeryBad()) {
                // TODO: test relative references.
                // TODO: bad URI test cases
                Iterator it = rslt
                        .exceptions(IRIConformanceLevels.RDF_URI_Reference);
                while (it.hasNext()) {
                    IRIException irie = (IRIException) it.next();
                    String msg = irie.getMessage();
                    String uri = rslt.toString();
                    if (msg.matches("[a-zA-Z.]*Exception:.*")) {
                        int colon = msg.indexOf(':');
                        msg = msg.substring(colon+1);
                    }
                    if (msg.endsWith(uri)) {
                        msg = msg.substring(0, msg.length() - uri.length())
                                + "<" + uri + ">";
                    } else {
                        msg = "<" + uri + "> " + msg;
                    }
                    errorReported = true;
                    forErrors.warning(taintMe, WARN_MALFORMED_URI, "Bad URI: "
                            + msg);
                }
            }
        }
        if ((!rslt.isAbsolute()) && (!forErrors.allowRelativeURIs()) && !errorReported)
            forErrors.warning(taintMe, WARN_RELATIVE_URI, "Relative URIs are not permitted in RDF  <"+rslt.toString()+">");
    }

    public String resolve(XMLHandler forErrors, Taint taintMe, String uri)
            throws SAXParseException {
        return resolveAsURI(forErrors, taintMe, uri).toString();
    }

    private void checkXMLLang(XMLHandler arp, Taint taintMe, String lang)
            throws SAXParseException {
        if (lang.equals(""))
            return;
        try {
            LanguageTag tag = new LanguageTag(lang);
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
                        "Use of deprecated language tag \"" + lang + "\".");
            }
            if ((tagType & LT_PRIVATE_USE) == LT_PRIVATE_USE) {
                arp.warning(taintMe, IGN_PRIVATE_XMLLANG,
                        "Use of (IANA) private language tag \"" + lang + "\".");
            } else if ((tagType & LT_LOCAL_USE) == LT_LOCAL_USE) {
                arp.warning(taintMe, IGN_PRIVATE_XMLLANG,
                        "Use of (ISO639-2) local use language tag \"" + lang
                                + "\".");
            } else if ((tagType & LT_EXTRA) == LT_EXTRA) {
                arp.warning(taintMe, IGN_PRIVATE_XMLLANG,
                        "Use of additional private subtags on language \""
                                + lang + "\".");
            }
        } catch (LanguageTagSyntaxException e) {
            arp.warning(taintMe, WARN_MALFORMED_XMLLANG, e.getMessage());
        }
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

