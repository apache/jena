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

import org.apache.jena.sparql.resultset.SPARQLResult;

/** This class is a wrapper for SPARQL_Query SPARQLResult and data byte array */
public class CacheEntry {

    /** The flag to identify if SPARQL_Query data is initialized */
    private boolean initialized = false;

    /** The SPARQLResult object **/
    private SPARQLResult result;

    /** The byte array to store the SPARQL_Query result data. */
    private byte[] data;

    public SPARQLResult getResult() {
        return result;
    }

    public void setResult(SPARQLResult result) {
        this.result = result;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void initialized(){
        if ( this.initialized )
            return ;
        initialized = true ;
    }

    public boolean isInitialized(){
        return this.initialized;
    }

}
