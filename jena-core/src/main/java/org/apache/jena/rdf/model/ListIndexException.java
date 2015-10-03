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
package org.apache.jena.rdf.model;



// Imports
///////////////
import org.apache.jena.shared.JenaException ;


/**
 * <p>
 * A exception that is thrown when an operation attempts to access an indexed
 * list element beyond the length of the list.
 * </p>
 */
public class ListIndexException
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
     * Construct a list index exception with a default message.
     */
    public ListIndexException() {
        super( "Tried to index beyond the length of a list" );
    }

    /**
     * Construct a list index exception with a given message.
     * 
     * @param msg The exception message.
     */
    public ListIndexException( String msg ) {
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
