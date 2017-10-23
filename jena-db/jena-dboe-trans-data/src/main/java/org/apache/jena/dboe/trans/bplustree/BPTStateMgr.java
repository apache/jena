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

package org.apache.jena.dboe.trans.bplustree;

import org.apache.jena.atlas.logging.FmtLog ;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.transaction.txn.StateMgrData;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Manage the persistent state of the tree
 * 
 * This consists of last committed root, and the limits on the blocks for both
 * nodes and records. 
 * 
 * (rootId/int-as-long, nodeAllocLimit/long, recordsAllocLimit/long) 
 */
public class BPTStateMgr extends StateMgrData {
    private static Logger log = LoggerFactory.getLogger(BPTStateMgr.class) ;
    
    private static int idxRoot                  = 0 ;
    private static int idxNodeBlocksLimit       = 1 ;
    private static int idxRecordsBlocksLimit    = 2 ;
    
    private int currentRoot()                   { return (int)super.get(idxRoot) ; }
    private long nodeBlocksLimit()              { return super.get(idxNodeBlocksLimit) ; }
    private long recordsBlocksLimit()           { return super.get(idxRecordsBlocksLimit) ; }
    
    private void currentRoot(int x)             { super.set(idxRoot, x) ; }
    private void nodeBlocksLimit(long x)        { super.set(idxNodeBlocksLimit, x) ; }
    private void recordsBlocksLimit(long x)     { super.set(idxRecordsBlocksLimit, x) ; }

    private boolean LOGGING = BPT.Logging ;

    public BPTStateMgr(BufferChannel storage) {
        // These values are the values for a null tree (no blocks).
        super(storage, 0L, 0L, 0L) ;
    }

    /*package*/ void setState(int rootIdx, long nodeBlkLimit, long recordsBlkLimit) {
        currentRoot(rootIdx) ;
        nodeBlocksLimit(nodeBlkLimit) ;
        recordsBlocksLimit(recordsBlkLimit) ;
        log("Set") ;
        setDirtyFlag() ;
        // But don't write it.
    }
    @Override
    protected void writeStateEvent() {
        log("Write") ;
    }

    @Override
    protected void readStateEvent() {
        log("Read") ;
    }

    private void log(String operation) {
        if ( LOGGING )
            FmtLog.info(log, "%s state:  root=%d // node block limit = %d // records block limit %d", operation, currentRoot(), nodeBlocksLimit(), recordsBlocksLimit()) ;
    }
    
    public int getRoot() {
        return currentRoot() ;
    }

    public long getNodeBlocksLimit() {
        return nodeBlocksLimit() ;
    }

    public long getRecordsBlocksLimit() {
        return recordsBlocksLimit() ;
    }
}
