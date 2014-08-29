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

import com.hp.hpl.jena.rdfxml.xmlinput.impl.* ;

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
