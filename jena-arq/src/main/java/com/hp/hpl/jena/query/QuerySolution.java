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

package com.hp.hpl.jena.query;
import java.util.Iterator ;

import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;


/**
 * A single answer from a SELECT query. */

public interface QuerySolution
{
    /** Return the value of the named variable in this binding.
     *  A return of null indicates that the variable is not present in this solution.
     *  @param varName
     *  @return RDFNode
     */
    public RDFNode get(String varName);

    /** Return the value of the named variable in this binding, casting to a Resource.
     *  A return of null indicates that the variable is not present in this solution.
     *  An exception indicates it was present but not a resource.
     *  @param varName
     *  @return Resource
     */
    public Resource getResource(String varName);

    /** Return the value of the named variable in this binding, casting to a Literal.
     *  A return of null indicates that the variable is not present in this solution.
     *  An exception indicates it was present but not a literal.
     *  @param varName
     *  @return Resource
     */
    public Literal getLiteral(String varName);

    
    /** Return true if the named variable is in this binding */
    public boolean contains(String varName);

    /** Iterate over the variable names (strings) in this QuerySolution.
     * @return Iterator of strings
     */ 
    public Iterator<String> varNames() ;
    
}
