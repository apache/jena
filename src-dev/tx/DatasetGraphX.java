/**
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

package tx;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;

public class DatasetGraphX implements TransactionLifecycle
{
    // DatasetGraphWrapper with mutable DatasetGraph
    private DatasetGraphTxn dsg = null ;
    private ReadWrite ReadWrite = null ;
    private final Location location ;
    private final StoreConnection sConn ;
    
    
    @Override
    public void begin(ReadWrite readWrite)
    {
        checkNotInTransaction() ;
        dsg = sConn.begin(readWrite) ; 
    }
    
    @Override
    public void commit()
    {
        checkInTransaction() ;
        dsg.commit() ; 
    }

    @Override
    public void abort()
    {
        checkInTransaction() ;
        dsg.abort() ;
    }
    
    private void checkInTransaction()
    {}
    
    private void checkNotInTransaction()
    {}
    
    // For each operation, checkInTransaction()

    public DatasetGraphX(Location location)
    {
        this.location = location ;
        sConn = StoreConnection.make(location) ;
    }

    //@Override
    public boolean containsGraph(Node graphNode)
    { return dsg.containsGraph(graphNode) ; }

    //@Override
    public Graph getDefaultGraph()
    { return dsg.getDefaultGraph(); }

    //@Override
    public Graph getGraph(Node graphNode)
    { return dsg.getGraph(graphNode) ; }

    public void addGraph(Node graphName, Graph graph)
    { dsg.addGraph(graphName, graph) ; }

    public void removeGraph(Node graphName)
    { dsg.removeGraph(graphName) ; }

    public void setDefaultGraph(Graph g)
    { dsg.setDefaultGraph(g) ; }

    //@Override
    public Lock getLock()
    { return dsg.getLock() ; }

    //@Override
    public Iterator<Node> listGraphNodes()
    { return dsg.listGraphNodes() ; }

    //@Override
    public void add(Quad quad)
    { dsg.add(quad) ; }

    //@Override
    public void delete(Quad quad)
    { dsg.delete(quad) ; }

    //@Override
    public void add(Node g, Node s, Node p, Node o)
    { dsg.add(g, s, p, o) ; }

    //@Override
    public void delete(Node g, Node s, Node p, Node o)
    { dsg.delete(g, s, p, o) ; }
    
    public void deleteAny(Node g, Node s, Node p, Node o)
    { dsg.deleteAny(g, s, p, o) ; }

    //@Override
    public boolean isEmpty()
    { return dsg.isEmpty() ; }
    
    //@Override
    public Iterator<Quad> find()
    { return dsg.find() ; }

    //@Override
    public Iterator<Quad> find(Quad quad)
    { return dsg.find(quad) ; }

    //@Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
    { return dsg.find(g, s, p, o) ; }

    //@Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o)
    { return dsg.findNG(g, s, p, o) ; }

    //@Override
    public boolean contains(Quad quad)
    { return dsg.contains(quad) ; }

    //@Override
    public boolean contains(Node g, Node s, Node p, Node o)
    { return dsg.contains(g, s, p, o) ; }

    //@Override
    public Context getContext()
    { return dsg.getContext() ; }

    //@Override
    public long size()
    { return dsg.size() ; }

    //@Override
    public void close()
    { dsg.close() ; }
}

