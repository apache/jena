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
 * Exception that is raised when an ontology operation is attempted that is 
 * not present in the language profile for the current ontology model. For example,
 * if the language profile is 
 * {@linkplain com.hp.hpl.jena.ontology.ProfileRegistry#OWL_LITE_LANG owl lite},
 * class complements are not permitted and so that element of the profile is null.
 * An attempt to create a class complement in an OWL Lite model will raise a 
 * ProfileException. 
 * </p>
 */
public class ProfileException
    extends OntologyException 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public ProfileException( String element, Profile profile ) {
        super( "Attempted to use language construct " + element + " that is not supported in the current language profile: " + 
               ((profile == null) ? "not specified" : profile.getLabel()) );
    }
    
    
    // External signature methods
    //////////////////////////////////

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
