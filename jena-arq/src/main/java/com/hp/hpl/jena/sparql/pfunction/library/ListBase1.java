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

package com.hp.hpl.jena.sparql.pfunction.library;

import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArgType ;

/** List operation - object can't be a list. */
public abstract class ListBase1 extends ListBase
{
    public ListBase1()
    { super(PropFuncArgType.PF_ARG_SINGLE) ; }

    @Override
    final protected 
    QueryIterator execOneList(Binding binding, 
                              Node listNode, Node predicate, PropFuncArg object,
                              ExecutionContext execCxt)
    {
        return execOneList(binding, listNode, predicate, object.getArg(), execCxt) ; 
    }

    @Override
    protected abstract QueryIterator execObjectBound(Binding binding, 
                                                     Var listVar, Node predicate, Node object,
                                                     ExecutionContext execCxt) ;

    @Override
    protected QueryIterator execObjectList(Binding binding, Var listVar, Node predicate, List<Node> objectArgs,
                                            ExecutionContext execCxt)
    {
        throw new ARQException("List operation: Object list passed to single object node predicate: "+predicate) ;
    }

    protected abstract QueryIterator execOneList(Binding binding, 
                                                 Node listNode, Node predicate, Node object, 
                                                 ExecutionContext execCxt);
}
