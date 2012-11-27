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

import org.apache.jena.atlas.lib.XMLLib ;
import org.junit.Assert ;
import org.junit.Test ;

public class TestXMLLib
{
    @Test public void ws_collapse_01()  { test("abc", "abc") ; }
    @Test public void ws_collapse_02()  { test(" abc", "abc") ; }
    @Test public void ws_collapse_03()  { test(" abc ", "abc") ; }
    @Test public void ws_collapse_04()  { test(" a b c ", "a b c") ; }
    @Test public void ws_collapse_05()  { test("\babc", "\babc") ; }
    @Test public void ws_collapse_06()  { test("", "") ; }
    @Test public void ws_collapse_07()  { test(" ", "") ; }
    @Test public void ws_collapse_08()  { test(" \t\t\t\t\t\t\t   ", "") ; }
    
    // String.trim : "Returns a copy of the string, with leading and trailing whitespace omitted."
    // but later says it trims anything <= 0x20.  There are lots of control characters in x01-x1F. 
    // We only want to trim \n \r \t and space. 
    
    private static void test(String str1, String str2)
    {
        String result = XMLLib.WScollapse(str1) ;
        Assert.assertEquals(str2, result) ;
    }

}
