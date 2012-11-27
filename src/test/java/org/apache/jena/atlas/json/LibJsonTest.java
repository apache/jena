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


import static org.junit.Assert.assertEquals ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonValue ;

public class LibJsonTest
{
    /** Round trip string->json->string->json, compare two JOSN steps */
    public static void read(String string)
    { 
        JsonValue v = JSON.parseAny(string) ;
        writeRead(v) ;
    }

    /** Read-compare */
    public static void read(String string, JsonValue expected)
    { 
        JsonValue v = JSON.parseAny(string) ;
        assertEquals(expected, v) ;
    }
    
    /** Round trip json->string->json */
    public static void write(JsonValue v, String output, boolean whitespace)
    { 
        String str2 = v.toString();
        if ( ! whitespace )
        {
            output = output.replaceAll("[ \t\n\r]", "") ;
            str2 = str2.replaceAll("[ \t\n\r]", "") ; 
        }
        assertEquals(output, str2) ;
    }

    /** Round trip json->string->json */
    public static void writeRead(JsonValue v)
    { 
        String str2 = v.toString();
        JsonValue v2 = JSON.parseAny(str2) ;
        assertEquals(v, v2) ;
    }

   
}
