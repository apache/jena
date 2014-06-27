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
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Implementation of the hasValue restriction abstraction.
 * </p>
 */
public class HasValueRestrictionImpl
    extends RestrictionImpl
    implements HasValueRestriction
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating HasValueRestriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new HasValueRestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to HasValueRestriction");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being a HasValueRestriction facet if it has rdf:type owl:Restriction or equivalent
            // and the combination of owl:onProperty and owl:hasValue (or equivalents)
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, HasValueRestriction.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a hasValue restriction node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public HasValueRestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // hasValue

    /**
     * <p>Assert that this restriction restricts the property to have the given
     * value. Any existing statements for <code>hasValue</code>
     * will be removed.</p>
     * @param value The RDF value (an individual or a literal)
     * that is the value that the restricted property must have to be a member of the
     * class defined by this restriction.
     * @exception ProfileException If the {@link Profile#HAS_VALUE()} property is not supported in the current language profile.
     */
    @Override
    public void setHasValue( RDFNode value ) {
        setPropertyValue( getProfile().HAS_VALUE(), "HAS_VALUE", value );
    }

    /**
     * <p>Answer the RDF value that all values of the restricted property must be equal to.</p>
     * @return An RDFNode that is the value of the restricted property
     * @exception ProfileException If the {@link Profile#HAS_VALUE()} property is not supported in the current language profile.
     */
    @Override
    public RDFNode getHasValue() {
        checkProfile( getProfile().HAS_VALUE(), "HAS_VALUE" );
        RDFNode n = getPropertyValue( getProfile().HAS_VALUE() );

        // map to an individual in the case of a resource value
        if (!(n instanceof Literal) && n.canAs( Individual.class )) {
            n = n.as( Individual.class );
        }

        return n;
    }

    /**
     * <p>Answer true if this property restriction has the given RDF value as the value which all
     * values of the restricted property must equal.</p>
     * @param value An RDF value to test
     * @return True if the given value is the value of the restricted property in this restriction
     * @exception ProfileException If the {@link Profile#HAS_VALUE()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasValue( RDFNode value ) {
        return hasPropertyValue( getProfile().HAS_VALUE(), "HAS_VALUE", value );
    }

    /**
     * <p>Remove the statement that this restriction requires the restricted property to have
     * the given value.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param value An RDF value that is to be removed as the required value for the restricted property
     */
    @Override
    public void removeHasValue( RDFNode value ) {
        removePropertyValue( getProfile().HAS_VALUE(), "HAS_VALUE", value );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
