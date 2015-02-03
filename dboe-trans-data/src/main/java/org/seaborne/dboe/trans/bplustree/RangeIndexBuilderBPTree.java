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

import org.seaborne.dboe.base.block.BlockMgr ;
import org.seaborne.dboe.base.block.BlockMgrBuilder ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.IndexParams ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.dboe.index.RangeIndexBuilder ;
import org.seaborne.dboe.sys.Names ;

/** RangeIndexBuilder for B+Trees */
public class RangeIndexBuilderBPTree implements RangeIndexBuilder
{
    private BlockMgrBuilder bMgrNodes ;
    private BlockMgrBuilder bMgrRecords ;

    public RangeIndexBuilderBPTree(BlockMgrBuilder blockMgrBuilderNodes, BlockMgrBuilder blockMgrBuilderRecords) {
        this.bMgrNodes = blockMgrBuilderNodes ;
        this.bMgrRecords = blockMgrBuilderRecords ;
    }

    @Override
    public RangeIndex buildRangeIndex(FileSet fileSet, RecordFactory recordFactory, IndexParams indexParams) {
        int blkSize = indexParams.getBlockSize() ;
        int order = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength()) ;
        RangeIndex rIndex = createBPTree(fileSet, order, bMgrNodes, bMgrRecords, recordFactory, indexParams) ;
        return rIndex ;
    }
    
    /** Knowing all the parameters, create a B+Tree */
    private RangeIndex createBPTree(FileSet fileset, int order, 
                                    BlockMgrBuilder blockMgrBuilderNodes,
                                    BlockMgrBuilder blockMgrBuilderRecords,
                                    RecordFactory factory, IndexParams indexParams)
    {
        // ---- Checking
        {
            int blockSize = indexParams.getBlockSize() ;
            if (blockSize < 0 )
                throw new IllegalArgumentException("Negative blocksize: "+blockSize) ;
            if (blockSize < 0 && order < 0) throw new IllegalArgumentException("Neither blocksize nor order specified") ;
            if (blockSize >= 0 && order < 0) order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
            if (blockSize >= 0 && order >= 0)
            {
                int order2 = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
                if (order != order2) 
                    throw new IllegalArgumentException("Wrong order (" + order + "), calculated = " + order2) ;
            }
        }

        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;
        
        BlockMgr blkMgrNodes = blockMgrBuilderNodes.buildBlockMgr(fileset, Names.bptExtTree, indexParams) ;
        BlockMgr blkMgrRecords = blockMgrBuilderRecords.buildBlockMgr(fileset, Names.bptExtRecords, indexParams) ;
        return BPlusTreeFactory.create(params, blkMgrNodes, blkMgrRecords) ;
    }
}
