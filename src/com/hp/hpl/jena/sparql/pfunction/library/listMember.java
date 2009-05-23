/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction.library;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterExtendByVar;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterYieldN;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.util.graph.GNode;
import com.hp.hpl.jena.sparql.util.graph.GraphList;


/** List membership : property function implementation of list:member. 
 * 
 * @author Andy Seaborne
 */ 

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

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */