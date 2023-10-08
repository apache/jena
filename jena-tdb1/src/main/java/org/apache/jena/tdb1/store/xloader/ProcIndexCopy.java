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

package org.apache.jena.tdb1.store.xloader;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.setup.Build;
import org.apache.jena.tdb1.store.NodeId;
import org.apache.jena.tdb1.store.tupletable.TupleIndex;
import org.apache.jena.tdb1.sys.SystemTDB;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Copy one index to another, probably with a different key order */
public class ProcIndexCopy
{
    private static Logger log = LoggerFactory.getLogger(ProcIndexCopy.class) ;

    static long tickQuantum = 100_000 ;
    static int superTick = 10 ;

    // Ideas:
    // Copy to buffer, sort, write in sequential clumps.
    // Profile code for hotspots

    // Maybe be worth opening the data file (the leaves) as a regular,
    // non-memory mapped file as we read it through once, in natural order,
    // and it may be laid out in increasing block order on-disk, e.g. repacked
    // and in increasing order with occassional oddities if SPO from the bulk loader.

    public static void exec(String locationStr1, String indexName1, String locationStr2, String indexName2) {
        // Argument processing

        Location location1 = Location.create(locationStr1) ;
        Location location2 = Location.create(locationStr2) ;

        int keyLength = SystemTDB.SizeOfNodeId * indexName1.length() ;
        int valueLength = 0 ;

        // The name is the order.
        String primary = "SPO" ;

        String indexOrder = indexName2 ;
        String label = indexName1+" => "+indexName2 ;

        TupleIndex index1 = Build.openTupleIndex(location1, indexName1, primary, indexName1, 10, 10, keyLength, valueLength) ;
        TupleIndex index2 = Build.openTupleIndex(location2, indexName2, primary, indexOrder, 10, 10, keyLength, valueLength) ;
        tupleIndexCopy(index1, index2, label) ;
        index1.close() ;
        index2.close() ;
    }

    private static void tupleIndexCopy(TupleIndex index1, TupleIndex index2, String label) {
        ProgressMonitor monitor = ProgressMonitorOutput.create(log, label, tickQuantum, superTick);
        Timer timer = new Timer();
        timer.startTimer();
        monitor.start();

        Iterator<Tuple<NodeId>> iter1 = index1.all();

        long counter = 0;
        for ( ; iter1.hasNext() ; ) {
            counter++;
            Tuple<NodeId> tuple = iter1.next();
            index2.add(tuple);
            monitor.tick();
        }

        index2.sync();
        monitor.finish();
        long time = timer.endTimer();
        float elapsedSecs = time / 1000F;

        float rate = (elapsedSecs != 0) ? counter / elapsedSecs : 0;

        print("Total: %,d records : %,.2f seconds : %,.2f records/sec [%s]", counter, elapsedSecs, rate, DateTimeUtils.nowAsString());
    }

    static private void print(String fmt, Object... args) {
        if ( log != null && log.isInfoEnabled() ) {
            String str = String.format(fmt, args);
            log.info(str);
        }
    }
}
