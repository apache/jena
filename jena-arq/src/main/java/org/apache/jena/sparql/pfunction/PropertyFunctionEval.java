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

package org.apache.jena.sparql.pfunction;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;

/** Basic property function handler that calls the implementation 
 * subclass one binding at a time after evaluating the arguments (if bound). */ 

public abstract class PropertyFunctionEval extends PropertyFunctionBase
{
    protected PropertyFunctionEval(PropFuncArgType subjArgType,  PropFuncArgType objFuncArgType)
    {
        super(subjArgType, objFuncArgType) ;
    }

    @Override
    public final QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        argSubject = Substitute.substitute(argSubject, binding) ;
        argObject =  Substitute.substitute(argObject, binding) ;
        return execEvaluated(binding, argSubject, predicate, argObject, execCxt) ;
    }
    
    /** 
     * @param binding       Current solution from previous query stage 
     * @param argSubject    PropFuncArg, after substitution of any bound variables in this binding
     * @param predicate     This predicate
     * @param argObject     PropFuncArg, after substitution of any bound variables in this binding
     * @param execCxt       Execution context
     * @return              QueryIterator
     */
    public abstract QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) ;
    
}
