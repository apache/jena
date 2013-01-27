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
package com.hp.hpl.jena.rdf.model;




// Imports
///////////////
import com.hp.hpl.jena.shared.JenaException;


/**
 * <p>
 * A exception that is thrown when an operation is attempted on an empty (nil)
 * list that actually requires a list of length one or more.
 * </p>
 */
public class EmptyListException
    extends JenaException
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
     * Construct an empty list exception with a default message.
     */
    public EmptyListException() {
        super( "Tried to perform an operation that requires a non-empty list" );
    }

    /**
     * Construct an empty list exception with a given message.
     * 
     * @param msg The exception message.
     */
    public EmptyListException( String msg ) {
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
