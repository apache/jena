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

package org.apache.jena.riot.lang.rdfxml.rrx_stax_sr;

import static javax.xml.stream.XMLStreamConstants.*;
import static org.apache.jena.riot.SysRIOT.fmtMessage;

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.rdfxml.RDFXMLParseException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.XML11Char;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDF.Nodes;

public class RDFXMLParser_StAX_SR {
    public static boolean TRACE = false;
    private static boolean EVENTS = true;
    private final IndentedWriter trace;

    private final XMLStreamReader xmlSource;
    // Stacks.

    // Constants
    private static final String XML_PREFIX = "xml";
    private static final String rdfNS = RDF.uri;
    private static final String xmlNS = "http://www.w3.org/XML/1998/namespace";
    private static final String ID = "ID";
    private static final String NODE_ID = "nodeID";
    private static final String ABOUT = "about";
    private int blankNodeCounter  = 0 ;
    private boolean hasRDF = false;

    private final ParserProfile parserProfile;
    private final ErrorHandler errorHandler;
    private final Context context;
    private final String initialXmlBase;
    private final String initialXmlLang;
    private final StreamRDF destination;

    private record BaseLang(IRIx base, String lang) {}
    private Deque<BaseLang> stack = new ArrayDeque<>();
    // Just these operations:

    private void pushFrame(IRIx base, String lang) {
        BaseLang frame = new BaseLang(currentBase, currentLang);
        stack.push(frame);
        currentBase = base;
        currentLang = lang;
    }

    private void popFrame() {
        BaseLang frame = stack.pop();
        currentBase = frame.base;
        currentLang = frame.lang;
    }

    // ---- Error handlers

    private RiotException RDFXMLparseError(String message) {
        return RDFXMLparseError(message, location());
    }

    private void RDFXMLparseWarning(String message) {
        RDFXMLparseWarning(message, location());
    }

    private RiotException RDFXMLparseError(String message, Location location) {
        if ( location != null )
            errorHandler.error(message, location.getLineNumber(), location.getColumnNumber());
        else
            errorHandler.error(message, -1, -1);
        // The error handler normally throws an exception - for RDF/XML parsing it is required.
        return new RiotException(fmtMessage(message, location.getLineNumber(), location.getColumnNumber())) ;
    }

    private void RDFXMLparseWarning(String message, Location location) {
        if ( location != null )
            errorHandler.warning(message, location.getLineNumber(), location.getColumnNumber());
        else
            errorHandler.warning(message, -1, -1);
    }
    // Tracking for ID on nodes (not reification usage)
    // We limit the number of local fragment IDs tracked because map only grows.
    // A base URI may be re-introduced so this isn't nested scoping.
    private int countTrackingIDs = 0;
    private Map<IRIx, Map<String,  Location>> trackUsedIDs = new HashMap<>();
    private Location previousUseOfID(String idStr, Location location) {
        Map<String, Location> scope = trackUsedIDs.computeIfAbsent(currentBase, k->new HashMap<>());
        Location prev = scope.get(idStr);
        if ( prev != null )
            return prev;
        if ( countTrackingIDs > 10000 )
            return null;
        scope.put(idStr, location);
        countTrackingIDs++;
        return null;
    }

    // Collecting characters does not need to be a stack because there are
    // no nested objects while gathering characters for lexical or XMLLiterals.
    private StringBuilder accCharacters = new StringBuilder(100);

    private IRIx currentBase = null;
    private String currentLang = null;

    /** Integer holder for rdf:li */
    private static class Counter { int value = 1; }

    RDFXMLParser_StAX_SR(XMLStreamReader reader, String xmlBase, ParserProfile parserProfile, StreamRDF destination, Context context) {
        // Debug
        IndentedWriter out = IndentedWriter.stdout.clone();
        out.setFlushOnNewline(true);
        out.setUnitIndent(4);
        out.setLinePrefix("# ");
        this.trace = out;
        EVENTS = TRACE;
        // Debug

        this.xmlSource = reader;
        this.parserProfile = parserProfile;
        this.context = context;
        this.errorHandler = parserProfile.getErrorHandler();
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

    private static final QName rdfRDF = new QName(rdfNS, "RDF");
    private static final QName rdfDescription = new QName(rdfNS, "Description");
    private static final QName rdfID = new QName(rdfNS, "ID");
    private static final QName rdfNodeID = new QName(rdfNS, "nodeID");
    private static final QName rdfAbout = new QName(rdfNS, "about");
    private static final QName rdfType = new QName(rdfNS, "type");

    private static final QName rdfSeq = new QName(rdfNS, "Seq");
    private static final QName rdfBag = new QName(rdfNS, "Bag");
    private static final QName rdfAlt = new QName(rdfNS, "Alt");

    private static final QName rdfContainerItem = new QName(rdfNS, "li");
    private static final QName rdfDatatype = new QName(rdfNS, "datatype");
    private static final QName rdfParseType = new QName(rdfNS, "parseType");
    private static final QName rdfResource = new QName(rdfNS, "resource");

    private static final QName rdfAboutEach = new QName(rdfNS, "aboutEach");
    private static final QName rdfAboutEachPrefix = new QName(rdfNS, "aboutEachPrefix");
    private static final QName rdfBagID = new QName(rdfNS, "bagID");

    private static final QName xmlQNameBase = new QName(XMLConstants.XML_NS_URI, "base");
    private static final QName xmlQNameLang = new QName(XMLConstants.XML_NS_URI, "lang");
    // xml:space is a now-deprecated XML attribute that related to handing
    // whitespace characters inside elements. Skip it.
    private static final QName xmlQNameSpace = new QName(XMLConstants.XML_NS_URI, "space");

    private static final String parseTypeCollection = "Collection";
    private static final String parseTypeLiteral = "Literal";
    private static final String parseTypeResource = "Resource";
    // This is a dummy parseType for when there is no given rdf:parseType.
    private static final String parseTypePlain = "$$";

    // Grammar productions.
    // 6.2.2 Production coreSyntaxTerms
    // rdf:RDF | rdf:ID | rdf:about | rdf:parseType | rdf:resource | rdf:nodeID | rdf:datatype
    private static Set<QName> $coreSyntaxTerms =
            Set.of(rdfRDF, rdfID, rdfAbout, rdfParseType, rdfResource, rdfNodeID, rdfDatatype);

    // 6.2.3 Production syntaxTerms
    // coreSyntaxTerms | rdf:Description | rdf:li
    private static Set<QName> $syntaxTerms =
            Set.of(rdfRDF, rdfID, rdfAbout, rdfParseType, rdfResource, rdfNodeID, rdfDatatype,
                   rdfDescription, rdfContainerItem);

    // 6.2.4 Production oldTerms
    // rdf:aboutEach | rdf:aboutEachPrefix | rdf:bagID
    private static Set<QName> $oldTerms = Set.of(rdfAboutEach, rdfAboutEachPrefix, rdfBagID);

    private static Set<QName> disallowedPropertyAttributeTerms =
            Set.of(rdfRDF, rdfID, rdfAbout, rdfParseType, rdfResource, rdfNodeID, rdfDatatype,
                   rdfDescription, rdfContainerItem, rdfAboutEach, rdfAboutEachPrefix, rdfBagID);

    // 6.2.5 Production nodeElementURIs
    // anyURI - ( coreSyntaxTerms | rdf:li | oldTerms )
    private static boolean allowedNodeElementURIs(QName qName) {
        if ( ! rdfNS.equals(qName.getNamespaceURI()) )
            return true;
        if ( $coreSyntaxTerms.contains(qName) )
            return false;
        if ( rdfContainerItem.equals(qName) )
            return false;
        if ( $oldTerms.contains(qName) )
            return false;
        return true;
    }

    // 6.2.6 Production propertyElementURIs
    // anyURI - ( coreSyntaxTerms | rdf:Description | oldTerms )
    private static boolean allowedPropertyElementURIs(QName qName) {
        if ( ! rdfNS.equals(qName.getNamespaceURI()) )
            return true;
        if ( $coreSyntaxTerms.contains(qName) )
            return false;
        if ( rdfDescription.equals(qName) )
            return false;
        if ( $oldTerms.contains(qName) )
            return false;
        return true;
    }

    // 6.2.7 Production propertyAttributeURIs
    // anyURI - ( coreSyntaxTerms | rdf:Description | rdf:li | oldTerms )
    private static boolean allowedPropertyAttributeURIs(QName qName) {
        if ( ! rdfNS.equals(qName.getNamespaceURI()) )
            return true;
        if ( $coreSyntaxTerms.contains(qName) )
            return false;
        if ( rdfDescription.equals(qName) )
            return false;
        if ( rdfContainerItem.equals(qName) )
            return false;
        if ( $oldTerms.contains(qName) )
            return false;
        return true;
    }

    /** The attributes that guide the RDF/XML parser. */
    private static Set<QName> $rdfSyntaxAttributes =
            Set.of(rdfRDF, rdfAbout, rdfNodeID, rdfID, rdfParseType, rdfDatatype, rdfResource);

    private static boolean isSyntaxAttribute(QName qName) {
        return $rdfSyntaxAttributes.contains(qName);
    }

    // xml:space is a now-deprecated XML attribute that related to handing
    // whitespace characters inside elements.
    private static Set<QName> $xmlReservedTerms = Set.of(xmlQNameBase, xmlQNameLang, xmlQNameSpace);

    /** Recognized XML namespace Qname */
    private static boolean isXMLQName(QName qName) {
        return $xmlReservedTerms.contains(qName);
    }

    private static boolean isXMLNamespace(QName qName) {
        return xmlNS.equals(qName.getNamespaceURI());
    }

    // start: whole doc
    //   6.2.8 Production doc
    //   6.2.11 Production nodeElement
    // doc or production nodeElement.
    // start:
    //  6.2.9 Production RDF
    public void parse() {

        int eventType = nextEventAny();

        // XMLStreamReader does not generate "START_DOCUMENT"
        // The first event is DTD or START_ELEMENT.
        // XMLEventReader does generate a START_DOCUMENT

        if ( lookingAt(eventType, END_DOCUMENT) )
            throw RDFXMLparseError("Empty document", location());

        if ( lookingAt(eventType, DTD) )
            eventType = nextEventTag();

        // rdf:RDF or rdf:Description.
        if ( ! lookingAt(eventType, START_ELEMENT) )
            throw RDFXMLparseError("Not a start element: "+strEventType(eventType), location());

        boolean hasFrame = false;

        // <rdf:RDF>
        if ( qNameMatches(rdfRDF, qName()) ) {
            if ( TRACE )
                trace.println("rdf:RDF");
            // Not necessary to track this element when parsing.
            hasFrame = startElement();
            // Only the XML base and namespaces that apply throughout rdf:RDF are parser output.
            emitInitialBaseAndNamespaces();
            hasRDF = true;
            eventType = nextEventTag();
        }

        incIndent();
        if ( hasRDF )
            nodeElementLoop(eventType);
        else
            nodeElementSingle(eventType);
        decIndent();

        // Possible </rdf:RDF>
        if ( hasRDF ) {
            endElement(hasFrame);
            if ( TRACE )
                trace.println("/rdf:RDF");
            eventType = nextEventAny();
        }
        // Now past <rdf:RDF...></rdf:RDF>
        while ( isWhitespace(eventType) )
            eventType = nextEventAny();
    }

    // ---- Node elements

    /**
     * Top level node element loop inside &lt;rdf:RDF&gt;, &lt;/rdf:RDF&gt;.
     * This is zero or more node elements.
     */
    private void nodeElementLoop(int eventType) {
        // There was an rdf:RDF
        // ---- Node Loop - sequence of zero or more RDF/XML node elements.
        // generic move over multiple elements
        while ( eventType >= 0 ) {
            if ( ! lookingAt(eventType, START_ELEMENT) )
                break;
            nodeElement();
            eventType = nextEventTag();
        }
    }

    /** Top level single node element. (no &lt;rdf:RDF&gt;, &lt;/rdf:RDF&gt;) */
    private void nodeElementSingle(int eventType) {
//          if ( VERBOSE )
//              out.println("-- single node element: "+str(event));
        if ( ! lookingAt(eventType, START_ELEMENT) )
            return ;
        nodeElement();
    }

    /**
     * One node element.
     * On entry, the start of node element has been read.
     * On exit, have read the end tag for the node element.
     */
    private void nodeElement() {
        nodeElement(null);
    }

    /**
     * Process one node element. The subject may have been been determined;
     * this is needed for nested node elements.
     */
    private void nodeElement(Node subject) {
        QName qName = qName();

        if ( TRACE )
            trace.println(">> nodeElement: "+str(location())+" "+str(qName));

        if ( ! allowedNodeElementURIs(qName) )
            throw RDFXMLparseError("Not allowed as a node element tag: '"+str(qName)+"'");

        incIndent();
        boolean hasFrame = startElement();
        if ( subject == null )
            subject = attributesToSubjectNode();
        nodeElementProcess(subject);
        endElement(hasFrame);
        decIndent();

        if ( TRACE )
            trace.println("<< nodeElement: "+str(location())+" "+strEventType(eventType()));
    }

    private void nodeElementProcess(Node subject) {
        QName qName = qName();
        Location location = location();

        if ( ! qNameMatches(qName, rdfDescription) ) {
            // Typed Node Element
            if ( isMemberProperty(qName) )
                RDFXMLparseWarning(str(qName)+" is being used on a typed node");
            else {
                if ( isNotRecognizedRDFtype(qName) )
                    RDFXMLparseWarning(str(qName)+" is not a recognized RDF term for a type");
            }

            Node object = qNameToURI(qName, location);
            emit(subject, NodeConst.nodeRDFType, object, location);
        }

        // Other attributes are properties.
        // rdf:type is special.

        if ( hasAttributeProperties() )
            processPropertyAttributes(subject, location);

        // Finished with the node start tag.
        int event = nextEventTag();
        event = propertyElementlLoop(subject, event);

        if ( ! lookingAt(event, END_ELEMENT) )
            throw RDFXMLparseError("Expected end element for "+qName());
    }

    // Property attributes.
    // The checking is done by the call to hasPropertyAttributes.
    private void processPropertyAttributes(Node subject, Location location) {
        // This both nodes and properties. There is an implicit bnode in when used with properties.
        int N = xmlSource.getAttributeCount();
        for ( int i = 0 ; i < N ; i++ ) {
            QName qName =  xmlSource.getAttributeName(i);
            boolean isPropertyAttribute = checkPropertyAttribute(qName, false);
            if ( ! isPropertyAttribute )
                continue;
            if ( rdfType.equals(qName) ) {
                String iriStr = xmlSource.getAttributeValue(i);
                Node type = iriResolve(iriStr, location);
                emit(subject, RDF.Nodes.type, type, location);
                continue;
            }
            Node property = qNameToURI(qName, location);
            String lexicalForm =  xmlSource.getAttributeValue(i);
            Node object = literal(lexicalForm, currentLang, location);
            emit(subject, property, object, location);
        }
    }

    private boolean hasAttributeProperties() {
        int N = xmlSource.getAttributeCount();
        for ( int i = 0 ; i < N ; i++ ) {
            QName qName =  xmlSource.getAttributeName(i);
            boolean isPropertyAttribute = checkPropertyAttribute(qName, true);
            if ( ! isPropertyAttribute )
                continue;
            return true;
        }
        return false;
    }

    /**
     * Return true if this is a property attribute.
     * @param qName
     */
    private boolean checkPropertyAttribute(QName qName, boolean outputWarnings) {
        String namespace = qName.getNamespaceURI();
        if ( namespace == null || namespace.isEmpty() ) {
            // SAX passes xmlns as attributes with namespace and local name of "". The qname is "xmlns:"/"xmlns"
            // StAX, does not pass namespaces.
            if ( outputWarnings )
                RDFXMLparseWarning("XML attribute '"+qName.getLocalPart()+"' used for RDF property attribute - ignored");
            return false;
        }
        if ( isSyntaxAttribute(qName) )
            return false;

        if ( ! allowedPropertyAttributeURIs(qName) )
            throw RDFXMLparseError("Not allowed as a property attribute: '"+str(qName)+"'");

        if ( outputWarnings && isNotRecognizedRDFproperty(qName) )
            RDFXMLparseWarning(str(qName)+" is not a recognized RDF term for a property attribute");

        if ( isXMLQName(qName) )
            return false;

        if ( isXMLNamespace(qName) ) {
            // Unrecognized qnames in the XMLnamespace are a warning and are ignored.
            RDFXMLparseWarning("Unrecognized XML attribute: '"+str(qName)+"'");
            return false;
        }
        return true;
    }

    // ---- Property elements

    private int propertyElementlLoop(Node subject, int event) {
        Counter listElementCounter = new Counter();
        while (true) {
//            if ( VERBOSE )
//                out.println("-- property loop: "+str(event));
            if ( ! lookingAt(event, START_ELEMENT) )
                break;
            propertyElement(subject, listElementCounter, location());
            event = nextEventTag();
        }
        return event;
    }

    /**
     * Property elements.
     * On entry, the start of property has been inspected but not consumed.
     */
    private void propertyElement(Node subject, Counter listElementCounter, Location location) {
        // Pass in location?
        if ( TRACE )
            trace.println(">> propertyElement: "+str(location())+" "+str(qName()));

        incIndent();
        boolean hasFrame = startElement();
        QName qName = qName();

        if ( ! allowedPropertyElementURIs(qName) )
            throw RDFXMLparseError("QName not allowed for property: "+str(qName));

        if ( isNotRecognizedRDFproperty(qName) )
            RDFXMLparseWarning(str(qName)+" is not a recognized RDF property");

        propertyElementProcess(subject, qName, listElementCounter, location);
        endElement(hasFrame);
        decIndent();

        if ( TRACE )
            trace.println("<< propertyElement: "+str(location())+" "+str(qName));
    }

    private int propertyElementProcess(Node subject, QName qName,
                                       Counter listElementCounter, Location location) {
        Node property;

        if ( qNameMatches(rdfContainerItem, qName) )
            property = iriDirect(rdfNS+"_"+Integer.toString(listElementCounter.value++), location());
        else
            property = qNameToURI(qName, location);

        Node reify = reifyStatement(location);
        Emitter emitter = (reify==null) ? this::emit : (s,p,o,loc)->emitReify(reify, s, p, o, loc);

        // If there is a blank node label, the element must be empty,
        // Check NCName if blank node created
        String objBlanklNodeLabel = attribute(rdfNodeID);
        String rdfResourceStr = attribute(rdfResource);
        String datatype = attribute(rdfDatatype);
        String parseType = objectParseType();

        // Checking
        if ( rdfResourceStr != null && objBlanklNodeLabel != null )
            throw RDFXMLparseError("Can't have both rdf:nodeId and rdf:resource on a property element");

        if ( rdfResourceStr != null && parseType != parseTypePlain )
            throw RDFXMLparseError("Both rdf:resource and rdf:ParseType on a property element. Only one allowed");

        if ( objBlanklNodeLabel != null && parseType != parseTypePlain )
            throw RDFXMLparseError("Both rdf:NodeId and rdf:ParseType on a property element. Only one allowed");

        Node resourceObj = null;

        if ( rdfResourceStr != null )
            resourceObj = iriResolve(rdfResourceStr, location);

        if ( objBlanklNodeLabel != null )
            resourceObj = blankNode(objBlanklNodeLabel, location);

        if ( hasAttributeProperties() ) {
            if ( parseType != parseTypePlain  ) {
                // rdf:parseType found.
                throw RDFXMLparseError("The attribute rdf:parseType is not permitted with property attributes on a property element: "+str(qName));
            }
            Node innerSubject = (resourceObj==null) ? blankNode(location) : resourceObj;
            processPropertyAttributes(innerSubject, location);
            emitter.emit(subject, property, innerSubject, location);
            int event = nextEventAny();
            if ( ! lookingAt(event, END_ELEMENT) )
                throw RDFXMLparseError("Expecting end element tag when using property attributes on a property element");
            return event;
        }

        if ( resourceObj != null ) {
            emitter.emit(subject, property, resourceObj, location);
            // Must be an empty element.
            int event = nextEventAny();
            if ( ! lookingAt(event, END_ELEMENT) )
                throw RDFXMLparseError("Expecting end element tag when using rdf:resource or rdf:NodeId on a proeprty.");
            return event;
        }

        switch(parseType) {
            case parseTypeResource: {
                // Implicit <rdf:Description><rdf:Description> i.e. fresh blank node
                if ( TRACE )
                    trace.println("rdfParseType=Resource");
                int event = parseTypeResource(subject, property, emitter, location);
                return event;
            }
            case parseTypeLiteral: {
                if ( TRACE )
                    trace.println("rdfParseType=Literal");
                int event = parseTypeLiteral(subject, property, emitter, location);
                return event;
            }
            case parseTypeCollection:  {
                if ( TRACE )
                    trace.println("rdfParseType=Collection");
                int event = parseTypeCollection(subject, property, emitter, location);
                return event;
            }
            case parseTypePlain:
                // The code below.
                break;
            default:
                throw RDFXMLparseError("Not a legal defined rdf:parseType: "+parseType);
        }

        // General read anything.
        // Can't peek, which would have allowed using getElementText.
        // We need a lookahead of 2 to see if this is a text-only element, then an end element
        // or ignorable whitespace then a nested node start element.
        int event = nextEventAny();

        // Need to see if text or nested.
        if ( lookingAt(event, CHARACTERS) ) {
            // Initial characters.
            // This may include comments. Slurp until not characters.
            accCharacters.setLength(0);

            while(lookingAt(event, CHARACTERS)) {
                String text = xmlSource.getText();
                accCharacters.append(text);
                event = nextEventAny();
            }
            if ( lookingAt(event, START_ELEMENT) ) {
                if ( ! isWhitespace(accCharacters) ) {
                    String msg = nonWhitespaceForMsg(accCharacters.toString());
                    throw RDFXMLparseError("Content before node element. '"+msg+"'");
                }
                event = processNestedNodeElement(event, subject, property, emitter);
                return event;
            }
            if ( lookingAt(event, END_ELEMENT) ) {
                String lexicalForm = accCharacters.toString();
                Location loc = location();
                Node obj;
                // Characters - lexical form.
                if ( datatype != null )
                    obj = literalDatatype(lexicalForm, datatype, loc);
                else if ( currentLang() != null )
                    obj = literal(lexicalForm, currentLang, loc);
                else
                    obj = literal(lexicalForm, loc);
                emitter.emit(subject, property, obj, loc);
                return event;
            }
            throw RDFXMLparseError("Unexpected element: "+strEventType(event));

        } else if ( lookingAt(event, START_ELEMENT) ) {
            // No content before start element
            event = processNestedNodeElement(event, subject, property, emitter);
            return event;
        } else if (lookingAt(event, END_ELEMENT) ) {
            emitter.emit(subject, property, NodeConst.emptyString, location);
        } else {
            throw RDFXMLparseError("Malformed property. "+strEventType(event));
        }
        return event;
    }

    private String objectParseType() {
        String parseTypeStr = attribute(rdfParseType);
        return  ( parseTypeStr != null ) ? parseTypeStr : parseTypePlain;
    }

    /**
     * Accumulate text for a text element, given some characters already read.
     * This method skips comments.
     * This method returns up to the closing end element tag.
     */
    private String accumulateLexicalForm(int initialEvent, StringBuilder sBuff) {
        String lexicalForm;
        int eventType = initialEvent;
        while(eventType >= 0 ) {
            if ( lookingAt(eventType, END_ELEMENT) )
                break;
            if ( ! lookingAt(eventType, CHARACTERS) )
                throw RDFXMLparseError("Unexpected element in text element: "+strEventType(eventType));
            sBuff.append(xmlSource.getText());
            eventType = nextEventAny();
        }
        lexicalForm = sBuff.toString();
        return lexicalForm;
    }

    private int parseTypeResource(Node subject, Node property, Emitter emitter, Location location) {
        Node innerSubject = blankNode(location);
        emitter.emit(subject, property, innerSubject, location);
        // Move to first property
        int event = nextEventTag();
        event = propertyElementlLoop(innerSubject, event);
        return event;
    }

    private int parseTypeLiteral(Node subject, Node property, Emitter emitter, Location location) {
        String text = xmlLiteralAccumulateText();
        Node object = literalDatatype(text, XMLLiteralType.theXMLLiteralType, location);
        emitter.emit(subject, property, object, location);
        return END_ELEMENT;
    }

    private static final String openStartTag = "<";
    private static final String closeStartTag = ">";
    private static final String openEndTag = "</";
    private static final String closeEndTag = ">";

    private String xmlLiteralAccumulateText() {

        // Map prefix -> URI
        Map<String, String> namespaces = Map.of();
        Deque<Map<String, String>> stackNamespaces = new ArrayDeque<>();

        // Current inscope.
        // Need to remember?
        //startElt.getNamespaceContext().

        accCharacters.setLength(0);
        StringBuilder sBuff = accCharacters;
        int event = START_ELEMENT;
        int depth = 0;
        while( event >= 0 ) {
            // Really, really any event.
            event = nextEventRaw();
            if ( lookingAt(event, START_ELEMENT) ) {
                depth++;
                incIndent();
                // -- Namespaces
                // The StAX parser has already processed the xmlns for the
                // declarations element and the setting  are in the NamespaceContext.
                stackNamespaces.push(namespaces);
                namespaces = new HashMap<>(namespaces);

                sBuff.append(openStartTag);
                QName qname = qName();
                if ( qname.getPrefix() != null && ! XMLConstants.DEFAULT_NS_PREFIX.equals(qname.getPrefix()) ) {
                    sBuff.append(qname.getPrefix());
                    sBuff.append(":");
                }
                sBuff.append(qname.getLocalPart());

                // -- namespaces
                xmlLiteralNamespaces(namespaces, sBuff);
                xmlLiteralAttributes(sBuff);

                sBuff.append(closeStartTag);

            } else if ( lookingAt(event, END_ELEMENT) ) {
                decIndent();
                depth--;
                if ( depth < 0 ) {
                    //stackNamespaces.clear();
                    //namespaces = Map.of();
                    break;
                }
                namespaces = stackNamespaces.pop();
                sBuff.append(openEndTag);
                QName qname = qName();
                if ( qname.getPrefix() != null && ! XMLConstants.DEFAULT_NS_PREFIX.equals(qname.getPrefix()) ) {
                    //sBuff.append(qname.getPrefix());
                    sBuff.append(xmlSource.getPrefix());
                    sBuff.append(":");
                }
                sBuff.append(qname.getLocalPart());
                sBuff.append(closeEndTag);
            } else if ( lookingAt(event, CHARACTERS) ) {
                String s = xmlSource.getText();
                s = xmlLiteralEscapeText(s);
                sBuff.append(s);
            } else if ( lookingAt(event, COMMENT) ) {
                String commentText = xmlSource.getText();
                sBuff.append("<!--");
                sBuff.append(commentText);
                sBuff.append("-->");
            } else if ( lookingAt(event, PROCESSING_INSTRUCTION) ) {
                String target = xmlSource.getPITarget();
                String data = xmlSource.getPIData();
                accCharacters.append("<?");
                accCharacters.append(target);
                accCharacters.append(' ');
                accCharacters.append(data);
                accCharacters.append("?>");
            } else {
                throw RDFXMLparseError("Unexpected event in rdf:XMLLiteral: "+strEventType(event));
            }
        }
        String x = sBuff.toString();
        accCharacters.setLength(0);
        return x ;
    }

    private void xmlLiteralAttributes(StringBuilder sBuff) {
        // Sort attributes
        Map<String, String> attrs = new TreeMap<>();
        int N = xmlSource.getAttributeCount();
        for ( int i = 0 ; i < N ; i++ ) {
            String value = xmlSource.getAttributeValue(i);
            QName qName =  xmlSource.getAttributeName(i);
            String attrName = str(qName);
            attrs.put(attrName, value);
        }

        for ( Map.Entry<String, String> e : attrs.entrySet() ) {
            String attrQName = e.getKey();
            String attrValue = e.getValue();
            sBuff.append(" ");
            sBuff.append(attrQName);
            sBuff.append("=\"");
            sBuff.append(xmlLiteralEscapeAttr(attrValue));
            sBuff.append("\"");
        }
    }

    /**
     * Process all QNames used in this element.
     */
    private void xmlLiteralNamespaces(Map<String, String> namespaces, StringBuilder sBuff) {
        NamespaceContext nsCxt = xmlSource.getNamespaceContext();

        // Map prefix -> URI, sorted by prefix. This is only the required namespaces.
        // The other map, namespaces, is all the namespaces in scope.
        Map<String, String> outputNS = new TreeMap<>();

        xmlLiteralNamespaceQName(outputNS, namespaces, nsCxt, qName());

        int N = xmlSource.getAttributeCount();
        for ( int i = 0 ; i < N ; i++ ) {
            xmlLiteralNamespaceQName(outputNS, namespaces, nsCxt, xmlSource.getAttributeName(i));
        }
        // Output.
        for ( String prefix : outputNS.keySet() ) {
            String uri = outputNS.get(prefix);
            if ( uri == null )
                // Undefined default namespace.
                continue;
            sBuff.append(" ");
            if ( prefix.isEmpty() ) {
                sBuff.append("xmlns=\"");
                sBuff.append(uri);
                sBuff.append("\"");
            } else {
                sBuff.append("xmlns:");
                sBuff.append(prefix);
                sBuff.append("=\"");
                sBuff.append(uri);
                sBuff.append("\"");
            }
        }
    }

    /** Process one QName - insert a namespace if the prefix is not in scope as given by the amespace mapping. */
    private void xmlLiteralNamespaceQName(Map<String, String> outputNS, Map<String, String> namespaces, NamespaceContext nsCxt, QName qName) {
        String prefix = qName.getPrefix();
        String namespaceURI = nsCxt.getNamespaceURI(prefix);
        if ( namespaceURI == null ) {
            // No default in scope.
        }

        // Not seen this prefix or it was a different value.
        if ( ! namespaces.containsKey(prefix) ||
                ( namespaceURI != null && ! namespaces.get(prefix).equals(namespaceURI)) ) {
            // Define in current XML subtree.
            outputNS.put(prefix, namespaceURI);
            namespaces.put(prefix, namespaceURI);
        }
    }


    // --- RDF Collections

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

    private int parseTypeCollection(Node subject, Node property, Emitter emitter, Location location) {
        Node lastCell = null ;
        int event = -1;
        while(true) {
            event = nextEventTag();
            if ( ! lookingAt(event, START_ELEMENT) )
                break;
            location = location();
            Node thisCell = blankNode(location);
            if ( lastCell == null ) {
                // First list item. Link the list in.
                lastCell = thisCell;
                emitter.emit(subject, property, thisCell, location);
            } else {
                // Link to the previous element. No reification.
                emit(lastCell, NodeConst.nodeRest, thisCell, location);
            }
            Node itemSubject = attributesToSubjectNode();
            emit(thisCell, RDF.Nodes.first, itemSubject, location);
            nodeElement(itemSubject);
            lastCell = thisCell;
        }

        // Finish the list.
        if ( lastCell != null ) {
            emit(lastCell, NodeConst.nodeRest, NodeConst.nodeNil, location);
        } else {
            // It was an empty list
            emitter.emit(subject, property, NodeConst.nodeNil, location);
        }

        return event;
    }

    private Node reifyStatement(Location location) {
        String reifyId = attribute(rdfID);
        if ( reifyId == null )
            return null;
        Node reify = iriFromID(reifyId, location);
        return reify;
    }

    /** Return the lang in-scope. Return null for none. */
    private String currentLang() {
        if ( currentLang == null || currentLang.isEmpty() )
            return null;
        return currentLang;
    }

    private int processNestedNodeElement(int event, Node subject, Node property, Emitter emitter) {
        // Nested / RDF/XML striped syntax.
        boolean hasFrame = startElement();
        Node subjectInner = attributesToSubjectNode();
        // subject property INNER
        emitter.emit(subject, property, subjectInner, location());

        // Process as a node element, having decied the subject.
        nodeElement(subjectInner);

        // End property tag.
        int eventType = nextEventTag();
        if ( lookingAt(eventType, START_ELEMENT) )
            throw RDFXMLparseError("Start tag after inner node element (only one node element permitted): got "+qName());
        if ( ! lookingAt(eventType, END_ELEMENT) )
            throw RDFXMLparseError("Expected an end element: got "+strEventType(eventType));
        // -- end
        endElement(hasFrame);
        return event;
    }

    /** Subject for a node element */
    private Node attributesToSubjectNode() {
        // Subject
        // The spec implies these happen in order (i.e later overrides earlier)
        // but the test suite has negative test cases.
        // If there is an attribute a with a.URI == rdf:ID, then e.subject := uri(identifier := resolve(e, concat("#", a.string-value))).
        // If there is an attribute a with a.URI == rdf:nodeID, then e.subject := bnodeid(identifier:=a.string-value).
        // If there is an attribute a with a.URI == rdf:about then e.subject := uri(identifier := resolve(e, a.string-value)).

        String iriStr = attribute(rdfAbout);
        // Check NCName if URI created,
        String idStr = attribute(rdfID);
        // Check NCName if blank node created
        String nodeId = attribute(rdfNodeID);

        if ( nodeId != null && iriStr != null && nodeId != null )
            throw RDFXMLparseError("All of rdf:about, rdf:NodeId and rdf:ID found. Must be only one.");

        if ( iriStr != null && idStr != null )
            throw RDFXMLparseError("Both rdf:about and rdf:ID found. Must be only one.");

        if ( nodeId != null && iriStr != null )
            throw RDFXMLparseError("Both rdf:about and rdf:NodeID found. Must be only one.");

        if ( nodeId != null && idStr != null )
            throw RDFXMLparseError("Both rdf:NodeID rdf:ID found. Must be only one.");

        Location location = location();

        if ( iriStr != null )
            return iriResolve(iriStr, location);

        if ( idStr != null )
            return iriFromID(idStr, location);

        if ( nodeId != null )
            return blankNode(nodeId, location);

        // None of the above. It's a fresh blank node.
        return blankNode(location);
    }

    private String attribute(StartElement startElt, QName attrName) {
        Attribute attr = startElt.getAttributeByName(attrName);
        if ( attr == null )
            return null;
        return attr.getValue();
    }

    // ---- Nodes

    private void setBase(String uriStr, Location location) {
        Node n = iriResolve(uriStr, location);
        parserProfile.setBaseIRI(n.getURI());
    }

    private Node qNameToURI(QName qName, Location location) {
        String uriStr = strQNameToURI(qName);
        return iriDirect(uriStr, location);
    }

    /** RDF rule */
    private String strQNameToURI(QName qName) {
        return qName.getNamespaceURI()+qName.getLocalPart();
    }

    // ---- Reading XML

    /** Get the string value for an attribute by QName */
    private String attribute(QName qName) {
        return xmlSource.getAttributeValue(qName.getNamespaceURI(), qName.getLocalPart());
    }

    /** XML parsing error. */
    private RiotException handleXMLStreamException(XMLStreamException ex) {
        String msg = xmlStreamExceptionMessage(ex);
        if ( ex.getLocation() != null ) {
            int line = ex.getLocation().getLineNumber();
            int col = ex.getLocation().getColumnNumber();
            errorHandler.fatal(msg, line, col);
        } else
            errorHandler.fatal(msg, -1, -1);
        // Should not happen.
        return new RiotException(ex.getMessage(), ex);
    }

    /** Get the detail message from an XMLStreamException */
    private String xmlStreamExceptionMessage(XMLStreamException ex) {
        String msg = ex.getMessage();
        if ( ex.getLocation() != null ) {
            // XMLStreamException with a location is two lines, and has the line/col in the first line.
            // Deconstruct the XMLStreamException message to get the detail part.
            String marker = "\nMessage: ";
            int i = msg.indexOf(marker);
            if ( i > 0 ) {
                msg = msg.substring(i+marker.length());
            }
        }
        return msg;
    }

    private int eventType() {
        return xmlSource.getEventType();
    }

    /** Only valid in START_ELEMENT and END_ELEMENT */
    private QName qName() {
        return xmlSource.getName();
    }

    private static boolean lookingAt(int eventType, int expectedEventType) {
        return eventType == expectedEventType;
    }

    private Location location() { return xmlSource.getLocation(); }

    /**
     * Move to next tag, skipping DTDs and "skipping unimportant whitespace and comments".
     * Returns a start event, endEvent or null.
     */
    private int nextEventTag() {
        // Similar to XMLStreamreader::nextTag(). This code works with DTD local
        // character entities and assumes the StAX parser
        // character entities and then manages the text replacement.
        try {
            while(xmlSource.hasNext()) {
                int evType = read();
                switch (evType) {
                    case START_ELEMENT:
                    case END_ELEMENT:
                        if ( EVENTS )
                            System.out.println("-- Tag: "+strEventType(evType));
                        return evType;
                    case CHARACTERS:
                    case CDATA:
                        String chars = xmlSource.getText();
                        if ( ! isWhitespace(chars) )
                            throw RDFXMLparseError("Read "+nonWhitespaceForMsg(chars)+" when expecting a start or end element.");
                        // Skip
                        break;
                    case COMMENT:
                    case DTD:
                        // Loop
                        continue;
                    //case SPACE:
                    //case PROCESSING_INSTRUCTION:
                    //case ENTITY_DECLARATION:
                    default:
                        // Not expecting any other type of event.
                        throw RDFXMLparseError("Unexpected  event "+strEventType(evType));
                }
                // and loop
            }
            return -1;
        } catch (XMLStreamException ex) {
            throw handleXMLStreamException(ex);
        }
    }

    /** Move to XMLEvent, skipping comments. */
    private int nextEventAny() {
        try {
            int ev = -1;
            while(xmlSource.hasNext()) {
                ev = read();
                if ( isComment(ev) )
                    continue;
                if ( lookingAt(ev, PROCESSING_INSTRUCTION) ) {
                    RDFXMLparseWarning("XML Processing instruction - ignored");
                    continue;
                }
                break;
            }
            if ( EVENTS ) {
                if ( ev < 0 )
                    System.out.println("-- Read: end of events");
                else
                    System.out.println("-- Event: "+strEventType(ev));
            }
            return ev;
        } catch (XMLStreamException ex) {
            throw handleXMLStreamException(ex);
        }
    }

    /** Move to next XMLEvent regardless of the event type. */
    private int nextEventRaw() {
        try {
            int ev = -1;
            if ( ! xmlSource.hasNext() )
                return ev;
            ev = read();
            if ( EVENTS ) {
                if ( ev < 0 )
                    System.out.println("-- Read: end of events");
                else
                    System.out.println("-- Event: "+strEventType(ev));
            }
            return ev;
        } catch (XMLStreamException ex) {
            throw handleXMLStreamException(ex);
        }
    }

    private int read() throws XMLStreamException {
        int eventType = xmlSource.next();
        if ( EVENTS )
            System.out.println("-- Read: "+strEventType(eventType));
        return eventType;
    }

    // ---- XML element processing

    /**
     * State an element (rdf:RDF, node, property)
     * Process namespace (tracing in development).
     * Process xml:base, xml:lang.
     * These set the context for inner elements and need a stack.
     */
    private boolean startElement() {
        processNamespaces();
        boolean hasFrame = processBaseAndLang();
        return hasFrame;
    }

    private boolean processBaseAndLang() {
        String xmlBase = attribute(xmlQNameBase);
        String xmlLang = attribute(xmlQNameLang);
        if ( TRACE ) {
            if ( xmlBase != null )
                trace.printf("+ BASE <%s>\n", xmlBase);
            if ( xmlLang != null )
                trace.printf("+ LANG @%s\n", xmlLang);
        }
        boolean hasFrame = (xmlBase != null || xmlLang != null);
        if ( hasFrame ) {
            pushFrame(currentBase, currentLang);
            if ( xmlBase != null )
                currentBase = currentBase.resolve(xmlBase);
            if ( xmlLang != null )
                currentLang = xmlLang;
        }
        return hasFrame;
    }

    private void endElement(boolean hasFrame) {
        if ( hasFrame )
            popFrame();
    }

    /**
     * XML namespaces are handled by the StAX parser.
     * This is only for tracking.
     * Return true if there is a namespace declaration.
     */
    private void processNamespaces() {
        if ( TRACE ) {
            int numNS = xmlSource.getNamespaceCount();
            for ( int i = 0 ; i < numNS ; i++ ) {
                String prefix = xmlSource.getNamespacePrefix(i);
                String prefixURI = xmlSource.getNamespaceURI(i);
                //emitPrefix(prefix, prefixURI);
            }
        }
    }

    // ---- Parser output

    private void emitInitialBaseAndNamespaces() {
        String xmlBase = attribute(xmlQNameBase);
        if ( xmlBase != null )
            emitBase(xmlBase);
        int numNS = xmlSource.getNamespaceCount();
        for ( int i = 0 ; i < numNS ; i++ ) {
            String prefix = xmlSource.getNamespacePrefix(i);
            String prefixURI = xmlSource.getNamespaceURI(i);
            if ( prefix == null )
                prefix = "";
            emitPrefix(prefix, prefixURI);
        }
    }

    interface Emitter { void emit(Node subject, Node property, Node object, Location location); }

    private void emit(Node subject, Node property, Node object, Location location) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(property);
        Objects.requireNonNull(object);
        Objects.requireNonNull(location);
        destination.triple(Triple.create(subject, property, object));
    }

    private void emitReify(Node reify, Node subject, Node property, Node object, Location location) {
        emit(subject, property, object, location);
        if ( reify != null ) {
            emit(reify, NodeConst.nodeRDFType, Nodes.Statement, location);
            emit(reify, Nodes.subject, subject, location);
            emit(reify, Nodes.predicate, property, location);
            emit(reify, Nodes.object, object, location);
        }
    }

    private void emitBase(String base) {
        destination.base(base);
    }

    private void emitPrefix(String prefix, String iriStr) {
        destination.prefix(prefix, iriStr);
    }

    // ---- RDF Terms (Nodes)

    private Node iriFromID(String idStr, Location location) {
        checkValidNCName(idStr, location);
        Location prev = previousUseOfID(idStr, location);
        if ( prev != null )
            // Already in use
            RDFXMLparseWarning("Reuse of rdf:ID '"+idStr+"' at "+str(prev), location);
        Node uri = iriResolve("#"+idStr, location);
        return uri;
    }

    /** Create a URI. The IRI is known to be resolved. */
    private Node iriDirect(String uriStr, Location location) {
        Objects.requireNonNull(uriStr);
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        return parserProfile.createURI(uriStr, line, col);
    }

    /** Create a URI. The IRI is resolved by this operation. */
    private Node iriResolve(String uriStr, Location location) {
        Objects.requireNonNull(uriStr);
        Objects.requireNonNull(location);
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        String resolved = resolveIRI(uriStr, location);
        return parserProfile.createURI(resolved, line, col);
    }

    /** Resolve an IRI. */
    private String resolveIRI(String uriStr, Location location) {
        if ( uriStr.startsWith("_:") )
            // <_:label> syntax. Handled by the FactoryRDF via the parser profile.
            return uriStr;
        return resolveIRIx(uriStr, location).str();
    }

    private IRIx resolveIRIx(String uriStr, Location location) {
        // This does not use the parser profile because the base stacks and unstacks in RDF/XML.
        try {
            if ( currentBase != null )
                return currentBase.resolve(uriStr);
            IRIx iri = IRIx.create(uriStr);
            if ( iri.isRelative() )
                throw RDFXMLparseError("Base URI is null, but there are relative URIs to resolve" , location);
            return iri;
        } catch (IRIException ex) {
            throw RDFXMLparseError(ex.getMessage(), location);
        }
    }

    private Node blankNode(Location location) {
        Objects.requireNonNull(location);
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        return parserProfile.createBlankNode(null, line, col);
    }

    private Node blankNode(String label, Location location) {
        Objects.requireNonNull(label);
        Objects.requireNonNull(location);
        checkValidNCName(label, location);
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        return parserProfile.createBlankNode(null, label, line, col);
    }

//    private Node literal(String lex, String datatype, String lang, Location location) {
//        int line = location.getLineNumber();
//        int col = location.getColumnNumber();
//        return parserProfile.createL
//    }
    // literal(lex, datatype, lang)

    private Node literal(String lexical, Location location) {
        Objects.requireNonNull(lexical);
        Objects.requireNonNull(location);
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        return parserProfile.createStringLiteral(lexical, line, col);
    }

    /**
     * Create literal with a language (rdf:langString).
     * If lang is null or "", create an xsd:string
     */
    private Node literal(String lexical, String lang, Location location) {
        if ( lang == null )
            return literal(lexical, location);
        Objects.requireNonNull(lexical);
        Objects.requireNonNull(location);
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        return parserProfile.createLangLiteral(lexical, lang, line, col);
    }

    private Node literalDatatype(String lexical, String datatype, Location location) {
        Objects.requireNonNull(lexical);
        Objects.requireNonNull(datatype);
        Objects.requireNonNull(location);
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        RDFDatatype dt = NodeFactory.getType(datatype);
        return parserProfile.createTypedLiteral(lexical, dt, line, col);
    }

    private Node literalDatatype(String lexical, RDFDatatype datatype, Location location) {
        Objects.requireNonNull(lexical);
        Objects.requireNonNull(datatype);
        Objects.requireNonNull(location);
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        return parserProfile.createTypedLiteral(lexical, datatype, line, col);
    }

    // ---- Functions

    private boolean qNameMatches(QName qName1, QName qName2) {
        // QName actually ignores prefix for QName.equals.
        return Objects.equals(qName1.getNamespaceURI(), qName2.getNamespaceURI()) &&
                Objects.equals(qName1.getLocalPart(), qName2.getLocalPart());
    }

    private void checkValidNCName(String string, Location location) {
        //boolean isValid = XMLChar.isValidNCName(string);
        boolean isValid = XML11Char.isXML11ValidNCName(string);
        if ( ! isValid )
            RDFXMLparseWarning("Not a valid XML NCName: '"+string+"'", location);
    }

    private void noContentAllowed(XMLEvent event) {
        if ( event.isCharacters() ) {
            String content = event.asCharacters().getData();
            content = nonWhitespaceForMsg(content);
            throw RDFXMLparseError("Expected XML start tag or end tag. Found text content (possible striping error): \""+content+"\"");
        }
    }

    private static boolean isRDF(QName qName) {
        return rdfNS.equals(qName.getNamespaceURI());
    }

    /** Test for {@code rdf:_NNNNN}. */
    private static boolean isMemberProperty(QName qName) {
        if ( ! isRDF(qName) )
            return false;
        return isMemberPropertyLocalName(qName.getLocalPart());
    }

    private static boolean isMemberPropertyLocalName(String localName) {
        if ( ! localName.startsWith("_") )
            return false;
        String number = localName.substring(1);
        if (number.startsWith("-") || number.startsWith("0"))
            return false;

        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            try {
                // It might be > Integer.MAX_VALUE !!
                java.math.BigInteger i = new java.math.BigInteger(number);
                return true;
            } catch (NumberFormatException ee) {
                return false;
            }
        }
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
    private boolean isNotRecognizedRDFtype(QName qName) {
        if ( ! isRDF(qName) )
            return false;
        String ln = qName.getLocalPart();
        return ! knownRDFTypes.contains(ln);
    }

    private boolean isNotRecognizedRDFproperty(QName qName) {
        if ( ! isRDF(qName) )
            return false;
        String ln = qName.getLocalPart();
        if ( isMemberPropertyLocalName(ln) )
            return false;
        return ! knownRDFProperties.contains(ln);
    }

    private void incIndent() {
        if ( TRACE )
            trace.incIndent();
    }

    private void decIndent() {
        if ( TRACE )
            trace.decIndent();
    }

    // ---- XMLEvents functions

//    /** Return the contents of a text-only element.*/
//    private String nextText()

    private static boolean isComment(int eventType) {
        return eventType == XMLStreamReader.COMMENT;
    }

    private boolean isWhitespace(int eventType) {
        if ( lookingAt(eventType, CHARACTERS ) ) {
            String s = xmlSource.getText();
            return isWhitespace(s);
        }
        return false;
    }

//    private static boolean isWhitespace(char[] ch) {
//        return isWhitespace(ch, 0, ch.length);
//    }
//
    private static boolean isWhitespace(char[] ch, int start, int length) {
        for ( int i = start ; i < start + length ; i++ ) {
            char ich = ch[i];
            if ( !Character.isWhitespace(ich) )
                return false;
        }
        return true;
    }

    private static boolean isWhitespace(CharSequence x) {
        return StringUtils.isWhitespace(x);
    }

    private static String str(Location location) {
        if ( location == null )
            return "[-,-]";
        if ( location.getLineNumber() < 0 && location.getColumnNumber() < 0 )
            return "[?,?]";
        if ( location.getLineNumber() < 0 )
            return String.format("[-, Col: %d]", location.getColumnNumber());
        if ( location.getColumnNumber() < 0 )
            return String.format("[Line: %d, -]", location.getLineNumber());
        return String.format("[Line: %d, Col: %d]", location.getLineNumber(), location.getColumnNumber());
    }

    private static String str(QName qName) {
        String prefix = qName.getPrefix();
        if ( prefix == null || prefix.isEmpty() )
            return String.format("%s", qName.getLocalPart());
        return String.format("%s:%s", qName.getPrefix(), qName.getLocalPart());
    }

    private String strEventType(int eventType) {
        switch (eventType) {
            case START_ELEMENT:
                return str(xmlSource.getName());
            case END_ELEMENT:
                return "/"+str(xmlSource.getName());
            case CHARACTERS:
                return "Event Characters";
            // @see #ATTRIBUTE
            // @see #NAMESPACE
            // @see #PROCESSING_INSTRUCTION
            // @see #SPACE:
            case COMMENT:
                return "Event Comment";
            case START_DOCUMENT:
                return "Event StartDocument";
            case XMLEvent.END_DOCUMENT:
                return "Event EndDocument";
            case DTD:
                return "DTD";
            case ENTITY_DECLARATION:
                return "DTD Entity Decl";
            case ENTITY_REFERENCE:
                return "DTD Entity Ref";
            // @see #DTD
                default:
                    return ""+eventType;
        }
    }

    /** The string for the first non-whitespace index. */
    private static String nonWhitespaceForMsg(String string) {
        for ( int i = 0 ; i < string.length() ; i++ ) {
            if ( !Character.isWhitespace(string.charAt(i)) ) {
                int index = Math.min(20, string.length()-i);
                return string.substring(index);
            }
        }
        throw new RDFXMLParseException("Failed to find any non-whitespace characters");
    }

}
