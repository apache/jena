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
import static org.apache.jena.atlas.json.LibJsonTest.writeRead ;
import org.apache.jena.atlas.json.JsonNumber ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.atlas.json.JsonString ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

/** Test that are of extension of JSON */ 
public class TestJsonExt extends BaseTest
{
    // -------- Non-standard things.
    
    @Test public void js_value_ext_1()
    { 
        read("'abc'", new JsonString("abc")) ;
    }
    
    @Test public void js_value_ext_2()
    { 
        read("'''abc'''", new JsonString("abc")) ;
    }

    @Test public void js_value_ext_3()
    { 
        read("\"\"\"abc\"\"\"", new JsonString("abc")) ;
    }
    
    @Test public void js_map_ext_1()
    { 
        JsonObject obj = new JsonObject() ;
        obj.put("abc", JsonNumber.value(123)) ;
        writeRead(obj) ;
        // Use of key.
        read("{abc: 123}", obj) ;
    }
}
