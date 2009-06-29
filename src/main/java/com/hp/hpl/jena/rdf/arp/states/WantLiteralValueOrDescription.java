/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.impl.ARPString;
import com.hp.hpl.jena.rdf.arp.impl.AbsXMLContext;

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

