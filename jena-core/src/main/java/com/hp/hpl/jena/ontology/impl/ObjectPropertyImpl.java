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
import java.util.*;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;



/**
 * <p>
 * Implementation of the object property abstraction
 * </p>
 */
public class ObjectPropertyImpl
    extends OntPropertyImpl
    implements ObjectProperty
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating ObjectProperty facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new ObjectPropertyImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to ObjectProperty");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an ObjectProperty facet if it has rdf:type owl:ObjectProperty or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, ObjectProperty.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a functional property node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public ObjectPropertyImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer a property that is an inverse of this property, ensuring that it
     * presents the ObjectProperty facet.</p>
     * @return A property inverse to this property
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    @Override
    public OntProperty getInverseOf() {
        OntProperty inv = super.getInverseOf();
        return (inv == null) ? null : inv.asObjectProperty();
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be inverse properties of
     * this property, ensuring that each presents the objectProperty facet.</p>
     * @return An iterator over the properties inverse to this property.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<? extends OntProperty> listInverseOf() {
        List<OntProperty> objPs = new ArrayList<>();
        for (Iterator<? extends OntProperty> i = super.listInverseOf(); i.hasNext(); ) {
            objPs.add( i.next().as( ObjectProperty.class ) );
        }
        return WrappedIterator.create( objPs.iterator() );
    }

    /**
     * <p>Answer the property that is the inverse of this property, ensuring that it presents
     * the object property facet.</p>
     * @return The property that is the inverse of this property, or null.
     */
    @Override
    public OntProperty getInverse() {
        OntProperty inv = super.getInverse();
        return (inv != null) ? inv.asObjectProperty() : null;
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
