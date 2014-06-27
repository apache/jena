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
 * Represents a qualified restriction, in which all values of the restricted property
 * are required to be members of a given class.  At present, this capability is only
 * part of DAML+OIL, not OWL.
 * </p>
 */
public interface QualifiedRestriction
    extends Restriction
{
    // Constants
    //////////////////////////////////

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
    public void setHasClassQ( OntClass cls );

    /**
     * <p>Answer the class or datarange to which all values of the restricted property belong.</p>
     * @return The ontology class of the restricted property values
     * @exception ProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.
     */
    public OntResource getHasClassQ();

    /**
     * <p>Answer true if this qualified property restriction has the given class as
     * the class to which all of the property values must belong.</p>
     * @param cls The class to test against
     * @return True if the given class is the class to which all members of this restriction must belong
     * @exception ProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.
     */
    public boolean hasHasClassQ( OntClass cls );

    /**
     * <p>Answer true if this qualified property restriction has the given datarange as
     * the class to which all of the property values must belong.</p>
     * @param dr The datarange to test against
     * @return True if the given class is the class to which all members of this restriction must belong
     * @exception ProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.
     */
    public boolean hasHasClassQ( DataRange dr );

    /**
     * <p>Remove the statement that this restriction has the given class
     * as the class to which all values must belong.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls The ont class that is the object of the <code>hasClassQ</code> property.
     */
    public void removeHasClassQ( OntClass cls );

    /**
     * <p>Remove the statement that this restriction has the given datarange
     * as the class to which all values must belong.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param dr The datarange that is the object of the <code>hasClassQ</code> property.
     */
    public void removeHasClassQ( DataRange dr );


}
