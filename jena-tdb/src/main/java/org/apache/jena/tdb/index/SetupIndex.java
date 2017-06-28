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

package org.apache.jena.tdb.index;

import org.apache.jena.tdb.base.block.BlockMgr ;
import org.apache.jena.tdb.base.block.BlockMgrFactory ;
import org.apache.jena.tdb.base.file.FileSet ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.base.record.RecordFactory ;
import org.apache.jena.tdb.index.bplustree.BPlusTree ;
import org.apache.jena.tdb.index.bplustree.BPlusTreeParams ;
import org.apache.jena.tdb.sys.Names ;
import org.apache.jena.tdb.sys.SystemTDB ;

public class SetupIndex {

    /** Create a B+Tree using defaults */
    public static RangeIndex createBPTree(FileSet fileset,
                                          RecordFactory factory)
    {
        int readCacheSize = SystemTDB.BlockReadCacheSize ;
        int writeCacheSize = SystemTDB.BlockWriteCacheSize ;
        int blockSize = SystemTDB.BlockSize ;
        if ( fileset.isMem() )
        {
            readCacheSize = 0 ;
            writeCacheSize = 0 ;
            blockSize = SystemTDB.BlockSizeTest ;
        }
        
        return createBPTreeByBlockSize(fileset, blockSize, readCacheSize, writeCacheSize, factory) ; 
    }

    /** Create a B+Tree by BlockSize */
    public static RangeIndex createBPTreeByBlockSize(FileSet fileset,
                                                     int blockSize,
                                                     int readCacheSize, int writeCacheSize,
                                                     RecordFactory factory)
    {
        return createBPTree(fileset, -1, blockSize, readCacheSize, writeCacheSize, factory) ; 
    }

    /** Create a B+Tree by Order */
    public static RangeIndex createBPTreeByOrder(FileSet fileset,
                                                 int order,
                                                 int readCacheSize, int writeCacheSize,
                                                 RecordFactory factory)
    {
        return createBPTree(fileset, order, -1, readCacheSize, writeCacheSize, factory) ; 
    }

    /** Knowing all the parameters, create a B+Tree */
    public static BPlusTree createBPTree(FileSet fileset, int order, int blockSize,
                                          int readCacheSize, int writeCacheSize,
                                          RecordFactory factory)
    {
        // ---- Checking
        if (blockSize < 0 && order < 0) throw new IllegalArgumentException("Neither blocksize nor order specified") ;
        if (blockSize >= 0 && order < 0) order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
        if (blockSize >= 0 && order >= 0)
        {
            int order2 = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
            if (order != order2) throw new IllegalArgumentException("Wrong order (" + order + "), calculated = "
                                                                    + order2) ;
        }
    
        // Iffy - does not allow for slop.
        if (blockSize < 0 && order >= 0)
        {
            // Only in-memory.
            blockSize = BPlusTreeParams.calcBlockSize(order, factory) ;
        }
    
        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;
        BlockMgr blkMgrNodes = BlockMgrFactory.create(fileset, Names.bptExtTree, blockSize, readCacheSize, writeCacheSize) ;
        BlockMgr blkMgrRecords = BlockMgrFactory.create(fileset, Names.bptExtRecords, blockSize, readCacheSize, writeCacheSize) ;
        return BPlusTree.create(params, blkMgrNodes, blkMgrRecords) ;
    }

    public static Index makeIndex(Location location, String indexName,
                                  int blkSize,
                                  int dftKeyLength, int dftValueLength, 
                                  int readCacheSize,int writeCacheSize)
    {
        return makeRangeIndex(location, indexName, blkSize, dftKeyLength, dftValueLength, readCacheSize, writeCacheSize) ;
    }

    public static RangeIndex makeRangeIndex(Location location, String indexName, 
                                            int blkSize,
                                             int dftKeyLength, int dftValueLength,
                                             int readCacheSize,int writeCacheSize)
    {
         FileSet fs = new FileSet(location, indexName) ;
         RangeIndex rIndex =  makeBPlusTree(fs, blkSize, readCacheSize, writeCacheSize, dftKeyLength, dftValueLength) ;
         return rIndex ;
    }

    public static RangeIndex makeBPlusTree(FileSet fs, int blkSize,
                                           int readCacheSize, int writeCacheSize,
                                           int dftKeyLength, int dftValueLength)
    {
        RecordFactory recordFactory = makeRecordFactory(dftKeyLength, dftValueLength) ;
        int order = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength()) ;
        RangeIndex rIndex = createBPTree(fs, order, blkSize, readCacheSize, writeCacheSize, recordFactory) ;
        return rIndex ;
    }

    public static RecordFactory makeRecordFactory(int keyLen, int valueLen)
    {
        return new RecordFactory(keyLen, valueLen) ;
    }
}

