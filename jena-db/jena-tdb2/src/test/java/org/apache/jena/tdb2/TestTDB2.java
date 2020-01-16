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

package org.apache.jena.tdb2;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.ThreadLib;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** Misc tests for TDB2. */
public class TestTDB2 {
    // Safe on MS Windows - different directories for abort1 and abort2.
    static String DIR1 = "DB_1";
    static String DIR2 = "DB_2";

    @BeforeClass public static void beforeClass() {
        FileOps.ensureDir(DIR1);
        FileOps.ensureDir(DIR2);
    }
    
    @AfterClass public static void afterClass() {
        try { 
            FileOps.clearAll(DIR1);
            FileOps.clearAll(DIR2);
            FileOps.deleteSilent(DIR1);
            FileOps.deleteSilent(DIR2);
        } catch (Exception ex) {}
    }

    // JENA-1746 : tests abort1 and abort2.
    // Inlines are not relevant.
    // Errors that can occur:
    //   One common term -> no conversion.
    //   Two common terms -> bad read.

    @Test public void abort1() {
        Quad q1 = SSE.parseQuad("(:g :s :p :o)");
        // One term different.
        Quad q2 = SSE.parseQuad("(:g1 :s :p 123)");
        testAbort(DIR1, q1, q2);
    }
    
    @Test public void abort2() {
        Quad q1 = SSE.parseQuad("(:g :s :p :o)");
        // Two terms different.
        Quad q2 = SSE.parseQuad("(:g1 :s :p1 123)");
        testAbort(DIR2, q1, q2);
    }
        
    private void testAbort(String DIR, Quad q1, Quad q2) {
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(DIR);
        
        // Abort.
        dsg.begin(TxnType.WRITE);
        dsg.add(q1);
        dsg.abort();
        dsg.end();

        // Add different data.
        dsg.begin(TxnType.WRITE);
        dsg.add(q2);
        dsg.commit();
        dsg.end();

        output(dsg);
        TDBInternal.expel(dsg, true);
        DatasetGraph dsg2 = DatabaseMgr.connectDatasetGraph(DIR);
        output(dsg2);
    }

    private static void output(DatasetGraph dsg) {
        Txn.executeRead(dsg, ()->RDFDataMgr.write(new ByteArrayOutputStream(),  dsg, Lang.NQUADS));
    }
    
    //JENA-1817: Two W txn, where the second queues on entry.  
    @Test public void multiple_writers() {
        Quad q1 = SSE.parseQuad("(:g :s :p :o1)");
        Quad q2 = SSE.parseQuad("(:g :s :p :o2)");
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        
        // Test controls
        Semaphore sema =  new Semaphore(0);
        Semaphore semaTestFinished =  new Semaphore(0);
        
        // Setup writers.
        Runnable r1 = ()->{
            Txn.executeWrite(dsg,  ()->{
                // Allow thread 2 run and try to enter the W txn 
                sema.release(1);
                dsg.add(q1);
                // Gives thread2 a chance to enter (can't do this by lock).
                // It is unfortunate that it's a timeout.
                Lib.sleep(250);
            });
            // Finished.
            semaTestFinished.release(1);
        };
        
        Runnable r2 = ()->{
            acquire(sema,1);
            // Thread 1 is now inside its W txn.
            Txn.executeWrite(dsg, () -> dsg.add(q2));
            semaTestFinished.release(1);
        };
        ThreadLib.async(r2);
        ThreadLib.async(r1);
        
        // Trigger writers. 
        sema.release(2);
        // Wait until test threads have finished
        acquire(semaTestFinished, 2);
    }
    
    private static void acquire(Semaphore semaphore, int permits) {
        try {
            boolean b = semaphore.tryAcquire(permits, 1000, TimeUnit.MILLISECONDS);
            if ( !b )
                throw new RuntimeException("Test failure - did not get permits in the time allowed");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
