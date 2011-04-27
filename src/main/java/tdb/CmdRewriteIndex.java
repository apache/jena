/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.Iterator ;

import org.openjena.atlas.lib.FileOps ;

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
        
        if ( FileOps.exists(dstLoc.getPath(indexName, Names.bptExt1)) )
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
        Iterator<Record> iterator ;
        
        RangeIndex rangeIndex = SetupTDB.makeRangeIndex(srcLoc, indexName, dftKeyLength, dftValueLength, readCacheSize, writeCacheSize) ;
        BPlusTree bpt = (BPlusTree)rangeIndex ;
        bptParams = bpt.getParams() ;
        recordFactory = bpt.getRecordFactory() ;

        int blockSizeNodes = bpt.getNodeManager().getBlockMgr().blockSize() ;
        int blockSizeRecords = bpt.getNodeManager().getBlockMgr().blockSize() ;

        blkMgrNodes = BlockMgrFactory.create(destination, Names.bptExt1, blockSizeNodes, readCacheSize, writeCacheSize) ;
        blkMgrRecords = BlockMgrFactory.create(destination, Names.bptExt2, blockSizeRecords, readCacheSize, writeCacheSize) ;

        iterator = bpt.iterator() ;
            
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