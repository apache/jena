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

package org.apache.jena.riot.lang.rdfxml.rrx;

import static org.apache.jena.riot.SysRIOT.fmtMessage;

import java.io.IOException;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.rdfxml.RDFXMLParseException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.FactoryRDF;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.XML11Char;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDF.Nodes;
import org.xml.sax.*;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.ext.LexicalHandler;

public class ParserRDFXML_SAX
        implements
            ContentHandler,
            ErrorHandler,
            EntityResolver,
            DTDHandler,
            LexicalHandler,
            DeclHandler,
            EntityResolver2 {
    public static boolean TRACE = false;
    private static boolean VERBOSE = false;
    // Addition tracing for SAX events we don't care about.
    private static boolean EVENTS = false;
    private final IndentedWriter trace;
    private final IndentedWriter traceXML;

    // ---- Constants.

    private static final String rdfNS = RDF.uri;
    private static final String xmlNS = XMLConstants.XML_NS_URI;

    // QName local names.
    private static final String rdfRDF = "RDF";
    private static final String rdfDescription = "Description";
    private static final String rdfID = "ID";
    private static final String rdfNodeID = "nodeID";
    private static final String rdfAbout = "about";
    private static final String rdfType = "type";

    private static final String rdfSeq = "Seq";
    private static final String rdfBag = "Bag";
    private static final String rdfAlt = "Alt";

    private static final String rdfDatatype = "datatype";
    private static final String rdfParseType = "parseType";
    private static final String rdfResource = "resource";

    private static final String rdfContainerItem = "li";
    private static final String rdfAboutEach = "aboutEach";
    private static final String rdfAboutEachPrefix = "aboutEachPrefix";
    private static final String rdfBagID = "bagID";

    private static final RDFDatatype rdfXmlLiteralDT = XMLLiteralType.theXMLLiteralType;

    // LN = Local name.
    private static final String xmlBaseLN = "base";
    private static final String xmlLangLN = "lang";
    // xml:space is a now-deprecated XML attribute that related to handing
    // whitespace characters inside elements.
    private static final String xmlSpaceLN = "space";

    // Grammar productions.
    // 6.2.2 Production coreSyntaxTerms
    // rdf:RDF | rdf:ID | rdf:about | rdf:parseType | rdf:resource | rdf:nodeID | rdf:datatype
    private static Set<String> $coreSyntaxTerms =
            Set.of(rdfRDF, rdfID, rdfAbout, rdfParseType, rdfResource, rdfNodeID, rdfDatatype);

    // 6.2.3 Production syntaxTerms
    // coreSyntaxTerms | rdf:Description | rdf:li
    private static Set<String> $syntaxTerms =
            Set.of(rdfRDF, rdfID, rdfAbout, rdfParseType, rdfResource, rdfNodeID, rdfDatatype,
                   rdfDescription, rdfContainerItem);

    // 6.2.4 Production oldTerms
    // rdf:aboutEach | rdf:aboutEachPrefix | rdf:bagID
    private static Set<String> $oldTerms = Set.of(rdfAboutEach, rdfAboutEachPrefix, rdfBagID);

    private static Set<String> disallowedPropertyAttributeTerms =
            Set.of(rdfRDF, rdfID, rdfAbout, rdfParseType, rdfResource, rdfNodeID, rdfDatatype,
                   rdfDescription, rdfContainerItem, "aboutEach", "aboutEachPrefix", "bagID");

    // 6.2.5 Production nodeElementURIs
    // anyURI - ( coreSyntaxTerms | rdf:li | oldTerms )
    private static boolean allowedNodeElementURIs(String namespace, String localName) {
        if ( ! rdfNS.equals(namespace) )
            return true;
        if ( $coreSyntaxTerms.contains(localName) )
            return false;
        if ( rdfContainerItem.equals(localName) )
            return false;
        if ( $oldTerms.contains(localName) )
            return false;
        return true;
    }

    // 6.2.6 Production propertyElementURIs
    // anyURI - ( coreSyntaxTerms | rdf:Description | oldTerms )
    private static boolean allowedPropertyElementURIs(String namespace, String localName) {
        if ( ! rdfNS.equals(namespace) )
            return true;
        if ( $coreSyntaxTerms.contains(localName) )
            return false;
        if ( rdfDescription.equals(localName) )
            return false;
        if ( $oldTerms.contains(localName) )
            return false;
        return true;
    }

    // 6.2.7 Production propertyAttributeURIs
    // anyURI - ( coreSyntaxTerms | rdf:Description | rdf:li | oldTerms )
    private static boolean allowedPropertyAttributeURIs(String namespace, String localName) {
        if ( ! rdfNS.equals(namespace) )
            return true;
        if ( $coreSyntaxTerms.contains(localName) )
            return false;
        if ( rdfDescription.equals(localName) )
            return false;
        if ( rdfContainerItem.equals(localName) )
            return false;
        if ( $oldTerms.contains(localName) )
            return false;
        return true;
    }

    /** The attributes that guide the RDF/XML parser. */
    private static Set<String> $rdfSyntaxAttributes =
            Set.of(rdfRDF, rdfAbout, rdfNodeID, rdfID, rdfParseType, rdfDatatype, rdfResource);
    private boolean isSyntaxAttribute(String namespace, String localName) {
        if ( ! rdfNS.equals(namespace) )
            return false;
        return $rdfSyntaxAttributes.contains(localName);
    }

    private static Set<String> $xmlReservedTerms = Set.of(xmlBaseLN, xmlLangLN, xmlSpaceLN);

    /** Recognized XML namespace Qname */
    private static boolean isXMLQName(String namespace, String localName) {
        if ( ! isXMLNamespace(namespace) )
            return false;
        return $xmlReservedTerms.contains(localName);
    }

    private static boolean isXMLNamespace(String namespace) {
        return xmlNS.equals(namespace);
    }

    private static boolean isXMLNamespaceQName(String qName) {
        if ( qName != null && (qName.equals("xmlns") || qName.startsWith("xmlns:")) )
            return true;
        return false;
    }

    // ---- Parser internal

    private record Position(int line, int column) {}
    private static String str(Position position) {
        if ( position == null )
            return "[-,-]";
        if ( position.line() < 0 && position.column() < 0 )
            return "[?,?]";
        if ( position.column() < 0 )
            return String.format("[-, Col: %d]", position.line());
        if ( position.line() < 0 )
            return String.format("[Line: %d, -]", position.column());
        return String.format("[Line: %d, Col: %d]", position.line(), position.column());
    }

    // ---- Parser internal

    // Single element node is "(!hasRDF && parserMode=NodeElement)"

    /**
     * ParserMode This directs the code at stateElement and how to complete at
     * endElement.
     */
    private enum ParserMode {
        // The first startElement is rdf:RDF, with multiple children,
        // or it is a single NodeElement.
        TOP,

        // Top level element, parseType=resource (implicit) or a nested startElement
        // inside a property.
        NodeElement,

        // Looking for the start that set "property" and parserType
        // If the immediately next tag is a start, it's a property.
        // If at the end of a property-value, an immediately starting tag is another
        // property-value.
        PropertyElement,

        // Within a property, gathering the lexical form for the object.
        ObjectLex,

        // The node implied by rdf:parseType=Resource
        ObjectParserTypeResource,

        // The object is rdf:parseType=Literal. Collecting characters of a RDF XML Literal
        ObjectParseTypeLiteral,

        // The object is rdf:parseType=Collection (RDF List)
        ObjectParseTypeCollection
    }

    /** Integer holder for rdf:li */
    private static class Counter { int value = 1; }

    /** Node holder for collection items. Holds the node for the last item added in the collection at this level. */
    private static class NodeHolder { Node node = null; }

    /** rdf:parseType for objects, with a default "Lexical" case */
    private enum ObjectParseType { Literal, Collection, Resource,
        // This is a extra parseType to indicate the "no ParseType" case
        // which is a plain lexical or nested resource.
        Plain }

    // ---- Parser output
    interface Emitter { void emit(Node subject, Node property, Node object, Position position); }

    // ---- Parser state

    private record ParserFrame(IRIx base, String lang,
                               Node subject, Node property,
                               Counter containerPropertyCounter,
                               NodeHolder collectionNode,
                               Emitter emitter,
                               ParserMode parserMode
                               ) {}

    private Deque<ParserFrame> parserStack = new ArrayDeque<>();

    // Normal case
    private void pushParserFrame() {
        pushParserFrame(parserMode);
    }

    // Called directly when ObjectLex turns out to be a resource object after all.
    private void pushParserFrame(ParserMode frameParserMode) {
        if ( TRACE )
            trace.printf("Push frame: S: %s P: %s -- mode=%s\n",
                         str(currentSubject), str(currentProperty), frameParserMode);

        ParserFrame frame = new ParserFrame(currentBase, currentLang,
                                            currentSubject, currentProperty,
                                            containerPropertyCounter,
                                            collectionNode,
                                            currentEmitter,
                                            frameParserMode);
        parserStack.push(frame);
    }

    private void popParserFrame() {
        ParserFrame frame = parserStack.pop();

        if ( TRACE ) {
            trace.printf("Pop frame: S: %s -> %s : P: %s -> %s\n", str(currentSubject), frame.subject,
                         str(currentProperty), frame.property);
        }

        this.currentBase = frame.base;
        this.currentLang = frame.lang;
        this.currentSubject = frame.subject;
        this.currentProperty = frame.property;
        this.currentEmitter = frame.emitter;
        this.collectionNode = frame.collectionNode;
        this.containerPropertyCounter = frame.containerPropertyCounter;
        this.parserMode = frame.parserMode;

        // If this frame is ParserMode.ObjectResource , then it is an implicit frame
        // inserted for the implied node. Pop the stack again to balance the push of
        // the implicit node element.
        if ( parserMode == ParserMode.ObjectParserTypeResource ) {
            popParserFrame();
            decIndent();
        }
    }

    private static String str(Node node) {
        if ( node == null )
            return "null";
        return NodeFmtLib.displayStr(node);
    }

    // ---- Error handlers

    private RiotException RDFXMLparseError(String message, Position position) {
        if ( position != null )
            errorHandler.error(message, position.line(), position.column());
        else
            errorHandler.error(message, -1, -1);
        // The error handler normally does this but for RDF/XML parsing it is required.
        return new RiotException(fmtMessage(message, position.line(), position.column())) ;
    }

    private void RDFXMLparseWarning(String message, Position position) {
        if ( position != null )
            errorHandler.warning(message, position.line(), position.column());
        else
            errorHandler.warning(message, -1, -1);
    }

    // ---- Parser Setup
    private final ParserProfile parserProfile;
    private final FactoryRDF factory;
    private final Context context;
    private final org.apache.jena.riot.system.ErrorHandler errorHandler;
    private final String initialXmlBase;
    private final String initialXmlLang;
    private final StreamRDF destination;

    // Tracking for ID on nodes (not reification usage)
    // We limit the number of local fragment IDs tracked because map only grows.
    // A base URI may be re-introduced so this isn't nested scoping.
    private int countTrackingIDs = 0;
    private Map<IRIx, Map<String,  Position>> trackUsedIDs = new HashMap<>();
    private Position previousUseOfID(String idStr, Position position) {
        Map<String, Position> scope = trackUsedIDs.computeIfAbsent(currentBase, k->new HashMap<>());
        Position prev = scope.get(idStr);
        if ( prev != null )
            return prev;
        if ( countTrackingIDs > 10000 )
            return null;
        scope.put(idStr, position);
        countTrackingIDs++;
        return null;
    }

    // -- The XML state
    private Locator locator = null;

    // ---- Parser state
    // Structure.
    private boolean hasRDF = false;
    private boolean hasDocument = false;

    // Not needed on the stack because it is only used for non-nesting object lexical.
    private RDFDatatype datatype;

    // Collecting characters does not need to be a stack because there are
    // no nested objects while gathering characters for lexical or XMLLiterals.
    private StringBuilder accCharacters = new StringBuilder(100);

    // Element depth is incremented at the end of "startElement" and decremented at
    // the beginning of "endElement". Used for collecting XML Literals.
    private int elementDepth = 0;
    private void incElementDepth() {
        if ( TRACE && VERBOSE )
            trace.printf("~~ incElementDepth %d -> %d\n", elementDepth, elementDepth + 1);
        elementDepth++;
    }

    private void decElementDepth() {
        if ( TRACE && VERBOSE )
            trace.printf("~~ decElementDepth %d -> %d\n", elementDepth, elementDepth - 1);
        --elementDepth;
    }

    // Level at which we started collecting an XML Literal.
    private int xmlLiteralStartDepth = -1;

    // Parser stack frame items.
    private IRIx currentBase;
    private String currentLang = null;
    private Node currentSubject = null;
    private Node currentProperty = null;
    private Counter containerPropertyCounter = null;        // For rdf:li
    private NodeHolder collectionNode = null;               // For parseType=Collection
    private Emitter currentEmitter = null;

    private ParserMode parserMode = ParserMode.TOP;

    private void parserMode(ParserMode parserMode) {
        this.parserMode = parserMode;
    }

// // Forming objects.
// private ParseType parseType = null;
    public ParserRDFXML_SAX(String xmlBase, ParserProfile parserProfile, StreamRDF destination, Context context) {
        // Debug
        if ( TRACE )
        {
            IndentedWriter out1 = IndentedWriter.stdout.clone().setFlushOnNewline(true).setUnitIndent(4).setLinePrefix("# ");
            this.trace = out1;
            //IndentedWriter out2 = IndentedWriter.stdout.clone().setFlushOnNewline(true).setUnitIndent(4).setLinePrefix("! ");
        } else {
            this.trace = null;
        }
        this.traceXML = this.trace;
        EVENTS = TRACE;
        // Debug

        this.parserProfile = parserProfile;
        this.factory = parserProfile.getFactorRDF();
        this.errorHandler = parserProfile.getErrorHandler();
        this.context = context;
        this.initialXmlBase = xmlBase;
        this.initialXmlLang = "";
        if ( xmlBase != null ) {
            this.currentBase = IRIx.create(xmlBase);
            parserProfile.setBaseIRI(currentBase.str());
        } else {
            this.currentBase = null;
        }
        this.currentLang = "";
        this.destination = destination;
    }

    // ---- ContentHandler

    @Override
    public void startDocument() throws SAXException {
        if ( TRACE )
            traceXML.println("Doc start");
        hasDocument = true;
    }

    @Override
    public void endDocument() throws SAXException {
        if ( TRACE )
            traceXML.println("Doc end");
    }

    @Override
    public void startElement(final String namespaceURI, final String localName, String qName, Attributes attributes) {
        if ( xmlLiteralCollecting() ) {
            if ( TRACE )
                trace.printf("startElement: XML Literal[%s]: depth = %d\n", qName, elementDepth);
            xmlLiteralCollectStartElement(namespaceURI, localName, qName, attributes);
            return;
        }

        if ( TRACE ) {
            trace.printf("%s StartElement(%s", here(), qName);
            for ( int i = 0 ; i < attributes.getLength() ; i++ ) {
                String x = attributes.getQName(i);
                String v = attributes.getValue(i);
                trace.printf(", %s=%s", x, attributes.getValue(i));
            }
            trace.printf(") mode = %s\n", parserMode);
        }
        incIndent();
        Position position = position();

        if ( TRACE )
            trace.printf("StartElement parserMode=%s\n", parserMode);

        // Special case.
        // Gathering characters for an object lexical form, then encountering a start element
        // which is a resource. This is the only case of lookahead in the RDF/XML grammar.
        switch (parserMode) {
            case ObjectLex:
                // While processing ObjectLex, we found a startElement.
                // The "ObjectLex" decision needs updating. This is a  ParserMode.NodeElement.
                // This is not parseType=Resource.
                if ( !isWhitespace(accCharacters) )
                    throw RDFXMLparseError("XML content before nested element", position);
                accCharacters.setLength(0);
                // Declare that the containing frame is expecting a node element mode.
                // Leave in parserMode=ObjectLex
                pushParserFrame(ParserMode.NodeElement);
                processBaseAndLang(attributes, position);
                break;
            default:
                // For everything else.
                pushParserFrame();
                processBaseAndLang(attributes, position);
        }

        switch (parserMode) {
            case TOP:
                // Document element: Either a one element fragment or rdf:RDF
                // rdf:RDF => nodeElementList
                // nodeElementList = ws* (nodeElement ws* )* or nodeElement
                if ( qNameMatches(rdfNS, rdfRDF, namespaceURI, localName) ) {
                    // Emits declarations.
                    processBaseAndLang(attributes, position);
                    rdfRDF(namespaceURI, localName, qName, attributes, position);
                    return;
                }
                // The top element can be a single nodeElement.
                startNodeElement(namespaceURI, localName, qName, attributes, position);
                break;
            case NodeElement:
                startNodeElement(namespaceURI, localName, qName, attributes, position);
                break;
            case PropertyElement:
                startPropertyElement(namespaceURI, localName, qName, attributes, position);
                break;
            case ObjectLex:
                // Finish ObjectLex. Generate the triple.
                Node innerSubject = attributesToSubjectNode(attributes, position);
                currentEmitter.emit(currentSubject, currentProperty, innerSubject, position);
                // This is an rdf:Description or a typed node element.
                startNodeElementWithSubject(innerSubject, namespaceURI, localName, qName, attributes, position);
                break;
            case ObjectParseTypeLiteral:
                // Handled on entry.
                throw RDFXMLparseError("Unexpected parserMode " + parserMode, position);
            case ObjectParseTypeCollection:
                startCollectionItem(namespaceURI, localName, qName, attributes, position);
                break;
            default:
                break;
        }
        incElementDepth();
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) {
        if ( qNameMatches(rdfNS, rdfRDF, namespaceURI, localName) ) {
            decIndent();
            parserMode(ParserMode.TOP);
            return;
        }

        if ( TRACE ) {
            decIndent();
            trace.printf("%s enter endElement(%s) mode = %s\n", here(), qName, parserMode);
            incIndent();
        }

        Position position = position();
        if ( xmlLiteralCollecting() ) {
            if ( TRACE )
                trace.printf("Collecting: elementDepth=%d / xmlLiteralStartDepth=%s\n", elementDepth, xmlLiteralStartDepth);
            if ( elementDepth-1 > xmlLiteralStartDepth ) {
                if ( TRACE )
                    trace.print("Continue collecting\n");
                xmlLiteralCollectEndElement(namespaceURI, localName, qName);
                return;
            }
            endXMLLiteral(position);
            if ( TRACE )
                trace.printf("**** End XML Literal[%s]: elementDepth=%d / xmlLiteralStartDepth=%s\n", qName, elementDepth, xmlLiteralStartDepth);
            // Keep going to finish the end tag.
        }

        switch (parserMode) {
            case NodeElement:
                endNodeElement(position);
                break;
            case PropertyElement:
                if ( isEndNodeElement() )
                    // Possible next property but it's a node element so no property
                    // and it's end of node, with two "end property" tags seen in a row.
                    endNodeElement(position);
                else
                    endPropertyElement(position);
                // How to tell this is end of the properties?
                break;
            case ObjectLex:
                endObjectLexical(position);
                break;
            case ObjectParseTypeLiteral:
                endObjectXMLLiteral(position);
                break;
            case ObjectParseTypeCollection:
                endCollectionItem(position);
                break;
            default:
                throw RDFXMLparseError("Inconsistent parserMode:" + parserMode, position);
        }

        popParserFrame();

        decIndent();
        decElementDepth();
        if ( TRACE )
            trace.printf("%s EndElement(%s) mode = %s\n", here(), qName, parserMode);
    }

    private void rdfRDF(String namespaceURI, String localName, String qName, Attributes attributes, Position position) {
        if ( TRACE )
            trace.println("rdf:RDF");
        if ( hasRDF )
            throw RDFXMLparseError("Nested rdf:RDF", position);
        if ( elementDepth != 0 )
            throw RDFXMLparseError("rdf:RDF not at top level", position);

        String xmlBaseURI = attributes.getValue(xmlNS, xmlBaseLN);
        if ( xmlBaseURI != null ) {
            emitBase(xmlBaseURI, position);
            currentBase = resolveIRIx(xmlBaseURI, position);
        }

        for ( int i = 0 ; i < attributes.getLength() ; i++ ) {
            String x = attributes.getQName(i);
            if ( x.startsWith("xmlns") ) {
                String prefix;
                String prefixURI;
                if ( x.equals("xmlns") ) {
                    prefix = "";
                    prefixURI = attributes.getValue(i);
                    emitPrefix(prefix, prefixURI, position);
                } else if ( x.startsWith("xmlns:") ) {
                    prefix = x.substring("xmlns:".length());
                    prefixURI = attributes.getValue(i);
                    emitPrefix(prefix, prefixURI, position);
                }
                // xmlns.... - Not an xmlns after all.
            }
        }
        hasRDF = true;
        parserMode(ParserMode.NodeElement);
    }

    /* ++ nodeElement
     *
     * start-element(URI == nodeElementURIs attributes == set((idAttr | nodeIdAttr |
     * aboutAttr )?, propertyAttr*)) propertyEltList end-element()
     *
     * ++ nodeElementURIs anyURI - ( coreSyntaxTerms | rdf:li | oldTerms ) */

    private void startNodeElement(String namespaceURI, String localName, String qName, Attributes attributes, Position position) {
        // Top level object - maybe inside rdf:RDF
        Node thisSubject = attributesToSubjectNode(attributes, position);
        startNodeElementWithSubject(thisSubject, namespaceURI, localName, qName, attributes, position);
    }

    // Subject already determined - e.g. needed for inner resource.
    private void startNodeElementWithSubject(Node thisSubject,
                                             String namespaceURI, String localName, String qName, Attributes attributes,
                                             Position position) {
        if ( TRACE )
            trace.printf("Start nodeElement: subject = %s\n", str(thisSubject));

        currentSubject = thisSubject;
        containerPropertyCounter = new Counter();

        // Check allowed tag name.
        if ( ! allowedNodeElementURIs(namespaceURI, localName) )
            throw RDFXMLparseError("Not allowed as a node element tag: '"+qName+"'", position);

        if ( ! qNameMatches(rdfNS, rdfDescription, namespaceURI, localName) ) {
            // Typed Node Element
            if ( isMemberProperty(namespaceURI, localName) )
                RDFXMLparseWarning(qName+" is being used on a typed node", position);
            else {
                if ( isNotRecognizedRDFtype(namespaceURI, localName) )
                    RDFXMLparseWarning(qName+" is not a recognized RDF term for a type", position);
            }
            Node object = qNameToIRI(namespaceURI, localName, position);
            emit(currentSubject, RDF.Nodes.type, object, position);
        }

        if ( hasPropertyAttributes(attributes, position) )
            processPropertyAttributes(currentSubject, attributes, position);
        parserMode(ParserMode.PropertyElement);
    }

    private void endNodeElement(Position position) {
        if ( TRACE )
            trace.println("endNodeElement. ParserMode = "+parserMode);
    }

    private void startPropertyElement(String namespaceURI, String localName, String qName, Attributes attributes, Position position) {
        if ( TRACE )
            trace.printf("Start propertyElement: subject = %s\n", str(currentSubject));

        if ( ! allowedPropertyElementURIs(namespaceURI, localName) )
            throw RDFXMLparseError("QName not allowed for property: "+qName, position);

        if ( isNotRecognizedRDFproperty(namespaceURI, localName) )
            RDFXMLparseWarning(qName+" is not a recognized RDF property", position);

        if ( qNameMatches(rdfNS, rdfContainerItem, namespaceURI, localName) ) {
            int i =  containerPropertyCounter.value++;
            String p = rdfNS+"_"+i;
            currentProperty = iri(p, position);
        } else
            currentProperty = qNameToIRI(namespaceURI, localName, position);

        if ( TRACE )
            trace.printf("Property = %s\n", str(currentProperty));

        String dt = attributes.getValue(rdfNS, rdfDatatype);
        datatype = (dt != null) ? NodeFactory.getType(dt) : null;

        currentEmitter = maybeReifyStatement(attributes, position);

        // Resource object and subject of further triples.

        // This will be checked for a valid IRI later.
        String rdfResourceStr = attributes.getValue(rdfNS, rdfResource);
        // Checked if the blank node is created.
        String objBlankNodeLabel = attributes.getValue(rdfNS, rdfNodeID);

        String parseTypeStr = attributes.getValue(rdfNS, rdfParseType);

        Node resourceObj = null;

        if ( rdfResourceStr != null && objBlankNodeLabel != null )
            throw RDFXMLparseError("Both rdf:resource and rdf:NodeId on a property element. Only one allowed", position);

        if ( rdfResourceStr != null &&  parseTypeStr != null )
            throw RDFXMLparseError("Both rdf:resource and rdf:ParseType on a property element. Only one allowed", position);

        if ( objBlankNodeLabel != null &&  parseTypeStr != null )
            throw RDFXMLparseError("Both rdf:NodeId and rdf:ParseType on a property element. Only one allowed", position);

        if ( rdfResourceStr != null )
            resourceObj = iriResolve(rdfResourceStr, position);

        if ( objBlankNodeLabel != null )
            resourceObj = blankNode(objBlankNodeLabel, position);

        if ( hasPropertyAttributes(attributes, position) ) {
            if ( parseTypeStr != null  ) {
                // rdf:parseType found.
                throw RDFXMLparseError("The attribute rdf:parseType is not permitted with property attributes on a property element: "+qName, position);
            }
            // AND must be empty tag
            Node innerSubject = (resourceObj==null) ? blankNode(position) : resourceObj;
            processPropertyAttributes(innerSubject, attributes, position);
            currentEmitter.emit(currentSubject, currentProperty, innerSubject, position);
            return;
        }

        if ( resourceObj != null ) {
            currentEmitter.emit(currentSubject, currentProperty, resourceObj, position);
            // And empty tag.
            return;
        }

        ObjectParseType objectParseType = objectParseType(parseTypeStr, position);

        switch (objectParseType) {
            case Plain:
                parserMode(ParserMode.ObjectLex);
                accCharacters.setLength(0);
                // This may turn into a resource object if a startTag is encountered next.
                break;
            case Resource:
                // Change of subject to a blank node subject.
                Node nested = blankNode(position);
                if ( TRACE )
                    trace.printf("Subject = %s\n", str(nested));
                currentEmitter.emit(currentSubject, currentProperty, nested, position);
                // Clear property now it's been used.
                currentProperty = null;
                // ... reset the subject
                currentSubject = nested;
                // There isn't a startElement, endElement pair for parseType=Resource.
                // Push a frame here as an implicit node frame because the subject is changing.
                // The companion "end frame" is handled in "popParserFrame" which
                // checks for parserMode=ImplicitNode
                parserMode(ParserMode.ObjectParserTypeResource);
                pushParserFrame();
                // ... expect a property element start or an end element.
                parserMode(ParserMode.PropertyElement);
                // There is nothing else special to do other than the implicit pop.
                break;
            case Literal:
                startXMLLiteral(position);
                break;
            case Collection:
                parserMode(ParserMode.ObjectParseTypeCollection);
                collectionNode = new NodeHolder();
                break;
        }
    }

    private void endPropertyElement(Position position) {
        if ( TRACE )
            trace.println("endPropertyElement");
    }

    private boolean isEndNodeElement() {
        return currentProperty == null;
    }

    //    private String xmlBaseStr(Attributes attributes, Position position) {
    //        String baseStr = attributes.getValue(xmlNS, xmlBaseLN);
    //        if ( baseStr == null )
    //            return null;
    //        return IRIs.resolve(currentBase, baseStr);
    //    }

        // Start element encountered when expecting a ObjectCollection
        private void startCollectionItem(String namespaceURI, String localName, String qName, Attributes attributes, Position position) {
            // Finish last list cell, start new one.
            if ( TRACE )
                trace.println("Generate list cell");
            // Preceding cell in list.
            Node previousCollectionNode = collectionNode.node;
            Node thisCollectionNode = blankNode(position);
            // New cell in list.
            // Either link up to the origin or fixup previous cell.
            if ( previousCollectionNode == null )
                currentEmitter.emit(currentSubject, currentProperty, thisCollectionNode, position);
            else
                emit(previousCollectionNode, Nodes.rest, thisCollectionNode, position);
            collectionNode.node = thisCollectionNode;

            // Start the item.
            Node itemSubject = attributesToSubjectNode(attributes, position);
            emit(thisCollectionNode, RDF.Nodes.first, itemSubject, position);
            startNodeElementWithSubject(itemSubject, namespaceURI, localName, qName, attributes, position);
        }

    private void endCollectionItem(Position position) {
        if ( TRACE )
            trace.println("endObjectCollectionItem");
        if ( collectionNode.node != null ) {
            emit(collectionNode.node, Nodes.rest, Nodes.nil, position);
        } else {
            // Empty list
            emit(currentSubject, currentProperty, Nodes.nil, position);
        }
    }

    private void endObjectLexical(Position position) {
        if ( TRACE )
            trace.println("endObjectLexical");
        Node object = generateLiteral(position);
        currentEmitter.emit(currentSubject, currentProperty, object, position);
        // Finished a triple.
        accCharacters.setLength(0);
    }

    private void endObjectXMLLiteral(Position position) {
        if ( TRACE )
            trace.println("endObjectXMLLiteral");
        Node object = generateXMLLiteral(position);
        currentEmitter.emit(currentSubject, currentProperty, object, position);
        namespaces = Map.of();
        stackNamespaces.clear();
        accCharacters.setLength(0);
    }

    /** Subject for a node element */
    private Node attributesToSubjectNode(Attributes attributes, Position position) {
        // Subject
        //
        // If there is an attribute a with a.URI == rdf:ID, then e.subject :=
        // uri(identifier := resolve(e, concat("#", a.string-value))).
        //
        // If there is an attribute a with a.URI == rdf:nodeID, then e.subject :=
        // bnodeid(identifier:=a.string-value).
        //
        // If there is an attribute a with a.URI == rdf:about then e.subject :=
        // uri(identifier := resolve(e, a.string-value)).
        //
        // Text quoted is implicitly "latter overrides former" but it seems ARP generate an error.

        // This will be resolved and checked for a valid IRI later.
        String iriStr = attributes.getValue(rdfNS, rdfAbout);
        // Checked when the blank node is created.
        String idStr = attributes.getValue(rdfNS, rdfID);
        // Checked when the blank node is created.
        String blankNodelabel = attributes.getValue(rdfNS, rdfNodeID);

        if ( blankNodelabel != null && iriStr != null && blankNodelabel != null )
            throw RDFXMLparseError("All of rdf:about, rdf:NodeId and rdf:ID found. Must be only one.", position);

        if ( iriStr != null && idStr != null )
            throw RDFXMLparseError("Both rdf:about and rdf:ID found. Must be only one.", position);

        if ( blankNodelabel != null && iriStr != null )
            throw RDFXMLparseError("Both rdf:about and rdf:NodeID found. Must be only one.", position);

        if ( blankNodelabel != null && idStr != null )
            throw RDFXMLparseError("Both rdf:NodeID rdf:ID found. Must be only one.", position);

        if ( iriStr != null )
            return iriResolve(iriStr, position);

        if ( idStr != null )
            return iriFromID(idStr, position);

        if ( blankNodelabel != null )
            return blankNode(blankNodelabel, position);

        // None of the above. It's a fresh blank node.
        return blankNode(position);
    }

    private void processBaseAndLang(Attributes attributes, Position position) {
        // Too early.
        IRIx base = xmlBase(attributes, position);
        if ( base != null ) {
            currentBase = base;
        }
        String lang = xmlLang(attributes, position);
        if ( lang != null )
            currentLang = lang;
    }

    // Property attributes.
    // The checking is done by the call to hasPropertyAttributes.
    private void processPropertyAttributes(Node subject, Attributes attributes, Position position) {
        for ( int i = 0 ; i < attributes.getLength() ; i++ ) {
            boolean isPropertyAttribute = checkPropertyAttribute(attributes, i, false, position);
            if ( ! isPropertyAttribute )
                continue;
            String namespace = attributes.getURI(i);
            String localName = attributes.getLocalName(i);
            String qName = attributes.getQName(i);
            propertyAttribute(subject, attributes, i, position);
        }
    }

    // Early abort! But also used to avoid creating a Node for
    // processPropertyAttributes which has no work to do.
    private boolean hasPropertyAttributes(Attributes attributes, Position position) {
        for ( int i = 0 ; i < attributes.getLength() ; i++ ) {
            boolean isPropertyAttribute = checkPropertyAttribute(attributes, i, true, position);
            if ( ! isPropertyAttribute )
                continue;
            return true;
        }
        return false;
    }

    /** Return true if this is a property attribute. */
    private boolean checkPropertyAttribute(Attributes attributes, int index, boolean outputWarnings, Position position) {
        String namespace = attributes.getURI(index);
        String localName = attributes.getLocalName(index);
        String qName = attributes.getQName(index);

        if ( namespace == null || namespace.isEmpty() ) {
            if ( outputWarnings ){
                //In SAX, xmlns: is qname, but namespace and local name are "".
                if ( ! localName.isEmpty() )    // Skip XML namespace declarations.
                    RDFXMLparseWarning("XML attribute '"+qName+"' used for RDF property attribute - ignored", position);
            }
            return false;
        }
        if ( isSyntaxAttribute(namespace, localName) )
            return false;

        if ( ! allowedPropertyAttributeURIs(namespace, localName) )
            throw RDFXMLparseError("Not allowed as a property attribute: '"+attributes.getQName(index)+"'", position);

        if ( outputWarnings && isNotRecognizedRDFproperty(namespace, localName) )
            RDFXMLparseWarning(qName+" is not a recognized RDF term for a property attribute", position);

        if ( isXMLQName(namespace, localName) )
            return false;

        if ( isXMLNamespace(namespace) ) {
            // Unrecognized qnames in the XMLnamespace are a warning and are ignored.
            RDFXMLparseWarning("Unrecognized XML attribute: '"+attributes.getQName(index)+"'", position);
            return false;
        }

        if ( isXMLNamespaceQName(qName) )
            return false;
        return true;
    }

    /** Output for a property attribute (already checked) */
    private void propertyAttribute(Node subject, Attributes attributes, int index, Position position) {
        String namespace = attributes.getURI(index);
        String localName = attributes.getLocalName(index);
        String value = attributes.getValue(index);

        if ( rdfNS.equals(namespace) ) {
            if ( rdfType.equals(localName) ) {
                Node type = iriResolve(value, position);
                emit(subject, Nodes.type, type, position);
                return;
            }
        }
        Node property = qNameToIRI(namespace, localName, position);
        String lex = value;
        Node object = literal(lex, currentLang, position);
        emit(subject, property, object, position);
    }

    private IRIx xmlBase(Attributes attributes, Position position) {
        String baseStr = attributes.getValue(xmlNS, xmlBaseLN);
        return xmlBase(baseStr);
    }

    private IRIx xmlBase(String baseStr) {
        if ( baseStr == null )
            return null;
        if ( currentBase == null )
            return null;
        return currentBase.resolve(baseStr);
    }

    private String xmlBaseStr(Attributes attributes, Position position) {
        String baseStr = attributes.getValue(xmlNS, xmlBaseLN);
        if ( baseStr == null )
            return null;
        return IRIs.resolve(currentBase, baseStr);
    }

    private String xmlLang(Attributes attributes, Position position) {
        String langStr = attributes.getValue(xmlNS, xmlLangLN);
        if ( langStr == null )
            return null; // We use null for "no language" so that explicit
        // xml:lang="" is different.
        return langStr;
    }


    private ObjectParseType objectParseType(String parseTypeStr, Position position) {
        if ( parseTypeStr == null )
            return ObjectParseType.Plain;
        try {
            return ObjectParseType.valueOf(parseTypeStr);
        } catch (IllegalArgumentException ex) {
            throw RDFXMLparseError("Not a legal value for rdf:parseType: '"+parseTypeStr+"'", position);
        }
    }

    // Whether to generate the reification as well.
    private Emitter maybeReifyStatement(Attributes attributes, Position position) {
        // Checked when the resolved IRI is created.
        String reifyId = attributes.getValue(rdfNS, rdfID);
        if ( reifyId == null )
            return this::emit;
        Node reify = iriFromID(reifyId, position);
        return (s, p, o, loc) -> emitReify(reify, s, p, o, loc);
    }

//    private String xmlBaseStr(Attributes attributes, Position position) {
//        String baseStr = attributes.getValue(xmlNS, xmlBaseLN);
//        if ( baseStr == null )
//            return null;
//        return IRIs.resolve(currentBase, baseStr);
//    }

    private Node generateLiteral(Position position) {
        String lex = accCharacters.toString();
        if ( datatype != null )
            return literalDatatype(lex, datatype, position);
        else
            return literal(lex, currentLang, position);
    }

    private Node generateXMLLiteral(Position position) {
        String lex = xmlLiteralCollectText();
        return literalDatatype(lex, rdfXmlLiteralDT, position);
    }

    // ---- SAX

    @Override
    public void setDocumentLocator(Locator locator) {
        if ( EVENTS )
            traceXML.println("setDocumentLocator");
        this.locator = locator;
    }

    /**
     * Empty Return an immutable location for the current position in the parse
     * stream.
     */
    private Position position() {
        // calling it "Location" is unhelpful - automatic imports keeps finding the
        // StAX javax.xml.stream.Location!
        return new Position(locator.getLineNumber(), locator.getColumnNumber());
    }

    // These happen before startElement.
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if ( TRACE )
            trace.printf("startPrefixMapping: %s: <%s>\n", prefix, uri);
        // Output only the top level prefix mappings.
        // Done in startElement to test for rdf:RDF
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if ( TRACE )
            trace.printf("endPrefixMapping: %s\n", prefix);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if ( xmlLiteralCollecting() ) {
            xmlLiteralCollectCharacters(ch, start, length);
            return;
        }

        switch (parserMode) {
            case ObjectLex:
                accCharacters.append(ch, start, length);
                return;
            case ObjectParseTypeLiteral:
                // Dealt with above.
                return;
            // Allow whitespace only
            case ObjectParserTypeResource:
            case NodeElement:
            case PropertyElement:
            case ObjectParseTypeCollection:
                if ( !isWhitespace(ch, start, length) )
                    throw RDFXMLparseError("Non-whitespace text content between element tags: "
                                                             + nonWhitespaceForMsg(ch, start, length), position());
                break;
            case TOP:
                if ( !isWhitespace(ch, start, length) ) {
                    throw RDFXMLparseError("Non-whitespace text content outside element tags: "
                                                             + nonWhitespaceForMsg(ch, start, length), position());
                }
                break;
        }
    }

    /** The string for the first non-whitespace index. */
    private static String nonWhitespaceForMsg(char[] ch, int start, int length) {
        for ( int i = start ; i < start + length ; i++ ) {
            if ( !Character.isWhitespace(ch[i]) ) {
                int len = Math.min(20, start - i);
                return new String(ch, i, len);
            }
        }
        throw new RDFXMLParseException("Failed to find any non-whitespace characters");
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if ( TRACE )
            traceXML.println("ignorableWhitespace");
    }

    private static boolean isWhitespace(char[] ch, int start, int length) {
        for ( int i = start ; i < start + length ; i++ ) {
            char ich = ch[i];
            if ( !Character.isWhitespace(ich) )
                return false;
        }
        return true;
    }

    private static boolean isWhitespace(CharSequence chars) {
        for ( int i = 0 ; i < chars.length() ; i++ ) {
            char ich = chars.charAt(i);
            if ( !isWhitespace(ich) )
                return false;
        }
        return true;
    }

    private static boolean isWhitespace(char ch) {
        return Character.isWhitespace(ch);
    }

    private String here() {
        Position position = position();
      if ( position == null )
          return "[?, ?]";
      return String.format("[line:%d, col:%d]", position.line(), position.column());
    }

    // ---- Parser output

    private void emit(Node subject, Node property, Node object, Position position) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(property, "property");
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(position, "position");
        // out.printf("Triple: %s %s %s %s\n", str(position), str(subject),
        // str(property), str(object));
        destination.triple(Triple.create(subject, property, object));
    }

    private void emitReify(Node reify, Node subject, Node property, Node object, Position position) {
        emit(subject, property, object, position);
        if ( reify != null ) {
            emit(reify, Nodes.type, Nodes.Statement, position);
            emit(reify, Nodes.subject, subject, position);
            emit(reify, Nodes.predicate, property, position);
            emit(reify, Nodes.object, object, position);
        }
    }

    private void emitBase(String base, Position position) {
        destination.base(base);
    }

    private void emitPrefix(String prefix, String iriStr, Position position) {
        destination.prefix(prefix, iriStr);
    }

    // ---- Creating terms.

    private Node qNameToIRI(String namespace, String localName, Position position) {
        String uriStr = qNameToIRI(namespace, localName);
        return iri(uriStr, position);
    }

    /** This is the RDF rule for creating an IRI from QName. */
    private String qNameToIRI(String namespace, String localName) {
        String iriStr = namespace + localName;
        return iriStr;
    }

    private Node iri(String uriStr, Position position) {
        Objects.requireNonNull(uriStr);
        Objects.requireNonNull(position);
        return createURI(uriStr, position);
    }

    private Node iriFromID(String idStr, Position position) {
        checkValidNCName(idStr, position);
        Position prev = previousUseOfID(idStr, position);
        if ( prev != null )
            // Already in use
            RDFXMLparseWarning("Reuse of rdf:ID '"+idStr+"' at "+str(prev), position);
        Node uri = iriResolve("#"+idStr,position);
        return uri;
    }

    private Node iriResolve(String uriStr, Position position) {
        Objects.requireNonNull(uriStr);
        Objects.requireNonNull(position);
        int line = position.line();
        int col = position.column();
        String resolved = resolveIRI(uriStr, position);
        return createURI(resolved, position);
    }

    private String resolveIRI(String uriStr, Position position) {
        if ( uriStr.startsWith("_:") )
            // <_:label> syntax. Handled by the FactoryRDF via the parser profile.
            return uriStr;
        return resolveIRIx(uriStr, position).str();
    }

    private IRIx resolveIRIx(String uriStr, Position position) {
        try {
            if ( currentBase != null )
                return currentBase.resolve(uriStr);
            IRIx iri = IRIx.create(uriStr);
            if ( iri.isRelative() )
                throw RDFXMLparseError("Base URI is null, but there are relative URIs to resolve" , position);
            return iri;
        } catch (IRIException ex) {
            throw RDFXMLparseError(ex.getMessage(), position);
        }
    }

    /** Done in accordance to the parser profile policy. */
    private Node createURI(String iriStr, Position position) {
        int line = position.line();
        int col = position.column();
        // Checking
        return parserProfile.createURI(iriStr, line, col);
    }

//    private Node iriDirect(String uriStr, Position position) {
//        Objects.requireNonNull(uriStr);
//        // No checking.
//        return createURIdirect(uriStr, position);
//    }
//
//    /** Always done without checking. */
//    private Node createURIdirect(String uriStr, Position position) {
//        return factory.createURI(uriStr);
//    }

    private Node blankNode(Position position) {
        Objects.requireNonNull(position);
        int line = position.line();
        int col = position.column();
        return parserProfile.createBlankNode(null, line, col);
    }

    private void checkValidNCName(String string, Position position) {
        //boolean isValid = XMLChar.isValidNCName(string);
        boolean isValid = XML11Char.isXML11ValidNCName(string);
        if ( ! isValid )
            RDFXMLparseWarning("Not a valid XML NCName: '"+string+"'", position);
    }

    private static boolean isRDF(String namespaceURI) {
        return rdfNS.equals(namespaceURI);
    }

    /** Test for {@code rdf:_NNNNN}. */
    private static boolean isMemberProperty(String namespaceURI, String localName) {
        if ( ! isRDF(namespaceURI) )
            return false;
        return isMemberPropertyLocalName(localName);
    }

    private static boolean isMemberPropertyLocalName(String localName) {
        if (localName.startsWith("_")) {
            String number = localName.substring(1);
            if (number.startsWith("-") || number.startsWith("0"))
                return false;
            try {
                Integer.parseInt(number);
                return true;
            } catch (NumberFormatException e) {
                try {
                    // It might be > Integer.MAX_VALUE
                    java.math.BigInteger i = new java.math.BigInteger(number);
                    return true;
                } catch (NumberFormatException ee) {
                    return false;
                }
            }
        }
        return false;
    }

    // "nil" is in the W3C RDF test suite
    private static final Set<String> knownRDF = Set.of
            ("Bag", "Seq", "Alt", "List","XMLLiteral", "Property", "Statement",
             "type", "li", "subject", "predicate","object","value","first","rest", "nil");
    private static final Set<String> knownRDFProperties = knownRDF;
    private static final Set<String> knownRDFTypes = knownRDF;

    /**
     * Return false if acceptable: not the RDF namespace, in the RDF namespace but
     * recognized for a type.
     * If return true, issue a warning.
     */
    private boolean isNotRecognizedRDFtype(String namespaceURI, String localName) {
        if ( ! isRDF(namespaceURI) )
            return false;
        return ! knownRDFTypes.contains(localName);
    }

    private boolean isNotRecognizedRDFproperty(String namespaceURI, String localName) {
        if ( ! isRDF(namespaceURI) )
            return false;
        if ( isMemberPropertyLocalName(localName) )
            return false;
        return ! knownRDFProperties.contains(localName);
    }

    private Node blankNode(String label, Position position) {
        Objects.requireNonNull(label);
        Objects.requireNonNull(position);
        // RDF/XML restriction.
        checkValidNCName(label, position);
        int line = position.line();
        int col = position.column();
        return parserProfile.createBlankNode(null, label, line, col);
    }

    private Node literal(String lexical, Position position) {
        Objects.requireNonNull(lexical);
        Objects.requireNonNull(position);
        int line = position.line();
        int col = position.column();
        return parserProfile.createStringLiteral(lexical, line, col);
    }

    /**
     * Create literal with a language (rdf:langString). If lang is null or "", create
     * an xsd:string
     */
    private Node literal(String lexical, String lang, Position position) {
        if ( lang == null || lang.isEmpty() )
            return literal(lexical, position);
        Objects.requireNonNull(lexical);
        Objects.requireNonNull(position);
        int line = position.line();
        int col = position.column();
        return parserProfile.createLangLiteral(lexical, lang, line, col);
    }

    private Node literalDatatype(String lexical, String datatype, Position position) {
        Objects.requireNonNull(lexical);
        Objects.requireNonNull(datatype);
        Objects.requireNonNull(position);
        int line = position.line();
        int col = position.column();
        RDFDatatype dt = NodeFactory.getType(datatype);
        return parserProfile.createTypedLiteral(lexical, dt, line, col);
    }

    private Node literalDatatype(String lexical, RDFDatatype datatype, Position position) {
        Objects.requireNonNull(lexical);
        Objects.requireNonNull(datatype);
        Objects.requireNonNull(position);
        int line = position.line();
        int col = position.column();
        return parserProfile.createTypedLiteral(lexical, datatype, line, col);
    }

    // ---- Functions

    private boolean qNameMatches(String ns1, String local1, String ns2, String local2) {
        // QName actually ignores prefix for QName.equals.
        return Objects.equals(ns1, ns2) && Objects.equals(local1, local2);
    }

    // ---- Development

    private void incIndent() {
        if ( TRACE )
            trace.incIndent();
    }

    private void decIndent() {
        if ( TRACE )
            trace.decIndent();
    }

    // ---- RDF XML Literal

    private void startXMLLiteral(Position position) {
        if ( TRACE )
            trace.printf("Start XML Literal : depth=%d\n", elementDepth);
        incIndent();
        parserMode(ParserMode.ObjectParseTypeLiteral);
        xmlLiteralStartDepth = elementDepth;
        accCharacters.setLength(0);
    }

    private void endXMLLiteral(Position position) {
        decIndent();
        if ( TRACE )
            trace.printf("End XML Literal : depth=%d\n", elementDepth);
        xmlLiteralStartDepth = -1;
    }

    private String xmlLiteralCollectText() {
        String lexical = xmlEscapeStrText(accCharacters);
        return lexical;
    }

    /** Building an RDF XML Literal. */
    private boolean xmlLiteralCollecting() {
        return xmlLiteralStartDepth > 0;
    }

    /** XML text, not XML Literal, not in an attribute. */
    private String xmlEscapeStrText(CharSequence stringAcc) {
        // Nothing to do.
        return stringAcc.toString();
    }

    // ---- RDF XML Literals

     private static final String openStartTag = "<";
     private static final String closeStartTag = ">";
     private static final String openEndTag = "</";
     private static final String closeEndTag = ">";

     private Map<String, String> namespaces = Map.of();
     private Deque<Map<String, String>> stackNamespaces = new ArrayDeque<>();

     private void xmlLiteralCollectStartElement(String namespaceURI, String localName, String qName, Attributes attributes) {
         if ( TRACE )
             trace.printf("XML Literal[%s]: depth=%d\n", qName, elementDepth);
         incIndent();
         incElementDepth();
         stackNamespaces.push(namespaces);
         namespaces = new HashMap<>(namespaces);
         Map<String, String> outputNS = new TreeMap<>();

         accCharacters.append(openStartTag);
         accCharacters.append(qName);

         xmlLiteralNamespaces(outputNS, namespaceURI, localName, qName, attributes);

         xmlLiteralAttributes(attributes);

         accCharacters.append(closeStartTag);
     }

     private void xmlLiteralNamespaces(Map<String, String> outputNS, String namespaceURI, String localName, String qName, Attributes attributes) {
         xmlLiteralNamespacesForQName(outputNS, namespaceURI, localName, qName);

         // Needs more. Determine namespace for attributes
         for ( int i = 0 ; i < attributes.getLength() ; i++ ) {
             String attrQName = attributes.getQName(i);
             String u = attributes.getURI(i);
             if ( u.isEmpty() )
                 continue;
             // namespaces handled separately.
             if ( attrQName.equals("xmlns") || attrQName.startsWith("xmlns:") )
                 // Not namespaces.
                 continue;
             xmlLiteralNamespacesForQName(outputNS, attributes.getURI(i), attributes.getLocalName(i), attrQName);
         }

         // Output.
         for ( String prefix : outputNS.keySet() ) {
             String uri = outputNS.get(prefix);
             // Unset default namespace
             if ( uri.isEmpty() )
                 continue;
             accCharacters.append(" ");
             accCharacters.append(prefix);
             accCharacters.append("=\"");
             accCharacters.append(uri);
             accCharacters.append("\"");
         }
     }

     /** Process one QName - insert a namespace if the prefix is not in scope as given by the amespace mapping. */
     private void xmlLiteralNamespaceQName(Map<String, String> outputNS, Map<String, String> namespaces, NamespaceContext nsCxt, QName qName) {
         String prefix = qName.getPrefix();
         String namespaceURI = nsCxt.getNamespaceURI(prefix);
         if ( ! namespaces.containsKey(prefix) || ! namespaces.get(prefix).equals(namespaceURI) ) {
             // Define in current XML subtree.
             outputNS.put(prefix, namespaceURI);
             namespaces.put(prefix, namespaceURI);
         }
     }

     private void xmlLiteralAttributes(Attributes attributes) {
         // Map qname -> index, sorted by qname
         Map<String, Integer> attrs = new TreeMap<>();

         for ( int i = 0 ; i < attributes.getLength() ; i++ ) {
             String attrQName = attributes.getQName(i);
             if ( attrQName.equals("xmlns") || attrQName.startsWith("xmlns:") )
                 // Not namespaces.
                 continue;
             attrs.put(attrQName, i) ;
         }

         Iterator<Integer> iterAttr = attrs.values().iterator();
         while(iterAttr.hasNext()) {
             int idx = iterAttr.next();
             String name = attributes.getQName(idx);
             String value = attributes.getValue(idx);
             accCharacters.append(" ");
             accCharacters.append(name);
             accCharacters.append("=\"");
             accCharacters.append(xmlLiteralEscapeAttr(value));
             accCharacters.append("\"");
         }
     }

     // ---- RDF Collections

    private void xmlLiteralCollectEndElement(String namespaceURI, String localName, String qName) {
        accCharacters.append(openEndTag);
        accCharacters.append(qName);
        accCharacters.append(closeEndTag);
        namespaces = stackNamespaces.pop();
        decElementDepth();
        decIndent();
        if ( TRACE )
            trace.printf("XML Literal[/%s]: depth=%d\n", qName, elementDepth);
    }

    private void xmlLiteralCollectCharacters(char[] ch, int start, int length) {
        if ( TRACE )
            trace.printf("XML Literal Characters: depth=%d\n", elementDepth);
        String s = new String(ch, start, length);
        s = xmlLiteralEscapeText(s);
        accCharacters.append(s);
    }

    /**
     * Note a namespace if not already set.
     * It is added to the namespaces recorded and also the output (sorted) set.
     */
    private void xmlLiteralNamespacesForQName(Map<String, String> outputNS, String namespaceURI, String localName, String qName) {
        int idx = qName.indexOf(':');
        String nsAttr;

        // Find xmlns key.
        if ( idx < 1 ) {
            nsAttr = "xmlns";
        } else {
            nsAttr = "xmlns:"+qName.substring(0, idx);
            //nsLocalName = qName.substring(idx+1);
        }

        // Update the current namespaces mapping.
        if ( ! namespaces.containsKey(nsAttr) || !namespaces.get(nsAttr).equals(namespaceURI) ) {
            namespaces.put(nsAttr, namespaceURI);
            outputNS.put(nsAttr, namespaceURI);
        }
    }

    /**
     * Escape text used in an XML content.
     * Escapes aligned to ARP.
     */
    /** XML text, context text. */
    private String xmlLiteralEscapeText(CharSequence stringAcc) {
        StringBuilder sBuff = new StringBuilder();
        int len = stringAcc.length() ;
        for (int i = 0; i < len; i++) {
            char c = stringAcc.charAt(i);
            String replace;
            switch (c) {
                case '&' : replace = "&amp;"; break;
                case '<' : replace = "&lt;"; break;
                case '>' : replace = "&gt;"; break;
                //case '"' : replace = "&quot;"; break;
                //case '\'' : replace = "&apos;"; break;
                default :
                    sBuff.append(c);
                    continue;
            }
            sBuff.append(replace);
        }
        return sBuff.toString();
    }


    /**
     * Escape text used in an XML attribute value.
     * Escapes aligned to ARP.
     */
    private String xmlLiteralEscapeAttr(CharSequence stringAcc) {
        StringBuilder sBuff = new StringBuilder();
        int len = stringAcc.length() ;
        for (int i = 0; i < len; i++) {
            char c = stringAcc.charAt(i);
            String replace;
            switch (c) {
                case '&' : replace = "&amp;"; break;
                case '<' : replace = "&lt;"; break;
                //case '>' : replace = "&gt;"; break;
                case '"' : replace = "&quot;"; break;
                //case '\'' : replace = "&apos;"; break;
                default :
                    sBuff.append(c);
                    continue;
            }
            sBuff.append(replace);
        }
        return sBuff.toString();
    }

    // -- SAX Operations not handled (org.xml.sax.ext.DefaultHandler2)
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if ( xmlLiteralCollecting() ) {
            accCharacters.append("<?");
            accCharacters.append(target);
            accCharacters.append(' ');
            accCharacters.append(data);
            accCharacters.append("?>");
            return;
        }

        if ( EVENTS )
            traceXML.println("processingInstruction");
        RDFXMLparseWarning("XML Processing instruction - ignored", position());
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        if ( EVENTS )
            traceXML.println("skippedEntity");
    }

    // ---- ErrorHandler

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        if ( EVENTS )
            traceXML.println("warning");
        errorHandler.warning(exception.getMessage(), exception.getLineNumber(), exception.getColumnNumber());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        if ( EVENTS )
            traceXML.println("error");
        // No recovery.
        errorHandler.fatal(exception.getMessage(), exception.getLineNumber(), exception.getColumnNumber());
        throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        if ( EVENTS )
            traceXML.println("fatalError");
        errorHandler.fatal(exception.getMessage(), exception.getLineNumber(), exception.getColumnNumber());
        // Should not happen.
        throw exception;
    }

    // ---- EntityResolver

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if ( EVENTS )
            traceXML.println("resolveEntity");
        return null;
    }

    // ---- EntityResolver2

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        if ( EVENTS )
            traceXML.println("SAX2-resolveEntity");
        return null;
    }

    // ---- DTDHandler

    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        if ( EVENTS )
            traceXML.println("notationDecl");
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        if ( EVENTS )
            traceXML.println("unparsedEntityDecl");
    }

    // ---- LexicalHandler

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-startDTD: " + systemId);
    }

    @Override
    public void endDTD() throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-endDTD");
    }

    @Override
    public void startEntity(String name) throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-startEntity");
    }

    @Override
    public void endEntity(String name) throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-endEntity");
    }

    @Override
    public void startCDATA() throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-startCDATA");
    }

    @Override
    public void endCDATA() throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-endCDATA");
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        if ( xmlLiteralCollecting() ) {
            accCharacters.append("<!--");
            accCharacters.append(ch, start, length);
            accCharacters.append("-->");
            return;
        }
        if ( EVENTS )
            traceXML.println("SAX2-comment");
    }

    // ---- DeclHandler

    @Override
    public void elementDecl(String name, String model) throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-elementDecl");
    }

    @Override
    public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-attributeDecl");
    }

    @Override
    public void internalEntityDecl(String name, String value) throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-internalEntityDecl");
    }

    @Override
    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
        if ( EVENTS )
            traceXML.println("SAX2-externalEntityDecl");
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        if ( EVENTS )
            traceXML.println("SAX2-getExternalSubset");
        return null;
    }
}
