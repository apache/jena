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
 * A exception that is thrown when an operation is attempted on a list that is
 * not well-formed, and is being processed in strict mode.
 * </p>
 */
public class InvalidListException
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
     * Construct an invalid list exception with a default message.
     */
    public InvalidListException() {
        super( "Tried to operate on a list that is not well-formed" );
    }

    /**
     * Construct an invalid list exception with a given message.
     * 
     * @param msg The exception message.
     */
    public InvalidListException( String msg ) {
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
