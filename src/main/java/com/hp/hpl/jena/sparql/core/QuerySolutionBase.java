/**
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

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;

/** Implementation of QuerySolution that contains the canonicalization and casting code. */ 

public abstract class QuerySolutionBase implements QuerySolution
{
    public RDFNode get(String varName)          { return _get(Var.canonical(varName)) ; }
    
    protected abstract RDFNode _get(String varName) ; 

    public Resource getResource(String varName) { return (Resource)get(varName) ; } 

    public Literal getLiteral(String varName)   { return (Literal)get(varName) ; }

    public boolean contains(String varName)     { return _contains(Var.canonical(varName)) ; }  

    protected abstract boolean _contains(String varName) ;
    
    public abstract Iterator<String> varNames() ;
}
