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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class TestJsonAPI
{
    @Test public void jsonAPI_01() {
        JsonObject obj = JSON.parse("{ key1: 'str1' , key2: [ 1 , 2 ] }") ;
        assertEquals(2, obj.size());
    }

    @Test public void jsonAPI_02() {
        JsonObject obj = JSON.parse("{ key1: 'str1' , key2: [ 1 , 2 ] }") ;
        JsonObject obj2 = (JsonObject)JSON.copy(obj);
        assertNotSame(obj, obj2);
        assertEquals(obj, obj2);
    }

    @Test public void jsonAPI_03() {
        JsonValue jv1 = JSON.parseAny("2") ;
        JsonValue jv2 = JSON.copy(jv1);
        assertSame(jv1, jv2);
    }
}
