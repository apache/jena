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

package org.seaborne.dboe.engine.tdb;

import java.util.Iterator ;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.LinkedBlockingQueue ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.sparql.core.Var ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.access.AccessData ;
import org.seaborne.dboe.engine.access.AccessRows ;
import org.seaborne.dboe.engine.explain.Explain2 ;
import org.seaborne.dboe.engine.row.RowBase ;
import org.seaborne.tdb2.store.NodeId ;

public class AccessorParallel extends AccessorTDB {

    // Default: 0, MAX, 60L 
    //static ExecutorService execService = Executors.newCachedThreadPool() ;
    
//    static ExecutorService execService = new ThreadPoolExecutor(2, Integer.MAX_VALUE,
//                                                                600L, TimeUnit.SECONDS,
//                                                                new SynchronousQueue<Runnable>());
    
    static ExecutorService execService = Executors.newFixedThreadPool(2) ;
    
    public AccessorParallel(StorageTDB db) { 
        super(db) ;
    }

    static Row<NodeId> marker = new RowBase<>() ;
    
    static class WorkItem implements Runnable {
        private final PredicateObjectList<NodeId> predObjList ;
        private final Row<NodeId> row ;
        private final AccessorTDB accessor ;
        private final BlockingQueue<Row<NodeId>> output ;
        
        public WorkItem(PredicateObjectList<NodeId> predObjList,
                        Row<NodeId> row, 
                        AccessorTDB accessor,
                        final BlockingQueue<Row<NodeId>> output) {
            this.predObjList = predObjList ;
            this.row = row ;
            this.accessor = accessor ;
            this.output = output ; 
        }
        
        @Override
        public void run() {
            try {
                // Ground, for this row.
                PredicateObjectList<NodeId> predObjList3 = EngLib.substitute(predObjList, row) ;
                if ( predObjList3.getSubject().isVar() )
                    throw new InternalErrorException() ;
                // Do something.
                // Makes this directly drive the BlockingDeque out
                Iterator<Row<NodeId>> results = accessor.fetch(predObjList3) ;
                results = RowLib.mergeRows(results, row) ;
                for ( ; results.hasNext() ; ) {
                    Row<NodeId> row2 = results.next() ;
                    output.put(row2) ;
                }
                output.put(marker) ;
            } catch (InterruptedException ex) {
                ex.printStackTrace(); 
            }
        }
    }

    
//    @Override
//    public Iterator<Tuple<NodeId>> accessTuples(Tuple<NodeId> pattern) {
//        return base.accessTuples(pattern) ;
//    }
//    
//    @Override
//    public Iterator<Tuple<NodeId>> accessTuples(Tuple<NodeId> pattern) {
//        return null ;
//    }
//
//    @Override
//    public Iterator<Row<NodeId>> accessRows(Tuple<Slot<NodeId>> pattern) {
//        return null ;
//    }

    @Override
    protected Iterator<Row<NodeId>> fetchVarSubject(PredicateObjectList<NodeId> predObjs, NodeId g , Var var ) {
        // Parallel.
        return executeVarSubjectParallel(var, this, this, predObjs) ;
    }

    
    public final /*package*/ Iterator<Row<NodeId>> executeVarSubjectParallel(final Var varSubject,
                                                                             final AccessData<NodeId> dataAccessor,
                                                                             AccessRows<NodeId> accessor, 
                                                                             PredicateObjectList<NodeId> predObjList) {
        BlockingQueue<Row<NodeId>> output = new LinkedBlockingQueue<>() ;
        final Tuple<Slot<NodeId>> tuple = predObjList.createTupleSlot(0) ;
        final PredicateObjectList<NodeId> predObjList2 = predObjList.slice(1) ;
        Explain2.explain(Quack.quackExec, "  AccessOps.executeVarSubject$ : subject=%s %s",varSubject, tuple) ;
        // Match the subjects.
        Iterator<Row<NodeId>> rows = accessor.accessRows(tuple) ;
        int countRows = 0 ;
        for ( ; rows.hasNext() ; ) {
            Row<NodeId> row = rows.next() ;
            Explain2.explain(Quack.quackExec, "  executeVarSubjectParallel : %s", row) ;
            WorkItem item = new WorkItem(predObjList2, row, this, output) ;
            execService.execute(item);
            countRows++ ;
        }
        return wrapper(output, marker, countRows) ;
    }
    
    /** An iterator that runs until markerCount occurences of a marker are seen on a BlockingQueue */ 
    static <Z> Iterator<Z> wrapper(final BlockingQueue<Z> iterator, final Z marker, final int markerCount) {
        return new IteratorSlotted<Z>() {
            private boolean finished = false ;
            private int seenCount = 0 ;
            @Override
            protected Z moveToNext() {
                try {
                    while ( seenCount < markerCount ) {
                        Z item = iterator.take() ;
                        if ( item != marker )
                            return item ;
                        seenCount++ ;
                    }
                    finished = true ;
                    return null ;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null ;
                }
            }

            @Override
            protected boolean hasMore() { return !finished ; }
        } ;
    }
}
