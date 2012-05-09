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

package com.hp.hpl.jena.sparql.pfunction;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;

/* Abstraction: QueryStage = PlanElement has a single "build"
 * but it's never worng - this two step process here allows for checking 
 */

/* Can have:
 * One arg or list for both subject and object
 * (?x) is not the same as ?x 
 */

public interface PropertyFunction
{
    /** Called during query plan construction immediately after the
     * construction of the property function instance.
     * @param argSubject   The parsed argument(s) in the subject position 
     * @param predicate    The extension URI (as a Node).
     * @param argObject    The parsed argument(s) in the object position 
     * @param execCxt      Execution context
     */ 
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) ;


    /** Create an iterator of bindings for the given inputs 
     * @param input       QueryIterator from the previous stage
     * @param argSubject  The parsed argument(s) in the subject position 
     * @param predicate    The extension URI (as a Node).
     * @param argObject   The parsed argument(s) in the object position 
     * @param execCxt     The execution context
     * @return            QueryIterator
     */
    public QueryIterator exec(QueryIterator input, 
                              PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                              ExecutionContext execCxt) ;
}
