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

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * Bridge StAX and SAX parsing.
 */
public class StAX2SAX {
    private final ContentHandler handler;
    private final LexicalHandler lhandler;
    private final XMLInputFactory xef;
    
    /**
     * Primes a converter with a SAX handler.
     * 
     * Note: if handler is also LexicalHandler it will pass on lexical events.
     * 
     * @param handler 
     */
    public StAX2SAX(ContentHandler handler) {
        this.handler = handler;
        this.lhandler = (handler instanceof LexicalHandler) ?
                (LexicalHandler) handler :
                NO_LEXICAL_HANDLER ;
        this.xef = XMLInputFactory.newInstance();
    }
    
    // I'd be happier doing stream reader, but we can only convert this way
    public void parse(XMLStreamReader xmlReader) throws XMLStreamException, SAXException {
        handler.setDocumentLocator(new LocatorConv(xmlReader));
        parse(xef.createXMLEventReader(xmlReader));
    }
    
    /**
     * Consumes all StAX events and passes them on to the content handler
     */
    public void parse(XMLEventReader xmlReader) throws XMLStreamException, SAXException {
        while (xmlReader.hasNext()) {
            XMLEvent e = xmlReader.nextEvent();
            if (e.isStartDocument()) handler.startDocument();
            else if (e.isEndDocument()) handler.endDocument();
            else if (e.isStartElement()) emitSE(e.asStartElement());
            else if (e.isEndElement()) emitEE(e.asEndElement()); 
            else if (e.isProcessingInstruction()) emitPi((ProcessingInstruction) e);
            else if (e.isCharacters()) emitChars(e.asCharacters());
            else if (e.isAttribute()) emitAttr((Attribute) e);
            else if (e.isEntityReference()) emitEnt((EntityDeclaration) e);
            else if (e.isNamespace()) emitNS((Namespace) e);
            else if (e instanceof Comment) emitComment((Comment) e);
            else if (e instanceof DTD) emitDTD((DTD) e);
            else {
                //System.err.println("Unknown / unhandled event type " + e);
                //throw new SAXException("Unknown / unhandled event type " + e);
            }            
        }
    }

    private void emitSE(StartElement se) throws SAXException {
        @SuppressWarnings("unchecked")
        Iterator<Attribute> aIter = se.getAttributes() ;
        handler.startElement(se.getName().getNamespaceURI(), 
                se.getName().getLocalPart(), qnameToS(se.getName()), convertAttrs(aIter));
        @SuppressWarnings("unchecked")
        Iterator<Namespace> it = se.getNamespaces();
        while (it.hasNext()) emitNS(it.next());
    }

    private void emitEE(EndElement ee) throws SAXException {
        handler.endElement(ee.getName().getNamespaceURI(), 
                ee.getName().getLocalPart(), qnameToS(ee.getName()));
        @SuppressWarnings("unchecked")
        Iterator<Namespace> it = ee.getNamespaces();
        while (it.hasNext()) emitNSGone(it.next());
    }

    private void emitPi(ProcessingInstruction pi) throws SAXException {
        handler.processingInstruction(pi.getTarget(), pi.getData());
    }

    private void emitChars(Characters chars) throws SAXException {
        if (chars.isIgnorableWhiteSpace()) 
            handler.ignorableWhitespace(chars.getData().toCharArray(), 
                    0, chars.getData().length());
        else
            handler.characters(chars.getData().toCharArray(), 
                    0, chars.getData().length());
    }

    private void emitAttr(Attribute attribute) {
        // nowt to do
    }

    private void emitEnt(EntityDeclaration entityDeclaration) {
        // nowt to do
    }

    private void emitNS(Namespace namespace) throws SAXException {
        // Safety check to work around nasty tests
        if (namespace.getPrefix() == null || 
                namespace.getNamespaceURI() == null) return;
        handler.startPrefixMapping(namespace.getPrefix(), namespace.getNamespaceURI());
    }
    
    private void emitNSGone(Namespace namespace) throws SAXException {
        handler.endPrefixMapping(namespace.getPrefix());
    }

    private void emitComment(Comment comment) throws SAXException {
        lhandler.comment(comment.getText().toCharArray(), 0, comment.getText().length());
    }

    private void emitDTD(DTD dtd) {
        // Is this useful??
    }
    
    private Attributes convertAttrs(Iterator<Attribute> attributes) {
        AttributesImpl toReturn = new AttributesImpl();
        while (attributes.hasNext()) {
            Attribute a = attributes.next();
            toReturn.addAttribute(a.getName().getNamespaceURI(), a.getName().getLocalPart(),
                    qnameToS(a.getName()), a.getDTDType(), a.getValue());
        }
        return toReturn;
    }

    private String qnameToS(QName name) {
        if (name.getPrefix().length() == 0) return name.getLocalPart();
        else return name.getPrefix() + ":" + name.getLocalPart();
    }
    
    static class LocatorConv implements Locator {
        private final XMLStreamReader reader;
        
        public LocatorConv(XMLStreamReader reader) { this.reader = reader; }

        @Override
        public final String getPublicId() { return reader.getLocation().getPublicId(); }
        @Override
        public final String getSystemId() { return reader.getLocation().getSystemId(); }
        @Override
        public final int getLineNumber() { return reader.getLocation().getLineNumber(); }
        @Override
        public final int getColumnNumber() { return reader.getLocation().getColumnNumber(); }
    }
    
    final static LexicalHandler NO_LEXICAL_HANDLER = new LexicalHandler() {
        @Override
        public void startDTD(String string, String string1, String string2) throws SAXException {}
        @Override
        public void endDTD() throws SAXException {}
        @Override
        public void startEntity(String string) throws SAXException {}
        @Override
        public void endEntity(String string) throws SAXException {}
        @Override
        public void startCDATA() throws SAXException {}
        @Override
        public void endCDATA() throws SAXException {}
        @Override
        public void comment(char[] chars, int i, int i1) throws SAXException {}
    };
}
