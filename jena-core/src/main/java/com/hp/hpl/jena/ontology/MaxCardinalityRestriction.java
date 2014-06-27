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

/**
 * <p>
 * A property restriction that requires the named property to have have at most
 * the given number of values for a given instance to be a member of the class defined
 * by the restriction.
 * </p>
 */
public interface MaxCardinalityRestriction
    extends Restriction 
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    // maxCardinality
    
    /**
     * <p>Assert that this restriction restricts the property to have the given
     * maximum cardinality. Any existing statements for <code>maxCardinality</code>
     * will be removed.</p>
     * @param cardinality The maximum cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#MAX_CARDINALITY()} property is not supported in the current language profile.   
     */ 
    public void setMaxCardinality( int cardinality );

    /**
     * <p>Answer the maximum cardinality of the restricted property.</p>
     * @return The maximum cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#MAX_CARDINALITY()} property is not supported in the current language profile.   
     */ 
    public int getMaxCardinality();

    /**
     * <p>Answer true if this property restriction has the given maximum cardinality.</p>
     * @param cardinality The cardinality to test against 
     * @return True if the given cardinality is the max cardinality of the restricted property in this restriction
     * @exception ProfileException If the {@link Profile#MAX_CARDINALITY()} property is not supported in the current language profile.   
     */
    public boolean hasMaxCardinality( int cardinality );
    
    /**
     * <p>Remove the statement that this restriction has the given maximum cardinality 
     * for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cardinality A max cardinality value to be removed from this restriction
     */
    public void removeMaxCardinality( int cardinality );
    


}
