/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.lib.Lib.printAbbrev;
import iterator.Iter;
import iterator.Transform;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.pgraph.NodeId;


/** Utilities used within the BGP solver */
public class SolverLib
{
    // ---- TDB specific
    // Transform : BindingNodeId ==> Binding
    public static Transform<BindingNodeId, Binding> convToBinding(final GraphTDB graph)
    {
        return new Transform<BindingNodeId, Binding>()
        {
            @Override
            public Binding convert(BindingNodeId bindingNodeIds)
            {
                if ( true )
                    return new BindingTDB(null, bindingNodeIds, graph.getNodeTable()) ;
                else
                {
                    // Makes nodes immediately.  Interacts with FILTERs, causing unecessary NodeTable accesses. 
                    Binding b = new BindingMap() ;
                    for ( Var v : bindingNodeIds )
                    {
                        NodeId id = bindingNodeIds.get(v) ;
                        Node n = graph.getNodeTable().retrieveNodeByNodeId(id) ;
                        b.add(v, n) ;
                    }
                    return b ;
                }
            }
        } ;
    }

    // Transform : Binding ==> BindingNodeId
    public static Transform<Binding, BindingNodeId> convFromBinding(final GraphTDB graph)
    {
        return new Transform<Binding, BindingNodeId>()
        {
            @Override
            public BindingNodeId convert(Binding binding)
            {
                BindingNodeId b = new BindingNodeId() ;
                @SuppressWarnings("unchecked")
                Iterator<Var> vars = (Iterator<Var>)binding.vars() ;
    
                for ( ; vars.hasNext() ; )
                {
                    Var v = vars.next() ;
                    Node n = binding.get(v) ;  
                    // Rely on the node table cache. 
                    NodeId id = graph.getNodeTable().nodeIdForNode(n) ;
                    b.put(v, id) ;
                }
                return b ;
            }
        } ;
    }

    static String strPattern(BasicPattern pattern)
    {
        @SuppressWarnings("unchecked")
        List<Triple> triples = (List<Triple>)pattern.getList() ;
        String x = Iter.asString(triples, "\n  ") ;
        return printAbbrev(x) ; 
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