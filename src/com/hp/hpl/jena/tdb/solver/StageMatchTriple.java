/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import iterator.Iter;
import iterator.NullIterator;
import iterator.RepeatApplyIterator;
import iterator.Transform;

import java.util.Iterator;

import lib.Tuple;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;

import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable;
import com.hp.hpl.jena.tdb.store.NodeId;

public class StageMatchTriple extends RepeatApplyIterator<BindingNodeId>
{
    private final NodeTupleTable nodeTupleTable ;
    private final Tuple<Node> tuple ;

    private final ExecutionContext execCxt ;

    public StageMatchTriple(NodeTupleTable nodeTupleTable, Iterator<BindingNodeId> input, Tuple<Node> tuple, ExecutionContext execCxt)
    {
        super(input) ;
        this.nodeTupleTable = nodeTupleTable ; 
        this.tuple = tuple ;
        this.execCxt = execCxt ;
    }

    @Override
    protected Iterator<BindingNodeId> makeNextStage(final BindingNodeId input)
    {
        NodeId ids[] = new NodeId[tuple.size()] ;
        // Variables for this tuple after subsitution
        final Var[] var = new Var[tuple.size()] ;

        // Process the Node to NodeId conversion ourselves because
        // we wish to abort if an unknown node is seen.
        for ( int i = 0 ; i < tuple.size() ; i++ )
        {
            Node n = tuple.get(i) ;
            // Substitution and turning into NodeIds
            // Varaibles unsubstituted are null NodeIds
            NodeId nId = idFor(nodeTupleTable.getNodeTable(), input, n) ;
            if ( nId == NodeId.NodeDoesNotExist )
                new NullIterator<BindingNodeId>() ;
            ids[i] = nId ;
            if ( nId == null )
                var[i] = asVar(n) ;
        }

        // Go directly to the tuple table 
        Iterator<Tuple<NodeId>> tuples = nodeTupleTable.getTupleTable().find(new Tuple<NodeId>(ids)) ;

        // Map to BindingNodeId
        Transform<Tuple<NodeId>, BindingNodeId> binder = new Transform<Tuple<NodeId>, BindingNodeId>()
        {
            @Override
            public BindingNodeId convert(Tuple<NodeId> tuple)
            {
                BindingNodeId output = new BindingNodeId(input) ;
                for ( int i = 0 ; i < var.length ; i++ )
                {
                    Var v = var[i] ;
                    if ( v == null )
                        continue ;
                    NodeId id = tuple.get(i) ;
                    if ( reject(output, v,id) )
                        return null ;
                    output.put(v, id) ;
                }
                return output ;
            }
        } ;
        return Iter.iter(tuples).map(binder).removeNulls() ;
    }
    
    private static boolean reject(BindingNodeId output , Var var, NodeId value)
    {
        if ( ! output.containsKey(var) )
            return false ;
        
        if ( output.get(var).equals(value) )
            return false ;

        return true ;
    }
    
    private static Var asVar(Node node)
    {
        if ( Var.isVar(node) )
            return Var.alloc(node) ;
        return null ;
    }

    /** Return null for variables, and for nodes, the node id or NodeDoesNotExist */
    private static NodeId idFor(NodeTable nodeTable, BindingNodeId input, Node node)
    {
        if ( Var.isVar(node) )
        {
            NodeId n = input.get((Var.alloc(node))) ;
            // Bound to NodeId or null. 
            return n ;
        } 
        // May return NodeId.NodeDoesNotExist which must not be null. 
        return nodeTable.nodeIdForNode(node) ;
    }
}
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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