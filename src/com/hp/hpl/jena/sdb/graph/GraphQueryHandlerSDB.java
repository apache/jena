/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.graph.query.Pipe;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class GraphQueryHandlerSDB extends QueryHandlerBase
{
    DatasetStoreGraph datasetStore ;
    Node graphNode ;

    public GraphQueryHandlerSDB(Graph graph, Node graphNode, DatasetStoreGraph datasetStore)
    { 
        super(graph) ;
        this.datasetStore = datasetStore ;
        this.graphNode = graphNode ;
    }

    @Override
    protected void initialize(Mapping map, Triple[] pattern)
    {
      for ( Triple t : pattern ) 
      {     
          Node s = t.getSubject() ;
          Node p = t.getPredicate() ;
          Node o = t.getObject() ;
          
          if ( s.isVariable() && ! map.hasBound(s) ) 
              map.newIndex(s) ;
          if ( p.isVariable() && ! map.hasBound(p) ) 
              map.newIndex(p) ;
          if ( o.isVariable() && ! map.hasBound(o) ) 
              map.newIndex(o) ;
      }
    }
    
    @Override
    protected void execute(Mapping map, Triple[] pattern, Pipe inputPipe, Pipe outputPipe)
    {
        while ( inputPipe.hasNext() )
        {
            Domain input = inputPipe.get() ;

            Op op = prepareTriples(map, pattern, graphNode, input) ;
            @SuppressWarnings("unchecked")
            Set<Var> vars = (Set<Var>)OpVars.allVars(op) ;
            Plan plan = QueryEngineSDB.getFactory().create(op, datasetStore, null, null) ;
            QueryIterator qIter = plan.iterator() ;

            for ( ; qIter.hasNext() ; )
            {
                Domain output = new Domain(input.size()) ;
                Binding binding = qIter.nextBinding() ;
                for ( Var v : vars )
                {     
                    Node value = binding.get(v) ;
                    int idx = map.lookUp(v) ;
                    output.setElement(idx, value) ;
                }
                outputPipe.put(output) ;
            }
        }
        outputPipe.close() ;
    }

    private static Op prepareTriples(Mapping map, Triple[] pattern, Node graphNode, Domain input)
    {
        Triple[] groundedPattern = new Triple[pattern.length] ;

        for ( int i = 0 ; i < pattern.length ; i++ )
        {
            Triple t = subst(map, pattern[i], input) ;
            groundedPattern[i] = t ;
        }

        // -- BGP - quad pattern
        BasicPattern bgp = new BasicPattern() ;
        for ( int i = 0 ; i < groundedPattern.length ; i ++ )
            bgp.add(groundedPattern[i]) ;
        Op op = new OpQuadPattern(graphNode, bgp) ;
        return op ;
    }

    private static Triple subst(Mapping map, Triple triple, Domain input)
    {
        Node s = subst(map, triple.getSubject(), input) ;
        Node p = subst(map, triple.getPredicate(), input) ;
        Node o = subst(map, triple.getObject(), input) ;
        return new Triple(s, p, o) ;
    }

    private static Node subst(Mapping map, Node node, Domain input)
    {
        if ( ! map.hasBound(node) ) 
            return node ;
        int idx = map.lookUp(node) ;
        if ( idx == -1 )
            return node ;
        Node n = input.getElement(idx) ;
        if ( n == null )
            return  node ;
        return n ;
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