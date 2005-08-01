/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.impl.AttributeLexer;
import com.hp.hpl.jena.rdf.arp.impl.ElementLexer;
import com.hp.hpl.jena.rdf.arp.impl.XMLContext;
import com.hp.hpl.jena.rdf.arp.impl.XMLHandler;

public class WantRDFFrame extends Frame {

    public WantRDFFrame(FrameI s, AttributeLexer x)  throws SAXParseException {
        super(s, x);
    }

    public WantRDFFrame(XMLHandler s, XMLContext x) {
        super(s, x);
    }

    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXParseException {
        ElementLexer el = new ElementLexer(this,uri,localName,rawName,
                    E_RDF,0);
        if (el.goodMatch)  {
                AttributeLexer ap = new AttributeLexer(this, A_XMLBASE
                        | A_XMLLANG | A_XML_OTHER, 0);
                if (ap.processSpecials(atts) != atts.getLength()) {
                    warning(ERR_SYNTAX_ERROR,"Illegal attributes on rdf:RDF");
                }
                arp.startRDF();
                return new WantTopLevelDescriptionFrame(this, ap); 
        }
        AttributeLexer ap = new AttributeLexer(this, A_XMLBASE
                | A_XMLLANG, 0);
        ap.processSpecials(atts);
        return new WantRDFFrame(this, ap);

    }

    public void characters(char[] ch, int start, int length) {
        // ignore
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

