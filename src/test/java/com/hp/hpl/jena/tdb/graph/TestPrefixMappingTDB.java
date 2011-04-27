/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicyMRSW ;
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
        
        DatasetPrefixesTDB prefixes = DatasetPrefixesTDB.create(new Location(dir), new ConcurrencyPolicyMRSW()) ;
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
        
        DatasetPrefixesTDB prefixes = DatasetPrefixesTDB.create(new Location(dir), new ConcurrencyPolicyMRSW()) ;
        PrefixMapping pmap1 = prefixes.getPrefixMapping() ;
        
        pmap1.setNsPrefix("x", "http://foo/") ;
        prefixes.close() ;
        
        prefixes = DatasetPrefixesTDB.create(new Location(dir), new ConcurrencyPolicyMRSW()) ;
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */