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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GuavaCacheClient extends CacheClient{

    /** The cache for storing SPARQL Query results **/
    private Cache<String, Object> cache;

    /** Maximum size of cache data **/
    private static int MAX_SIZE = 1000;

    /** How long cache data will be stored after write **/
    private static int EXPIRE_TIME = 5;


    public GuavaCacheClient(){
        cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_SIZE)
                .expireAfterWrite(EXPIRE_TIME, TimeUnit.MINUTES)
                .build();
    }

    public Object get(String key) throws InterruptedException, ExecutionException, TimeoutException {
        return cache.getIfPresent(key);
   }

    public boolean set(String key, Object value) throws InterruptedException, ExecutionException, TimeoutException {
        cache.put(key,value);
        return true;
    }

    public boolean unset(String key) throws InterruptedException, ExecutionException, TimeoutException {
        cache.invalidate(key);
        return true;
    }

    /** Getters / Setters */
    public Cache<String, Object> getCache() {
        return cache;
    }

    public void setCache(Cache<String, Object> cache) {
        this.cache = cache;
    }
    /** Getters / Setters */



}
