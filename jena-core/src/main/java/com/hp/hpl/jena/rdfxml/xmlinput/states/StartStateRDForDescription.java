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
import com.hp.hpl.jena.rdfxml.xmlinput.impl.XMLHandler ;

public class StartStateRDForDescription extends WantTopLevelDescription {

    boolean sawRdfRDF;
    
    public StartStateRDForDescription(XMLHandler handler, AbsXMLContext x) {
        super(handler, x);
    }
    
    @Override
    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXParseException {
        if (localName.equals("RDF")) {
          if (uri.equals(rdfns)) {
              sawRdfRDF = true;
            return rdfStartElement(uri, localName, rawName, atts);
          }
          warning(WARN_NOT_RDF_NAMESPACE,"Top-level "+rawName+" element is not in the RDF namespace. Probably a mistake.");
        }
        sawRdfRDF = false;
        arp.startRDF();
        return super.startElement(uri,localName,rawName,atts);
    }
    
    @Override
    public void abort() {
        if (!sawRdfRDF)
            super.abort();
    }

}
