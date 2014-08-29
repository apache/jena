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

import com.hp.hpl.jena.rdfxml.xmlinput.impl.AbsXMLContext ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.AttributeLexer ;

abstract class AbsWantLiteralValueOrDescription extends
        WantDescription {

    private StringBuffer buf;
    
    private boolean checkComposingChar = true;

    public AbsWantLiteralValueOrDescription(FrameI s, AbsXMLContext x) {
        super(s, x);
    }

    public AbsWantLiteralValueOrDescription(FrameI s, AttributeLexer x)
            throws SAXParseException {
        super(s, x);
    }

    /**
     * It is unclear to jjc, whether we are obliged to copy the characters, or
     * whether we know that they will not be overwritten after we return. For
     * safety, I hence copy them. Normally, we have two interesting cases: a)
     * characters is called once, then endElement, and we form a literal from
     * the characters. This involves creating a string, if we used a char[] then
     * we would have another char[] to char[] copy, which is one too many. Hence
     * we use a StringBuffer. b) <eg:prop> <eg:typedNode /> </eg:prop> with two
     * lots of characters both white. This case happens from within
     * InsidePropertyElementFrame, and the second lot of characters do not get
     * here.
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXParseException {
        if (checkComposingChar)
            checkComposingChar(taint,ch, start, length);
        checkComposingChar = false;
        if (buf == null)
            buf = new StringBuffer(length);
        getBuf().append(ch, start, length);
    }

    void setBuf(StringBuffer buf) {
        this.buf = buf;
    }

    StringBuffer getBuf() {
        if (buf == null)
            buf = new StringBuffer(0);
        return buf;
    }

    boolean bufIsSet() {
        return buf != null;
    }

    @Override
    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXParseException {
        checkComposingChar = true;
        return super.startElement(uri, localName, rawName, atts);
    }

    @Override
    public void comment(char ch[], int st, int lng) {
        checkComposingChar = true;
    }

    @Override
    public void processingInstruction(String a, String b)
            throws SAXParseException {
        checkComposingChar = true;
        super.processingInstruction(a,b);

    }

}
