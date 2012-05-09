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

package dev;

import junit.framework.TestSuite;

import com.hp.hpl.jena.sdb.test.graph.TestDerbyGraph;
import com.hp.hpl.jena.sdb.test.graph.TestDerbyGraph.TestDerbyIndexGraph;
import com.hp.hpl.jena.sdb.test.model.TestDerbyModel;
import com.hp.hpl.jena.sdb.test.model.TestDerbyModel.TestDerbyIndexModel;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner;

public class RunTests
{
    public static junit.framework.Test suite() {
        TestSuite ts = new TestSuite();
        
        ts.addTest(TestDerbyGraph.suite());
        ts.addTest(TestDerbyModel.suite());
        return ts;
    }
    
    
    public static void main(String ... argv)
    {
        TestSuite ts = new TestSuite();
        ts.addTestSuite(TestDerbyIndexGraph.class) ;
        SimpleTestRunner.runAndReport(ts) ;
        
        ts = new TestSuite();
        ts.addTestSuite(TestDerbyIndexModel.class) ;
        SimpleTestRunner.runAndReport(ts) ;
    }
}
