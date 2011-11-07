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

package com.hp.hpl.jena.sdb.graph;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.query.BindingQueryPlan ;
import com.hp.hpl.jena.graph.query.Domain ;
import com.hp.hpl.jena.graph.query.Query ;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler ;
import com.hp.hpl.jena.graph.query.TreeQueryPlan ;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB ;
import com.hp.hpl.jena.sdb.shared.SDBNotImplemented ;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

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

        for ( Triple t : q.getPattern() )
            bgp.add(t) ;
        
        op = new OpQuadPattern(graphNode, bgp) ;
        return new BindingQueryPlanSDB() ;
    }
    
    class BindingQueryPlanSDB implements BindingQueryPlan
    {
        // Iterator of domain objects
        
        @Override
        public ExtendedIterator<Domain> executeBindings()
        {
            Plan plan = QueryEngineSDB.getFactory().create(op, datasetStore, null, null) ;
            QueryIterator qIter = plan.iterator() ;

            Transform<Binding, Domain> b2d = new Transform<Binding, Domain>()
            {
                @Override
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
            Iterator<Domain> it = Iter.map(qIter, b2d) ;
            return WrappedIterator.create(it) ;
        }
    }
}
