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

package org.apache.jena.rdfxml.xmlinput1.impl;

import java.util.regex.Pattern;

import org.apache.jena.irix.IRIx;
import org.apache.jena.rdfxml.xmlinput1.ARPErrorNumbers;
import org.apache.jena.shared.JenaException;
import org.xml.sax.SAXParseException ;

public abstract class AbsXMLContext implements ARPErrorNumbers {

    protected final String lang;

    protected final Taint langTaint;

    final Taint baseTaint;

    protected final IRIx uri;

    protected final AbsXMLContext document;

    protected AbsXMLContext(boolean useDoc, AbsXMLContext document, IRIx uri, Taint baseT, String lang, Taint langT) {
        this.lang = lang;
        langTaint = langT;
        baseTaint = baseT;
        this.uri = uri;
        this.document = useDoc ? (document == null ? this : document) : null;
    }

    protected static Taint initTaint(XMLHandler h, IRIx base) throws SAXParseException {
        Taint rslt = new TaintImpl();
        checkURI(h, rslt, base);
        return rslt;
    }

    public AbsXMLContext withBase(XMLHandler forErrors, String baseURI) throws SAXParseException {

        TaintImpl taintB = new TaintImpl();
        IRIx newBase = resolveAsURI(forErrors, taintB, baseURI, false);

        if ( newBase.isRelative())
            return new XMLBaselessContext(forErrors,ERR_RESOLVING_AGAINST_RELATIVE_BASE, newBase);

        if ( newBase.hasViolations() )
            return new XMLBaselessContext(forErrors, ERR_RESOLVING_AGAINST_MALFORMED_BASE, newBase);

        return new XMLContext(keepDocument(forErrors), document, newBase, taintB, lang, langTaint);
    }

    abstract boolean keepDocument(XMLHandler forErrors);

    protected AbsXMLContext withLang(XMLHandler forErrors, String l) throws SAXParseException {
        Taint taint = new TaintImpl();
        checkXMLLang(forErrors, taint, l);
        return clone(uri, baseTaint, l, taint);
    }

    abstract AbsXMLContext clone(IRIx base, Taint baseT, String l, Taint langT);

    public String getLang(Taint taint) {
        if (langTaint.isTainted())
            taint.taint();
        return lang;
    }

    final IRIx resolveAsURI(XMLHandler forErrors, Taint taintMe, String relUri)
            throws SAXParseException {
        return resolveAsURI(forErrors, taintMe, relUri, true);
    }

    final IRIx resolveAsURI(XMLHandler forErrors, Taint taintMe, String relUri, boolean checkBaseUse)
            throws SAXParseException {

        IRIx rslt = uri.resolve(relUri);

        if (checkBaseUse)
            checkBaseUse(forErrors, taintMe, relUri, rslt);

        checkURI(forErrors, taintMe, rslt);

        return rslt;
    }

    abstract void checkBaseUse(XMLHandler forErrors, Taint taintMe, String relUri, IRIx rslt)
            throws SAXParseException;

    protected static void checkURI(XMLHandler forErrors, Taint taintMe, IRIx rslt) throws SAXParseException {
        try {
            if ( rslt.isRelative() && !forErrors.allowRelativeURIs() ) {
                forErrors.warning(taintMe, WARN_RELATIVE_URI,
                                  "Relative URIs are not permitted in RDF: specifically <"+rslt.toString()+">");
            }
        } catch (SAXParseException ex) {
            throw new JenaException("SAXParseException: "+ex.getMessage());
        }

        rslt.handleViolations((isError, msg) ->{
            try {
                forErrors.warning(taintMe, WARN_MALFORMED_URI, "Bad URI: " + msg);
            } catch (SAXParseException ex) {
                throw new JenaException("SAXParseException: "+ex.getMessage());
            }
        });

//        if (rslt.hasViolation(false)) {
//            Iterator<Violation> it = rslt.violations(false);
//            while (it.hasNext()) {
//                Violation irie = it.next();
//                // if (irie.getViolationCode() ==
//                // ViolationCodes.REQUIRED_COMPONENT_MISSING)
//                String msg = irie.getShortMessage();
//                if (irie.getViolationCode() == ViolationCodes.REQUIRED_COMPONENT_MISSING
//                        && irie.getComponent() == IRIComponents.SCHEME) {
//                    if (!forErrors.allowRelativeURIs())
//                        forErrors.warning(taintMe, WARN_RELATIVE_URI,
//                                "Relative URIs are not permitted in RDF: specifically <"+rslt.toString()+">");
//
//                } else
//                    forErrors.warning(taintMe, WARN_MALFORMED_URI, "Bad URI: " + msg);
//            }
//        }
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
