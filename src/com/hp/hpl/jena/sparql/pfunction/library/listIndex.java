/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.util.IterLib;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.sparql.util.graph.GNode;
import com.hp.hpl.jena.sparql.util.graph.GraphList;

/** List membership with index : property function to access list using index  
 *  Usage: ?list :listIndex (?index ?member)
 * @author Andy Seaborne
 */

public class listIndex extends ListBaseList
{
    @Override
    protected QueryIterator execObjectList(Binding binding, Var listVar, Node predicate, List<Node> objectArgs,
                                            ExecutionContext execCxt)
    {
        // subject a variable.
        
        if ( objectArgs.size() != 2 )
            throw new ExprEvalException("ListIndex : object not a list of length 2") ; 
        Node indexNode = objectArgs.get(0) ;
        Node memberNode = objectArgs.get(1) ;
        
        final Collection<Node> x ;
        if ( ! Var.isVar(memberNode) )
            // If memberNode is defined, find lists containing it.
            x = GraphList.listFromMember(new GNode(execCxt.getActiveGraph(), memberNode)) ;
        else    
            // Hard. Subject unbound, no fixed member. Find every list and use BFI.
            x = GraphList.findAllLists(execCxt.getActiveGraph()) ;
        return super.allLists(binding, x, listVar, predicate, new PropFuncArg(objectArgs, null), execCxt) ;
    }

    @Override
    protected QueryIterator execOneList(Binding binding, 
                                        Node listNode, Node predicate, List<Node> objectArgs,
                                        ExecutionContext execCxt)
    {
        if ( Var.isVar(listNode) )
            throw new ExprEvalException("ListIndex : subject not a list or variable bound to a list") ;

        if ( objectArgs.size() != 2 )
            throw new ExprEvalException("ListIndex : object not a list of length 2") ;

        Node indexNode = objectArgs.get(0) ;
        Node memberNode = objectArgs.get(1) ;
        
        Graph graph = execCxt.getActiveGraph() ;
        
        if ( Var.isVar(indexNode) && ! Var.isVar(memberNode) )
            return findIndex(graph, binding, listNode, Var.alloc(indexNode), memberNode, execCxt) ;
            
        if ( ! Var.isVar(indexNode) && Var.isVar(memberNode) )
            return getByIndex(graph, binding, listNode, indexNode, Var.alloc(memberNode), execCxt) ;
        
        if ( ! Var.isVar(indexNode) && ! Var.isVar(memberNode) )
            return testSlotValue(graph, binding, listNode, indexNode, memberNode, execCxt) ;
        
        return findIndexMember(graph, binding, listNode, Var.alloc(indexNode), Var.alloc(memberNode), execCxt) ;
        
    }

    private static QueryIterator getByIndex(Graph graph, Binding binding, 
                                            Node listNode, Node indexNode, Var varMember,
                                            ExecutionContext execCxt)
    {
        int i = NodeFactory.nodeToInt(indexNode) ;
        if ( i < 0 )
            return IterLib.noResults(execCxt) ;

        Node n = GraphList.get(new GNode(graph, listNode), i) ;
        if ( n == null )
            return IterLib.noResults(execCxt) ;
        return IterLib.oneResult(binding, varMember, n, execCxt) ; 
    }

    private static QueryIterator testSlotValue(Graph graph, Binding binding,
                                               Node listNode, Node indexNode, Node memberNode,
                                               ExecutionContext execCxt)
    {
        int i = NodeFactory.nodeToInt(indexNode) ;
        if ( i < 0 )
            return IterLib.noResults(execCxt) ;
        Node n = GraphList.get(new GNode(graph, listNode), i) ;
        if ( n == null )
            return IterLib.noResults(execCxt) ;
        if ( n.equals(memberNode) )
            return IterLib.result(binding, execCxt) ; 
        else
            return IterLib.noResults(execCxt) ;
    }

    private static QueryIterator findIndex(Graph graph, Binding binding,
                                           Node listNode, Var var, Node member,
                                           ExecutionContext execCxt)
    {
        // Find index of member.
        int i = GraphList.index(new GNode(graph, listNode), member) ;
        if ( i < 0 )
            return IterLib.noResults(execCxt) ;
        Node idx = NodeFactory.intToNode(i) ;
        return IterLib.oneResult(binding, var, idx, execCxt) ; 
    }

    private static QueryIterator findIndexMember(Graph graph, Binding binding, 
                                                 Node listNode, Var varIndex, Var varMember,
                                                 ExecutionContext execCxt)
    {
        // Iterate over list
        List<Node> members = GraphList.members(new GNode(graph, listNode)) ;
        List<Binding> bindings = new ArrayList<Binding>() ;
        for ( int i = 0 ; i < members.size() ; i++ )
        {
            Node idx = NodeFactory.intToNode(i) ;
            Node member = members.get(i) ;
            Binding b = new BindingMap(binding) ;
            b.add(varIndex, idx) ;
            b.add(varMember, member) ;
            bindings.add(b) ;
        }
        return new QueryIterPlainWrapper(bindings.iterator(), execCxt) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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