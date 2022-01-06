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

package org.apache.jena.tdb.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.graph.AbstractTestPrefixMappingView ;
import org.apache.jena.tdb.ConfigTest ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.setup.Build ;
import org.apache.jena.tdb.store.DatasetPrefixesTDB ;
import org.apache.jena.tdb.sys.DatasetControl ;
import org.apache.jena.tdb.sys.DatasetControlMRSW ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.jena.tdb.sys.TDBInternal;
import org.junit.* ;

public class TestPrefixMappingTDB1 extends AbstractTestPrefixMappingView
{
    static DatasetPrefixesTDB last = null ;

    @BeforeClass public static void beforeClass() {}
    @AfterClass public static void afterClass()   { TDBInternal.reset() ; ConfigTest.deleteTestingDir() ; }

    @Before public void before() { TDBInternal.reset() ; create(); }
    @After public  void after()  { }

    @Override
    protected PrefixMapping create() {
        last = createTestingMem() ;
        return view() ;
    }

    static DatasetPrefixesTDB createTestingMem() {
        return createTesting(Location.mem(), new DatasetControlMRSW()) ;
    }

    static DatasetPrefixesTDB createTesting(Location location, DatasetControl policy) {
        return Build.makePrefixes(location, policy) ;
    }

    @Override
    protected PrefixMapping view() {
        return Prefixes.adapt(last.getPrefixMap()) ;
    }

    protected PrefixMapping getPrefixMapping() {
        return Prefixes.adapt(last.getPrefixMap());
    }

    protected PrefixMapping getPrefixMapping(String graphName) {
        return Prefixes.adapt(last.getPrefixMap(graphName));
    }

    @Test public void multiple1() {
        DatasetPrefixesTDB prefixes = createTestingMem() ;
        PrefixMapping pmap1 = getPrefixMapping() ;
        PrefixMapping pmap2 = getPrefixMapping("http://graph/") ;
        pmap1.setNsPrefix("x", "http://foo/") ;
        assertNull(pmap2.getNsPrefixURI("x")) ;
        assertNotNull(pmap1.getNsPrefixURI("x")) ;
    }

    @Test public void multiple2() {
        DatasetPrefixesTDB prefixes = createTestingMem() ;
        PrefixMapping pmap1 = getPrefixMapping("http://graph/") ;  // Same
        PrefixMapping pmap2 = getPrefixMapping("http://graph/") ;
        pmap1.setNsPrefix("x", "http://foo/") ;
        assertNotNull(pmap2.getNsPrefixURI("x")) ;
        assertNotNull(pmap1.getNsPrefixURI("x")) ;
    }

    // Persistent.
    @Test
    public void persistent1() {
        String dir = ConfigTest.getTestingDir() ;
        FileOps.clearDirectory(dir) ;

        DatasetPrefixesTDB prefixes = createTesting(Location.create(dir), new DatasetControlMRSW()) ;
        PrefixMapping pmap1 = getPrefixMapping() ;

        String x = pmap1.getNsPrefixURI("x") ;
        assertNull(x) ;
        prefixes.close() ;
    }

    // Persistent.
    @Test
    public void persistent2() {
        String dir = ConfigTest.getTestingDir() ;
        FileOps.clearDirectory(dir) ;

        DatasetPrefixesTDB prefixes = createTesting(Location.create(dir), new DatasetControlMRSW()) ;
        PrefixMapping pmap1 = getPrefixMapping() ;

        pmap1.setNsPrefix("x", "http://foo/") ;
        prefixes.close() ;

        prefixes = createTesting(Location.create(dir), new DatasetControlMRSW()) ;
        PrefixMapping pmap2 = getPrefixMapping() ;
        String uri = pmap2.getNsPrefixURI("x");
        assertEquals("http://foo/", uri) ;
        prefixes.close() ;
    }

    @Test
    public void persistent3() {
        // Test case from a report by Holger Knublauch
        if ( false ) {
            // TDB.getContext().set(SystemTDB.symFileMode, "mapped") ;
            TDB.getContext().set(SystemTDB.symFileMode, "direct") ;
        }
        String DB = ConfigTest.getCleanDir() ;
        {
            // Create new DB (assuming it's empty now)
            Graph graph = TDBFactory.createDatasetGraph(DB).getDefaultGraph() ;
            PrefixMapping pm = graph.getPrefixMapping() ;
            pm.setNsPrefix("test", "http://test") ;
            graph.close() ;
        }

        {
            // Reconnect to the same DB
            Graph graph = TDBFactory.createDatasetGraph(DB).getDefaultGraph() ;
            PrefixMapping pm = graph.getPrefixMapping() ;
            Map<String, String> map = pm.getNsPrefixMap() ;
            assertEquals(1, map.size()) ;
            // System.out.println("Size: " + map.size());
            String ns = pm.getNsPrefixURI("test") ;
            // System.out.println("Namespace: " + ns);
            assertEquals("http://test", ns) ;
            graph.close() ;
        }
        FileOps.deleteSilent(DB) ;
    }
}
