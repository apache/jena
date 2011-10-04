/**
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

package com.hp.hpl.jena.tdb.graph;

import java.util.Map ;

import org.junit.Test ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.graph.AbstractTestPrefixMapping2 ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.sys.DatasetControlMRSW ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestPrefixMappingTDB extends AbstractTestPrefixMapping2
{
    static DatasetPrefixesTDB last = null ;
    
    @Override
    protected PrefixMapping create()
    {
        last = DatasetPrefixesTDB.testing() ;
        return view() ;
    }

    @Override
    protected PrefixMapping view()
    {
        return last.getPrefixMapping() ; 
    }

    @Test public void multiple1()
    {
        DatasetPrefixesTDB prefixes = DatasetPrefixesTDB.testing() ;
        PrefixMapping pmap1 = prefixes.getPrefixMapping() ;
        PrefixMapping pmap2 = prefixes.getPrefixMapping("http://graph/") ;
        pmap1.setNsPrefix("x", "http://foo/") ;
        assertNull(pmap2.getNsPrefixURI("x")) ;
        assertNotNull(pmap1.getNsPrefixURI("x")) ;
    }
    
    @Test public void multiple2()
    {
        DatasetPrefixesTDB prefixes = DatasetPrefixesTDB.testing() ;
        PrefixMapping pmap1 = prefixes.getPrefixMapping("http://graph/") ;  // Same
        PrefixMapping pmap2 = prefixes.getPrefixMapping("http://graph/") ;
        pmap1.setNsPrefix("x", "http://foo/") ;
        assertNotNull(pmap2.getNsPrefixURI("x")) ;
        assertNotNull(pmap1.getNsPrefixURI("x")) ;
    }
    
    // Persistent.
    @SuppressWarnings("deprecation")
    @Test public void persistent1()
    {
        String dir = ConfigTest.getTestingDir() ;
        FileOps.clearDirectory(dir) ;
        
        DatasetPrefixesTDB prefixes = DatasetPrefixesTDB.create(new Location(dir), new DatasetControlMRSW()) ;
        PrefixMapping pmap1 = prefixes.getPrefixMapping() ;
        
        String x = pmap1.getNsPrefixURI("x") ;
        assertNull(x) ;
        prefixes.close();
    }
    
    // Persistent.
    @SuppressWarnings("deprecation")
    @Test public void persistent2()
    {
        String dir = ConfigTest.getTestingDir() ;
        FileOps.clearDirectory(dir) ;
        
        DatasetPrefixesTDB prefixes = DatasetPrefixesTDB.create(new Location(dir), new DatasetControlMRSW()) ;
        PrefixMapping pmap1 = prefixes.getPrefixMapping() ;
        
        pmap1.setNsPrefix("x", "http://foo/") ;
        prefixes.close() ;
        
        prefixes = DatasetPrefixesTDB.create(new Location(dir), new DatasetControlMRSW()) ;
        assertEquals("http://foo/", pmap1.getNsPrefixURI("x")) ;
        prefixes.close();
    }
    
    @Test public void persistent3()
    {
        // Test case from a report by Holger Knublauch
        if ( false )
        {
            //TDB.getContext().set(SystemTDB.symFileMode, "mapped") ;
            TDB.getContext().set(SystemTDB.symFileMode, "direct") ;
        }
        String DB = ConfigTest.getTestingDirDB() ;
        FileOps.clearDirectory(DB) ;
        {
            // Create new DB (assuming it's empty now)
            Graph graph = TDBFactory.createGraph(DB);
            PrefixMapping pm = graph.getPrefixMapping();
            pm.setNsPrefix("test", "http://test");
            graph.close();
        }

        {
            // Reconnect to the same DB
            Graph graph = TDBFactory.createGraph(DB);
            PrefixMapping pm = graph.getPrefixMapping();
            Map<String, String> map = pm.getNsPrefixMap();
            assertEquals(1, map.size()) ;
            //System.out.println("Size: " + map.size());
            String ns = pm.getNsPrefixURI("test");
            //System.out.println("Namespace: " + ns);
            assertEquals("http://test", ns) ;
            graph.close();
        }
    }
}
