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

package tdb;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.FileOps ;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriter ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Rewrite one index */
public class CmdRewriteIndex
{
    public static void main(String...argv)
    {
        // Usage: srcLocation dstLocation indexName
        if ( argv.length != 3 )
        {
            System.err.println("Usage: "+Utils.classShortName(CmdRewriteIndex.class)+" SrcLocation DstLocation IndexName") ;
            System.exit(1) ;
        }
        
        Location srcLoc = new Location(argv[0]) ;
        Location dstLoc = new Location(argv[1]) ;
        String indexName = argv[2] ;
        
        if ( ! FileOps.exists(argv[1]) )
        {
            System.err.println("Destination directory does not exist") ;
            System.exit(1) ;
        }
        
        if ( FileOps.exists(dstLoc.getPath(indexName, Names.bptExtTree)) )
        {
            System.err.println("Destination contains an index of that name") ;
            System.exit(1) ;
        }
        
        // To current directory ....
        FileSet destination = new FileSet(dstLoc, indexName) ;
        // Check existance
        
//        FileOps.deleteSilent(destination.filename(Names.bptExt1)) ;
//        FileOps.deleteSilent(destination.filename(Names.bptExt2)) ;
        //FileSet destination = new FileSet(Location.mem(), destIndex) ;
        
        int readCacheSize = 0 ;
        int writeCacheSize = -1 ;
        
        int dftKeyLength ;
        int dftValueLength ;
        
        if ( indexName.length() == 3 )
        {
            dftKeyLength = SystemTDB.LenIndexTripleRecord ;
            dftValueLength = 0 ;
        }
        else if ( indexName.length() == 4 )
        {
            dftKeyLength = SystemTDB.LenIndexQuadRecord ;
            dftValueLength = 0 ;
        }
//        else if ( srcIndex.equals("node2id") )
//        { }
//      java -cp "$CP" -server -Xmx1000M bpt.CmdRewriteIndex "$@"  else if ( srcIndex.equals("prefixIdx") )
//        {}
//        else if ( srcIndex.equals("prefix2id") )
//        {}
        else
        {
            System.err.printf("Can't determine record size for %s\n",indexName) ;
            return ;
        }
        
        RecordFactory recordFactory = null ;
        BPlusTreeParams bptParams = null ;
        BlockMgr blkMgrNodes ;
        BlockMgr blkMgrRecords ;
        int blockSize = SystemTDB.BlockSize ;
        
        RangeIndex rangeIndex = SetupTDB.makeRangeIndex(srcLoc, indexName, blockSize, dftKeyLength, dftValueLength, readCacheSize, writeCacheSize) ;
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
