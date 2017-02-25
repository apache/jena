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

    public static final Resource Class          = RDFS.Init.Class();
    public static final Resource Datatype       = RDFS.Init.Datatype();
    
    public static final Resource Container      = RDFS.Init.Container();
    
    public static final Resource ContainerMembershipProperty = RDFS.Init.ContainerMembershipProperty();
    
    public static final Resource Literal        = RDFS.Init.Literal();
    public static final Resource Resource       = RDFS.Init.Resource();

    public static final Property comment        = RDFS.Init.comment();
    public static final Property domain         = RDFS.Init.domain();
    public static final Property label          = RDFS.Init.label();
    public static final Property isDefinedBy    = RDFS.Init.isDefinedBy();
    public static final Property range          = RDFS.Init.range();
    public static final Property seeAlso        = RDFS.Init.seeAlso();
    public static final Property subClassOf     = RDFS.Init.subClassOf();
    public static final Property subPropertyOf  = RDFS.Init.subPropertyOf();
    public static final Property member         = RDFS.Init.member();

    /* RDFS constants are used during Jena initialization.
     * <p>
     * If that initialization is triggered by touching the RDFS class,
     * then the constants are null.
     * <p>
     * So for these cases, call this helper class: RDFS.Init.function()   
     */
    public static class Init {
        public static Resource Class()          { return RDFS.resource( "Class"); }
        public static Resource Datatype()       { return RDFS.resource( "Datatype"); }
        public static Resource Container ()     { return RDFS.resource( "Container"); }
        public static Resource ContainerMembershipProperty()    { return RDFS.resource( "ContainerMembershipProperty");   }
        public static Resource Literal()        { return RDFS.resource( "Literal"); }
        public static Resource Resource()       { return RDFS.resource( "Resource"); }
        public static Property comment()        { return RDFS.property( "comment"); }
        public static Property domain()         { return RDFS.property( "domain"); }
        public static Property label()          { return RDFS.property( "label"); }
        public static Property isDefinedBy()    { return RDFS.property( "isDefinedBy"); }
        public static Property range()          { return RDFS.property( "range"); }
        public static Property seeAlso()        { return RDFS.property( "seeAlso"); }
        public static Property subClassOf ()    { return RDFS.property( "subClassOf"); }
        public static Property subPropertyOf () { return RDFS.property( "subPropertyOf"); }
        public static Property member ()        { return RDFS.property( "member"); }
    }
    
    /**
        The RDFS vocabulary, expressed for the SPI layer in terms of .graph Nodes.
    */
    @SuppressWarnings("hiding") public static class Nodes
        {
        public static final Node Class = RDFS.Init.Class().asNode();
        public static final Node Datatype = RDFS.Init.Datatype().asNode();
        public static final Node Container  = RDFS.Init.Container().asNode();
        public static final Node ContainerMembershipProperty
                                                         = RDFS.Init.ContainerMembershipProperty().asNode();
        public static final Node Literal = RDFS.Init.Literal().asNode();
        public static final Node Resource = RDFS.Init.Resource().asNode();
        public static final Node comment = RDFS.Init.comment().asNode();
        public static final Node domain = RDFS.Init.domain().asNode();
        public static final Node label = RDFS.Init.label().asNode();
        public static final Node isDefinedBy = RDFS.Init.isDefinedBy().asNode();
        public static final Node range = RDFS.Init.range().asNode();
        public static final Node seeAlso = RDFS.Init.seeAlso().asNode();
        public static final Node subClassOf  = RDFS.Init.subClassOf().asNode();
        public static final Node subPropertyOf  = RDFS.Init.subPropertyOf().asNode();
        public static final Node member  = RDFS.Init.member().asNode();
        }

    /**
        returns the URI for this schema
        @return the URI for this schema
    */
    public static String getURI() {
        return uri;
    }
}
