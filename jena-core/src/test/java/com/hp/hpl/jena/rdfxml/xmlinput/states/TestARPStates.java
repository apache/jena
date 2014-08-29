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

package com.hp.hpl.jena.rdfxml.xmlinput.states;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestARPStates extends TestCase {
    public TestARPStates() {
        super();
    }

    public static TestSuite suite() {
        TestSuite rslt = new TestSuite();
        rslt.setName("ARP state machine");
        Map<String, TestSuite> tests = new HashMap<>();
        try ( LineNumberReader r = new LineNumberReader(new FileReader(TestData.dataFile)) ) {
            while (true) {
                String line = r.readLine();
                if (line == null)
                    return rslt;
                int hash = line.indexOf('%');
                line = (hash==-1?line:line.substring(0,hash)).trim();
                String fields[] = line.split("  *");
                if (fields.length==0)
                    continue;
                TestSuite child = tests.get(fields[0]);
                if (child==null) {
                    child = new TestSuite();
                    child.setName(TestData.stateLongName(fields[0]));
                    rslt.addTest(child);
                    tests.put(fields[0],child);
                }
                child.addTest(TestEventList.create(line,fields));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return rslt;
        }
    }
}
