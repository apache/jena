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

import static org.apache.jena.atlas.json.LibJsonTest.read ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestJson extends BaseTest
{
    @Test public void js_value_1()
    { 
        read("\"abc\"", new JsonString("abc")) ;
    }
    
    @Test public void js_value_2()
    { 
        read("123", JsonNumber.value(123)) ;
    }
    
    @Test public void js_value_3()
    { 
        read("true", new JsonBoolean(true)) ;
    }

    @Test public void js_value_4()
    { 
        read("{}", new JsonObject()) ;
    }

    @Test public void js_value_5()
    { 
        JsonObject obj = new JsonObject() ;
        obj.put("a", JsonNumber.value(123)) ;
        read("{ \"a\": 123 }", obj) ;
    }

    
    @Test public void js_value_6()
    { 
        JsonArray array = new JsonArray() ;
        read("[ ]", array) ;
    }

    @Test public void js_value_7()
    { 
        JsonArray array = new JsonArray() ;
        array.add(JsonNumber.value(123)) ;
        read("[ 123 ]", array) ;
    }
    
    @Test public void js_value_8()
    { 
        JsonObject obj = new JsonObject() ;
        JsonArray array = new JsonArray() ;
        array.add(JsonNumber.value(123)) ;
        obj.put("a", array) ;
        read("{ \"a\" : [ 123 ] }", obj) ; 
    }

    
    @Test(expected=JsonParseException.class)
    public void js_value_9()
    {
        JSON.parse("[1 2 3]") ;
    }
    
    @Test(expected=JsonParseException.class)
    public void js_value_10()
    {
        JSON.parse("1") ;
    }

    @Test(expected=JsonParseException.class)
    public void js_value_11()
    {
        JSON.parse("\"foo\"") ;
    }
    
    @Test()
    public void js_value_12()
    {
        JSON.parse("{}") ;
    }
    
    @Test
    public void js_value_13() {
        read("\"abc\\bd\"", new JsonString("abc\bd")) ;
    }
}
