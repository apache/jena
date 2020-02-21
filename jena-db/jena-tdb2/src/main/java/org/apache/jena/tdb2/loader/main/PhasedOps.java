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

package org.apache.jena.tdb2.loader.main;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.tdb2.loader.BulkLoaderException;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.loader.base.ProgressMonitor;
import org.apache.jena.tdb2.loader.base.ProgressMonitorFactory;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

/**
 * Library of operations used by {@link LoaderMain}.
 */
class PhasedOps {

    /** Acquire one permit from a semaphore. Return the time spent waiting. */
    /* package */ static long acquire(Semaphore termination) {
        return acquire(termination, 1);
    }

    /** Acquire permits from a semaphore. Return the time spent waiting. */
    /* package */ static long acquire(Semaphore semaphore, int numPermits) {
        return Timer.time(()->{
            try { semaphore.acquire(numPermits); }
            catch (InterruptedException e) {
                Log.error(Indexer.class, "Interrupted", e);
                throw new RuntimeException(e);
            }
        });
    }

    static Map<String, TupleIndex> indexMap(DatasetGraphTDB dsgtdb) {
        Map<String, TupleIndex> indexMap = new HashMap<>();
        // All triple/quad indexes.
        Arrays.stream(dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes())
              .forEach(idx->indexMap.put(idx.getName(), idx));
        Arrays.stream(dsgtdb.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes())
              .forEach(idx->indexMap.put(idx.getName(), idx));
        return indexMap;
    }

    static TupleIndex[][] indexSetsFromNames(String[][] indexNames, Map<String, TupleIndex> indexMap) {
        // Bad error message!
        //return deepMap(indexNames, indexMap::get, TupleIndex[]::new, TupleIndex[][]::new);
        TupleIndex[][] z = Arrays.stream(indexNames)
            .map(indexSetNames->indexSetFromNames(indexSetNames, indexMap))
            .toArray(TupleIndex[][]::new);
        return z;
    }

    static TupleIndex[] indexSetFromNames(String[] indexNames, Map<String, TupleIndex> indexMap) {
        return Arrays.stream(indexNames)
            .map(name-> findInIndexMap(name, indexMap))
            .toArray(TupleIndex[]::new);
    }

    static TupleIndex findInIndexMap(String name,Map<String, TupleIndex> indexMap) {
        TupleIndex tIdx = indexMap.get(name);
        if ( tIdx == null )
            throw new IllegalArgumentException("No such index: "+name);
        return tIdx;
    }

    /** Check the loader plan makes sense. */
    private static void checkLoaderPlan(LoaderPlan loaderPlan, Map<String, TupleIndex> indexMap) {
        Consumer<String> checker3 = name -> {
            if ( name == null ) throw new BulkLoaderException("Null index name");
            if ( name.length() != 3 ) throw new BulkLoaderException("Bad length (expected 3): "+name);
            if ( !indexMap.containsKey(name) ) throw new BulkLoaderException("No such index: "+name);
        };
        Consumer<String> checker4 = name -> {
            if ( name == null ) throw new BulkLoaderException("Null index name");
            if ( name.length() != 4 ) throw new BulkLoaderException("Bad length (expected 4): "+name);
            if ( !indexMap.containsKey(name) ) throw new BulkLoaderException("No such index: "+name);
        };

        // -- Checking for nulls and bad index names.
        arrayApply1(loaderPlan.primaryLoad3(), checker3);
        arrayApply1(loaderPlan.primaryLoad4(), checker4);

        arrayApply2(loaderPlan.secondaryIndex3(), checker3);
        arrayApply2(loaderPlan.secondaryIndex4(), checker4);

        // -- Checking for duplicates
        checkUnique("Primary triples",loaderPlan.primaryLoad3());
        checkUnique("Primary quads", loaderPlan.primaryLoad4());

        String[] secondary3 = flatten(loaderPlan.secondaryIndex3(), String[]::new);
        String[] secondary4 = flatten(loaderPlan.secondaryIndex4(), String[]::new);

        checkUnique("Secondary triples", secondary3);
        checkUnique("Secondary quads", secondary4);
    }

    private static <X> void checkUnique(String errorMessage, X[] array) {
        Set<X> set = new HashSet<>();
        for ( X x : array ) {
            if ( set.contains(x) )
                throw new BulkLoaderException(errorMessage+" : Not unique: "+x);
            set.add(x);
        }
    }

    /** Indexes to a list of mappings suitable as a label  */
    /*package*/ static String indexMappings(TupleIndex[] indexes) {
        StringJoiner sj = new StringJoiner(", ");
        Arrays.stream(indexes).map(x->x.getMappingStr()).forEach(str->sj.add(str));
        return sj.toString();
    }

    // Hide java noise with specific function implementations of some operations.

    private static <X> void arrayApply2(X[][] array, Consumer<X> action) {
        if ( array == null )
            return;
        for ( X[] lines : array ) {
            for ( X item : lines ) {
                action.accept(item);
            }
        }
    }

    private static <X> void arrayApply1(X[] array, Consumer<X> action) {
        if ( array == null )
            return;
        for ( X item : array ) {
            action.accept(item);
        }
    }

    private static <X> X[] flatten(X[][] array, IntFunction<X[]> generator) {
        return flatten(array).toArray(generator);
    }

    private static <X> Stream<X> flatten(X[][] array) {
        if ( array == null )
            return null;
        return Arrays.stream(array).flatMap(Arrays::stream);
    }

    static class ReplayResult {
        final long items;
        final long elapsed;
        ReplayResult(long items, long timeInMs) {
            this.items = items;
            this.elapsed = timeInMs;
        }
    }

    /** Return (Number, Time in ms) */

    static ReplayResult replay(TupleIndex srcIdx, Destination<Tuple<NodeId>> dest, MonitorOutput output) {
        ProgressMonitor monitor =
            ProgressMonitorFactory.progressMonitor("Index", output, LoaderMain.IndexTickPoint, LoaderMain.IndexSuperTick);

        List<Tuple<NodeId>> block = null;

        int len = srcIdx.getTupleLength();

        monitor.start();
        Iterator<Tuple<NodeId>> iter = srcIdx.all();
        while (iter.hasNext()) {
            if ( block == null )
                block = new ArrayList<>(LoaderConst.ChunkSize);
            Tuple<NodeId> row = iter.next();
            block.add(row);
            monitor.tick();
            if ( block.size() == LoaderConst.ChunkSize ) {
                dest.deliver(block);
                block = null;
            }
        }
        if ( block != null )
            dest.deliver(block);
        dest.deliver(Collections.emptyList());
        monitor.finish();
        //monitor.finishMessage("Tuples["+len+"]");
        return new ReplayResult(monitor.getTicks(), monitor.getTime());
    }
}
