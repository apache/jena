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

package org.apache.jena.tdb1.setup;

import org.apache.jena.tdb1.base.block.FileMode;

/** Store parameters that can be adjusted after a store has been created,
 *  and given different values when the JVM attaches to a store area. 
 *  (They are still fixed for any given database once created in a JVM.) 
 */

public interface StoreParamsDynamic {
    
    /** Store-wide file access mode */ 
    public FileMode getFileMode() ;
    public boolean isSetFileMode() ;
    
    /** Block read cache (note: mapped files do not have a block cache) */
    public Integer getBlockReadCacheSize() ;
    public boolean isSetBlockReadCacheSize() ;

    /** Block write cache (note: mapped files do not have a block cache) */
    public Integer getBlockWriteCacheSize() ;
    public boolean isSetBlockWriteCacheSize() ;
    
    /** Node cache for Node{@literal ->}NodeId. */
    public Integer getNode2NodeIdCacheSize() ;
    public boolean isSetNode2NodeIdCacheSize() ;
    
    /** Node cache for NodeId{@literal ->}Node. Important for SPARQL results. */
    public Integer getNodeId2NodeCacheSize() ;
    public boolean isSetNodeId2NodeCacheSize() ;

    /** Node cache for recording known misses */
    public Integer getNodeMissCacheSize() ;
    public boolean isSetNodeMissCacheSize() ;
}

