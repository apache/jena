/*
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
 
package repack;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.sparql.core.Quad ;
import org.seaborne.dboe.base.file.BinaryDataFile ;
import org.seaborne.dboe.base.file.FileFactory ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.Index ;
import org.seaborne.dboe.jenax.Txn ;
import org.seaborne.dboe.sys.Names ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeParams ;
import org.seaborne.dboe.transaction.txn.TransactionalSystem ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.TDB2Factory ;
import org.seaborne.tdb2.setup.StoreParams ;
import org.seaborne.tdb2.setup.TDBBuilder ;
import org.seaborne.tdb2.setup.TDBDatasetDetails ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;
import org.seaborne.tdb2.store.nodetable.NodeTableTRDF ;
import org.seaborne.tdb2.store.tupletable.TupleIndex ;
import org.seaborne.tdb2.store.tupletable.TupleIndexRecord ;
import org.seaborne.tdb2.sys.StoreConnection ;

/** Code for cloning a dataset */ 
public class CloneTDB {
    /** quad-based copy of a dataset */ 
    public static DatasetGraphTDB cloneDatasetSimple(DatasetGraphTDB dsgBase, Location newLocation) {
//        if ( ! dsgBase.isInTransaction() )
//            throw new TDBException("Not in a transaction") ;
        
        if ( dsgBase.getLocation().equals(newLocation) )
            throw new TDBException("Attempt to clone into same location") ;
        if ( ! newLocation.isMem() && ! FileOps.isEmpty(newLocation.getDirectoryPath()) )
            throw new TDBException("Attempt to clone into existing location") ;
        
        StoreConnection sConn = StoreConnection.connectExisting(newLocation) ;
        if ( sConn != null )
            throw new TDBException("Attempt to clone into live location") ;
        
        StoreParams storeParams = dsgBase.getConfig() ;
        // Force 
        StoreConnection.make(newLocation, storeParams) ;
        DatasetGraphTDB dsg2 = (DatasetGraphTDB)TDB2Factory.connectDatasetGraph(newLocation) ;
        Txn.executeRead(dsgBase, () -> { 
            Txn.executeWrite(dsg2, () -> {
                Iterator<Quad> iter = dsgBase.find();
                iter.forEachRemaining(dsg2::add);
            });});
        return dsg2 ;
    }

    /** Attemnp to copy indexes and nodes raw */ 
    // Does not work for in-memory datasets
    public static DatasetGraphTDB cloneDataset(DatasetGraphTDB dsgBase, Location newLocation) {
            if ( dsgBase.getLocation().equals(newLocation) )
                throw new TDBException("Attempt to clone into same location") ;
            if ( newLocation.isMem() || dsgBase.getLocation().isMem() ) {
                // Need to do the safe way. 
                return cloneDatasetSimple(dsgBase, newLocation) ;
            }
                
            // Assume copy to disk.
            if ( ! newLocation.isMem() && ! FileOps.isEmpty(newLocation.getDirectoryPath()) )
                throw new TDBException("Attempt to clone into existing location") ;
            
            TDBDatasetDetails details = new TDBDatasetDetails(dsgBase) ;
            
            StoreParams storeParams = dsgBase.getConfig() ;
            for ( TupleIndex idx : details.tripleIndexes ) {
                // Clone
                BPlusTree bpt = (BPlusTree)((TupleIndexRecord)idx).getRangeIndex() ;
                FileSet fs = new FileSet(newLocation, idx.getName()) ;
                /* bulk writer */
                cloneBPlusTree(dsgBase.getTxnSystem(), bpt, fs, storeParams) ;
            }
            for ( TupleIndex idx : details.quadIndexes ) {
                // Clone
                BPlusTree bpt = (BPlusTree)((TupleIndexRecord)idx).getRangeIndex() ;
                FileSet fs = new FileSet(newLocation, idx.getName()) ;
                /* bulk writer */
                cloneBPlusTree(dsgBase.getTxnSystem(), bpt, fs, storeParams) ;
           }
            
            
            // NodeTable (index copy only).
            // ** COPY DATA
            
            {
                BinaryDataFile bdf = ((NodeTableTRDF)details.ntBase).getData() ;
                // UGLY
                FileSet fs = new FileSet(newLocation, storeParams.getNodeTableBaseName()+"-data") ;
                BinaryDataFile bdf2 = FileFactory.createBinaryDataFile(fs, Names.extObjNodeData) ;
                bdf2.open(); 
                // Could do better!
                Txn.executeRead(dsgBase.getTxnSystem(), ()->{
                    final int N = 1024*1024*10 ;
                    byte[] buff = new byte[1024*1024*10] ;
                    long addr = 0 ;
                    for ( ;;) {
                        int x = bdf.read(addr, buff) ;
                        if ( x <= 0 )
                            break ;
                        bdf2.write(buff,0,x) ;
                        addr += x ;
                        if ( x < N )
                            break ;
                    }
                }) ;
                bdf2.close() ;
            }
    //        String srcFile = dsgBase.get
    //        FileOps.copyFile(, null);
            
            
            Index ntIdx = ((NodeTableTRDF)details.ntBase).getIndex() ;
            FileSet fs = new FileSet(newLocation, storeParams.getNodeTableBaseName()) ;
            cloneBPlusTree(dsgBase.getTxnSystem(), (BPlusTree)ntIdx,  fs, storeParams) ;
            
            // Sync all files!
            return (DatasetGraphTDB)TDBBuilder.build(newLocation, storeParams) ;
        }

    public static BPlusTree cloneBPlusTree(TransactionalSystem transactionalSystem, BPlusTree bpt, FileSet fs, StoreParams storeParams) {
            BPlusTreeParams params = bpt.getParams() ;
            RecordFactory rf = bpt.getRecordFactory() ;
            BPlusTree bpt2 = (BPlusTree)BPlusTreeFactory.makeBPlusTree(bpt.getComponentId(),
                                                                       fs,
                                                                       storeParams.getBlockSize(),
                                                                       storeParams.getBlockReadCacheSize(),
                                                                       storeParams.getBlockWriteCacheSize(),
                                                                       rf.keyLength(),
                                                                       rf.valueLength()) ;
    //        Transactional transactional = null ;
    //        Txn.execWrite(transactional, ()->bpt.forEach(bpt2::insert)) ;
            
    
            bpt2.nonTransactional();
            Txn.executeRead(transactionalSystem, ()->{
                /* bulk writer */
                bpt.forEach(bpt2::insert) ;
            });
            bpt2.close();
            return bpt2 ;
        }

}

