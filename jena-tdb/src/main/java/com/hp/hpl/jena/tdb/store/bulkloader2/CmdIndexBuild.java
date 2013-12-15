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

package com.hp.hpl.jena.tdb.store.bulkloader2;

import java.io.InputStream ;
import java.util.Iterator ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.logging.LogCtl ;
import tdb.cmdline.CmdTDB ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriter ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** From a file of records, build a (packed) index */ 
public class CmdIndexBuild
{
    static { LogCtl.setLog4j() ; }
    
    public static void main(String...argv)
    {
        CmdTDB.init() ;
        // DATA IN S/P/O columns but sorted by index order.
        
        if ( argv.length != 3 )
        {
            System.err.println("Usage: Location Index dataFile") ;
            System.exit(1) ;
        }
        
        String locationStr = argv[0] ;
        String indexName = argv[1] ;
        
//        if ( ! Arrays.asList(Names.tripleIndexes).contains(indexName) &&
//            ! Arrays.asList(Names.quadIndexes).contains(indexName) )
//        {
//            System.err.println("Index name not recognized: "+indexName) ;
//            System.exit(1) ;
//        }
            
        String dataFile = argv[2] ;
        
        // Argument processing
        
        Location location = new Location(locationStr) ;
        
        //InputStream input = System.in ;
        InputStream input = IO.openFile(dataFile) ;
        
        int keyLength = SystemTDB.SizeOfNodeId * indexName.length() ;
        int valueLength = 0 ;
        
        // The name is the order.
        String primary = indexName ;  
        
        // Scope for optimization:
        // Null column map => no churn.
        // Do record -> record copy, not Tuple, Tuple copy.

        String primaryOrder ;
        int dftKeyLength ;
        int dftValueLength ;
        int tupleLength = indexName.length() ;

        if ( tupleLength == 3 )
        {
            primaryOrder = Names.primaryIndexTriples ;
            dftKeyLength = SystemTDB.LenIndexTripleRecord ;
            dftValueLength = 0 ;
        }
        else if ( tupleLength == 4 )
        {
            primaryOrder = Names.primaryIndexQuads ;
            dftKeyLength = SystemTDB.LenIndexQuadRecord ;
            dftValueLength = 0 ;
        }
        else
        {
            throw new AtlasException("Index name: "+indexName) ;
        }
        
        ColumnMap colMap = new ColumnMap(primaryOrder, indexName) ;


        // -1? Write only.
        // Also flush cache every so often => block writes (but not sequential so boring).
        int readCacheSize = 10 ;
        int writeCacheSize = 100 ;

        int blockSize = SystemTDB.BlockSize ;
        RecordFactory recordFactory = new RecordFactory(dftKeyLength, dftValueLength) ;
        
        int order = BPlusTreeParams.calcOrder(blockSize, recordFactory) ;
        BPlusTreeParams bptParams = new BPlusTreeParams(order, recordFactory) ;

        int blockSizeNodes = blockSize ;
        int blockSizeRecords = blockSize ;

        FileSet destination = new FileSet(location, indexName) ;

        BlockMgr blkMgrNodes = BlockMgrFactory.create(destination, Names.bptExtTree, blockSizeNodes, readCacheSize, writeCacheSize) ;
        BlockMgr blkMgrRecords = BlockMgrFactory.create(destination, Names.bptExtRecords, blockSizeRecords, readCacheSize, writeCacheSize) ;
        
        int rowBlock = 1000 ;
        Iterator<Record> iter = new RecordsFromInput(input, tupleLength, colMap, rowBlock) ;
        BPlusTree bpt2 = BPlusTreeRewriter.packIntoBPlusTree(iter, bptParams, recordFactory, blkMgrNodes, blkMgrRecords) ;
        bpt2.close() ;
    }
}
