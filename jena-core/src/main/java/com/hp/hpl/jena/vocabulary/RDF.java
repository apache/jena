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

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

/**
    The standard RDF vocabulary.
*/

public class RDF{

    protected static final String uri ="http://www.w3.org/1999/02/22-rdf-syntax-ns#";

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

    public static final Resource Alt = resource( "Alt" );
    public static final Resource Bag = resource( "Bag" );
    public static final Resource Property = resource( "Property" );
    public static final Resource Seq = resource( "Seq" );
    public static final Resource Statement = resource( "Statement" );
    public static final Resource List = resource( "List" );
    public static final Resource nil = resource( "nil" );

    public static final Property first = property( "first" );
    public static final Property rest = property( "rest" );
    public static final Property subject = property( "subject" );
    public static final Property predicate = property( "predicate" );
    public static final Property object = property( "object" );
    public static final Property type = property( "type" );
    public static final Property value = property( "value" );

    /**
        The same items of vocabulary, but at the Node level, parked inside a
        nested class so that there's a simple way to refer to them.
    */
    @SuppressWarnings("hiding") public static final class Nodes
        {
        public static final Node Alt = RDF.Alt.asNode();
        public static final Node Bag = RDF.Bag.asNode();
        public static final Node Property = RDF.Property.asNode();
        public static final Node Seq = RDF.Seq.asNode();
        public static final Node Statement = RDF.Statement.asNode();
        public static final Node List = RDF.List.asNode();
        public static final Node nil = RDF.nil.asNode();
        public static final Node first = RDF.first.asNode();
        public static final Node rest = RDF.rest.asNode();
        public static final Node subject = RDF.subject.asNode();
        public static final Node predicate = RDF.predicate.asNode();
        public static final Node object = RDF.object.asNode();
        public static final Node type = RDF.type.asNode();
        public static final Node value = RDF.value.asNode();
        }

}
