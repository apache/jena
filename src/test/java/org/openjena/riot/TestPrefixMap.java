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

package org.openjena.riot;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.system.PrefixMap ;

import com.hp.hpl.jena.iri.IRIFactory ;

public class TestPrefixMap extends BaseTest
{
    static IRIFactory factory = IRIFactory.iriImplementation() ; 
    
    @Test public void prefixMap1()
    {
        PrefixMap pmap = new PrefixMap() ;
        add(pmap, "", "http://example/") ;
        String x = pmap.expand("", "x") ;
        assertEquals("http://example/x", x) ;
    }

    @Test public void prefixMap2()
    {
        PrefixMap pmap = new PrefixMap() ;
        add(pmap, "ex", "http://example/") ;
        String x = pmap.expand("", "x") ;
        assertNull(x) ;
    }
    
    @Test public void prefixMap3()
    {
        PrefixMap pmap = new PrefixMap() ;
        add(pmap, "ex", "http://example/") ;
        add(pmap, "ex", "http://elsewhere/ns#") ;
        String x = pmap.expand("ex", "x") ;
        assertEquals("http://elsewhere/ns#x", x) ;
    }

    // PrefixMap2
    
    static void add(PrefixMap pmap, String prefix, String uri)
    {
        pmap.add(prefix, factory.create(uri)) ; 
    }
    
}
