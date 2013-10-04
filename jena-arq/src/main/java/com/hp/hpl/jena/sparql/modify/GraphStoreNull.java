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

package com.hp.hpl.jena.sparql.modify;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.impl.GraphBase ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraphQuad ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NullIterator ;

/**
 * A black hole for Quads, add as many as you want and it will forget them all.  Useful for testing.
 */
public class GraphStoreNull extends DatasetGraphQuad implements GraphStore
{
    // The baby brother
    private static final Graph GRAPH_NULL = new GraphBase()
    {
        @Override
        protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
        {
            return NullIterator.instance();
        }
    };

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
    {
        return Iter.nullIterator();
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o)
    {
        return Iter.nullIterator();
    }

    @Override
    public void add(Quad quad)
    { }

    @Override
    public void delete(Quad quad)
    { }

    @Override
    public void addGraph(Node graphName, Graph graph) {}

    @Override
    public Graph getDefaultGraph()
    {
        return GRAPH_NULL ;
    }

    @Override
    public Graph getGraph(Node graphNode)
    {
        return GRAPH_NULL ;
    }

    @Override
    public Dataset toDataset()
    {
        return DatasetFactory.create(this);
    }

    @Override
    public void startRequest()
    { }

    @Override
    public void finishRequest()
    { }
}
