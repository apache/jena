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

package org.apache.jena.rdfxml.xmlinput0.impl;

import java.util.Iterator ;
import java.util.regex.Pattern ;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIComponents ;
import org.apache.jena.iri.Violation ;
import org.apache.jena.iri.ViolationCodes ;
import org.apache.jena.rdfxml.xmlinput0.ARPErrorNumbers;
import org.xml.sax.SAXParseException ;

public abstract class AbsXMLContext implements ARPErrorNumbers {

    protected final String lang;

    protected final Taint langTaint;

    final Taint baseTaint;

    protected final IRI uri;

    protected final AbsXMLContext document;

    protected AbsXMLContext(boolean useDoc, AbsXMLContext document, IRI uri, Taint baseT, String lang, Taint langT) {
        this.lang = lang;
        langTaint = langT;
        baseTaint = baseT;
        this.uri = uri;
        this.document = useDoc ? (document == null ? this : document) : null;
    }

    protected static Taint initTaint(XMLHandler h, IRI base) throws SAXParseException {
        Taint rslt = new TaintImpl();
        checkURI(h, rslt, base);
        return rslt;
    }

    public AbsXMLContext withBase(XMLHandler forErrors, String b) throws SAXParseException {
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

    protected AbsXMLContext withLang(XMLHandler forErrors, String l) throws SAXParseException {
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

    final IRI resolveAsURI(XMLHandler forErrors, Taint taintMe, String relUri)
            throws SAXParseException {
        return resolveAsURI(forErrors, taintMe, relUri, true);
    }

    final IRI resolveAsURI(XMLHandler forErrors, Taint taintMe, String relUri, boolean checkBaseUse)
            throws SAXParseException {
        IRI rslt = uri.create(relUri);

        if (checkBaseUse)
            checkBaseUse(forErrors, taintMe, relUri, rslt);

        checkURI(forErrors, taintMe, rslt);

        return rslt;
    }

    abstract void checkBaseUse(XMLHandler forErrors, Taint taintMe, String relUri, IRI rslt)
            throws SAXParseException;

    protected static void checkURI(XMLHandler forErrors, Taint taintMe, IRI rslt)
            throws SAXParseException {
        if (rslt.hasViolation(false)) {
            Iterator<Violation> it = rslt.violations(false);
            while (it.hasNext()) {
                Violation irie = it.next();
                // if (irie.getViolationCode() ==
                // ViolationCodes.REQUIRED_COMPONENT_MISSING)
                String msg = irie.getShortMessage();
                if (irie.getViolationCode() == ViolationCodes.REQUIRED_COMPONENT_MISSING
                        && irie.getComponent() == IRIComponents.SCHEME) {
                    if (!forErrors.allowRelativeURIs())
                        forErrors.warning(taintMe, WARN_RELATIVE_URI,
                                "Relative URIs are not permitted in RDF: specifically <"+rslt.toString()+">");

                } else
                    forErrors.warning(taintMe, WARN_MALFORMED_URI, "Bad URI: " + msg);
            }
        }
    }

    public String resolve(XMLHandler forErrors, Taint taintMe, String u)
            throws SAXParseException {
        return resolveAsURI(forErrors, taintMe, u, true).toString();
    }

    private static Pattern langPattern = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*") ;

    /* This is just a light syntactic check of the language tag.
     * See JENA-827.
     * Jena, when parsing RDF/XML, used to check the syntax and against (an encoded copy of) the IANA registry.
     * Elsewhere, Turtle et al and SPARQL, Jena has always only performed this syntax check.
     */
    private void checkXMLLang(XMLHandler arp, Taint taintMe, String newLang) throws SAXParseException {
        if (newLang.equals(""))
            return;
        if (newLang.equalsIgnoreCase("und") )
            arp.warning(taintMe, WARN_BAD_XMLLANG, "Bad language tag: "+newLang+" (not allowed)") ;
        if ( ! langPattern.matcher(newLang).matches() )
            arp.warning(taintMe, WARN_BAD_XMLLANG, "Bad language tag: "+newLang) ;
    }
}
