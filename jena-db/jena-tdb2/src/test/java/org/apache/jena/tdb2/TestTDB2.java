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

import org.apache.jena.atlas.lib.FileOps;
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
    // Feature control: ThreadBufferingCache.BUFFERING

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
}
