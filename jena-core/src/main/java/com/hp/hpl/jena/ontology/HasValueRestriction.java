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
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * A property restriction that requires the named property to have a given individual as
 * its value. 
 * </p>
 */
public interface HasValueRestriction
    extends Restriction 
{
    // Constants
    //////////////////////////////////

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
    public void setHasValue( RDFNode value );

    /**
     * <p>Answer the RDF value that all values of the restricted property must be equal to.</p>
     * @return An RDFNode that is the value of the restricted property
     * @exception ProfileException If the {@link Profile#HAS_VALUE()} property is not supported in the current language profile.   
     */ 
    public RDFNode getHasValue();

    /**
     * <p>Answer true if this property restriction has the given RDF value as the value which all 
     * values of the restricted property must equal.</p>
     * @param value An RDF value to test 
     * @return True if the given value is the value of the restricted property in this restriction
     * @exception ProfileException If the {@link Profile#HAS_VALUE()} property is not supported in the current language profile.   
     */
    public boolean hasValue( RDFNode value );
    
    /**
     * <p>Remove the statement that this restriction requires the restricted property to have
     * the given value.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param value An RDF value that is to be removed as the required value for the restricted property
     */
    public void removeHasValue( RDFNode value );
    

}
