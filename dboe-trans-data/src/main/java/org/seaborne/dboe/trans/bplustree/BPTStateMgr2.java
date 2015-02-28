/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.trans.bplustree;

import static org.seaborne.dboe.sys.SystemBase.SizeOfInt ;
import static org.seaborne.dboe.sys.SystemBase.SizeOfLong ;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.logging.FmtLog ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.transaction.txn.StateMgrBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Manage the persistent state of the tree
 * 
 * This consists of last commited root, and the limits on the blocks for both
 * nodes and records. 
 * 
 * (rootId/int, nodeAllocLimit/long, recordsAllocLimit/long) 
 */
public class BPTStateMgr2 extends StateMgrBase {
    private static Logger log = LoggerFactory.getLogger(BPTStateMgr.class) ;
    private static final int SizePersistentState = SizeOfInt + SizeOfLong + SizeOfLong ;   

    // These values are the values for a null tree (no blocks).
    private int currentRoot           = 0 ;
    private long nodeBlocksLimit      = 0 ; 
    private long recordsBlocksLimit   = 0 ;

    private boolean LOGGING = BPT.Logging ;

    public BPTStateMgr2(BufferChannel storage) {
        super(storage, SizePersistentState) ;
        init() ;
    }

    void setState(int rootIdx, long nodeBlkLimit, long recordsBlkLimit) {
        currentRoot = rootIdx ;
        nodeBlocksLimit = nodeBlkLimit ;
        recordsBlocksLimit = recordsBlkLimit ;
        log("Set") ;
        setDirtyFlag() ;
        // But don't write it.
    }

    @Override
    protected void deserialize(ByteBuffer bytes) {
        int root = bytes.getInt() ;
        long nodeBlkLimit = bytes.getLong() ;
        long recordsBlkLimit = bytes.getLong() ;
        setState(root, nodeBlkLimit, recordsBlkLimit) ;
    }

    @Override
    protected ByteBuffer serialize(ByteBuffer bytes) {
        bytes.putInt(currentRoot) ;
        bytes.putLong(nodeBlocksLimit) ;
        bytes.putLong(recordsBlocksLimit) ;
        bytes.rewind() ;
        return bytes ;
    }

    @Override
    protected void writeStateEvent() {
        log("Write") ;
    }

    @Override
    protected void readStateEvent() {
        log("Rrite") ;
    }

    private void log(String operation) {
        if ( LOGGING )
            FmtLog.info(log, "%s state:  root=%d // node block limit = %d // records block limit %d", operation, currentRoot, nodeBlocksLimit, recordsBlocksLimit) ;
    }
    
    public int getRoot() {
        return currentRoot ;
    }

    public long getNodeBlocksLimit() {
        return nodeBlocksLimit ;
    }

    public long getRecordsBlocksLimit() {
        return recordsBlocksLimit ;
    }
}
