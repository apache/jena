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
 * Implementation of the cardinality restriction abstraction.
 * </p>
 */
public class CardinalityRestrictionImpl
    extends RestrictionImpl
    implements CardinalityRestriction
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating CardinalityRestriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new CardinalityRestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to CardinalityRestriction");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being a CardinalityRestriction facet if it has rdf:type owl:Restriction or equivalent
            // and the combination of owl:onProperty and owl:cardinality (or equivalents)
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, CardinalityRestriction.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a cardinality restriction node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public CardinalityRestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }

    // External signature methods
    //////////////////////////////////

    // cardinality

    /**
     * <p>Assert that this restriction restricts the property to have the given
     * cardinality. Any existing statements for <code>cardinality</code>
     * will be removed.</p>
     * @param cardinality The cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#CARDINALITY()} property is not supported in the current language profile.
     */
    @Override
    public void setCardinality( int cardinality ) {
        setPropertyValue( getProfile().CARDINALITY(), "CARDINALITY", getModel().createTypedLiteral( cardinality ) );
    }

    /**
     * <p>Answer the cardinality of the restricted property.</p>
     * @return The cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#CARDINALITY()} property is not supported in the current language profile.
     */
    @Override
    public int getCardinality() {
        return objectAsInt( getProfile().CARDINALITY(), "CARDINALITY" );
    }

    /**
     * <p>Answer true if this property restriction has the given cardinality.</p>
     * @param cardinality The cardinality to test against
     * @return True if the given cardinality is the cardinality of the restricted property in this restriction
     * @exception ProfileException If the {@link Profile#CARDINALITY()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasCardinality( int cardinality ) {
        return hasPropertyValue( getProfile().CARDINALITY(), "CARDINALITY", getModel().createTypedLiteral( cardinality ) );
    }

    /**
     * <p>Remove the statement that this restriction has the given cardinality
     * for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cardinality A cardinality value to be removed from this restriction
     */
    @Override
    public void removeCardinality( int cardinality ) {
        removePropertyValue( getProfile().CARDINALITY(), "CARDINALITY", getModel().createTypedLiteral( cardinality ) );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
