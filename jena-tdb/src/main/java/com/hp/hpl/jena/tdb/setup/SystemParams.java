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

import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** System parameters for a TDB database instance. */
public class SystemParams
{
    public int      blockSize            = SystemTDB.BlockSize ;
    public int      memBlockSize         = SystemTDB.BlockSizeTestMem ;
    public int      readCacheSize        = SystemTDB.BlockReadCacheSize ;
    public int      writeCacheSize       = SystemTDB.BlockWriteCacheSize ;
    public int      Node2NodeIdCacheSize = SystemTDB.Node2NodeIdCacheSize ;
    public int      NodeId2NodeCacheSize = SystemTDB.NodeId2NodeCacheSize ;
    public int      NodeMissCacheSize    = SystemTDB.NodeMissCacheSize ;

    public String   indexNode2Id         = Names.indexNode2Id ;
    public String   indexId2Node         = Names.indexId2Node ;
    public String   primaryIndexTriples  = Names.primaryIndexTriples ;
    public String[] tripleIndexes        = Names.tripleIndexes ;
    public String   primaryIndexQuads    = Names.primaryIndexQuads ;
    public String[] quadIndexes          = Names.quadIndexes ;
    public String   primaryIndexPrefix   = Names.primaryIndexPrefix ;
    public String[] prefixIndexes        = Names.prefixIndexes ;
    public String   indexPrefix          = Names.indexPrefix ;

    public String   prefixNode2Id        = Names.prefixNode2Id ;
    public String   prefixId2Node        = Names.prefixId2Node ;
    
    public SystemParams() {}
    
    public static SystemParams getStdSystemParams() {
        return new SystemParams() ;
    }
    
    @Override
    public String toString() {
        return StringUtils.str(tripleIndexes)+" "+StringUtils.str(quadIndexes) ;
    }
}
