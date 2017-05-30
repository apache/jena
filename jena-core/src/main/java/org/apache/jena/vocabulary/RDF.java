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

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.impl.RDFLangString ;
import org.apache.jena.datatypes.xsd.impl.RDFhtml ;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType ;
import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.ResourceFactory ;

/**
    The standard RDF vocabulary.
*/

public class RDF{

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** returns the URI for this schema
        @return the URI for this schema
    */
    public static String getURI()
        { return uri; }

    protected static final Resource resource( String local )
        { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
        { return ResourceFactory.createProperty( uri, local ); }

    public static Property li( int i )
        { return property( "_" + i ); }

    public static final Resource    Alt          = Init.Alt();
    public static final Resource    Bag          = Init.Bag();
    public static final Resource    Property     = Init._Property();
    public static final Resource    Seq          = Init.Seq();
    public static final Resource    Statement    = Init.Statement();
    public static final Resource    List         = Init.List();
    public static final Resource    nil          = Init.nil();

    public static final Property    first        = Init.first();
    public static final Property    rest         = Init.rest();
    public static final Property    subject      = Init.subject();
    public static final Property    predicate    = Init.predicate();
    public static final Property    object       = Init.object();
    public static final Property    type         = Init.type();
    public static final Property    value        = Init.value();

    // RDF 1.1 - the datatypes of language strings
    public static final Resource    langString   = Init.langString();

    // RDF 1.1 - rdf:HTML
    public static final Resource    HTML         = Init.HTML();

    // rdf:XMLLiteral
    public static final Resource    xmlLiteral   = Init.xmlLiteral();

    public static final RDFDatatype dtRDFHTML    = Init.dtRDFHTML();
    public static final RDFDatatype dtLangString = Init.dtLangString();
    public static final RDFDatatype dtXMLLiteral = Init.dtXMLLiteral();

    /** RDF constants are used during Jena initialization.
     * <p>
     * If that initialization is triggered by touching the RDF class,
     * then the constants are null.
     * <p>
     * So for these cases, call this helper class: Init.function()   
     */
    public static class Init {
        // JENA-1294
        // Version that calculate the constant when called. 
        public static Resource Alt()              { return resource( "Alt" ); }
        public static Resource Bag()              { return resource( "Bag" ); }
        // Java8 bug : https://bugzilla.redhat.com/show_bug.cgi?id=1423421
        // Can't have a methdo called Property() - it crashes the javadoc generation.
        //  https://bugzilla.redhat.com/show_bug.cgi?id=1423421 ==>
        //  https://bugs.openjdk.java.net/browse/JDK-8061305
        public static Resource _Property()         { return resource( "Property" ); }
        public static Resource Seq()              { return resource( "Seq" ); }
        public static Resource Statement()        { return resource( "Statement" ); }
        public static Resource List()             { return resource( "List" ); }
        public static Resource nil()              { return resource( "nil" ); }
        public static Property first()            { return property( "first" ); }
        public static Property rest()             { return property( "rest" ); }
        public static Property subject()          { return property( "subject" ); }
        public static Property predicate()        { return property( "predicate" ); }
        public static Property object()           { return property( "object" ); }
        public static Property type()             { return property( "type" ); }
        public static Property value()            { return property( "value" ); }
        
        public static Resource langString()       { return ResourceFactory.createResource(dtLangString().getURI()) ; }
        public static Resource HTML()             { return ResourceFactory.createResource(dtRDFHTML().getURI()) ; }
        public static Resource xmlLiteral()       { return ResourceFactory.createResource(dtXMLLiteral().getURI()) ; }
        
        public static RDFDatatype dtRDFHTML()     { return RDFhtml.rdfHTML; }
        public static RDFDatatype dtLangString()  { return RDFLangString.rdfLangString; }
        public static RDFDatatype dtXMLLiteral()  { return XMLLiteralType.theXMLLiteralType; }
    }
    
    /**
        The same items of vocabulary, but at the Node level, parked inside a
        nested class so that there's a simple way to refer to them.
    */
    @SuppressWarnings("hiding") 
    public static final class Nodes
    {
        public static final Node Alt        = Init.Alt().asNode();
        public static final Node Bag        = Init.Bag().asNode();
        public static final Node Property   = Init._Property().asNode();
        public static final Node Seq        = Init.Seq().asNode();
        public static final Node Statement  = Init.Statement().asNode();
        public static final Node List       = Init.List().asNode();
        public static final Node nil        = Init.nil().asNode();
        public static final Node first      = Init.first().asNode();
        public static final Node rest       = Init.rest().asNode();
        public static final Node subject    = Init.subject().asNode();
        public static final Node predicate  = Init.predicate().asNode();
        public static final Node object     = Init.object().asNode();
        public static final Node type       = Init.type().asNode();
        public static final Node value      = Init.value().asNode();
        public static final Node langString = Init.langString().asNode();
        public static final Node HTML       = Init.HTML().asNode();
        public static final Node xmlLiteral = Init.xmlLiteral().asNode();
    }
}
