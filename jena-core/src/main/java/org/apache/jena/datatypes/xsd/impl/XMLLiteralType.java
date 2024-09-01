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

package org.apache.jena.datatypes.xsd.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.JenaXMLInput;
import org.apache.jena.util.JenaXMLOutput;
import org.apache.jena.vocabulary.RDF;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <a href="https://www.w3.org/TR/rdf-concepts/#section-XMLLiteral">rdf:XMLLiteral</a>.
 * <p>
 * This implementation has RDF 1.1/RDF1.2 semantics.
 * <p>
 * Valid XML is legal. The value space is an XML document fragment.
 */
public class XMLLiteralType extends BaseDatatype implements RDFDatatype {

    public static String XMLLiteralTypeURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";

    /**
     * Singleton instance of the rdf:XMLLIteral datatype.
     * Prefer {@link RDF#dtXMLLiteral} in applications.
     */
    public static final RDFDatatype rdfXMLLiteral = new XMLLiteralType();

    /**
     * Singleton instance (legacy name)
     * @deprecated Prefer the constant {@link #rdfXMLLiteral} or {@link RDF#dtXMLLiteral}
     */
    @Deprecated
    public static final RDFDatatype theXMLLiteralType = rdfXMLLiteral;

    private static final String  xmlWrapperTagName  = "xml-literal-fragment";
    private static final String  xmlWrapperTagStart = "<"+xmlWrapperTagName+">";
    private static final String  xmlWrapperTagEnd   = "</"+xmlWrapperTagName+">";

    /**
     * Test where an {@link RDFDatatype} is that for {@code rdf:XMLLiteral}.
     */
    public static boolean isXMLLiteral(RDFDatatype rdfDatatype) {
        Objects.requireNonNull(rdfDatatype);
        return XMLLiteralTypeURI.equals(rdfDatatype.getURI());
    }

    private XMLLiteralType() {
        super(XMLLiteralTypeURI);
    }

    /**
     * Compares two instances of values of the given datatype.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        try {
            if ( ! value1.getDatatype().getURI().equals(XMLLiteralTypeURI) )
                return false;
            if ( ! value2.getDatatype().getURI().equals(XMLLiteralTypeURI) )
                return false;
            DocumentFragment f1 = (DocumentFragment)value1.getValue();
            DocumentFragment f2 = (DocumentFragment)value2.getValue();
            return f1.isEqualNode(f2);
        } catch (Exception ex) {
            throw new DatatypeFormatException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object parse(String lexicalForm) {
        try {
            return xmlLiteralValue(lexicalForm);
        } catch (Exception ex) {
            throw new DatatypeFormatException();
        }
    }

    /**
     * Parse a lexical form for an rdf:XMLLiteral to produce the literal value.
     * The value is an XML {@link DocumentFragment}.
     */
    public static DocumentFragment xmlLiteralValue(String string) {
        DocumentBuilder builder = newDocumentBuilder();
        builder.isNamespaceAware();
        ErrorHandlerCounting eh = new ErrorHandlerCounting();
        builder.setErrorHandler(eh);

        // Wrap in an outer start-finish to make it into a XML document
        String xmlString = xmlWrapperTagStart+string+xmlWrapperTagEnd;

        // Parse document.
        InputSource source = new InputSource(new StringReader(xmlString));
        final Document doc;
        try {
            doc = builder.parse(source);
            if ( eh.errorHappened() )
                return null;
            doc.normalizeDocument();
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException(ex);
        } catch (SAXException ex) {
            throw Lib.runtimeException(ex);
        }
        // Unwrap the outer start-finish element.
        NodeList nodeList0 = doc.getChildNodes();
        if ( nodeList0.getLength() != 1 )
            throw new JenaException("XML parser did not produce exactly one child node");
        // The DocumentFragment as a NodeList.
        NodeList nodeList = nodeList0.item(0).getChildNodes();

        // The value of an rdf:XMLLIteral is a org.w3c.dom.DocumentFragment object.
        // Build the DocumentFragment -- NB "appendChild" removes from the doc DOM
        // if the node comes form the parsed document, which is it in our situation.
        //  Hence the "item(0)" removes the nodes in order.
        DocumentFragment docFrag = doc.createDocumentFragment();
        while(nodeList.getLength() > 0) {
            Node n = nodeList.item(0);
            docFrag.appendChild(n);   // Destructive - removes from doc, changes nodeList
        }
        return docFrag;
    }

    /** {@inheritDoc} */
    @Override
    public String unparse(Object value) {
        // "unparse" value to string.
        if ( value instanceof DocumentFragment docFrag )
            return xmlDocumentFragmentToString(docFrag);
        throw new IllegalArgumentException("Value is not a ocumentFragment");
    }

    /**
     * Turn an org.w3c.dom.DocumentFragment into a string.
     */
    public static String xmlDocumentFragmentToString(DocumentFragment fragment) {
        StringWriter sw = new StringWriter();
        Source source = new DOMSource(fragment);
        try {
            Transformer transformer = JenaXMLOutput.xmlTransformer();
            // Essential for a DocumentFragment
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult output = new StreamResult(sw);
            transformer.transform(source, output);
        } catch (TransformerException ex) {
            Log.error(XMLLiteralType.class, "Failed to convert an org.w3c.dom.Node to a string", ex);
            // Fall through
        }
        return sw.toString();
    }

    private static DocumentBuilder newDocumentBuilder() {
        try {
            return docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw Lib.runtimeException(ex);
        }
    }

    private static DocumentBuilderFactory docBuilderFactory = createDocumentBuilderFactory();
    private static DocumentBuilderFactory createDocumentBuilderFactory() {
        try {
            return JenaXMLInput.newDocumentBuilderFactory();
        } catch (ParserConfigurationException ex) {
            Log.error(XMLLiteralType.class, "Failed to build a javax.xml.parsers.DocumentBuilderFactory", ex);
            return null;
        }
    }

    private static class ErrorHandlerCounting implements ErrorHandler {
        int warning = 0;
        int errors = 0;
        int fatalErrors = 0;

        boolean errorHappened() {
            return errors > 0 || fatalErrors > 0;
        }

        @Override
        public void warning(SAXParseException exception) {
            warning++;
        }

        @Override
        public void error(SAXParseException exception) {
            errors++;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            fatalErrors++;
            throw exception;
        }
    }
}
