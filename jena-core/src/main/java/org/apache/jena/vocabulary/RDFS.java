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

import org.apache.jena.graph.* ;
import org.apache.jena.rdf.model.* ;

/**
    RDFS vocabulary items
 */
public class RDFS {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri="http://www.w3.org/2000/01/rdf-schema#";

    protected static final Resource resource( String local )
        { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
        { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource Class          = Init.Class();
    public static final Resource Datatype       = Init.Datatype();
    
    public static final Resource Container      = Init.Container();
    
    public static final Resource ContainerMembershipProperty = Init.ContainerMembershipProperty();
    
    public static final Resource Literal        = Init.Literal();
    public static final Resource Resource       = Init.Resource();

    public static final Property comment        = Init.comment();
    public static final Property domain         = Init.domain();
    public static final Property label          = Init.label();
    public static final Property isDefinedBy    = Init.isDefinedBy();
    public static final Property range          = Init.range();
    public static final Property seeAlso        = Init.seeAlso();
    public static final Property subClassOf     = Init.subClassOf();
    public static final Property subPropertyOf  = Init.subPropertyOf();
    public static final Property member         = Init.member();

    /** RDFS constants are used during Jena initialization.
     * <p>
     * If that initialization is triggered by touching the RDFS class,
     * then the constants are null.
     * <p>
     * So for these cases, call this helper class: Init.function()   
     */
    public static class Init {
        public static Resource Class()          { return resource( "Class"); }
        public static Resource Datatype()       { return resource( "Datatype"); }
        public static Resource Container ()     { return resource( "Container"); }
        public static Resource ContainerMembershipProperty()
                                                { return resource( "ContainerMembershipProperty");   }
        public static Resource Literal()        { return resource( "Literal"); }
        public static Resource Resource()       { return resource( "Resource"); }
        public static Property comment()        { return property( "comment"); }
        public static Property domain()         { return property( "domain"); }
        public static Property label()          { return property( "label"); }
        public static Property isDefinedBy()    { return property( "isDefinedBy"); }
        public static Property range()          { return property( "range"); }
        public static Property seeAlso()        { return property( "seeAlso"); }
        public static Property subClassOf ()    { return property( "subClassOf"); }
        public static Property subPropertyOf () { return property( "subPropertyOf"); }
        public static Property member ()        { return property( "member"); }
    }
    
    /**
        The RDFS vocabulary, expressed for the SPI layer in terms of .graph Nodes.
    */
    @SuppressWarnings("hiding") public static class Nodes
        {
        public static final Node Class          = Init.Class().asNode();
        public static final Node Datatype       = Init.Datatype().asNode();
        public static final Node Container      = Init.Container().asNode();
        public static final Node ContainerMembershipProperty
                                                = Init.ContainerMembershipProperty().asNode();
        public static final Node Literal        = Init.Literal().asNode();
        public static final Node Resource       = Init.Resource().asNode();
        public static final Node comment        = Init.comment().asNode();
        public static final Node domain         = Init.domain().asNode();
        public static final Node label          = Init.label().asNode();
        public static final Node isDefinedBy    = Init.isDefinedBy().asNode();
        public static final Node range          = Init.range().asNode();
        public static final Node seeAlso        = Init.seeAlso().asNode();
        public static final Node subClassOf     = Init.subClassOf().asNode();
        public static final Node subPropertyOf  = Init.subPropertyOf().asNode();
        public static final Node member         = Init.member().asNode();
        }

    /**
        returns the URI for this schema
        @return the URI for this schema
    */
    public static String getURI() {
        return uri;
    }
}
