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

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.IterLib ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.sparql.util.graph.GNode ;
import com.hp.hpl.jena.sparql.util.graph.GraphList ;

/** List length : property function to get the length of a list. */ 

public class listLength extends ListBase1
{
    @Override
    public QueryIterator execOneList(Binding binding, Node listNode, Node predicate, Node length, ExecutionContext execCxt)
    {
        Graph graph = execCxt.getActiveGraph() ;
        if ( Var.isVar(listNode) )
            throw new ARQInternalErrorException("listLength: Subject is a variable") ;
        // Case : arg 1 (the list) is bound and arg 2 not bound => generate possibilities
        // Case : arg 1 is bound and arg 2 is bound => test for membership.

        if ( Var.isVar(length) )
            return length(binding, graph, listNode,  Var.alloc(length) , execCxt) ;
        else
            return verify(binding, graph, listNode, length, execCxt) ;
    }

    private QueryIterator length(Binding binding, Graph graph, 
                                 Node listNode, Var varLength,
                                 ExecutionContext execCxt)
    {
        int x = GraphList.length(new GNode(graph, listNode)) ;
        if ( x < 0 )
            return IterLib.noResults(execCxt) ;
        Node n = NodeFactoryExtra.intToNode(x) ;
        return IterLib.oneResult(binding, varLength, n, execCxt) ;
    }
    
    private QueryIterator verify(Binding binding, Graph graph, Node listNode, Node length, ExecutionContext execCxt)
    {
        int x = GraphList.length(new GNode(graph, listNode)) ;
        int len = NodeFactoryExtra.nodeToInt(length) ;
        
        if ( x == len )
            return IterLib.result(binding, execCxt) ;
        return IterLib.noResults(execCxt) ;
    }

    @Override
    protected QueryIterator execObjectBound(Binding binding, Var listVar, Node predicate, Node length,
                                            ExecutionContext execCxt)
    {
        Graph graph = execCxt.getActiveGraph() ;
        return length(binding, graph, listVar,  Var.alloc(length) , execCxt) ;
    }
}
