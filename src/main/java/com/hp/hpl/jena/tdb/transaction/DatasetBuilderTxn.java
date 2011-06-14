/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction ;

import java.util.Properties ;

import setup.BlockMgrBuilder ;
import setup.DatasetBuilderStd ;
import setup.IndexBuilder ;
import setup.NodeTableBuilder ;
import setup.ObjectFileBuilder ;
import setup.RangeIndexBuilder ;
import setup.TupleIndexBuilder ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelFile ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelMem ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

public class DatasetBuilderTxn extends DatasetBuilderStd
{
    public DatasetBuilderTxn(TransactionManager txnMgr) { setStd() ; this.txnMgr = txnMgr ; }
    
    @Override
    protected void setStd()
    {
        ObjectFileBuilder objectFileBuilder = new ObjectFileBuilderTx() ;
        BlockMgrBuilder blockMgrBuilder = new BlockMgrBuilderTx() ;

        // These are the usual.
        IndexBuilder indexBuilder = new IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
        RangeIndexBuilder rangeIndexBuilder = new RangeIndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;

        NodeTableBuilder nodeTableBuilder = new NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
        TupleIndexBuilder tupleIndexBuilder = new TupleIndexBuilderStd(rangeIndexBuilder) ;
        set(nodeTableBuilder, tupleIndexBuilder, indexBuilder, rangeIndexBuilder, blockMgrBuilder, objectFileBuilder) ;
    }

    @Override
    public DatasetGraphTDB build(Location location, Properties config)
    {
        this.location = location ;
        this.txn = txnMgr.createTransaction() ;
        
        BufferChannel chan ;
        if (location.isMem()) chan = new BufferChannelMem("journal") ;
        else
            chan = new BufferChannelFile(location.getPath(journalFilename, journalExt)) ;

        journal = new Journal(chan) ;
        txn.add(journal) ;

        DatasetGraphTDB dsg = super.build(location, config) ;
        return new DatasetGraphTxnTDB(dsg, txn) ;
    }

    public static final String journalExt = "jrnl" ;
    public static final String journalFilename = "journal" ;
   
    private TransactionManager txnMgr ;
    private Journal  journal ;
    private Transaction txn ;
    private Location location ;

    class ObjectFileBuilderTx implements ObjectFileBuilder
    {
        ObjectFileBuilder base = new ObjectFileBuilderStd() ;
        @Override
        public ObjectFile buildObjectFile(FileSet fileSet, String ext)
        {
            ObjectFile backing ;
            ObjectFile main = base.buildObjectFile(fileSet, ext) ;
            
            if ( location.isMem() )
                backing = FileFactory.createObjectFileMem() ;
            else
                backing = FileFactory.createObjectFileDisk(fileSet.filename(journalExt)) ;

            ObjectFileTrans objFileTrans = new ObjectFileTrans(txn, main, backing) ;
            txn.add(objFileTrans) ;
            return objFileTrans ;
        }
    }

    class BlockMgrBuilderTx implements BlockMgrBuilder
    {
        BlockMgrBuilder base = new BlockMgrBuilderStd() ;
        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, int blockSize)
        {
            BlockMgr baseMgr = base.buildBlockMgr(fileSet, ext, blockSize) ;
            BlockMgrJournal blkMg = new BlockMgrJournal(txn, null, baseMgr, journal) ;
            // Add to transaction.
            return blkMg ;
        }
    }

    /** Add transactions to an existing datatset */
    public static DatasetGraphTDB enhance(DatasetGraphTDB dsg)
    {

        return dsg ;

    }

}

/*
 * (c) Copyright 2011 Epimorphics Ltd. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */