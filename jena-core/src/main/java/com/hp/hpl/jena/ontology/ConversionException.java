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
 * Exception that is thrown when an ontology resource is converted to another
 * facet, using {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()}, and the requested conversion is not
 * possible. The reasons for the failure may be that the requested term is not
 * in the language {@linkplain Profile profile} of the language attached to the
 * ontology model, or because the pre-conditions for the conversion are not met. 
 * </p>
 */
public class ConversionException 
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

    /**
     * Construct an ontology exception with a given message.
     * 
     * @param msg The exception message.
     */
    public ConversionException( String msg ) {
        super( msg );
    }


    // External signature methods
    //////////////////////////////////

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
