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

package org.apache.jena.tdb.transaction ;

import java.util.Map ;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.tdb.TDBException ;
import org.apache.jena.tdb.base.block.BlockMgr ;
import org.apache.jena.tdb.base.block.BlockMgrLogger ;
import org.apache.jena.tdb.base.block.BlockMgrReadonly ;
import org.apache.jena.tdb.base.file.FileFactory;
import org.apache.jena.tdb.base.file.FileSet ;
import org.apache.jena.tdb.base.objectfile.ObjectFile ;
import org.apache.jena.tdb.base.objectfile.ObjectFileReadonly;
import org.apache.jena.tdb.base.record.RecordFactory;
import org.apache.jena.tdb.index.*;
import org.apache.jena.tdb.setup.*;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.nodetable.*;
import org.apache.jena.tdb.sys.FileRef ;
import org.apache.jena.tdb.sys.Names;
import org.apache.jena.tdb.sys.SystemTDB;

public class DatasetBuilderTxn
{
    // Ideally, don't make a DatasetGraphTDB to pass to new DatasetGraphTxn as it rips it apart.
    
    // Context for the build.
    private final TransactionManager txnMgr ;
    private final Map<FileRef, BlockMgr> blockMgrs ; 
    private final Map<FileRef, ObjectFile> objectFiles; 
    private final Map<FileRef, NodeTable> nodeTables;
    private final DatasetGraphTDB dsg ;
    private Transaction txn;

    public DatasetBuilderTxn(TransactionManager txnMgr, DatasetGraphTDB dsg) {
        this.txnMgr = txnMgr ;
        this.blockMgrs = dsg.getConfig().blockMgrs ;
        this.objectFiles = dsg.getConfig().objectFiles ;
        this.nodeTables = dsg.getConfig().nodeTables ;
        this.dsg = dsg ;
    }
    
    DatasetGraphTxn build(Transaction txn, ReadWrite mode) {
        this.txn = txn;
        DatasetGraphTDB dsgTDB ;

        switch(mode)
        {
            case READ :   dsgTDB = buildReadonly() ; break ;
            case WRITE :  dsgTDB = buildWritable() ;  break ;
            default:      dsgTDB = null ;
        }

        DatasetGraphTxn dsgTxn = new DatasetGraphTxn(dsgTDB, txn) ;
        // Copy context. Changes not propagated back to the base dataset. 
        dsgTxn.getContext().putAll(dsg.getContext()) ;
        return dsgTxn ;
    }

    private DatasetGraphTDB buildReadonly() {
        BlockMgrBuilder blockMgrBuilder = new BlockMgrBuilderReadonly();
        NodeTableBuilder nodeTableBuilder = new NodeTableBuilderReadonly();
        DatasetBuilderStd x = new DatasetBuilderStd(blockMgrBuilder, nodeTableBuilder) ;
        DatasetGraphTDB dsg2 = x._build(dsg.getLocation(), dsg.getConfig().params, false, dsg.getReorderTransform()) ;
        return dsg2 ;
    }

    private DatasetGraphTDB buildWritable() {
        BlockMgrBuilder blockMgrBuilder = new BlockMgrBuilderTx() ;
        NodeTableBuilder nodeTableBuilder = new NodeTableBuilderTx();
        DatasetBuilderStd x = new DatasetBuilderStd(blockMgrBuilder, nodeTableBuilder) ;
        DatasetGraphTDB dsg2 = x._build(dsg.getLocation(), dsg.getConfig().params, true, dsg.getReorderTransform()) ;
        dsg2.getContext().putAll(dsg.getContext()) ;
        return dsg2 ;
    }

    // ---- Add logging to a BlockMgr when built.
    static BlockMgrBuilder logging(BlockMgrBuilder other) { return new BlockMgrBuilderLogger(other) ; }
    
    static class BlockMgrBuilderLogger implements BlockMgrBuilder {
        public BlockMgrBuilder other ;

        public BlockMgrBuilderLogger(BlockMgrBuilder other) {
            this.other = other ;
        }

        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, IndexParams params) {
            BlockMgr blkMgr = other.buildBlockMgr(fileSet, ext, params) ;
            blkMgr = new BlockMgrLogger(blkMgr.getLabel(), blkMgr, true) ;
            return blkMgr ;
        }
    }

    // ---- Build transactional versions for update.
    
    class NodeTableBuilderTx implements NodeTableBuilder
    {
        @Override
        public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, StoreParams params) {
            FileRef ref = FileRef.create(fsObjectFile.filename(Names.extNodeData)) ;
            NodeTable ntBase = nodeTables.get(ref) ;
            if ( ntBase == null )
                throw new TDBException("No NodeTable for " + ref) ;

            RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
            Index idx = new IndexMap(recordFactory) ;
            String objFilename = fsObjectFile.filename(Names.extNodeData + "-" + Names.extJournal) ;
            ObjectFile objectFile ;

            if ( fsObjectFile.isMem() )
                objectFile = FileFactory.createObjectFileMem(objFilename) ;
            else
                objectFile = FileFactory.createObjectFileDisk(objFilename) ;

            // Allow for a modified base NodeTable. 
            NodeTable ntBaseTrans = ntBase;
            
            NodeTableTrans ntt = new NodeTableTrans(txn, fsObjectFile.getBasename(), ntBaseTrans, idx, objectFile) ;
            txn.addComponent(ntt) ;

            // Add inline wrapper.
            NodeTable nt = NodeTableInline.create(ntt) ;
            return nt ;
        }
    }
    
    class BlockMgrBuilderTx implements BlockMgrBuilder
    {
        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, IndexParams params) {
            // Find from file ref.
            FileRef ref = FileRef.create(fileSet, ext) ;
            BlockMgr baseMgr = blockMgrs.get(ref) ;
            if ( baseMgr == null )
                throw new TDBException("No BlockMgr for " + ref) ;
            BlockMgrJournal blkMgr = new BlockMgrJournal(txn, ref, baseMgr) ;
            txn.addComponent(blkMgr) ;
            return blkMgr ;
        }
    }

    // Object files currently don't need journalling. Because they are append only, they
    // are "self journalling" - we can append to them while used read-only elsewhere if
    // the index to access them is transactional and does not point into the appended new
    // bytes.
//    class ObjectFileBuilderTx implements ObjectFileBuilder
//    {
//        @Override
//        public ObjectFile buildObjectFile(FileSet fileSet, String ext) {
//            FileRef ref = FileRef.create(fileSet, ext) ;
//            ObjectFile baseObjFile = objectFiles.get(ref);
//            if ( baseObjFile == null )
//                throw new TDBException("No ObjectFile for " + ref) ;
//            ObjectFileJournal objFile = new ObjectFileJournal(txn, ref, baseObjectFile); 
//            return objFile;
//        }
//    }
    
    // ---- Build passthrough versions for readonly access
    
    class BlockMgrBuilderReadonly implements BlockMgrBuilder
    {
        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, IndexParams params) {
            FileRef ref = FileRef.create(fileSet, ext) ;
            BlockMgr blockMgr = blockMgrs.get(ref) ;
            if ( blockMgr == null )
                throw new TDBException("No BlockMgr for " + ref) ;
            blockMgr = new BlockMgrReadonly(blockMgr) ;
            return blockMgr ;
        }
    }
    
    class NodeTableBuilderReadonly implements NodeTableBuilder
    {
        @Override
        public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, StoreParams params) {
            FileRef ref = FileRef.create(fsObjectFile.filename(Names.extNodeData)) ;
            NodeTable nt = nodeTables.get(ref) ;
            nt = new NodeTableReadonly(nt) ;
            return nt ;
        }
    }
 
    class ObjectFileBuilderReadonly implements ObjectFileBuilder
    {
        @Override
        public ObjectFile buildObjectFile(FileSet fileSet, String ext) {
            FileRef ref = FileRef.create(fileSet, ext) ;
            ObjectFile objFile = objectFiles.get(ref);
            if ( objFile == null )
                throw new TDBException("No ObjectFile for " + ref) ;
            objFile = new ObjectFileReadonly(objFile) ;
            return objFile;
        }
    }
 
}
