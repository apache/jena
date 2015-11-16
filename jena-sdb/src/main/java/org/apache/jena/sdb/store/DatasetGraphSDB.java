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

package org.apache.jena.sdb.store;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.graph.GraphSDB ;
import org.apache.jena.sdb.util.StoreUtils ;
import org.apache.jena.shared.Lock ;
import org.apache.jena.shared.LockMRSW ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphTriplesQuads ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Context ;

public class DatasetGraphSDB extends DatasetGraphTriplesQuads
    implements DatasetGraph, Closeable 
{
    private final Store store ;
    private Lock lock = new LockMRSW() ;
    private final Context context ;
    private GraphSDB defaultGraph;
    
    public DatasetGraphSDB(Store store, Context context)
    {
        this(store, new GraphSDB(store), context) ;
    }
    
    public DatasetGraphSDB(Store store, GraphSDB graph, Context context)
    {
        this.store = store ;
        // Force the "default" graph
        this.defaultGraph = graph ;
        this.context = context ;
    }
    
    public Store getStore() { return store ; }
    
    @Override
    public Iterator<Node> listGraphNodes()
    {
        return StoreUtils.storeGraphNames(store) ;
    }

    @Override
    public boolean containsGraph(Node graphNode)
    {
        return StoreUtils.containsGraph(store, graphNode) ;
    }

    @Override
    public Graph getDefaultGraph()
    {
        return defaultGraph ;
    }

    @Override
    public Graph getGraph(Node graphNode)
    {
        return new GraphSDB(store, graphNode) ;
    }
    
    // Use unsubtle helper versions (the bulk loader copes with large additions).
    @Override
    protected void addToDftGraph(Node s, Node p, Node o)
    { Helper.addToDftGraph(this, s, p, o) ; }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o)
    { Helper.addToNamedGraph(this, g, s, p, o) ; }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o)
    { Helper.deleteFromDftGraph(this, s, p, o) ; }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o)
    { Helper.deleteFromNamedGraph(this, g, s, p, o) ; }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o)
    { return Helper.findInDftGraph(this, s, p, o) ; }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    { return Helper.findInAnyNamedGraphs(this, s, p, o) ; } 

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o)
    { return Helper.findInSpecificNamedGraph(this, g, s, p, o) ; }

    @Override
    public void close()
    { store.close() ; }

    // Helper implementations of operations.
    // Not necessarily efficient.
    
    private static class Helper {
        public static void addToDftGraph(DatasetGraph dsg, Node s, Node p, Node o) {
            dsg.getDefaultGraph().add(new Triple(s, p, o)) ;
        }

        public static void addToNamedGraph(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
            dsg.getGraph(g).add(new Triple(s, p, o)) ;
        }

        public static void deleteFromDftGraph(DatasetGraph dsg, Node s, Node p, Node o) {
            dsg.getDefaultGraph().delete(new Triple(s, p, o)) ;
        }

        public static void deleteFromNamedGraph(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
            dsg.getGraph(g).delete(new Triple(s, p, o)) ;
        }

        public static Iterator<Quad> findInAnyNamedGraphs(DatasetGraph dsg, Node s, Node p, Node o) {
            Iterator<Node> iter = dsg.listGraphNodes() ;
            Iterator<Quad> quads = null ;
            for ( ; iter.hasNext() ; ) {
                Node gn = iter.next() ;
                quads = Iter.append(quads, findInSpecificNamedGraph(dsg, gn, s, p, o)) ;
            }
            return quads ;
        }

        public static Iterator<Quad> findInDftGraph(DatasetGraph dsg, Node s, Node p, Node o) {
            return triples2quadsDftGraph(dsg.getDefaultGraph().find(s, p, o)) ;
        }

        public static Iterator<Quad> findInSpecificNamedGraph(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
            return triples2quadsDftGraph(dsg.getGraph(g).find(s, p, o)) ;
        }
    }
}
