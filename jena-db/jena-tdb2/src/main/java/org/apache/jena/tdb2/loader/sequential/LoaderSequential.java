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

package org.apache.jena.tdb2.loader.sequential;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.loader.BulkLoaderException;
import org.apache.jena.tdb2.loader.base.LoaderBase;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.TDBInternal;

/** Bulk loader. Algorithm: Parser to primary indexes, then builds secondary indexes one at a time. */ 
public class LoaderSequential extends LoaderBase {
    
    public static final int DataTickPoint   = 100_000;
    public static final int DataSuperTick   = 10;
    public static final int IndexTickPoint  = 1_000_000;
    public static final int IndexSuperTick  = 10;
    
    private final LoaderNodeTupleTable triplesLoader;
    private final LoaderNodeTupleTable quadsLoader;
    private final DatasetGraphTDB dsgtdb;
    
    private long countQuads;
    private long countTriples;
    private StreamRDF stream;
    
    public LoaderSequential(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        super(dsg, graphName, output);
        
        if ( ! TDBInternal.isBackedByTDB(dsg) )
            throw new BulkLoaderException("Not a TDB2 database");

        this.dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        this.triplesLoader = new LoaderNodeTupleTable(dsgtdb.getTripleTable().getNodeTupleTable(), output, "Triples");
        this.quadsLoader = new LoaderNodeTupleTable(dsgtdb.getQuadTable().getNodeTupleTable(), output, "Quads");
        StreamRDF s = StreamRDFLib.dataset(dsg);
        s = new StreamRDFWrapper(s) {
            @Override
            public void triple(Triple triple) {
                triplesLoader.load(triple.getSubject(), triple.getPredicate(), triple.getObject());
                countTriples++;
            }

            @Override
            public void quad(Quad quad) {
                quadsLoader.load(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
                countQuads++;
            }
        };    
        this.stream = LoaderOps.toNamedGraph(s, graphName);
    }
    
    @Override
    public void startBulk() {
        //Not in a transaction.
        //dsgtdb.getTxnSystem().getTxnMgr().startExclusiveMode();
        super.startBulk();
        triplesLoader.loadDataStart();
        quadsLoader.loadDataStart();
    }

    @Override
    public void finishBulk() {
        triplesLoader.loadDataFinish();
        quadsLoader.loadDataFinish();
        super.finishBulk();
        //dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }

    @Override
    public void finishException(Exception ex) {
        super.finishException(ex);
        //dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }

    @Override
    public StreamRDF stream() {
        return stream;
    }

    @Override
    protected void loadOne(String filename) {
        LoaderOps.inputFile(stream, filename, output, DataTickPoint, DataSuperTick);
    }

    @Override
    public boolean bulkUseTransaction() {
        return true;
    }
    
    @Override
    public long countTriples() {
        return countTriples;
    }

    @Override
    public long countQuads() {
        return countQuads;
    }
}
