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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

public class TestStrUtils extends BaseTest
{
    static char marker = '_' ;
    static char esc[] = { ' ' , '_' } ; 
    
    static void test(String x)
    {
        test(x, null) ;
    }
    
    static void test(String x, String z)
    {
        String y = StrUtils.encodeHex(x, marker, esc) ;
        if ( z != null )
            assertEquals(z, y) ;
        String x2 = StrUtils.decodeHex(y, marker) ;
        assertEquals(x, x2) ;
    }
    
    @Test public void enc01() { test("abc") ; } 

    @Test public void enc02() { test("") ; } 

    @Test public void enc03() { test("_", "_5F" ) ; } 
    
    @Test public void enc04() { test(" ", "_20" ) ; } 
    
    @Test public void enc05() { test("_ _", "_5F_20_5F" ) ; } 
    
    @Test public void enc06() { test("_5F", "_5F5F" ) ; } 
    
    @Test public void enc07() { test("_2") ; } 
    
    @Test public void enc08() { test("AB_CD", "AB_5FCD") ; } 
}
