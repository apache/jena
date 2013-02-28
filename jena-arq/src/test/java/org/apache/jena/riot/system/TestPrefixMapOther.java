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

package org.apache.jena.riot.system;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

/**
 * Test the standard {@link PrefixMapStd} implementation
 *
 */
public class TestPrefixMapOther extends BaseTest
{
    @Test(expected=UnsupportedOperationException.class) 
    public void other_01()
    {
        PrefixMap pmap = PrefixMapFactory.create() ;
        pmap = PrefixMapFactory.unmodifiablePrefixMap(pmap) ;
        pmap.add("foo", "bar") ;
    }
    
    @Test(expected=UnsupportedOperationException.class) 
    public void other_02()
    {
        PrefixMap pmap = PrefixMapFactory.create() ;
        pmap = PrefixMapFactory.unmodifiablePrefixMap(pmap) ;
        pmap.getMapping().put("ex", IRIResolver.iriFactory.construct("http://example/"))  ;
    }
    

    @Test(expected=UnsupportedOperationException.class) 
    public void other_10()
    {
        PrefixMap pmap = PrefixMapFactory.emptyPrefixMap() ;
        pmap.add("foo", "bar") ;
    }
    
    @Test(expected=UnsupportedOperationException.class) 
    public void other_11()
    {
        PrefixMap pmap = PrefixMapFactory.emptyPrefixMap() ;
        pmap.getMapping().put("ex", IRIResolver.iriFactory.construct("http://example/"))  ;
    }
    
}
