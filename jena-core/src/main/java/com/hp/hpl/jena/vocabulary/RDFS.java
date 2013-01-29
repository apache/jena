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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;

/**
    RDFS vocabulary items
 */
public class RDFS {

    protected static final String uri="http://www.w3.org/2000/01/rdf-schema#";

    protected static final Resource resource( String local )
        { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
        { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource Class = resource( "Class");
    public static final Resource Datatype = resource( "Datatype");
    
    public static final Resource Container  = resource( "Container");
    
    public static final Resource ContainerMembershipProperty
                                                     = resource( "ContainerMembershipProperty");  
    
    public static final Resource Literal = resource( "Literal");
    public static final Resource Resource = resource( "Resource");

    public static final Property comment = property( "comment");
    public static final Property domain = property( "domain");
    public static final Property label = property( "label");
    public static final Property isDefinedBy = property( "isDefinedBy");
    public static final Property range = property( "range");
    public static final Property seeAlso = property( "seeAlso");
    public static final Property subClassOf  = property( "subClassOf");
    public static final Property subPropertyOf  = property( "subPropertyOf");
    public static final Property member  = property( "member");

    /**
        The RDFS vocabulary, expressed for the SPI layer in terms of .graph Nodes.
    */
    @SuppressWarnings("hiding") public static class Nodes
        {
        public static final Node Class = RDFS.Class.asNode();
        public static final Node Datatype = RDFS.Datatype.asNode();
        public static final Node Container  = RDFS.Container.asNode();
        public static final Node ContainerMembershipProperty
                                                         = RDFS.ContainerMembershipProperty.asNode();
        public static final Node Literal = RDFS.Literal.asNode();
        public static final Node Resource = RDFS.Resource.asNode();
        public static final Node comment = RDFS.comment.asNode();
        public static final Node domain = RDFS.domain.asNode();
        public static final Node label = RDFS.label.asNode();
        public static final Node isDefinedBy = RDFS.isDefinedBy.asNode();
        public static final Node range = RDFS.range.asNode();
        public static final Node seeAlso = RDFS.seeAlso.asNode();
        public static final Node subClassOf  = RDFS.subClassOf.asNode();
        public static final Node subPropertyOf  = RDFS.subPropertyOf.asNode();
        public static final Node member  = RDFS.member.asNode();
        }

    /**
        returns the URI for this schema
        @return the URI for this schema
    */
    public static String getURI() {
        return uri;
    }
}
