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

package com.hp.hpl.jena.sparql.graph;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.shared.PrefixMapping ;

public abstract class AbstractTestPrefixMapping2 extends BaseTest
{
    static final String defaultPrefixURI  = "" ;

    /** Create a fresh PrefixMapping */
    protected abstract PrefixMapping create() ;
    /** Create a fresh view over the same storage as last create() */
    protected abstract PrefixMapping view() ; 
    
    @Test public void prefix1()
    {
        PrefixMapping pmap = create() ;
    }
    
    @Test public void prefix2()
    {
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", "http://example/") ;
        assertNotNull(pmap.getNsPrefixURI("ex")) ;
    }
    
    @Test public void prefix3()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", uri) ;
        
        // Create a second view onto the same storage.
        
        PrefixMapping pmap2 = view() ;
        String x = pmap2.getNsPrefixURI("ex") ;
        
        assertNotNull(x) ;
        assertEquals(uri,x) ;
    }
        
    @Test public void prefix4()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals("ex", pmap.getNsURIPrefix("http://example/")) ;
    }
        
    @Test public void prefix5()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals(uri+"foo", pmap.expandPrefix("ex:foo")) ;
    }

    @Test public void prefix6()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals("ex:foo", pmap.qnameFor("http://example/foo")) ;
    }

    @Test public void prefix7()
    {
        String uri1 = "http://example/" ;
        String uri2 = "http://example/ns#" ;
        
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex1", uri1) ;
        pmap.setNsPrefix("ex2", uri2) ;
        assertEquals("ex2:foo", pmap.qnameFor("http://example/ns#foo")) ;
    }
    
    @Test public void prefix8()
    {
        PrefixMapping pmap = create() ;
        String x = "scheme:i_do_not_exist" ;
        
        assertEquals(x, pmap.expandPrefix(x)) ;
        // Call again - used to cause problems. 
        assertEquals(x, pmap.expandPrefix(x)) ;
    }
    
}
