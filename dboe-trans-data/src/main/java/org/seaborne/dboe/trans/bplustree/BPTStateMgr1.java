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

import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Manage the persistent state of the tree
 * 
 * This consists of last commited root, and the limits on the blocks for both
 * nodes and records. 
 * 
 * (rootId/int, nodeAllocLimit/long, recordsAllocLimit/long) 
 */
public class BPTStateMgr1 implements Sync {
    private static Logger log = LoggerFactory.getLogger(BPTStateMgr1.class) ;
    private static final int SizePersistentState = SizeOfInt + SizeOfLong + SizeOfLong ;   
    
    // These values are the values for a null tree.
    private int currentLatestRoot     = 0 ;
    private long nodeBlocksLimit      = 0 ; 
    private long recordsBlocksLimit   = 0 ;
    
    private final BufferChannel storage ;
    private final ByteBuffer bb = allocBuffer() ;
    private boolean dirty = false ;
    private boolean LOGGING = BPT.Logging ;
    
    public BPTStateMgr1(BufferChannel storage) {
        this.storage = storage ;
        // TODO Separate out constructor and initialization.
        if ( ! storage.isEmpty() )
            readState() ;
        else
            writeState() ;
    }
    
    public BufferChannel getChannel() { return storage ; }
    
    private static ByteBuffer allocBuffer() {
        return ByteBuffer.allocate(SizePersistentState) ;
    }
    
    void setState(int rootIdx, long nodeBlkLimit, long recordsBlkLimit) {
        currentLatestRoot = rootIdx ;
        nodeBlocksLimit = nodeBlkLimit ;
        recordsBlocksLimit = recordsBlkLimit ;
        if ( LOGGING )
            FmtLog.info(log, "setState = %d %d %d", rootIdx, nodeBlkLimit, recordsBlkLimit) ;
        dirty = true ;
        // But don't write it.
    }
   
    void setState(ByteBuffer bytes) {
        deserialize(bytes) ;
        // But don't write it.
    }
    
    ByteBuffer getState() {
        bb.rewind() ;
        serialize(bb) ;
        return bb ;
    }

    void writeState() {
        if ( LOGGING )
            FmtLog.info(log, "writeState = %d %d %d", currentLatestRoot, nodeBlocksLimit, recordsBlocksLimit) ;
        bb.rewind() ;
        serialize(bb) ;
        storage.write(bb, 0) ;
        storage.sync() ;
    }

    void readState() {
        bb.rewind() ;
        storage.read(bb, 0) ;
        bb.rewind() ;
        deserialize(bb) ;
        if ( LOGGING )
            FmtLog.info(log, "readState = %d %d %d", currentLatestRoot, nodeBlocksLimit, recordsBlocksLimit) ;
    }

    void deserialize(ByteBuffer bytes) {
        int root = bytes.getInt() ;
        long nodeBlkLimit = bytes.getLong() ;
        long recordsBlkLimit = bytes.getLong() ;
        setState(root, nodeBlkLimit, recordsBlkLimit) ;
    }
    
    void serialize(ByteBuffer bytes) {
        bytes.putInt(currentLatestRoot) ;
        bytes.putLong(nodeBlocksLimit) ;
        bytes.putLong(recordsBlocksLimit) ;
        bytes.rewind() ;
    }

    public int getRoot() {
        return currentLatestRoot ;
    }
    
    public long getNodeBlocksLimit() {
        return nodeBlocksLimit ;
    }
    
    public long getRecordsBlocksLimit() {
        return recordsBlocksLimit ;
    }

    @Override
    public void sync() {
        if ( dirty )
            writeState() ;
        dirty = false ;
    }

    public void close() { storage.close(); }
}
