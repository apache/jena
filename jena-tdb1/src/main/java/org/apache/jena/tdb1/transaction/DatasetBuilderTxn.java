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

package org.apache.jena.tdb1.transaction ;

import java.util.Map ;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.tdb1.TDB1Exception;
import org.apache.jena.tdb1.base.block.BlockMgr;
import org.apache.jena.tdb1.base.block.BlockMgrReadonly;
import org.apache.jena.tdb1.base.file.FileSet;
import org.apache.jena.tdb1.base.objectfile.ObjectFile;
import org.apache.jena.tdb1.base.objectfile.ObjectFileReadonly;
import org.apache.jena.tdb1.index.IndexParams;
import org.apache.jena.tdb1.setup.BlockMgrBuilder;
import org.apache.jena.tdb1.setup.DatasetBuilderStd;
import org.apache.jena.tdb1.setup.ObjectFileBuilder;
import org.apache.jena.tdb1.store.DatasetGraphTDB;
import org.apache.jena.tdb1.sys.FileRef;

public class DatasetBuilderTxn
{
    // Context for the build.
    private final TransactionManager txnMgr ;
    private final Map<FileRef, BlockMgr> blockMgrs ; 
    private final Map<FileRef, ObjectFile> objectFiles; 
    private final DatasetGraphTDB dsg ;
    private Transaction txn;

    public DatasetBuilderTxn(TransactionManager txnMgr, DatasetGraphTDB dsg) {
        this.txnMgr = txnMgr ;
        this.blockMgrs = dsg.getConfig().blockMgrs ;
        this.objectFiles = dsg.getConfig().objectFiles ;
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
        ObjectFileBuilder objectFileBuilder = new ObjectFileBuilderReadonly();
        DatasetBuilderStd x = new DatasetBuilderStd(blockMgrBuilder, objectFileBuilder) ;
        DatasetGraphTDB dsg2 = x._build(dsg.getLocation(), dsg.getConfig().params, false, dsg.getReorderTransform()) ;
        return dsg2 ;
    }

    private DatasetGraphTDB buildWritable() {
        BlockMgrBuilder blockMgrBuilder = new BlockMgrBuilderTx() ;
        ObjectFileBuilder objectFileBuilder = new ObjectFileBuilderTx();
        DatasetBuilderStd x = new DatasetBuilderStd(blockMgrBuilder, objectFileBuilder);
        DatasetGraphTDB dsg2 = x._build(dsg.getLocation(), dsg.getConfig().params, true, dsg.getReorderTransform()) ;
        dsg2.getContext().putAll(dsg.getContext()) ;
        return dsg2 ;
    }

    // ---- Build transactional versions for update.
    
    class BlockMgrBuilderTx implements BlockMgrBuilder
    {
        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, IndexParams params) {
            // Find from file ref.
            FileRef ref = FileRef.create(fileSet, ext) ;
            BlockMgr baseMgr = blockMgrs.get(ref) ;
            if ( baseMgr == null ) {
                //System.out.flush();
                System.out.println("No BlockMgr for " + ref+" : "+blockMgrs.keySet());
                //throw new TDBException("No BlockMgr for " + ref) ;
            }
            BlockMgrJournal blkMgr = new BlockMgrJournal(txn, ref, baseMgr) ;
            txn.addComponent(blkMgr) ;
            return blkMgr ;
        }
    }

    class ObjectFileBuilderTx implements ObjectFileBuilder
    {
        @Override
        public ObjectFile buildObjectFile(FileSet fileSet, String ext) {
            FileRef ref = FileRef.create(fileSet, ext) ;
            ObjectFile base = objectFiles.get(ref) ;
            // Just write to the (append only) ObjectFile and manage aborts.
            ObjectFileTrans objFileTxn = new ObjectFileTrans(txn, base) ;
            txn.addComponent(objFileTxn);
            return objFileTxn;
        }
    }

    // ---- Build passthrough versions for readonly access

    class BlockMgrBuilderReadonly implements BlockMgrBuilder
    {
        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, IndexParams params) {
            FileRef ref = FileRef.create(fileSet, ext) ;
            BlockMgr blockMgr = blockMgrs.get(ref) ;
            if ( blockMgr == null )
                throw new TDB1Exception("No BlockMgr for " + ref) ;
            blockMgr = new BlockMgrReadonly(blockMgr) ;
            return blockMgr ;
        }
    }
    
    class ObjectFileBuilderReadonly implements ObjectFileBuilder
    {
        @Override
        public ObjectFile buildObjectFile(FileSet fileSet, String ext) {
            FileRef ref = FileRef.create(fileSet, ext) ;
            ObjectFile file = objectFiles.get(ref) ;
            return new ObjectFileReadonly(file);
        }
    }
}

