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

package quack;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.sys.Names ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.tdb2.store.NodeId ;
import org.seaborne.tdb2.store.tupletable.TupleIndex ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Operations related to indexes - non-transactional */
public class IndexLib {
    
    static Logger log = LoggerFactory.getLogger(IndexLib.class) ;

    /** Choose the usual (system default) primary name */ 
    public static String choosePrimary(IndexRef idx) {
        int N = idx.getIndexName().length() ;
        if ( N == 3 )
            return Names.primaryIndexTriples ;
        else if ( N == 4 )         
            return Names.primaryIndexQuads ;
        else
            throw new InternalErrorException("Index length") ;
    }

    public static TupleIndex connect(Location location, String primaryIndexName, String indexOrder) {
        FmtLog.debug(log, "connect(%s, %s, %s)\n", location, primaryIndexName, indexOrder) ;
        return connect$(location, primaryIndexName, indexOrder, indexOrder) ;
    }

    public static TupleIndex connect(IndexRef indexRef, String primaryIndexName) {
        FmtLog.debug(log, "connect(%s, %s)\n", indexRef, primaryIndexName) ;
        return connect$(indexRef.getLocation(), primaryIndexName, indexRef.getIndexName(), indexRef.getBaseFileName()) ;
    }

    private static TupleIndex connect$(Location location, String primaryIndexName, String indexOrder, String indexName) {
        int recordLength = NodeId.SIZE * primaryIndexName.length() ;
        Journal journal = Journal.create(location) ;
        throw new NotImplemented("IndexLib.connect$") ;
        
        //return SetupTDB.makeTupleIndex(location, primaryIndexName, indexOrder, indexName, recordLength) ;
    }
//
//    public static void dumpRangeIndex(RangeIndex rIndex) {
//        Iterator<Record> rIter = rIndex.iterator() ;
//        for (; rIter.hasNext();) {
//            Record r = rIter.next() ;
//            System.out.println(r) ;
//        }
//    }
//
//    public static void dumpTupleIndex(TupleIndex index) {
//        System.out.println("Index: " + index.getName()) ;
//        Iterator<Tuple<NodeId>> iter = index.all() ;
//        for (; iter.hasNext();) {
//            Tuple<NodeId> tuple = iter.next() ;
//            System.out.println(tuple) ;
//        }
//    }
//
//    private static int tick = 100000 ;
//    private static int superTick = 10 ;
//    
//    // Add start/stop consistent formatting ops to ProgressLogger or
//    // subclass general ProgressLogger
//    public static void copyIndex(TupleIndex srcIndex, TupleIndex destIndex) {
//        copyIndex(null, srcIndex, destIndex) ;
//    }
//       
//    // This has index recording powers. 
//    public static void copyIndex(Logger log, TupleIndex srcIndex, TupleIndex destIndex) {
//        ProgressLogger progress = new ProgressLogger(log, "tuples", tick, superTick) ;
//        progress.startMessage();
//        progress.start(); 
//        Iterator<Tuple<NodeId>> srcIter = srcIndex.all() ;
//        for (; srcIter.hasNext();) {
//            Tuple<NodeId> tuple = srcIter.next() ;
//            destIndex.add(tuple) ;
//            progress.tick();
//        }
//        destIndex.sync() ;
//        progress.finish() ;
//        progress.finishMessage();
//    }
//    
//    private static TupleIndex find(TupleIndex[] indexes, String srcIndex) {
//        for (TupleIndex idx : indexes) {
//            // Index named by simple "POS"
//            if (idx.getName().equals(srcIndex))
//                return idx ;
//
//            // Index named by column mapping "SPO->POS"
//            // This is silly.
//            int i = idx.getMapping().getLabel().indexOf('>') ;
//            String name = idx.getMapping().substring(i + 1) ;
//            if (name.equals(srcIndex))
//                return idx ;
//        }
//        return null ;
//    }
//    
//    public boolean isTripleIndex(String name) {
//        if ( name == null ) return false ;
//        if ( name.length() != 3 ) return false ;
//        return name.contains("S") && name.contains("P") && name.contains("O") ;
//    }
//    
//    public boolean isQuadIndex(String name) {
//        if ( name == null ) return false ;
//        if ( name.length() != 4 ) return false ;
//        return name.contains("S") && name.contains("P") && name.contains("O") && name.contains("G") ;  
//    }
}

