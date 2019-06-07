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

package org.apache.jena.tdb2.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPrefixMappingTDBExtra
{
    @Before
    public void before() { TDBInternal.reset(); ConfigTest.deleteTestingDir(); }

    @After
    public void after() {
        TDBInternal.reset();
        ConfigTest.deleteTestingDir();
    }
    
    // Persistent.
    @Test
    public void persistent1() {
        String dir = ConfigTest.getTestingDir();
        FileOps.clearAll(dir);
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(Location.create(dir));
        Txn.execute(dsg, ()->{
            PrefixMapping pmap1 = dsg.getDefaultGraph().getPrefixMapping();
            String x = pmap1.getNsPrefixURI("x");
            assertNull(x);
        });
    }

    // Persistent.
    @Test
    public void persistent2() {
        String dir = ConfigTest.getTestingDir();
        FileOps.clearAll(dir);
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(Location.create(dir));
        PrefixMapping pmap1 = dsg.getDefaultGraph().getPrefixMapping();
        Txn.execute(dsg, ()->{
            pmap1.setNsPrefix("x", "http://foo/");
            assertEquals("http://foo/", pmap1.getNsPrefixURI("x"));
        });
        Txn.execute(dsg, ()->{
            assertEquals("http://foo/", pmap1.getNsPrefixURI("x"));
        });
        Txn.execute(dsg, ()->{
            PrefixMapping pmap2 = dsg.getDefaultGraph().getPrefixMapping();
            assertEquals("http://foo/", pmap2.getNsPrefixURI("x"));
        });
        
    }

}
