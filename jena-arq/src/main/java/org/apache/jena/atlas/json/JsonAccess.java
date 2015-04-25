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

package org.apache.jena.atlas.json ;

/** Access json datastructures */
public class JsonAccess {
    public static JsonValue accessPath(JsonValue obj, String... path) {
        for ( String p : path ) {
            if ( !obj.isObject() ) {
                throw new JsonException("Path traverses non-object") ;
            }
            obj = obj.getAsObject().get(p) ;
        }
        return obj ;
    }

    public static JsonValue access(JsonValue obj, Object... path) {
        for ( Object p : path ) {
            if ( p instanceof String ) {
                if ( !obj.isObject() ) {
                    throw new JsonException("Path traverses non-object") ;
                }
                obj = obj.getAsObject().get((String)p) ;
            }
            if ( p instanceof Integer ) {
                if ( !obj.isArray() ) {
                    throw new JsonException("Path traverses non-array") ;
                }
                obj = obj.getAsArray().get((Integer)p) ;
            }
        }
        return obj ;
    }

}
