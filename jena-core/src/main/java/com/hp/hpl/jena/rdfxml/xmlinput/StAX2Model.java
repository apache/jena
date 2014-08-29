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

package com.hp.hpl.jena.rdfxml.xmlinput;

import com.hp.hpl.jena.rdf.model.Model;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * A collection of convenient functions to parse an XML stream using ARP.
 * They simply connect {@link StAX2SAX} to {@link SAX2Model} internally.
 * 
 * @see StAX2SAX
 */
public class StAX2Model {
    
    public static void read(XMLStreamReader streamReader, Model model)
            throws SAXParseException, XMLStreamException, SAXException {
        read(streamReader, model, "");
    }
    
    public static void read(XMLStreamReader streamReader, Model model, String base) 
            throws SAXParseException, XMLStreamException, SAXException {
        StAX2SAX s2s = new StAX2SAX(SAX2Model.create(base, model));
        s2s.parse(streamReader);
    }
    
    public static void read(XMLEventReader eventReader, Model model)
            throws SAXParseException, XMLStreamException, SAXException {
        read(eventReader, model, "");
    }
    
    public static void read(XMLEventReader eventReader, Model model, String base) 
            throws SAXParseException, XMLStreamException, SAXException {
        StAX2SAX s2s = new StAX2SAX(SAX2Model.create(base, model));
        s2s.parse(eventReader);
    }
}
