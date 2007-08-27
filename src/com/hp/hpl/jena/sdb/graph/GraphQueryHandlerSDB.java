/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.iterator.Stream;
import com.hp.hpl.jena.sdb.iterator.Transform;
import com.hp.hpl.jena.sdb.shared.SDBNotImplemented;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;

public class GraphQueryHandlerSDB extends SimpleQueryHandler
{
    DatasetStoreGraph datasetStore ;
    Node graphNode ;
    BasicPattern bgp = new BasicPattern() ;
    private Op op ;
    private Node[] variables ;
    private Map<Node, Integer> indexes ;
    private Triple[] pattern ; 
    
    public GraphQueryHandlerSDB(Graph graph, Node graphNode, DatasetStoreGraph datasetStore)
    { 
        super(graph) ;
        this.datasetStore = datasetStore ;
        this.graphNode = graphNode ;
    }

    @Override
    final public TreeQueryPlan prepareTree( Graph pattern )
    {
        throw new SDBNotImplemented("prepareTree - Chris says this will not be called") ;
    }
    
    @Override
    public BindingQueryPlan prepareBindings( Query q, Node [] variables )   
    {
        this.variables = variables ;
        this.indexes = new HashMap<Node, Integer>() ;
        int idx = 0 ;
        for ( Node v : variables )
            indexes.put(v, (idx++) ) ;

        @SuppressWarnings("unchecked")
        List<Triple> pattern = q.getPattern() ;
        for ( Triple t : pattern )
            bgp.add(t) ;
        
        op = new OpQuadPattern(graphNode, bgp) ;
        return new BindingQueryPlanSDB() ;
    }
    
    class BindingQueryPlanSDB implements BindingQueryPlan
    {
        // Iterator of domain objects
        @SuppressWarnings("unchecked")
        public ExtendedIterator executeBindings()
        {
            Set<Var> vars = (Set<Var>)OpVars.allVars(op) ;
            Plan plan = QueryEngineSDB.getFactory().create(op, datasetStore, null, null) ;
            QueryIterator qIter = plan.iterator() ;

            Transform<Binding, Domain> b2d = new Transform<Binding, Domain>()
            {
                public Domain convert(Binding binding)
                {
                    Domain d = new Domain(variables.length) ;
                    for ( Node n : variables )
                    {     
                        Var v = Var.alloc(n) ;
                        Node value = binding.get(v) ;
                        // Miss?
                        int idx = indexes.get(v) ;
                        d.setElement(idx, value) ;
                    }
                    return d ;
                }
            };
            return WrappedIterator.create(Stream.map(qIter, b2d)) ;
        }
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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