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
package com.hp.hpl.jena.ontology;



// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Encapsulates a class description representing a closed enumeration of individuals.
 * </p>
 */
public interface EnumeratedClass
    extends OntClass
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    // oneOf

    /**
     * <p>Assert that this class is exactly the enumeration of the given individuals. Any existing
     * statements for <code>oneOf</code> will be removed.</p>
     * @param en A list of individuals that defines the class extension for this class
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    public void setOneOf( RDFList en );

    /**
     * <p>Add an individual to the enumeration that defines the class extension of this class.</p>
     * @param res An individual to add to the enumeration
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    public void addOneOf( Resource res );

    /**
     * <p>Add each individual from the given iteration to the
     * enumeration that defines the class extension of this class.</p>
     * @param individuals An iterator over individuals
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    public void addOneOf( Iterator<? extends Resource> individuals );

    /**
     * <p>Answer a list of individuals that defines the extension of this class.</p>
     * @return A list of individuals that is the class extension
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    public RDFList getOneOf();

    /**
     * <p>Answer an iterator over all of the individuals that are declared to be the class extension for
     * this class. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the individuals in the class extension
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntResource> listOneOf();

    /**
     * <p>Answer true if the given individual is one of the enumerated individuals in the class extension
     * of this class.</p>
     * @param res An individual to test
     * @return True if the given individual is in the class extension for this class.
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.
     */
    public boolean hasOneOf( Resource res );

    /**
     * <p>Remove the statement that this enumeration includes <code>res</code> among its members.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be part of this enumeration, and which is
     * no longer one of the enumeration values.
     */
    public void removeOneOf( Resource res );


}
