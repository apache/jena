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

package org.apache.jena.sparql.core;

import java.util.Iterator;

import org.apache.jena.atlas.lib.Sync;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;

public class DatasetGraphWrapper implements DatasetGraph, Sync 
{
    // The wrapped DatasetGraph but all calls go via get() so this can be null.
    private final DatasetGraph dsg;
    
    /** Return the DatasetGraph being wrapped. */
    public final DatasetGraph getWrapped() { 
        return get();
    }
    
    /** Recursively unwrap a {@link DatasetGraphWrapper}.
     * 
     * @return the first found {@link DatasetGraph} that is not an instance of {@link DatasetGraphWrapper}
     */
    public final DatasetGraph getBase() { 
        DatasetGraph dsgw = dsg;
        while (dsgw instanceof DatasetGraphWrapper) {
            dsgw = ((DatasetGraphWrapper)dsg).getWrapped();
        }
        return dsgw;
    }
    
    /** Recursively unwrap a {@link DatasetGraphWrapper}, stopping at a {@link DatasetGraphWrapper}
     * that indicate it is "view changing", ie shows quads to the base dataset graph.  
     * 
     * @return the first found {@link DatasetGraph} that is not an instance of {@link DatasetGraphWrapper}
     */
    public final DatasetGraph getBaseForQuery() {
        DatasetGraph dsgw = dsg;
        while (dsgw instanceof DatasetGraphWrapper) {
            if ( dsgw instanceof DatasetGraphWrapperView )
                break;
            dsgw = ((DatasetGraphWrapper)dsg).getWrapped();
        }
        return dsgw;
    }

    /** The dataset to use for redirection - can be overridden.
     *  It is also guarantee that this is called only once per
     *  delegated call.  Changes to the wrapped object can be
     *  made based on that contract. 
     */
    protected DatasetGraph get() { return dsg; }

    /** For operations that only read the DatasetGraph. */ 
    protected DatasetGraph getR() { return get(); }
    
    /** For operations that write the DatasetGraph. */ 
    protected DatasetGraph getW() { return get(); }
    
    /** For operations that get a handle on a graph. */
    protected DatasetGraph getG() { return get(); }
    
    /** For operations that pass on transaction actions. */
    protected DatasetGraph getT() { return get(); }

    public DatasetGraphWrapper(DatasetGraph dsg) {
        this.dsg = dsg;
    }

    @Override
    public boolean containsGraph(Node graphNode)
    { return getR().containsGraph(graphNode); }

    @Override
    public Graph getDefaultGraph()
    { return getG().getDefaultGraph(); }

    @Override
    public Graph getUnionGraph()
    { return getG().getUnionGraph(); }

    @Override
    public Graph getGraph(Node graphNode)
    { return getG().getGraph(graphNode); }

    @Override
    public void addGraph(Node graphName, Graph graph)
    { getW().addGraph(graphName, graph); }

    @Override
    public void removeGraph(Node graphName)
    { getW().removeGraph(graphName); }

    @Override
    public void setDefaultGraph(Graph g)
    { getW().setDefaultGraph(g); }

    @Override
    public Lock getLock()
    { return getR().getLock(); }

    @Override
    public Iterator<Node> listGraphNodes()
    { return getR().listGraphNodes(); }

    @Override
    public void add(Quad quad)
    { getW().add(quad); }

    @Override
    public void delete(Quad quad)
    { getW().delete(quad); }

    @Override
    public void add(Node g, Node s, Node p, Node o)
    { getW().add(g, s, p, o); }

    @Override
    public void delete(Node g, Node s, Node p, Node o)
    { getW().delete(g, s, p, o); }
    
    @Override
    public void deleteAny(Node g, Node s, Node p, Node o)
    { getW().deleteAny(g, s, p, o); }

    @Override
    public void clear()
    { getW().clear(); }
    
    @Override
    public boolean isEmpty()
    { return getR().isEmpty(); }
    
    @Override
    public Iterator<Quad> find()
    { return getR().find(); }

    @Override
    public Iterator<Quad> find(Quad quad)
    { return getR().find(quad); }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
    { return getR().find(g, s, p, o); }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o)
    { return getR().findNG(g, s, p, o); }

    @Override
    public boolean contains(Quad quad)
    { return getR().contains(quad); }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o)
    { return getR().contains(g, s, p, o); }

    @Override
    public Context getContext()
    { return getR().getContext(); }

    @Override
    public long size()
    { return getR().size(); }

    @Override
    public void close()
    { getW().close(); }
    
    @Override
    public String toString() {
        DatasetGraph dsg = getR();
        return Txn.calculateRead(dsg, ()->dsg.toString() );
    }

    @Override
    public void sync() {
        // Pass down sync.
        SystemARQ.sync(getW()); 
    }

    @Override
    public void begin() { getT().begin(); }
    
    @Override
    public ReadWrite transactionMode() 
    { return getT().transactionMode(); }

    @Override
    public  TxnType transactionType() 
    { return getT().transactionType(); }
    
    @Override
    public void begin(TxnType type)
    { getT().begin(type); }

    @Override
    public void begin(ReadWrite readWrite) 
    { getT().begin(readWrite); }

    @Override
    public boolean promote()
    { return getT().promote(); }
    
    @Override
    public boolean promote(Promote type)
    { return getT().promote(type); }
    
    @Override
    public void commit() 
    { getT().commit(); }

    @Override
    public void abort() 
    { getT().abort(); }

    @Override
    public void end()
    { getT().end(); }

    @Override
    public boolean isInTransaction() 
    { return get().isInTransaction(); }    

    @Override
    public boolean supportsTransactions() 
    { return getT().supportsTransactions(); }

    @Override
    public boolean supportsTransactionAbort()
    { return getT().supportsTransactionAbort(); }
    
}
