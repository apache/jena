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
 * Represents an ontology DataRange: a class-like construct that contains only concrete
 * data literals.  See section 6.2 of the OWL language reference for details.  In OWL
 * Full, there is no difference between a DataRange and a Class.
 * </p>
 */
public interface DataRange 
    extends OntResource
{
    // Constants
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
    public void setOneOf( RDFList en );

    /**
     * <p>Add a literal to the enumeration that defines the permissible values of this class.</p>
     * @param lit A literal to add to the enumeration
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public void addOneOf( Literal lit );

    /**
     * <p>Add each literal from the given iteratation to the 
     * enumeration that defines the permissible values of this datarange.</p>
     * @param literals An iterator over literals
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public void addOneOf( Iterator<Literal> literals );

    /**
     * <p>Answer a list of literals that defines the extension of this datarange.</p>
     * @return A list of literals that is the permissible values
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public RDFList getOneOf();

    /**
     * <p>Answer an iterator over all of the literals that are declared to be the permissible values for
     * this class. Each element of the iterator will be an {@link Literal}.</p>
     * @return An iterator over the literals that are the permissible values
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator<Literal> listOneOf();

    /**
     * <p>Answer true if the given literal is one of the enumerated literals that are the permissible values
     * of this datarange.</p>
     * @param lit A literal to test
     * @return True if the given literal is in the permissible values for this class.
     * @exception ProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */
    public boolean hasOneOf( Literal lit );
    
    /**
     * <p>Remove the statement that this enumeration includes <code>lit</code> among its members.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param lit A literal that may be declared to be part of this data range, and which is
     * no longer to be one of the data range values.
     */
    public void removeOneOf( Literal lit );
    

}
