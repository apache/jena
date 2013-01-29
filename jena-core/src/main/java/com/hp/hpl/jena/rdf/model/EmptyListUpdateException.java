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
 * Exception that is thrown when an attept is made to perform a side-effectful
 * operation on an {@link RDFList} that is the empty list, or <code>rdf:nil</code>.
 * This is not permissible, since it would cause the URI of the RDFList to change
 * from <code>rdf:nil</code> to a new bNode, and in Jena the URI of a node is
 * invariant.  To avoid this operation, when extending an empty list use operations
 * that return the updated list (such as {@link RDFList#cons}, or {@link RDFList#with},
 * or check first to see if the list {@linkplain RDFList#isEmpty is empty}, and replace
 * it with a non-null list.
 * </p>
 */
public class EmptyListUpdateException 
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

    public EmptyListUpdateException() {
    }
    
    public EmptyListUpdateException( String message ) {
        super( message );
    }
    
    
    // External signature methods
    //////////////////////////////////

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
