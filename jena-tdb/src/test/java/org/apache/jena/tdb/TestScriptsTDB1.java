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

package org.apache.jena.tdb;

import junit.framework.TestSuite ;
import org.apache.jena.tdb.junit.TestFactoryTDB ;
import org.junit.runner.RunWith ;
import org.junit.runners.AllTests ;

/** Scripted test generation */

@RunWith(AllTests.class)
public class TestScriptsTDB1 extends TestSuite
{
    static final String ARQ_DIR = "../jena-arq/testing/ARQ";

    static public TestSuite suite() { return new TestScriptsTDB1() ; }

    private TestScriptsTDB1()
    {
        super("TDB-Scripts") ;
        String manifestMain1 = ConfigTest.getTestingDataRoot()+"/manifest.ttl" ;
        TestFactoryTDB.make(this, manifestMain1, "TDB-") ;

        // From ARQ
        String manifestMain2 = ARQ_DIR + "/RDF-Star/SPARQL-Star/manifest.ttl";
        TestFactoryTDB.make(this, manifestMain2, "TDB-");
    }
}
