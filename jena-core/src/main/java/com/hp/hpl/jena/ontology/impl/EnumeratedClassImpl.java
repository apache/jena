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
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Implementation of a node representing an enumerated class description.
 * </p>
 */
public class EnumeratedClassImpl
    extends OntClassImpl
    implements EnumeratedClass
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating EnumeratedClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new EnumeratedClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to EnumeratedClass");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an EnumeratedClass facet if it has rdf:type owl:Class and an owl:oneOf statement (or equivalents)
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&
                   profile.isSupported( node, eg, OntClass.class )  &&
                   eg.asGraph().contains( node, profile.ONE_OF().asNode(), Node.ANY );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an enumerated class node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public EnumeratedClassImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // oneOf

    /**
     * <p>Assert that this class is exactly the enumeration of the given individuals. Any existing
     * statements for <code>oneOf</code> will be removed.</p>
     * @param en A list of individuals that defines the class extension for this class
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void setOneOf( RDFList en ) {
        setPropertyValue( getProfile().ONE_OF(), "ONE_OF", en );
    }

    /**
     * <p>Add an individual to the enumeration that defines the class extension of this class.</p>
     * @param res An individual to add to the enumeration
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addOneOf( Resource res ) {
        addListPropertyValue( getProfile().ONE_OF(), "ONE_OF", res );
    }

    /**
     * <p>Add each individual from the given iteration to the
     * enumeration that defines the class extension of this class.</p>
     * @param individuals An iterator over individuals
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addOneOf( Iterator<? extends Resource> individuals ) {
        while( individuals.hasNext() ) {
            addOneOf( individuals.next() );
        }
    }

    /**
     * <p>Answer a list of individuals that defines the extension of this class.</p>
     * @return A list of individuals that is the class extension
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public RDFList getOneOf() {
        return objectAs( getProfile().ONE_OF(), "ONE_OF", RDFList.class );
    }

    /**
     * <p>Answer an iterator over all of the individuals that are declared to be the class extension for
     * this class. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the individuals in the class extension
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<? extends OntResource> listOneOf() {
        return getOneOf().iterator().mapWith( new AsMapper<>( OntResource.class ) );
    }

    /**
     * <p>Answer true if the given individual is one of the enumerated individuals in the class extension
     * of this class.</p>
     * @param res An individual to test
     * @return True if the given individual is in the class extension for this class.
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasOneOf( Resource res ) {
        return getOneOf().contains( res );
    }

    /**
     * <p>Remove the statement that this enumeration includes <code>res</code> among its members.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be part of this enumeration, and which is
     * no longer one of the enumeration values.
     */
    @Override
    public void removeOneOf( Resource res ) {
        setOneOf( getOneOf().remove( res ) );
    }



    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
