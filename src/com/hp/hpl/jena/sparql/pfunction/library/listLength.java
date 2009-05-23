/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction.library;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.util.IterLib;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.sparql.util.graph.GNode;
import com.hp.hpl.jena.sparql.util.graph.GraphList;

/** List length : property function to get the length of a list.
 * 
 * @author Andy Seaborne
 */ 

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
        Node n = NodeFactory.intToNode(x) ;
        return IterLib.oneResult(binding, varLength, n, execCxt) ;
    }
    
    private QueryIterator verify(Binding binding, Graph graph, Node listNode, Node length, ExecutionContext execCxt)
    {
        int x = GraphList.length(new GNode(graph, listNode)) ;
        int len = NodeFactory.nodeToInt(length) ;
        
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