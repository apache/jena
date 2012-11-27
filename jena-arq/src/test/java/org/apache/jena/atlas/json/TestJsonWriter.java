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

package org.apache.jena.atlas.json;

import static org.apache.jena.atlas.json.LibJsonTest.writeRead ;
import org.apache.jena.atlas.json.JsonString ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestJsonWriter extends BaseTest
{
    @Test public void js_write_str_1()  { test("foo") ; }
    
    @Test public void js_write_str_2()  { test("foo bar") ; }

    @Test public void js_write_str_3()  { test("foo\nbar") ; }
    
    @Test public void js_write_str_4()  { test("x\ty", "\"x\\ty\"") ; }

    @Test public void js_write_str_5()  { test("\r", "\"\\r\"") ; }

    @Test public void js_write_str_6()  { test("\u0000", "\"\\u0000\"") ; }

    @Test public void js_write_str_7()  { test("\u0001", "\"\\u0001\"") ; }

    @Test public void js_write_str_8()  { test("\u001F", "\"\\u001F\"") ; }

    @Test public void js_write_str_9()  { test("\u007F", "\"\\u007F\"") ; }

    @Test public void js_write_str_10() { test("\u009F", "\"\\u009F\"") ; }

    @Test public void js_write_str_11() { test("\u2001", "\"\\u2001\"") ; }
    
    
    private static void test(String str, String expected)
    {
        JsonValue v = new JsonString(str) ;
        str = v.toString() ;
        assertEquals(expected, str) ; 
    }
    
    private static void test(String str)
    {
        JsonValue v = new JsonString(str) ;
        writeRead(v) ;
    }
}
