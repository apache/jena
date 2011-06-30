/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction ;

import java.util.HashMap ;
import java.util.Map ;
import java.util.Properties ;

import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrLogger ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelFile ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelMem ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileLogger ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableLogger ;
import com.hp.hpl.jena.tdb.setup.BlockMgrBuilder ;
import com.hp.hpl.jena.tdb.setup.Builder ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.setup.IndexBuilder ;
import com.hp.hpl.jena.tdb.setup.NodeTableBuilder ;
import com.hp.hpl.jena.tdb.setup.ObjectFileBuilder ;
import com.hp.hpl.jena.tdb.setup.RangeIndexBuilder ;
import com.hp.hpl.jena.tdb.setup.TupleIndexBuilder ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixStorageLogger ;
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicy ;
import com.hp.hpl.jena.tdb.sys.FileRef ;

public class DatasetBuilderTxn extends DatasetBuilderStd
{
    // Track resources for the datsetgraph as built.
    //    BlockMgr.
    //    ObjectFile.
    
    
    public DatasetBuilderTxn(TransactionManager txnMgr) { setStd() ; this.txnMgr = txnMgr ; }
    
    // ---- Add logging to a BlockMgr when built.
    static BlockMgrBuilder logging(BlockMgrBuilder other) { return new BlockMgrBuilderLogger(other) ; }
    
    static class BlockMgrBuilderLogger implements BlockMgrBuilder
    {
        public BlockMgrBuilder other ;
        public BlockMgrBuilderLogger(BlockMgrBuilder other)
        { 
            this.other = other ;
        }
        
        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, int blockSize)
        {
            BlockMgr blkMgr = other.buildBlockMgr(fileSet, ext, blockSize) ;
            blkMgr = new BlockMgrLogger(blkMgr.getLabel(), blkMgr, true) ;
            return blkMgr ;
        }
    }
    // ----
    
    @Override
    protected void setStd()
    {
        ObjectFileBuilder objectFileBuilder = new ObjectFileBuilderTx() ;
        BlockMgrBuilder blockMgrBuilder = new BlockMgrBuilderTx() ;

        // Add track(...) to log.
        IndexBuilder indexBuilder = new Builder.IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;

        // Add logging to a BlockMgrBuilder (here, just the records par of the B+Tree
        //RangeIndexBuilder rangeIndexBuilder = new RangeIndexBuilderStd(blockMgrBuilder, logging(blockMgrBuilder)) ;
        RangeIndexBuilder rangeIndexBuilder = new Builder.RangeIndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;

        NodeTableBuilder nodeTableBuilder = new Builder.NodeTableBuilderStd(indexBuilder, objectFileBuilder)
        {
            // track all NodeTable operations
            @Override
            public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, int sizeNode2NodeIdCache, int sizeNodeId2NodeCache)
            {
                NodeTable nt = super.buildNodeTable(fsIndex, fsObjectFile, sizeNode2NodeIdCache, sizeNodeId2NodeCache) ;
                if ( false )
                    nt = new NodeTableLogger(fsObjectFile.getBasename(), nt) ;
                return nt ;
                
            }
        } ; 
        
        TupleIndexBuilder tupleIndexBuilder = new Builder.TupleIndexBuilderStd(rangeIndexBuilder) ;
        set(nodeTableBuilder, tupleIndexBuilder, indexBuilder, rangeIndexBuilder, blockMgrBuilder, objectFileBuilder) ;
    }
    
    @Override
    protected DatasetPrefixStorage makePrefixTable(Location location, ConcurrencyPolicy policy)
    {
        DatasetPrefixStorage x = super.makePrefixTable(location, policy) ;
        // Logging.
        if ( false )
            x = new DatasetPrefixStorageLogger(x) ;
        return x ;
    }

    @Override
    protected DatasetGraphTDB _build(Location location, Properties config)
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
    private Map<FileRef, BlockMgr> blockMgrs = new HashMap<FileRef, BlockMgr>() ;
    
    
    // *** TODO NodTbale has cachign over this ==> broken.
    private Map<FileRef, ObjectFile> objectFile = new HashMap<FileRef, ObjectFile>() ;

    class ObjectFileBuilderTx implements ObjectFileBuilder
    {
        ObjectFileBuilder base = new Builder.ObjectFileBuilderStd() ;
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
            ObjectFile objFile = objFileTrans ;
            if ( false )
            {
                String fn = FileOps.basename(fileSet.filename(ext)) ;
                objFile = new ObjectFileLogger(fn, objFile) ;
            }
            
            { 
                FileRef fileref = FileRef.create(fileSet, ext) ;
                Log.info(DatasetBuilderTxn.class, "ObjectFile: "+fileref) ;
            }
            
            return objFile ;
        }
    }

    class BlockMgrBuilderTx implements BlockMgrBuilder
    {
        BlockMgrBuilder base = new Builder.BlockMgrBuilderStd() ;
        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, int blockSize)
        {
            BlockMgr baseMgr = base.buildBlockMgr(fileSet, ext, blockSize) ;
            FileRef ref = FileRef.create(fileSet.filename(ext)) ;
            BlockMgrJournal blkMg = new BlockMgrJournal(txn, ref, baseMgr, journal) ;
            // [TxTDB:TODO]
            
            { 
                FileRef fileref = FileRef.create(fileSet, ext) ;
                Log.info(DatasetBuilderTxn.class, "BlockMgr: "+fileref) ;
            }
            
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