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
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * <p>
 * Implementation of the someValuesFrom restriction abstraction.
 * </p>
 */
public class SomeValuesFromRestrictionImpl
    extends RestrictionImpl
    implements SomeValuesFromRestriction
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating SomeValuesFromRestriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new SomeValuesFromRestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to SomeValuesFromRestriction");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being a SomeValuesFromRestriction facet if it has rdf:type owl:Restriction or equivalent
            // and the combination of owl:onProperty and owl:someValuesFrom (or equivalents)
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, SomeValuesFromRestriction.class );
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
    public SomeValuesFromRestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // someValuesFrom

    /**
     * <p>Assert that this restriction restricts the property to have at least one value
     * that is a member of the given class. Any existing statements for <code>someValuesFrom</code>
     * will be removed.</p>
     * @param cls The class that at least one value of the property must belong to
     * @exception ProfileException If the {@link Profile#SOME_VALUES_FROM()} property is not supported in the current language profile.
     */
    @Override
    public void setSomeValuesFrom( Resource cls ) {
        setPropertyValue( getProfile().SOME_VALUES_FROM(), "SOME_VALUES_FROM", cls );
    }

    /**
     * <p>Answer the resource characterising the constraint on at least one value of the restricted property. This may be
     * a class, the URI of a concrete datatype, a DataRange object or the URI rdfs:Literal.</p>
     * @return A resource, which will have been pre-converted to the appropriate Java value type
     *        ({@link OntClass} or {@link DataRange}) if appropriate.
     * @exception ProfileException If the {@link Profile#SOME_VALUES_FROM()} property is not supported in the current language profile.
     */
    @Override
    public Resource getSomeValuesFrom() {
        checkProfile( getProfile().SOME_VALUES_FROM(), "SOME_VALUES_FROM" );
        Resource r = (Resource) getRequiredProperty( getProfile().SOME_VALUES_FROM() ).getObject();

        boolean currentStrict = ((OntModel) getModel()).strictMode();
        ((OntModel) getModel()).setStrictMode( true );

        try {
            if (r.canAs( OntClass.class )) {
                // all values from a class
                return r.as( OntClass.class );
            }
            else if (r.canAs( DataRange.class )) {
                // all values from a given data range
                return r.as( DataRange.class );
            }
            else {
                // must be a datatype ID or rdfs:Literal
                return r;
            }
        }
        finally {
            ((OntModel) getModel()).setStrictMode( currentStrict );
        }
    }

    /**
     * <p>Answer true if this property restriction has the given class as the class to which at least one
     * value of the restricted property must belong.</p>
     * @param cls A class to test
     * @return True if the given class is the class to which at least one value must belong
     * @exception ProfileException If the {@link Profile#SOME_VALUES_FROM()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasSomeValuesFrom( Resource cls ) {
        return hasPropertyValue( getProfile().SOME_VALUES_FROM(), "SOME_VALUES_FROM", cls );
    }

    /**
     * <p>Remove the statement that this restriction has some values from the given class among
     * the values for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A Resource the denotes the class to be removed from this restriction
     */
    @Override
    public void removeSomeValuesFrom( Resource cls ) {
        removePropertyValue( getProfile().SOME_VALUES_FROM(), "SOME_VALUES_FROM", cls );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
