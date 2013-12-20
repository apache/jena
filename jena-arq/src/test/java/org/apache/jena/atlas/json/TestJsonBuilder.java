/**
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

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestJsonBuilder extends BaseTest{
    // The Jena JSON parser is more livberal than strict JSON to make embedding easier.
    // * Keys do not need quotes
    // * Strings can use ''
    
    @Test public void jsonBuild01() {
        JsonValue x = JSON.parseAny("{ }") ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("A") ;
        builder.finishObject("A") ;
        JsonValue v = builder.build() ;
        assertEquals(x,v) ;
    }
    
    @Test public void jsonBuild02() {
        JsonValue x = JSON.parseAny("{ a: 'A', b:'B'}") ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("Obj1") ;
        builder.key("a").value("A") ;
        builder.key("b").value("B") ;
        builder.finishObject("Obj1") ;
        JsonValue v = builder.build() ;
        assertEquals(x,v) ;
    }
    

    @Test public void jsonBuild03() {
        JsonValue x = JSON.parseAny("[ ]") ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startArray() ;
        builder.finishArray() ;
        JsonValue v = builder.build() ;
        assertEquals(x,v) ;
    }
    
    @Test public void jsonBuild04() {
        JsonValue x = JSON.parseAny("{ a: [1], b:'B'}") ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject() ;
        builder.key("a").startArray().value(1).finishArray() ;
        builder.key("b").value("B") ;
        builder.finishObject() ;
        JsonValue v = builder.build() ;
        assertEquals(x,v) ;
    }

    @Test public void jsonBuild05() {
        JsonValue x = JSON.parseAny("[ { a: 'B'} , 56]") ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startArray() ;
        
        builder.startObject().key("a").value("B").finishObject() ;
        builder.value(56) ;
        
        builder.finishArray() ;
        JsonValue v = builder.build() ;
        assertEquals(x,v) ;
    }

    
    @Test(expected=JsonException.class)
    public void jsonBuildErr00() {
        JsonBuilder builder = new JsonBuilder() ;
        JsonValue v = builder.build() ;
    }

    @Test(expected=JsonException.class)
    public void jsonBuildErr01() {
        JsonBuilder builder = new JsonBuilder() ;
        builder.startArray() ;
        builder.finishObject() ;
    }

    @Test(expected=JsonException.class)
    public void jsonBuildErr02() {
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject() ;
        builder.finishArray() ;
    }
    
    @Test(expected=JsonException.class)
    public void jsonBuildErr03() {
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("A") ;
        builder.finishObject("B") ;
    }

}
