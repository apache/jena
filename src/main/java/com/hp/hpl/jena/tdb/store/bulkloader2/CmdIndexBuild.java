/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader2;

import java.io.InputStream ;
import java.util.Arrays ;
import java.util.Iterator ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.logging.Log ;
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
    static { Log.setLog4j() ; }
    
    public static void main(String...argv)
    {
        CmdTDB.setLogging() ;
        // DATA IN S/P/O columns but sorted by index order.
        
        if ( argv.length != 3 )
        {
            System.err.println("Usage: Location Index dataFile") ;
            System.exit(1) ;
        }
        
        String locationStr = argv[0] ;
        String indexName = argv[1] ;
        if ( ! Arrays.asList(Names.tripleIndexes).contains(indexName) &&
            ! Arrays.asList(Names.quadIndexes).contains(indexName) )
//        
//        if ( !indexName.equals("SPO") && !indexName.equals("POS") && !indexName.equals("OSP") )
        {
            System.err.println("Index name not recognized: "+indexName) ;
            System.exit(1) ;
        }
            
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

        BlockMgr blkMgrNodes = BlockMgrFactory.create(destination, Names.bptExt1, blockSizeNodes, readCacheSize, writeCacheSize) ;
        BlockMgr blkMgrRecords = BlockMgrFactory.create(destination, Names.bptExt2, blockSizeRecords, readCacheSize, writeCacheSize) ;
        
        int rowBlock = 1000 ;
        Iterator<Record> iter = new RecordsFromInput(input, tupleLength, colMap, rowBlock) ;
        BPlusTree bpt2 = BPlusTreeRewriter.packIntoBPlusTree(iter, bptParams, recordFactory, blkMgrNodes, blkMgrRecords) ;
        bpt2.sync() ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */