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
import com.hp.hpl.jena.rdfxml.xmlinput.impl.XMLHandler ;

public class LookingForRDF extends Frame {

    public LookingForRDF(FrameI s, AttributeLexer x)  throws SAXParseException {
        super(s, x);
    }
    
    @Override
    String suggestParsetypeLiteral() {
        return "";
    }

    public LookingForRDF(XMLHandler s, AbsXMLContext x) {
        super(s, x);
    }

    @Override
    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXParseException {
        return rdfStartElement(uri, localName, rawName, atts);

    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // ignore
    }

}
