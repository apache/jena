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
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;


/**
 * <p>
 * Implementation of the min cardinality restriction abstraction.
 * </p>
 */
public class MinCardinalityRestrictionImpl
    extends RestrictionImpl
    implements MinCardinalityRestriction
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating MinCardinalityRestriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new MinCardinalityRestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to MinCardinalityRestriction");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being a MinCardinalityRestriction facet if it has rdf:type owl:Restriction or equivalent
            // and the combination of owl:onProperty and owl:cardinality (or equivalents)
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, MinCardinalityRestriction.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a min cardinality restriction node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public MinCardinalityRestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }

    // External signature methods
    //////////////////////////////////

    // minCardinality

    /**
     * <p>Assert that this restriction restricts the property to have the given
     * minimum cardinality. Any existing statements for <code>minCardinality</code>
     * will be removed.</p>
     * @param cardinality The minimum cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#MIN_CARDINALITY()} property is not supported in the current language profile.
     */
    @Override
    public void setMinCardinality( int cardinality ) {
        setPropertyValue( getProfile().MIN_CARDINALITY(), "MIN_CARDINALITY", getModel().createTypedLiteral( cardinality ) );
    }

    /**
     * <p>Answer the minimum cardinality of the restricted property.</p>
     * @return The minimum cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#MIN_CARDINALITY()} property is not supported in the current language profile.
     */
    @Override
    public int getMinCardinality() {
        return objectAsInt( getProfile().MIN_CARDINALITY(), "MIN_CARDINALITY" );
    }

    /**
     * <p>Answer true if this property restriction has the given minimum cardinality.</p>
     * @param cardinality The cardinality to test against
     * @return True if the given cardinality is the min cardinality of the restricted property in this restriction
     * @exception ProfileException If the {@link Profile#MIN_CARDINALITY()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasMinCardinality( int cardinality ) {
        return hasPropertyValue( getProfile().MIN_CARDINALITY(), "MIN_CARDINALITY", getModel().createTypedLiteral( cardinality ) );
    }

    /**
     * <p>Remove the statement that this restriction has the given minimum cardinality
     * for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cardinality A min cardinality value to be removed from this restriction
     */
    @Override
    public void removeMinCardinality( int cardinality ) {
        removePropertyValue( getProfile().MIN_CARDINALITY(), "MIN_CARDINALITY", getModel().createTypedLiteral( cardinality ) );
    }



    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
