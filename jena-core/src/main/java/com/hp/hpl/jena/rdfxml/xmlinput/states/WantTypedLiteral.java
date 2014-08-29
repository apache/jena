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

import com.hp.hpl.jena.rdfxml.xmlinput.impl.ARPDatatypeLiteral ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.AbsXMLContext ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.URIReference ;

public class WantTypedLiteral extends AbsWantLiteralValueOrDescription implements FrameI {

    final URIReference dtURI;
    public WantTypedLiteral(WantsObjectFrameI p, String datatypeURI, AbsXMLContext ap)
      throws SAXParseException {
        super(p, ap);
        dtURI = URIReference.resolve(this,xml,datatypeURI);
    }
    @Override
    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXParseException {
        warning(ERR_SYNTAX_ERROR,"Cannot have XML element content <"+rawName+">as part of typed literal");
        
        return super.startElement(uri,localName,rawName,atts);
    }

    @Override
    public void endElement() throws SAXParseException {
       ARPDatatypeLiteral datatypeLiteral = new ARPDatatypeLiteral(this,getBuf().toString(),
                      dtURI);
       if (taint.isTainted())
           datatypeLiteral.taint();
    ((WantsObjectFrameI) getParent()).theObject(
              datatypeLiteral); 
       super.endElement();
    }
    @Override
    public void afterChild() {
    }
    
    

}
