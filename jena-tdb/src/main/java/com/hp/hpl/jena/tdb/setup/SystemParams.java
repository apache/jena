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

package com.hp.hpl.jena.tdb.setup;

import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** System parameters for a TDB database instance.  Currently, fixed.  
 */
public class SystemParams
{
    public final int      blockSize            = SystemTDB.BlockSize ;
    public final int      memBlockSize         = SystemTDB.BlockSizeTestMem ;
    public final int      readCacheSize        = SystemTDB.BlockReadCacheSize ;
    public final int      writeCacheSize       = SystemTDB.BlockWriteCacheSize ;
    public final int      Node2NodeIdCacheSize = SystemTDB.Node2NodeIdCacheSize ;
    public final int      NodeId2NodeCacheSize = SystemTDB.NodeId2NodeCacheSize ;
    public final int      NodeMissCacheSize    = SystemTDB.NodeMissCacheSize ;

    public final String   indexNode2Id         = Names.indexNode2Id ;
    public final String   indexId2Node         = Names.indexId2Node ;
    public final String   primaryIndexTriples  = Names.primaryIndexTriples ;
    public final String[] tripleIndexes        = Names.tripleIndexes ;
    public final String   primaryIndexQuads    = Names.primaryIndexQuads ;
    public final String[] quadIndexes          = Names.quadIndexes ;
    public final String   primaryIndexPrefix   = Names.primaryIndexPrefix ;
    public final String[] prefixIndexes        = Names.prefixIndexes ;
    public final String   indexPrefix          = Names.indexPrefix ;

    public final String   prefixNode2Id        = Names.prefixNode2Id ;
    public final String   prefixId2Node        = Names.prefixId2Node ;
    
    private SystemParams() {}
    
    private static SystemParams standard = new SystemParams() ;
    
    public static SystemParams getStdSystemParams()
    {
        return standard ;
    }
}
