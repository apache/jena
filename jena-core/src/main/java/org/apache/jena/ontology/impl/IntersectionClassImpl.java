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
package org.apache.jena.ontology.impl;


// Imports
///////////////
import org.apache.jena.enhanced.* ;
import org.apache.jena.graph.Node ;
import org.apache.jena.ontology.* ;
import org.apache.jena.rdf.model.Property ;


/**
 * <p>
 * Implementation of a node representing an intersection class description.
 * </p>
 */
public class IntersectionClassImpl
    extends BooleanClassDescriptionImpl
    implements IntersectionClass
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating IntersectionClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link org.apache.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new IntersectionClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to IntersectionClass");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an IntersectionClass facet if it has rdf:type owl:Class and an owl:intersectionOf statement (or equivalents)
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&
                   profile.isSupported( node, eg, OntClass.class )  &&
                   AbstractProfile.containsSome( eg, node, profile.INTERSECTION_OF() );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an intersection class node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public IntersectionClassImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    @Override
    public Property operator() {return getProfile().INTERSECTION_OF();}
    @Override
    public String getOperatorName() {return "INTERSECTION_OF";}


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
