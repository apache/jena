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

package com.hp.hpl.jena.tdb.lib;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test;

public class TestStringAbbrev extends BaseTest
{
    @Test public void abbrev_01()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        test("Hello", "Hello", abbrev) ;
    }
    
    @Test public void abbrev_02()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        test(":Hello", ":_:Hello", abbrev) ;
    }
    
    @Test public void abbrev_03()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        test("::Hello", ":_::Hello", abbrev) ;
    }
    
    @Test public void abbrev_04()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        abbrev.add("x", "He") ;
        test("Hello", ":x:llo", abbrev) ;
        test("hello", "hello", abbrev) ;
        test(":hello", ":_:hello", abbrev) ;
    }
    
    private void test(String x, String y, StringAbbrev abbrev)
    {
        String z1 = abbrev.abbreviate(x) ;
        assertEquals(y, z1) ;
        String z2 = abbrev.expand(z1) ;
        assertEquals(x, z2) ;
    }
}
