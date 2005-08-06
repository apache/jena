/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.impl.ARPString;
import com.hp.hpl.jena.rdf.arp.impl.AttributeLexer;
import com.hp.hpl.jena.rdf.arp.impl.ElementLexer;
import com.hp.hpl.jena.rdf.arp.impl.Names;
import com.hp.hpl.jena.rdf.arp.impl.ParserSupport;
import com.hp.hpl.jena.rdf.arp.impl.URIReference;
import com.hp.hpl.jena.rdf.arp.impl.XMLContext;
import com.hp.hpl.jena.rdf.arp.impl.XMLHandler;

public abstract class Frame extends ParserSupport implements  Names, FrameI, ARPErrorNumbers {
    final FrameI parent;
    protected boolean nonWhiteMsgGiven = false;
    public FrameI getParent() {
        return parent;
    }
    public Frame(FrameI p, AttributeLexer ap) throws SAXParseException {
        super(p.getXMLHandler(),ap.xml(p.getXMLContext()));
        parent = p;
    }
    public Frame(FrameI p, XMLContext x) {
        super(p.getXMLHandler(),x);
        parent = p;
    }
    public Frame(XMLHandler a, XMLContext x) {
        super(a,x);
        parent = null;
    }
    public void comment(char[] ch, int start, int length) throws SAXParseException {
     // generally ignore
     }
    
//    public void checkIdSymbol(String id) throws SAXParseException {
//        checkIdSymbol(xml,id);
//    }
    /**
      * endElement is called on the state of the frame created
      * by the matching startElement.
     * @throws SAXParseException 
      *
      */
      public   void endElement() throws SAXParseException {
            // often nothing
        }
    public void processingInstruction(String target, String data)  throws SAXParseException {
       // generally ignored, maybe not what was intended.
        warning(
            WARN_PROCESSING_INSTRUCTION_IN_RDF,
            "A processing instruction is in RDF content. No processing was done. "+suggestParsetypeLiteral()
                );
     }
    void processPropertyAttributes(AttributeLexer ap,
            Attributes atts, XMLContext x) throws SAXParseException {
        if (ap.type!=null) {
            ((HasSubjectFrameI)this).aPredAndObj(RDF_TYPE,
                    URIReference.resolve(this,x,ap.type) );
        }
        int sz = atts.getLength();
        if (ap.count != sz) {
            for (int i=0;i<sz;i++) {
                if (!ap.done(i)) {
                    ((HasSubjectFrameI)this).aPredAndObj(
                            URIReference.fromQName(this,atts.getURI(i),atts.getLocalName(i)),
                            new ARPString(this,atts.getValue(i),x.getLang()));
                }
            }
        }
    }

    public void abort() {
      // nothing.
    }
    protected FrameI rdfStartElement(String uri, String localName, String rawName, Attributes atts) throws SAXParseException {
        ElementLexer el = new ElementLexer(this,uri,localName,rawName,
                    E_RDF,0);
        if (el.goodMatch)  {
                AttributeLexer ap = new AttributeLexer(this, A_XMLBASE
                        | A_XMLLANG | A_XML_OTHER, 0);
                if (ap.processSpecials(atts) != atts.getLength()) {
                    warning(ERR_SYNTAX_ERROR,"Illegal attributes on rdf:RDF");
                }
                arp.startRDF();
                return new WantTopLevelDescription(this, ap); 
        }
        AttributeLexer ap = new AttributeLexer(this, A_XMLBASE
                | A_XMLLANG, 0);
        ap.processSpecials(atts);
        return new LookingForRDF(this, ap);
    }
  
    /**
     * Additional message if mixed content is found in a syntactically
     * disallowed place. Subclasses override to suppress message.
     * @return
     */
    String suggestParsetypeLiteral() {
        return "Maybe there should be an rdf:parseType='Literal' for embedding miixed XML content in RDF.";
    }
    public void characters(char[] ch, int start, int length) throws SAXParseException {
        if ((!nonWhiteMsgGiven) && !isWhite(ch, start, length)) {
            nonWhiteMsgGiven = true;
            warning(ERR_NOT_WHITESPACE,
              "Expecting propertyElement(s). String data \"" +
              new String(ch,start,length)+
              "\" not allowed. " + suggestParsetypeLiteral());
        }
    }

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
