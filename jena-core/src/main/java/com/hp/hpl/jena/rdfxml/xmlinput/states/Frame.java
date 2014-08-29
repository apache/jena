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

package com.hp.hpl.jena.rdfxml.xmlinput.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.* ;

public abstract class Frame extends ParserSupport implements Names, FrameI,
        ARPErrorNumbers {
    final FrameI parent;

    protected boolean nonWhiteMsgGiven = false;

    public Taint taint = new TaintImpl();

    @Override
    public FrameI getParent() {
        return parent;
    }

    public Frame(FrameI p, AttributeLexer ap) throws SAXParseException {
        super(p.getXMLHandler(), ap.xml(p.getXMLContext()));
        parent = p;
    }

    public Frame(FrameI p, AbsXMLContext x) {
        super(p.getXMLHandler(), x);
        parent = p;
    }

    public Frame(XMLHandler a, AbsXMLContext x) {
        super(a, x);
        parent = null;
    }

    protected void warning(int i, String msg) throws SAXParseException {
        warning(taint, i, msg);
    }
    

    @Override
    public void afterChild() {
        taint = new TaintImpl();
    }

    @Override
    public void comment(char[] ch, int start, int length)
            throws SAXParseException {
        // generally ignore
    }

    // public void checkIdSymbol(String id) throws SAXParseException {
    // checkIdSymbol(xml,id);
    // }
    /**
     * endElement is called on the state of the frame created by the matching
     * startElement.
     * 
     * @throws SAXParseException
     * 
     */
    @Override
    public void endElement() throws SAXParseException {
        // often nothing
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXParseException {
        // generally ignored, maybe not what was intended.
        warning(null, WARN_PROCESSING_INSTRUCTION_IN_RDF,
                "A processing instruction is in RDF content. No processing was done. "
                        + suggestParsetypeLiteral());
    }

    void processPropertyAttributes(AttributeLexer ap, Attributes atts,
            AbsXMLContext x) throws SAXParseException {
        if (ap.type != null) {
            ((HasSubjectFrameI) this).aPredAndObj(RDF_TYPE, URIReference
                    .resolve(this, x, ap.type));
        }
        int sz = atts.getLength();
        if (ap.count != sz) {
            for (int i = 0; i < sz; i++) {
                if (!ap.done(i)) {
                    String uri = atts.getURI(i);
                    String lName = atts.getLocalName(i);
                    URIReference pred = URIReference
                            .fromQName(this, uri, lName);
                    if (uri==null || uri.equals("")) {
                        warning(pred,WARN_UNQUALIFIED_ATTRIBUTE,
                                "Unqualified property attributes are not allowed. Property treated as a relative URI.");
                    }
                    if (rdfns.equals(uri) && !QNameLexer.isKnownRDFProperty(lName)) {
                        warning(
                                        pred,
                                        WARN_UNKNOWN_RDF_ATTRIBUTE,
                                        atts.getQName(i)
                                                + " is not a recognized RDF property or type.");
                    }
                    ((HasSubjectFrameI) this).aPredAndObj(pred, new ARPString(
                            this, atts.getValue(i), x));
                }
            }
        }
    }

    @Override
    public void abort() {
        // nothing.
    }

    protected FrameI rdfStartElement(String uri, String localName,
            String rawName, Attributes atts) throws SAXParseException {
        ElementLexer el = new ElementLexer(taint, this, uri, localName,
                rawName, E_RDF, 0, false);
        if (el.goodMatch) {
            AttributeLexer ap = new AttributeLexer(this, A_XMLBASE | A_XMLLANG
                    | A_XML_OTHER, 0);
            if (ap.processSpecials(taint, atts) != atts.getLength()) {
                warning(ERR_SYNTAX_ERROR, "Illegal attributes on rdf:RDF");
            }
            // TODO this may be one point to intercept xml:base.
            arp.startRDF();
            return new WantTopLevelDescription(this, ap);
        }
        AttributeLexer ap = new AttributeLexer(this, A_XMLBASE | A_XMLLANG, 0);
        ap.processSpecials(taint, atts);
        return new LookingForRDF(this, ap);
    }

    /**
     * Additional message if mixed content is found in a syntactically
     * disallowed place. Subclasses override to suppress message.
     * 
     */
    String suggestParsetypeLiteral() {
        return " Maybe there should be an rdf:parseType='Literal' for embedding mixed XML content in RDF.";
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXParseException {
        if ((!nonWhiteMsgGiven) && !isWhite(ch, start, length)) {
            nonWhiteMsgGiven = true;
            warning(ERR_NOT_WHITESPACE,
                    "Expecting XML start or end element(s). String data \""
                            + new String(ch, start, length)
                            + "\" not allowed." + suggestParsetypeLiteral()
                            + " Maybe a striping error.");
        }
    }

}
