/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.vocabulary ;

import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary definitions from result-set.n3
 */
public class ResultSetGraphVocab {
    /** The namespace of the vocabulary as a string */
    public static final String NS = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";

    /** The namespace of the vocabulary as a string
     *  @see #NS */
    public static String getURI() {return NS;}

    /** The namespace of the vocabulary as a resource */
    public static final Resource NAMESPACE = ResourceFactory.createResource( NS );

    /** Boolean result */
    public static final Property p_boolean = ResourceFactory.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#boolean" );

    /** Variable name */
    public static final Property value = ResourceFactory.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#value" );

    /** Variable name */
    public static final Property variable = ResourceFactory.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#variable" );

    /** Index for ordered result sets */
    public static final Property index = ResourceFactory.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#index" );

    /** Multi-occurrence property associating a result solution (row) resource to
     *  a single (variable, value) binding
     */
    public static final Property binding = ResourceFactory.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#binding" );

    /** MultivaluedName of a variable used in the result set */
    public static final Property resultVariable = ResourceFactory.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#resultVariable" );

    /** Number of rows in the result table */
    public static final Property size = ResourceFactory.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#size" );

    public static final Property solution = ResourceFactory.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#solution" );

    /** Class of things that represent a single (variable, value) pairing */
    public static final Resource ResultBinding = ResourceFactory.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#ResultBinding" );

    /** Class of things that represent a row in the result table - one solution to
     *  the query
     */
    public static final Resource ResultSolution = ResourceFactory.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#ResultSolution" );

    /** Class of things that represent the result set */
    public static final Resource ResultSet = ResourceFactory.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/result-set#ResultSet" );

}
