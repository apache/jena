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

package org.seaborne.tdb2.junit;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetPrefixStorage ;
import org.seaborne.dboe.base.file.* ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.Index ;
import org.seaborne.dboe.index.IndexParams ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.tdb2.setup.StoreParams ;
import org.seaborne.tdb2.setup.TDBBuilder ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;
import org.seaborne.tdb2.store.nodetable.NodeTable ;
import org.seaborne.tdb2.store.nodetable.NodeTableCache ;
import org.seaborne.tdb2.store.nodetable.NodeTableInline ;
import org.seaborne.tdb2.store.nodetable.NodeTableTRDF ;
import org.seaborne.tdb2.sys.SystemTDB ;

/** Build things for non-transactional tests.
 * Sometimes, create a daatset and find the relevant part. 
 */
public class BuildTestLib {

    public static RangeIndex buildRangeIndex(FileSet mem, RecordFactory factory, IndexParams indexParams) {
        BPlusTree bpt = BPlusTreeFactory.makeMem(5, factory.keyLength(), factory.valueLength()) ;
        bpt.nonTransactional() ;
        return bpt ; 
    }

    public static NodeTable makeNodeTable(Location location, String basename, StoreParams params) {
        NodeTable nt = makeNodeTableBase(location, basename, params) ;
        nt = NodeTableCache.create(nt, params) ;
        nt = NodeTableInline.create(nt) ;
        return nt ;
    }
    
    public static NodeTable makeNodeTableBase(Location location, String basename, StoreParams params) {
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
        FileSet fs = new FileSet(location, basename) ;
                
        Index index = buildRangeIndex(fs, recordFactory, params) ;
        BinaryDataFile bdf = createBinaryDataFile(location, basename+"-data") ;
        NodeTable nt = new NodeTableTRDF(index, bdf) ;
        return nt ;
    }

    public static DatasetPrefixStorage makePrefixes(Location location) {
        DatasetGraphTDB ds = (DatasetGraphTDB)TDBBuilder.build(location) ;
        ds.begin(ReadWrite.WRITE);
        return ds.getPrefixes() ; 
    }
    
    /** Create a non-thread-safe BinaryDataFile*/ 
    public static BinaryDataFile createBinaryDataFile(Location loc, String name) {
        if ( loc.isMem() )
            return new BinaryDataFileMem() ;
        String filename = loc.getPath(name) ;
        return new BinaryDataFileRandomAccess(filename) ;
    }

}

