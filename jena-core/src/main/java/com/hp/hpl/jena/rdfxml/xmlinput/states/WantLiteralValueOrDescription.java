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

import com.hp.hpl.jena.rdfxml.xmlinput.impl.ARPString ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.AbsXMLContext ;

public class WantLiteralValueOrDescription extends AbsWantLiteralValueOrDescription {

    boolean seenAnElement = false;

    public WantLiteralValueOrDescription(WantsObjectFrameI s, AbsXMLContext x) {
        super(s, x);
    }

    @Override
    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts)  throws SAXParseException {
        if (seenAnElement) {
            warning(ERR_SYNTAX_ERROR,"Multiple children of property element");
        }
        seenAnElement = true;
        if (bufIsSet()) {
            if (!isWhite(getBuf())) {

                seenNonWhiteText=true;
                warning(ERR_NOT_WHITESPACE,
                        "Cannot have both string data \"" +
                        getBuf().toString() +
                        "\" and XML data <"+ rawName +
                        "> inside a property element. Maybe you want rdf:parseType='Literal'.");
//            setBuf(null);
            } else {
                setBuf(null);
            }
        }
        FrameI rslt = super.startElement(uri, localName, rawName, atts);
        ((WantsObjectFrameI) getParent()).theObject(subject);
        return rslt;

    }

    /** flag for seen non-white.
     * Note: buf may have non-white text in, even if this falg is false.
     * The following holds:
     *    seenAnElement && non white text as occurred => seenNonWhiteText
     */
    private boolean seenNonWhiteText = false;
    @Override
    public void characters(char[] ch, int start, int length)  throws SAXParseException {
        if (seenAnElement) {
            if (!isWhite(ch, start, length)) {
                seenNonWhiteText=true;
                warning(ERR_NOT_WHITESPACE,"Cannot have both string data: \"" +
                 new String(ch,start,length)   +     
                "\"and XML data inside a property element. "+ suggestParsetypeLiteral());
            }
        }
        super.characters(ch, start, length);
    }
    @Override
    public void endElement() throws SAXParseException {
        if ((!seenAnElement)||seenNonWhiteText) {    
            ARPString literal = new ARPString(this,getBuf().toString(),xml);
            if (taint.isTainted()||seenAnElement)
                literal.taint();
            ((WantsObjectFrameI) getParent()).theObject(
              literal); 
        }
        super.endElement();
    }
  
}
