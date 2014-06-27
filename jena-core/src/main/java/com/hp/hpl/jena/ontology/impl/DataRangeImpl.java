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
import java.util.Iterator;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Default implementation of the interface that defines a closed enumeration
 * of concrete values for the range of a property.
 * </p>
 */
public class DataRangeImpl
    extends OntResourceImpl
    implements DataRange
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DataRange facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new DataRangeImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to DataRange");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an DataRange facet if it has rdf:type owl:Datarange and is a bNode
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, DataRange.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a data range node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DataRangeImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    // oneOf

    /**
     * <p>Assert that this data range is exactly the enumeration of the given individuals. Any existing
     * statements for <code>oneOf</code> will be removed.</p>
     * @param en A list of literals that defines the permissible values for this datarange
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void setOneOf( RDFList en ) {
        setPropertyValue( getProfile().ONE_OF(), "ONE_OF", en );
    }

    /**
     * <p>Add a literal to the enumeration that defines the permissible values of this class.</p>
     * @param lit A literal to add to the enumeration
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addOneOf( Literal lit ) {
        addListPropertyValue( getProfile().ONE_OF(), "ONE_OF", lit );
    }

    /**
     * <p>Add each literal from the given iteratation to the
     * enumeration that defines the permissible values of this datarange.</p>
     * @param literals An iterator over literals
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addOneOf( Iterator<Literal> literals ) {
        while( literals.hasNext() ) {
            addOneOf( literals.next() );
        }
    }

    /**
     * <p>Answer a list of literals that defines the extension of this datarange.</p>
     * @return A list of literals that is the permissible values
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public RDFList getOneOf() {
        return objectAs( getProfile().ONE_OF(), "ONE_OF", RDFList.class );
    }

    /**
     * <p>Answer an iterator over all of the literals that are declared to be the permissible values for
     * this class. Each element of the iterator will be an {@link Literal}.</p>
     * @return An iterator over the literals that are the permissible values
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<Literal> listOneOf() {
        return getOneOf().iterator().mapWith( new AsMapper<>( Literal.class ) );
    }

    /**
     * <p>Answer true if the given literal is one of the enumerated literals that are the permissible values
     * of this datarange.</p>
     * @param lit A literal to test
     * @return True if the given literal is in the permissible values for this class.
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasOneOf( Literal lit ) {
        return getOneOf().contains( lit );
    }

    /**
     * <p>Remove the statement that this enumeration includes <code>lit</code> among its members.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param lit A literal that may be declared to be part of this data range, and which is
     * no longer to be one of the data range values.
     */
    @Override
    public void removeOneOf( Literal lit ) {
        setOneOf( getOneOf().remove( lit ) );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
