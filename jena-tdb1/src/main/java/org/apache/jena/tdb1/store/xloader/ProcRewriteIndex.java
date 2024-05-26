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

package org.apache.jena.tdb1.store.xloader;

import java.util.Iterator ;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.tdb1.base.block.BlockMgr;
import org.apache.jena.tdb1.base.block.BlockMgrFactory;
import org.apache.jena.tdb1.base.file.FileSet;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.base.record.Record;
import org.apache.jena.tdb1.base.record.RecordFactory;
import org.apache.jena.tdb1.index.RangeIndex;
import org.apache.jena.tdb1.index.SetupIndex;
import org.apache.jena.tdb1.index.bplustree.BPlusTree;
import org.apache.jena.tdb1.index.bplustree.BPlusTreeParams;
import org.apache.jena.tdb1.index.bplustree.BPlusTreeRewriter;
import org.apache.jena.tdb1.sys.Names;
import org.apache.jena.tdb1.sys.SystemTDB;

public class ProcRewriteIndex {
    public static void exec(Location srcLoc, Location dstLoc, String indexName) 
    {
        FileSet destination = new FileSet(dstLoc, indexName) ;
        int readCacheSize = 0 ;
        int writeCacheSize = -1 ;
        
        int dftKeyLength ;
        int dftValueLength ;
        
        if ( indexName.length() == 3 ) {
            dftKeyLength = SystemTDB.LenIndexTripleRecord;
            dftValueLength = 0;
        } else if ( indexName.length() == 4 ) {
            dftKeyLength = SystemTDB.LenIndexQuadRecord;
            dftValueLength = 0;
        } else {
            FmtLog.error(ProcRewriteIndex.class, "Can't determine record size for %s\n", indexName);
            return;
        }

        RecordFactory recordFactory = null ;
        BPlusTreeParams bptParams = null ;
        BlockMgr blkMgrNodes ;
        BlockMgr blkMgrRecords ;
        int blockSize = SystemTDB.BlockSize ;
        
        RangeIndex rangeIndex = SetupIndex.makeRangeIndex(srcLoc, indexName, blockSize, dftKeyLength, dftValueLength, readCacheSize, writeCacheSize) ;
        BPlusTree bpt = (BPlusTree)rangeIndex ;
        bptParams = bpt.getParams() ;
        recordFactory = bpt.getRecordFactory() ;

        int blockSizeNodes = blockSize ;
        int blockSizeRecords = blockSize ;

        blkMgrNodes = BlockMgrFactory.create(destination, Names.bptExtTree, blockSizeNodes, readCacheSize, writeCacheSize) ;
        blkMgrRecords = BlockMgrFactory.create(destination, Names.bptExtRecords, blockSizeRecords, readCacheSize, writeCacheSize) ;

        Iterator<Record>  iterator = bpt.iterator() ;
            
//            // Fakery.
//            blkMgrNodes = BlockMgrFactory.create(destination, Names.bptExt1, blockSize, readCacheSize, writeCacheSize) ;
//            blkMgrRecords = BlockMgrFactory.create(destination, Names.bptExt2, blockSize, readCacheSize, writeCacheSize) ;
//            recordFactory = new RecordFactory(dftKeyLength, dftValueLength) ;
//            bptParams = new BPlusTreeParams(3, recordFactory) ;
//            List<Record> data = TestBPlusTreeRewriter.createData(10, recordFactory) ;
//            iterator = data.iterator() ;
        
        //System.out.println("Rewrite: "+srcLoc+" "+indexName+" --> "+destination) ;
        
        BPlusTree bpt2 = BPlusTreeRewriter.packIntoBPlusTree(iterator, 
                                                             bptParams, recordFactory,
                                                             blkMgrNodes, blkMgrRecords) ;
        if ( bpt2 == null )
            return ;
//        
//        Iterator<Record> iter = bpt2.iterator() ;
//        for ( ; iter.hasNext() ; )
//        {
//            Record r = iter.next() ;
//            System.out.println(r) ;
//        }

        bpt2.close() ;
    }
}
