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

package org.apache.jena.vocabulary;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFDirLangString;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.datatypes.xsd.impl.RDFhtml;
import org.apache.jena.datatypes.xsd.impl.RDFjson;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary definition for the standard RDF.
 * See <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#">RDF schema (turtle)</a>.
 */
public class RDF {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
     * returns the URI for this schema
     *
     * @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }

    protected static Resource resource(String local) {
        return ResourceFactory.createResource(uri + local);
    }

    protected static Property property(String local) {
        return ResourceFactory.createProperty(uri, local);
    }

    public static final Resource    Alt             = resource("Alt");
    public static final Resource    Bag             = resource("Bag");
    public static final Resource    Property        = resource("Property");
    public static final Resource    Seq             = resource("Seq");
    public static Property li(int i) {
        return property("_" + i);
    }

    public static final Resource    Statement       = resource("Statement");
    public static final Resource    List            = resource("List");
    public static final Resource    nil             = resource("nil");

    // rdfs:comment "A class representing a compound literal."
    public static final Resource CompoundLiteral    = resource("CompoundLiteral");

    public static final Property    first           = property("first");
    public static final Property    rest            = property("rest");
    public static final Property    subject         = property("subject");
    public static final Property    predicate       = property("predicate");
    public static final Property    object          = property("object");
    public static final Property    type            = property("type");
    public static final Property    value           = property("value");
    // RDF 1.1 - the datatypes of language strings
    public static final Resource    langString      = resource("langString");
    // RDF 1.2 - the datatypes of language strings with text direction
    public static final Resource    dirLangString   = resource("dirLangString");
    // rdf:XMLLiteral
    public static final Resource    xmlLiteral      = resource("XMLLiteral");
    // RDF 1.2 - rdf:JSON
    public static final Resource    JSON            = resource("JSON");
    // RDF 1.1 - rdf:HTML
    public static final Resource    HTML            = resource("HTML");

    /**
     * This property is used explicitly in facet restrictions.
     * Also, it can be used as a literal type
     * (e.g., {@code 'test'^^rdf:PlainLiteral}) in old ontologies based on RDF-1.0
     *
     * @see <a href="https://www.w3.org/TR/rdf-plain-literal">rdf:PlainLiteral: A Datatype for RDF Plain Literals (Second Edition)</a>
     */
    public static final Resource    PlainLiteral    = resource("PlainLiteral");

    /**
     * This property is used in facet restrictions.
     * The facet {@code rdf:langRange} can be used to refer to a subset of strings containing the language tag.
     *
     * @see <a href="https://www.w3.org/TR/rdf-plain-literal/#langRange">rdf:langRange</a>
     */
    public static final Property    langRange       = property("langRange");

    // rdfs:comment "The language component of a CompoundLiteral."
    public static final Property    language        = property("language");

    // rdfs:comment "The base direction component of a CompoundLiteral."
    public static final Property    direction       = property("direction");

    // The constant "ltr" (left to right)
    public static final String dirLTR               = TextDirection.LTR.direction();
    // The constant "rtl" (right to left)
    public static final String dirRTL               = TextDirection.RTL.direction();

    public static final RDFDatatype dtLangString    = RDFLangString.rdfLangString;
    public static final RDFDatatype dtDirLangString = RDFDirLangString.rdfDirLangString;
    public static final RDFDatatype dtXMLLiteral    = XMLLiteralType.rdfXMLLiteral;
    // Added to the RDF namespace December 2019
    // https://lists.w3.org/Archives/Public/semantic-web/2019Dec/0027.html
    // rdfs:comment "The datatype of RDF literals storing JSON content."
    public static final RDFDatatype dtRDFJSON       = RDFjson.rdfJSON;
    public static final RDFDatatype dtRDFHTML       = RDFhtml.rdfHTML;

    @Deprecated(forRemoval = true)
    private static class Init {
        // This should not be needed anymore.
        public static Resource Alt()                { return resource( "Alt" ); }
        public static Resource Bag()                { return resource( "Bag" ); }
        // Java8 bug : https://bugzilla.redhat.com/show_bug.cgi?id=1423421
        // Can't have a method called Property() - it crashes the javadoc generation.
        //  https://bugzilla.redhat.com/show_bug.cgi?id=1423421 ==>
        //  https://bugs.openjdk.java.net/browse/JDK-8061305
        public static Resource _Property()          { return resource( "Property" ); }
        public static Resource Seq()                { return resource( "Seq" ); }
        public static Resource Statement()          { return resource( "Statement" ); }
        public static Resource List()               { return resource( "List" ); }
        public static Resource nil()                { return resource( "nil" ); }
        public static Resource PlainLiteral()       { return resource("PlainLiteral"); }
        public static Property first()              { return property( "first" ); }
        public static Property rest()               { return property( "rest" ); }
        public static Property subject()            { return property( "subject" ); }
        public static Property predicate()          { return property( "predicate" ); }
        public static Property object()             { return property( "object" ); }
        public static Property type()               { return property( "type" ); }
        public static Property value()              { return property( "value" ); }
        public static Property langRange()          { return property( "langRange" ); }

        public static Resource langString()         { return ResourceFactory.createResource(dtLangString().getURI()); }
        public static Resource dirLangString()      { return ResourceFactory.createResource(dtDirLangString().getURI()); }
        public static Resource HTML()               { return ResourceFactory.createResource(dtRDFHTML().getURI()); }
        public static Resource xmlLiteral()         { return ResourceFactory.createResource(dtXMLLiteral().getURI()); }
        public static Resource JSON()               { return ResourceFactory.createResource(dtRDFJSON().getURI()) ; }

        public static Resource CompoundLiteral()    { return resource( "CompoundLiteral" ); }
        public static Property language()           { return property( "language" ); }
        public static Property direction()          { return property( "direction" ); }

        public static RDFDatatype dtLangString()    { return RDFLangString.rdfLangString; }
        public static RDFDatatype dtDirLangString() { return RDFDirLangString.rdfDirLangString; }
        public static RDFDatatype dtXMLLiteral()    { return XMLLiteralType.rdfXMLLiteral; }
        public static RDFDatatype dtRDFJSON()       { return RDFjson.rdfJSON; }
        public static RDFDatatype dtRDFHTML()       { return RDFhtml.rdfHTML; }
    }

    /**
     * The main items of RDF vocabulary, but at the Node level, parked inside a nested
     * class so that there's a simple way to refer to them.
     */
    @SuppressWarnings("hiding")
    public static final class Nodes
    {
        public static final Node Alt            = RDF.Alt.asNode();
        public static final Node Bag            = RDF.Bag.asNode();
        public static final Node Property       = RDF.Property.asNode();
        public static final Node Seq            = RDF.Seq.asNode();
        public static Node li(int i)            { return RDF.li(i).asNode(); }
        public static final Node Statement      = RDF.Statement.asNode();
        public static final Node List           = RDF.List.asNode();
        public static final Node nil            = RDF.nil.asNode();
        public static final Node CompoundLiteral = RDF.CompoundLiteral.asNode();
        public static final Node first          = RDF.first.asNode();
        public static final Node rest           = RDF.rest.asNode();
        public static final Node subject        = RDF.subject.asNode();
        public static final Node predicate      = RDF.predicate.asNode();
        public static final Node object         = RDF.object.asNode();
        public static final Node type           = RDF.type.asNode();
        public static final Node value          = RDF.value.asNode();
        public static final Node langString     = RDF.langString.asNode();
        public static final Node dirLangString  = RDF.dirLangString.asNode();
        public static final Node xmlLiteral     = RDF.xmlLiteral.asNode();
        public static final Node HTML           = RDF.HTML.asNode();
        // Added to the RDF namespace December 2019
        // https://lists.w3.org/Archives/Public/semantic-web/2019Dec/0027.html
        public static final Node JSON           = RDF.JSON.asNode();
        public static final Node langRange      = RDF.langRange.asNode();
        public static final Node language       = RDF.language.asNode();
        public static final Node direction      = RDF.direction.asNode();
        public static final Node PlainLiteral   = RDF.PlainLiteral.asNode();
    }
}
