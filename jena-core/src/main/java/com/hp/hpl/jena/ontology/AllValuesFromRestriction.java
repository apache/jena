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
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * <p>
 * A property restriction that requires the named property to have have any range
 * instances to be members of a given class.
 * </p>
 */
public interface AllValuesFromRestriction
    extends Restriction 
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    // allValuesFrom
    
    /**
     * <p>Assert that this restriction restricts the property to have all values
     * be members of the given class. Any existing statements for <code>allValuesFrom</code>
     * will be removed.</p>
     * @param cls The class that all values of the property must belong to
     * @exception ProfileException If the {@link Profile#ALL_VALUES_FROM()} property is not supported in the current language profile.   
     */ 
    public void setAllValuesFrom( Resource cls );

    /**
     * <p>Answer the resource characterising the constraint on all values of the restricted property. This may be
     * a class, the URI of a concrete datatype, a DataRange object or the URI rdfs:Literal.</p>
     * @return A resource, which will have been pre-converted to the appropriate Java value type
     *        ({@link OntClass} or {@link DataRange}) if appropriate.
     * @exception ProfileException If the {@link Profile#ALL_VALUES_FROM()} property is not supported in the current language profile.   
     */ 
    public Resource getAllValuesFrom();

    /**
     * <p>Answer true if this property restriction has the given class as the class to which all 
     * values of the restricted property must belong.</p>
     * @param cls A class to test 
     * @return True if the given class is the class to which all values must belong
     * @exception ProfileException If the {@link Profile#ALL_VALUES_FROM()} property is not supported in the current language profile.   
     */
    public boolean hasAllValuesFrom( Resource cls );
    
    /**
     * <p>Remove the statement that this restriction has all values from the given class among
     * the values for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A Resource the denotes the class to be removed from this restriction
     */
    public void removeAllValuesFrom( Resource cls );
    

}
