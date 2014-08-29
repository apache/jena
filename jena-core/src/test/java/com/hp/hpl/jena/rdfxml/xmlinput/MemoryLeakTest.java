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

package com.hp.hpl.jena.rdfxml.xmlinput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdfxml.xmlinput.NTriple ;

public class MemoryLeakTest extends TestCase {

    static public Test suite() {
        TestSuite suite = new TestSuite("ARP Memory Leak");

        suite.addTest(new MemoryLeakTest("testWineMemoryLeak"));
        return suite;
    }


    public MemoryLeakTest(String arg0) {
        super(arg0);
    }
    
    public void testWineMemoryLeak() {
        // warmup
        Runtime rt = Runtime.getRuntime();
        loadFile("testing/wg/miscellaneous/consistent001.rdf");
        rt.gc();
        rt.gc();
        rt.gc();
        long inUse = rt.totalMemory() - rt.freeMemory();
        loadFile("testing/arp/wineRenamed.rdf");
        rt.gc();
        rt.gc();
        rt.gc();
        long leaked = rt.totalMemory() - rt.freeMemory() - inUse;
        System.err.println("Leaked: "+ leaked);
        
        
    }

    static void loadFile(String fileName) {
        PrintStream oldOut = System.out;
        try ( PrintStream out = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            });) {
            NTriple.mainEh(new String[] { "-b", "http://eg.org/", "-t", fileName }, null, null);
        } finally { System.setOut(oldOut); }
    }

}
