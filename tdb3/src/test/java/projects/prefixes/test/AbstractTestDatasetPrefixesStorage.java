/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package projects.prefixes.test;


import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.junit.Test ;
import projects.prefixes.DatasetPrefixesStorage2;
import projects.prefixes.PrefixEntry;

public abstract class AbstractTestDatasetPrefixesStorage extends BaseTest
{
    /** Create a fresh PrefixMapping */
    protected abstract DatasetPrefixesStorage2 create() ;
    /** Create a fresh view over the same storage as last create() */
    protected abstract DatasetPrefixesStorage2 view() ;
    
    protected Node g1 = NodeFactory.createURI("http://example.org/g1") ;
    protected Node g2 = NodeFactory.createURI("http://example.org/g2") ;
    protected String pref1 = "pref1" ;
    protected String pref1a = "pref1:" ;
    protected String pref2 = "pref2" ;
    
    @Test public void dsg_prefixes_01()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;
    }
    
    @Test public void dsg_prefixes_02()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;
        String x1 = prefixes.get(g1, pref1) ;
        assertEquals("http://example.net/ns#", x1) ;
        String x2 = prefixes.get(g1, pref1a) ;
        assertEquals("http://example.net/ns#", x2) ;
    }
    
    @Test public void dsg_prefixes_03()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;
        String x1 = prefixes.get(g2, pref1) ;
        assertNull(x1) ;
    }
    
    @Test public void dsg_prefixes_04()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;
        prefixes.delete(g1, pref1) ;
        String x1 = prefixes.get(g1, pref1) ;
        assertNull(x1) ;
    }

    // abbreviate
    
    @Test public void dsg_prefixes_05()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;

        String x = prefixes.abbreviate(g1, "http://example.net/ns#xyz") ;
        assertEquals("pref1:xyz", x) ;
    }

    @Test public void dsg_prefixes_06()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;

        String x = prefixes.abbreviate(g1, "http://other/ns#xyz") ;
        assertNull(x) ;
    }
    
    // abbrev

    @Test public void dsg_prefixes_07()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;

        Pair<String,String> x = prefixes.abbrev(g1, "http://example.net/ns#xyz") ;
        
        assertEquals("pref1", x.getLeft()) ;
        assertEquals("xyz", x.getRight()) ;
    }

    @Test public void dsg_prefixes_08()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;
        Pair<String,String> x = prefixes.abbrev(g1, "http://other/ns#xyz") ;
        assertNull(x) ;
    }

    // expand[graph]/1
    @Test public void dsg_prefixes_09()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;

        String x = prefixes.expand(g1, "pref1:abc") ;
        assertEquals("http://example.net/ns#abc", x) ;
        String x2 = prefixes.expand(g2, "pref1:abc") ;
        assertNull(x2) ;

    }

    // expand[graph]/2
    @Test public void dsg_prefixes_10()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;
        String x = prefixes.expand(g1, "pref1", "abc") ; 
        assertEquals("http://example.net/ns#abc", x) ;
        String x2 = prefixes.expand(g2, "pref1", "abc") ;
        assertNull(x2) ;
    }
    
    // Accessors
    @Test public void dsg_prefixes_11()
    {
        DatasetPrefixesStorage2 prefixes = create() ;
        prefixes.add(g1, pref1, "http://example.net/ns#") ;
        
        List<Node> x = Iter.toList(prefixes.listGraphNodes()) ;
        assertEquals(1, x.size()) ;
        
        List<PrefixEntry> y = Iter.toList(prefixes.get(g1)) ;
        assertEquals(1, y.size()) ;
    }

}

