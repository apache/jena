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

package org.apache.jena.fuseki.cache;

/** This class act as a switch to perform operation on cache. Once the requests
 *  reaches SPARQL_Query_Cache, the CacheAction type can be either READ_CACHE or
 *  WRITE_CACHE based on cache hit or miss operation.
 */
public class CacheAction {

    /** The cache operation action that need to be taken to serve the
     * request. The cache operation can be READ_CACHE or WRITE_CACHE or
     * IDLE.
     */
    public final CacheAction.Type type;

    /** The SPARQL Query cache key. The key is combination of dataset,
     * query string and response content type.The key is generated in
     * {@code SPARQL_Query_Cache}
     */
    private final String key;

    public CacheAction(String key,CacheAction.Type type){
        this.key = key;
        this.type = type;
    }

    public enum Type{
        READ_CACHE,
        WRITE_CACHE,
        IDLE
    }

    public String getKey() {
        return key;
    }

}
