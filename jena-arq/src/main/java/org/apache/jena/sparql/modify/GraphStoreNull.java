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

package org.apache.jena.sparql.modify;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Factory ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.DatasetGraphQuad ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.update.GraphStore ;

/**
 * A black hole for Quads, add as many as you want and it will forget them all.  Useful for testing.
 */
@SuppressWarnings("deprecation")
public class GraphStoreNull extends DatasetGraphQuad implements GraphStore
{
    public GraphStoreNull() {}
    
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
        return Factory.empty() ;
    }

    @Override
    public Graph getGraph(Node graphNode)
    {
        return Factory.empty() ;
    }
}
