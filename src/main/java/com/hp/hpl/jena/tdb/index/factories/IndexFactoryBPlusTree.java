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

package com.hp.hpl.jena.tdb.index.factories;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexFactory ;
import com.hp.hpl.jena.tdb.index.IndexRangeFactory ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class IndexFactoryBPlusTree implements IndexFactory, IndexRangeFactory
{
    private static Logger log = LoggerFactory.getLogger(IndexFactoryBPlusTree.class) ;
    private final int blockSize ;

    public IndexFactoryBPlusTree()
    { this(SystemTDB.BlockSize) ; }
    
    public IndexFactoryBPlusTree(int blockSize)
    {
        this.blockSize = blockSize ;
    }
    
    @Override
    public Index createIndex(FileSet fileset, RecordFactory factory)
    {
        return createRangeIndex(fileset, factory) ;
    }
    
    @Override
    public RangeIndex createRangeIndex(FileSet fileset, RecordFactory factory)
    {
        int order = BPlusTreeParams.calcOrder(blockSize, factory) ;
        
        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;
        if ( params.getCalcBlockSize() > blockSize )
            throw new TDBException("Calculated block size is greater than required size") ;
        
        BlockMgr blkMgrNodes = createBlockMgr(fileset, Names.bptExtTree, blockSize) ;
        BlockMgr blkMgrRecords = createBlockMgr(fileset, Names.bptExtRecords, blockSize) ;
        return BPlusTree.create(params, blkMgrNodes, blkMgrRecords) ;
    }
    
    static BlockMgr createBlockMgr(FileSet fileset, String filename, int blockSize)
    {
        if ( fileset.isMem() )
            return BlockMgrFactory.createMem(filename, blockSize) ;
        
        String fnNodes = fileset.filename(filename) ;
        return BlockMgrFactory.createFile(fnNodes, blockSize, 
                                          SystemTDB.BlockReadCacheSize,
                                          SystemTDB.BlockWriteCacheSize) ;
    }
}
