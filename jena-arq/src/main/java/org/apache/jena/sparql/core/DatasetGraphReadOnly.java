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

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.apache.jena.sparql.util.Context;

/** Read-only view of a DatasetGraph.  Assumes the dataset underneath isn't changing.
 */
public class DatasetGraphReadOnly extends DatasetGraphWrapper
{
    // Add a read-only wrapper any graphs returned.
    // Block write access at getW().

    public DatasetGraphReadOnly(DatasetGraph dsg) {
        super(dsg);
    }
    
    public DatasetGraphReadOnly(DatasetGraph dsg, Context cxt) {
        super(dsg, cxt);
    }

    private Graph dftGraph = new GraphReadOnly(super.getDefaultGraph());
    
    @Override
    public Graph getDefaultGraph() {
        return dftGraph;
    }

    @Override public void begin(ReadWrite mode) {
        if ( mode == ReadWrite.WRITE )
            //throw new JenaTransactionException("read-only dataset : no write transactions");
            Log.warn(this, "Write transaction on a read-only dataset");
        get().begin(mode); 
    }
    
    @Override
    public Graph getGraph(Node graphNode) {
        Graph g = get().getGraph(graphNode);
        if ( g == null )
            return null;
        g = new GraphReadOnly(g);
        return g;
    }

    /** For operations that write the DatasetGraph. */
    @Override
    protected DatasetGraph getW() {
        throw new UnsupportedOperationException("read-only dataset");
    }

    @Override
    public void close() {
        get().close();
    }
}
