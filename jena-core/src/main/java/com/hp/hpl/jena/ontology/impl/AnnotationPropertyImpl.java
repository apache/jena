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

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Implementation for ontology abstraction of annotation property
 * </p>
 */
public class AnnotationPropertyImpl
    extends OntPropertyImpl
    implements AnnotationProperty
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating AnnotationProperty facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new AnnotationPropertyImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to AnnotationProperty");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an AnnotationProperty facet if it has rdf:type owl:AnnotationProperty or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, AnnotationProperty.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    /**
     * <p>
     * Construct an ontology resource represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public AnnotationPropertyImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer true to indicate that this resource is an RDF property.
     * </p>
     *
     * @return True.
     */
    @Override
    public boolean isProperty() {
        return true;
    }


    /**
     * @see Property#getOrdinal()
     */
    @Override
    public int getOrdinal() {
        return (as( Property.class )).getOrdinal();
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
