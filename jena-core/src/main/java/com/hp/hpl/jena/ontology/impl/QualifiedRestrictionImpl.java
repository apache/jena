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
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Implementation of qualied restrictions.
 * </p>
 */
public class QualifiedRestrictionImpl
    extends RestrictionImpl
    implements QualifiedRestriction
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating QualifiedRestriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new QualifiedRestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to QualifiedRestriction");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg )
            { return isValidQualifiedRestriction( node, eg ); }
    };

    private static boolean isValidQualifiedRestriction( Node node, EnhGraph eg )
        {
        // node will support being a QualifiedRestriction facet if it has rdf:type owl:Restriction or equivalent
        Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
        return (profile != null)  &&  profile.isSupported( node, eg, QualifiedRestriction.class );
        }

    @Override
    public boolean isValid()
        { return isValidQualifiedRestriction( asNode(), getGraph() ); }

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a qualified restriction node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public QualifiedRestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert that this qualified restriction restricts the property to have a given
     * cardinality and to have values belonging to the class denoted by <code>hasClassQ</code>.
     * Any existing statements for <code>hasClassQ</code>
     * will be removed.</p>
     * @param cls The class to which all of the value of the restricted property must belong
     * @exception ProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.
     */
    @Override
    public void setHasClassQ( OntClass cls ) {
        setPropertyValue( getProfile().HAS_CLASS_Q(), "HAS_CLASS_Q", cls );
    }

    /**
     * <p>Answer the class or datarnage to which all values of the restricted property belong.</p>
     * @return The ontology class of the restricted property values
     * @exception ProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.
     */
    @Override
    public OntResource getHasClassQ() {
        checkProfile( getProfile().HAS_CLASS_Q(), "HAS_CLASS_Q" );
        Resource r = getProperty( getProfile().HAS_CLASS_Q() ).getResource();
        if (r.canAs( OntClass.class )) {
            return r.as( OntClass.class );
        }
        else if (r.canAs( DataRange.class )) {
            return r.as( DataRange.class );
        }
        else {
            return r.as( OntResource.class );
        }
    }

    /**
     * <p>Answer true if this qualified property restriction has the given class as
     * the class to which all of the property values must belong.</p>
     * @param cls The class to test against
     * @return True if the given class is the class to which all members of this restriction must belong
     * @exception ProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasHasClassQ( OntClass cls ) {
        return hasPropertyValue( getProfile().HAS_CLASS_Q(), "HAS_CLASS_Q", cls );
    }

    /**
     * <p>Answer true if this qualified property restriction has the given datarange as
     * the class to which all of the property values must belong.</p>
     * @param dr The datarange to test against
     * @return True if the given class is the class to which all members of this restriction must belong
     * @exception ProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasHasClassQ( DataRange dr ) {
        return hasPropertyValue( getProfile().HAS_CLASS_Q(), "HAS_CLASS_Q", dr );
    }

    /**
     * <p>Remove the statement that this restriction has the given class
     * as the class to which all values must belong.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls The ont class that is the object of the <code>hasClassQ</code> property.
     */
    @Override
    public void removeHasClassQ( OntClass cls ) {
        Property has_class_q = getProfile().HAS_CLASS_Q();
        removePropertyValue( has_class_q, "HAS_CLASS_Q", cls );
    }

    /**
     * <p>Remove the statement that this restriction has the given datarange
     * as the class to which all values must belong.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param dr The datarange that is the object of the <code>hasClassQ</code> property.
     */
    @Override
    public void removeHasClassQ( DataRange dr ) {
        removePropertyValue( getProfile().HAS_CLASS_Q(), "HAS_CLASS_Q", dr );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
