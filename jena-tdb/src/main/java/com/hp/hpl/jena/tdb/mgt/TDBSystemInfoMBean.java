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

package com.hp.hpl.jena.tdb.mgt;

/** Static information relating to TDB */
public interface TDBSystemInfoMBean
{
    /** Size, in bytes, of a disk block */
    public int getBlockSize() ;
    
    /** Size of a memory-mapped file segment */ 
    public int getSegmentSize() ;

//    /** Number of adds/deletes between calls to sync (-ve to disable) */
//    public int getSyncTick() ;
    
    /** Size of Node to NodeId cache.
     *  Used to map from Node to NodeId spaces.
     *  Used for loading and for query preparation.
     */
   public int getNode2NodeIdCacheSize() ;
   
   /** Size of NodeId to Node cache.
    *  Used to map from NodeId to Node spaces.
    *  Used for retriveing results.
    */
   public int getNodeId2NodeCacheSize() ;
   
   /** Size of NodeTable lookup miss cache
    */
   public int getNodeMissCacheSize() ;
    
   /** Size of the delayed-write block cache (32 bit systems only) (per file) */
   public int getBlockWriteCacheSize() ;

   /** Size of read block cache (32 bit systems only).  Increase JVM size as necessary. Per file. */
   public int getBlockReadCacheSize() ;
}
