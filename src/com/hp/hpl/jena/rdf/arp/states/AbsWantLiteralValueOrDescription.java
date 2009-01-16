/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.impl.AbsXMLContext;
import com.hp.hpl.jena.rdf.arp.impl.AttributeLexer;

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

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
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

