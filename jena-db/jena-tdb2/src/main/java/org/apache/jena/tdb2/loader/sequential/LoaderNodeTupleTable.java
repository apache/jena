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

import org.apache.jena.atlas.lib.ArrayUtils;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.lib.Sync;
import org.apache.jena.graph.Node;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

/** 
 * Load into one NodeTupleTable (triples, quads, other).
 * 
 */

public class LoaderNodeTupleTable implements Closeable, Sync
{
    private int          numIndexes; 
    private TupleIndex   primaryIndex;
    private TupleIndex[] secondaryIndexes;
    
    private NodeTupleTable nodeTupleTable;
    
    private boolean dropAndRebuildIndexes;
    //private Timer timer;
    private long count = 0;
    private long countTriples = 0;
    private long countQuads = 0;
    private String itemsName;
    private final MonitorOutput output;
    
    private static Object lock = new Object();
        
    public LoaderNodeTupleTable(NodeTupleTable nodeTupleTable, MonitorOutput output, String itemsName)
    {
        this.nodeTupleTable = nodeTupleTable;
        this.itemsName = itemsName;
        this.output = output;
    }

    // -- LoaderFramework
    
    protected void loadPrepare() {
        dropAndRebuildIndexes = nodeTupleTable.isEmpty();

        if ( dropAndRebuildIndexes ) {
            //output.print("** Load empty %s table", itemsName);
            // SPO, GSPO only.
            dropSecondaryIndexes();
        } else {
            output.print("** Load into %s table with existing data", itemsName);
        }
    }
        
    protected void loadSecondaryIndexes() {
        if ( count > 0 ) {
            if ( dropAndRebuildIndexes )
                // Now do secondary indexes.
                createSecondaryIndexes();
        }
        attachSecondaryIndexes();
    }

    public void loadStart() {
    }

    public void loadFinish() {
    }
    
    /** Notify start of loading process */
    public void loadDataStart() {
        loadPrepare();
    }
    
    /** Notify End of data to load - this operation may 
     * undertake a significant amount of work.
     */
    public void loadDataFinish()
    {
        if ( count > 0 ) {
            // Do index phase only if any items seen.
           // monitor.startIndexPhase();
        }
        // Always do this - it reattaches the secondary indexes.
        loadSecondaryIndexes();
    }
    
    /** Stream in items to load ... */
    public void load(Node...nodes) {
        if ( nodes.length == 3 )
            countTriples++;
        if ( nodes.length == 4 )
            countQuads++;
        count++;
        nodeTupleTable.addRow(nodes);
    }

//    public void loadIndexStart()
//    {
//        if ( count > 0 )
//            // Do index phase only if any items seen.
//            monitor.startIndexPhase();
//        // Always do this - it reattaches the secondary indexes.
//        loadSecondaryIndexes();
//    }
//
//    public void loadIndexFinish()
//    {
//        if ( count > 0 )
//            monitor.finishIndexPhase();
//    }
    
    public void sync(boolean force) {}
    @Override
    public void sync() {}
    
    // --------
    
    @Override
    public void close()
    { sync(); }

    private void dropSecondaryIndexes() {
        numIndexes = nodeTupleTable.getTupleTable().numIndexes();
        primaryIndex = nodeTupleTable.getTupleTable().getIndex(0);

        secondaryIndexes = ArrayUtils.alloc(TupleIndex.class, numIndexes-1);
        System.arraycopy(nodeTupleTable.getTupleTable().getIndexes(), 1, 
                         secondaryIndexes, 0,
                         numIndexes-1);
        // Set non-primary indexes to null.
        for ( int i = 1; i < numIndexes; i++ )
            nodeTupleTable.getTupleTable().setTupleIndex(i, null);
    }
    
    private void createSecondaryIndexes() {        
        BuilderSecondaryIndexes builder = new BuilderSecondaryIndexesSequential();
        builder.createSecondaryIndexes(output, primaryIndex, secondaryIndexes);
    }
    
    private void attachSecondaryIndexes() {
        for ( int i = 1; i < numIndexes; i++ )
            nodeTupleTable.getTupleTable().setTupleIndex(i, secondaryIndexes[i-1]);
    }
    
    static private void sync(TupleIndex[] indexes) {
        for ( TupleIndex idx : indexes ) {
            if ( idx != null )
                idx.sync();
        }
    }

    private static boolean tickPoint(long counter, long quantum) {
        return counter % quantum == 0;
    }
}
