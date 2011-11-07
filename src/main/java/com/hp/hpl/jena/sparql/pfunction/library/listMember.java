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
import com.hp.hpl.jena.query.QueryBuildException ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterExtendByVar ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterYieldN ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.util.graph.GNode ;
import com.hp.hpl.jena.sparql.util.graph.GraphList ;


/** List membership : property function implementation of list:member. */ 

public class listMember extends ListBase1
{
    // Does not work for   ... list:member ?x . 
    // See old execOneList
    public listMember()
    { super() ; }
   
    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        super.build(argSubject, predicate, argObject, execCxt) ;
        
        if ( argObject.isList() && argObject.getArgList().size() != 0 )
            throw new QueryBuildException("List arguments (object) to "+predicate.getURI()) ;
    }
    
    @Override
    protected QueryIterator execOneList(Binding binding, Node listNode, Node predicate, Node member, ExecutionContext execCxt)
    {
        if ( Var.isVar(listNode) )
            throw new QueryExecException("List : subject not a list or variable bound to a list") ;
        // Case : arg 1 (the list) is bound and arg 2 not bound => generate possibilities
        // Case : arg 1 is bound and arg 2 is bound => test for membership.

        if ( Var.isVar(member) )
            return members(binding, listNode,  Var.alloc(member) , execCxt) ;
        else
            return verify(binding, listNode, member, execCxt) ;
    }

    @Override
    protected QueryIterator execObjectBound(Binding binding, Var listVar, Node predicate, Node object,
                                            ExecutionContext execCxt)
    {
        // Given a concrete node, find lists it's in
        GNode gnode = new GNode(execCxt.getActiveGraph(), object) ;
        List<Node> lists = GraphList.listFromMember(gnode) ;
        return new QueryIterExtendByVar(binding, listVar, lists.iterator(), execCxt) ;
    }



    private QueryIterator members(Binding binding, Node listNode, Var itemVar, ExecutionContext execCxt)
    {
        List<Node> members = GraphList.members(new GNode(execCxt.getActiveGraph(), listNode)) ;
        return new QueryIterExtendByVar(binding, itemVar, members.iterator(), execCxt) ;
    }
    
    private QueryIterator verify(Binding binding, Node listNode, Node member, ExecutionContext execCxt)
    {
        int count = GraphList.occurs(new GNode(execCxt.getActiveGraph(), listNode), member) ;
        return new QueryIterYieldN(count, binding) ;
    }

}
