/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.impl.ANode;
import com.hp.hpl.jena.rdf.arp.impl.ARPResource;
import com.hp.hpl.jena.rdf.arp.impl.AbsXMLContext;
import com.hp.hpl.jena.rdf.arp.impl.AttributeLexer;
import com.hp.hpl.jena.rdf.arp.impl.ElementLexer;
import com.hp.hpl.jena.rdf.arp.impl.URIReference;
import com.hp.hpl.jena.rdf.arp.impl.XMLHandler;

abstract public class WantDescription extends Frame implements HasSubjectFrameI {

    public WantDescription(FrameI s, AbsXMLContext x) {
        super(s, x);
    }
    public WantDescription(FrameI s, AttributeLexer x) throws SAXParseException {
        super(s, x);
    }
    public WantDescription(XMLHandler handler, AbsXMLContext x) {
        super(handler,x);
    }
    ANode subject;
    boolean subjectIsBlank = false;
    @Override
    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXParseException {
        clearSubject();

        if (uri==null || uri.equals("")) {
            warning(WARN_UNQUALIFIED_ELEMENT,"Unqualified typed nodes are not allowed. Type treated as a relative URI.");
        }
        AttributeLexer ap = new AttributeLexer(this,
                A_XMLLANG| A_XMLBASE | A_XML_OTHER |
                // legal rdf:
                A_ID | A_NODEID | A_ABOUT | A_TYPE,
                // bad rdf:
                A_BADATTRS );
        
        ap.processSpecials(taint,atts);
        
        AbsXMLContext x = ap.xml(xml);
        
        if (ap.id!=null){
            subject = URIReference.fromID(this, x, ap.id);
        }
        if (ap.about!=null) {
            if (subject != null)
                warning(ERR_SYNTAX_ERROR,"Both ID and about");
            subject = URIReference.resolve(this,x,ap.about);
            
        }
        if (ap.nodeID!=null) {
            if (subject != null) {
                if (ap.about!=null)
                    warning(ERR_SYNTAX_ERROR,"Both nodeID and about");
                if (ap.id != null)
                    warning(ERR_SYNTAX_ERROR,"Both ID and nodeID");
            }
            subject = new ARPResource(arp,ap.nodeID);
            checkXMLName(subject,ap.nodeID);
            subjectIsBlank = true;
        }
        if (subject==null) {
            subject = new ARPResource(arp);
            subjectIsBlank = true;
        } 
        ElementLexer el = new ElementLexer(taint,this,uri,localName,
                rawName,
                E_DESCRIPTION,
                CoreAndOldTerms|E_LI, true);
        if (taint.isTainted())
            subject.taint();
        if (!el.goodMatch) {
            URIReference type = URIReference.fromQName(this,uri,localName);
            if (el.badMatch && taint.isTainted()) {
              type.taint();  
            } 
            triple(subject,RDF_TYPE,type);
        }
            
        processPropertyAttributes(ap,atts,x);
        
        return new WantPropertyElement(this,x);
   }
    private void clearSubject() {
        if (subjectIsBlank)
            arp.endLocalScope(subject);
        subject = null;
        subjectIsBlank = false;
    }

 
    @Override
    public void aPredAndObj(ANode p, ANode o) {
        triple(subject,p,o);
        
    }

    @Override
    public void makeSubjectReificationWith(ANode r) {
        triple(r,RDF_SUBJECT,subject);
    }
    @Override
    public void endElement() throws SAXParseException {
        clearSubject();
    }
    @Override
    public void abort() {
        clearSubject();
    }
}


/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 
