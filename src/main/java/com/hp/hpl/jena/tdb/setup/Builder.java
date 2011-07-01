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

package com.hp.hpl.jena.tdb.setup;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId ;
import org.openjena.atlas.lib.ColumnMap ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableCache ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableInline ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableNative ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class Builder
{
    private static boolean VERBOSE = true ;
    private static Logger log = LoggerFactory.getLogger(Builder.class) ;
    
    public static class TupleIndexBuilderStd implements TupleIndexBuilder
    {
        private final RangeIndexBuilder rangeIndexBuilder ;
    
        public TupleIndexBuilderStd(RangeIndexBuilder rangeIndexBuilder)
        {
            this.rangeIndexBuilder = rangeIndexBuilder ;
        }
        
        @Override
        public TupleIndex buildTupleIndex(FileSet fileSet, ColumnMap colMap)
        {
            RecordFactory recordFactory = new RecordFactory(SizeOfNodeId*colMap.length(),0) ;
            
            RangeIndex rIdx = rangeIndexBuilder.buildRangeIndex(fileSet, recordFactory) ;
            TupleIndex tIdx = new TupleIndexRecord(colMap.length(), colMap, recordFactory, rIdx) ;
            return tIdx ;
        }
    }

    public static class NodeTableBuilderStd implements NodeTableBuilder
    {
        private final IndexBuilder indexBuilder ;
        private final ObjectFileBuilder objectFileBuilder ;
        
        public NodeTableBuilderStd(IndexBuilder indexBuilder, ObjectFileBuilder objectFileBuilder)
        { 
            this.indexBuilder = indexBuilder ;
            this.objectFileBuilder = objectFileBuilder ;
        }
        
        @Override
        public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, int sizeNode2NodeIdCache, int sizeNodeId2NodeCache)
        {
            RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
            Index idx = indexBuilder.buildIndex(fsIndex, recordFactory) ;
            ObjectFile objectFile = objectFileBuilder.buildObjectFile(fsObjectFile, Names.extNodeData) ;
            NodeTable nodeTable = new NodeTableNative(idx, objectFile) ;
            nodeTable = NodeTableCache.create(nodeTable, sizeNode2NodeIdCache, sizeNodeId2NodeCache) ;
            nodeTable = NodeTableInline.create(nodeTable) ;
            return nodeTable ;
        }
    }
    // ----

    public static class IndexBuilderStd implements IndexBuilder
    {
        protected BlockMgrBuilder bMgrNodes ;
        protected BlockMgrBuilder bMgrRecords ;
        protected RangeIndexBuilderStd other ;
    
        public IndexBuilderStd(BlockMgrBuilder bMgrNodes, BlockMgrBuilder bMgrRecords)
        {
            this.bMgrNodes = bMgrNodes ;
            this.bMgrRecords = bMgrRecords ;
            this.other = new RangeIndexBuilderStd(bMgrNodes, bMgrRecords) ;
        }
        
        @Override
        public Index buildIndex(FileSet fileSet, RecordFactory recordFactory)
        {
            // Cheap.
            return other.buildRangeIndex(fileSet, recordFactory) ;
        }
    }

    public static class RangeIndexBuilderStd implements RangeIndexBuilder
        {
            private BlockMgrBuilder bMgrNodes ;
            private BlockMgrBuilder bMgrRecords ;
            public RangeIndexBuilderStd( BlockMgrBuilder blockMgrBuilderNodes,
                                         BlockMgrBuilder blockMgrBuilderRecords)
            {
                this.bMgrNodes = blockMgrBuilderNodes ;
                this.bMgrRecords = blockMgrBuilderRecords ;
            }
    
            @Override
            public RangeIndex buildRangeIndex(FileSet fileSet, RecordFactory recordFactory)
            {
                int blkSize = SystemTDB.BlockSize ;
                int order = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength()) ;
                int readCacheSize = SystemTDB.BlockReadCacheSize ;
                int writeCacheSize = SystemTDB.BlockWriteCacheSize ;
                RangeIndex rIndex = createBPTree(fileSet, order, blkSize, readCacheSize, writeCacheSize, bMgrNodes, bMgrRecords, recordFactory) ;
                return rIndex ;
            }
            
            /** Knowing all the parameters, create a B+Tree */
            private RangeIndex createBPTree(FileSet fileset, int order, 
                                            int blockSize,
                                            int readCacheSize, int writeCacheSize,
                                            BlockMgrBuilder blockMgrBuilderNodes,
                                            BlockMgrBuilder blockMgrBuilderRecords,
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
                
                BlockMgr blkMgrNodes = blockMgrBuilderNodes.buildBlockMgr(fileset, Names.bptExtTree, blockSize) ;
                BlockMgr blkMgrRecords = blockMgrBuilderRecords.buildBlockMgr(fileset, Names.bptExtRecords, blockSize) ;
                return BPlusTree.create(params, blkMgrNodes, blkMgrRecords) ;
            }
        }

    public static class ObjectFileBuilderStd implements ObjectFileBuilder
    {
        @Override
        public ObjectFile buildObjectFile(FileSet fileSet, String ext)
        {
            if ( fileSet.isMem() )
                return FileFactory.createObjectFileMem() ;
            String filename = fileSet.filename(ext) ;
            return FileFactory.createObjectFileDisk(filename) ;
        }
    }

    public static class BlockMgrBuilderStd implements BlockMgrBuilder
    {
        public BlockMgrBuilderStd() {}
    
        @Override
        public BlockMgr buildBlockMgr(FileSet fileset, String ext, int blockSize)
        {
            //int readCacheSize = PropertyUtils.getPropertyAsInteger(config, Names.pBlockReadCacheSize) ;
            //int writeCacheSize = PropertyUtils.getPropertyAsInteger(config, Names.pBlockWriteCacheSize) ;
            
            int readCacheSize = SystemTDB.BlockReadCacheSize ;
            int writeCacheSize = SystemTDB.BlockWriteCacheSize ;
            
            BlockMgr mgr = BlockMgrFactory.create(fileset, ext, blockSize, readCacheSize, writeCacheSize) ;
            return mgr ;
        }
        
    }

}

