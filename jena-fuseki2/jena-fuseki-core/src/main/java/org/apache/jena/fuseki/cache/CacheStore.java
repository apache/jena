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

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;

public class CacheStore {

    /** flag to check if data store was initialized */
    public static boolean initialized = false ;

    private static Logger log = Fuseki.cacheLog ;

    /** execution timeout for any method */
    private int defaultExecutionTimeout = 500 ;

    /** thread pool size for this data store */
    protected int defaultThreadPoolSize = 500 ;


    /** client for interacting with  Cache store **/
    private final CacheClient client = new GuavaCacheClient();

    /** Time to live for Cache data **/
    private int ttl;


    /** For testing - reset the places which initialize once */
    public synchronized static void reset() {
        initialized = false ;
        CacheStore.initialized = false ;
    }

    public synchronized static void init(){
        if ( initialized )
            return ;
        initialized = true ;

    }

    /**
     * Get cache data from cache store.
     * @param key Cache store key
     */
    public Object doGet(String key) throws CacheStoreException{
        try{
        Object data = client.get(key);
            if(data == null)
                return null;
            else
                return data;
        }catch (Exception e){
            throw new CacheStoreException("CACHE SELECT FAILED", e);
        }
    }

    /**
     * Set cache data in cache store.
     * @param key Cache store key
     * @param data SPARQL Query results set object
     */
    public boolean doSet(String key, Object data) throws CacheStoreException{
        try{
          if(client.set(key,data,ttl))
              return true;
          else
              throw new CacheStoreException("CACHE  INSERT FAILED");
        }catch (Exception e){
            throw new CacheStoreException("CACHE INSERT FAILED",e);
        }
    }

    /**
     * Delete cache data in cache store.
     * @param key Cache store key
     *
     */
    public boolean doUnset(String key) throws CacheStoreException{
        try{
           if(client.unset(key))
               return true;
           else{
               Object data = client.get(key);
               if(data == null)
                   return true;

           }
            throw new CacheStoreException("CACHE DELETE FAILED");
        }catch (Exception e){
            throw new CacheStoreException("CACHE DELETE FAILED", e);
        }
    }

    public static String generateKey(HttpAction action, String queryString) {
        HttpServletRequest req = action.getRequest();
        String uri = ActionLib.actionURI(req);
        String dataSetUri = ActionLib.mapActionRequestToDataset(uri);
        log.info("CacheStore Key " +dataSetUri+queryString);
        return dataSetUri+queryString;
    }

    /** Getters / Setters */

    public int getDefaultExecutionTimeout() {
        return defaultExecutionTimeout;
    }

    public void setDefaultExecutionTimeout(int defaultExecutionTimeout) {
        this.defaultExecutionTimeout = defaultExecutionTimeout;
    }

    public int getDefaultThreadPoolSize() {
        return defaultThreadPoolSize;
    }

    public void setDefaultThreadPoolSize(int defaultThreadPoolSize) {
        this.defaultThreadPoolSize = defaultThreadPoolSize;
    }
    public int getTtl() {return ttl;}

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

   /** Getters / Setters */
}
