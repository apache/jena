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

import com.hp.hpl.jena.tdb.setup.StoreParams ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TDBSystemInfo implements TDBSystemInfoMBean
{
    private static StoreParams params = StoreParams.getDftStoreParams() ;

    @Override
    public int getSegmentSize()             { return SystemTDB.SegmentSize; }
    @Override
    public int getNodeId2NodeCacheSize()    { return params.getNodeId2NodeCacheSize() ; }
    @Override
    public int getNode2NodeIdCacheSize()    { return params.getNode2NodeIdCacheSize() ; }
    @Override
    public int getNodeMissCacheSize()       { return params.getNodeMissCacheSize() ; }
    @Override
    public int getBlockSize()               { return params.getBlockSize() ; }
    @Override
    public int getBlockReadCacheSize()      { return params.getBlockReadCacheSize() ; }
    @Override
    public int getBlockWriteCacheSize()     { return params.getBlockWriteCacheSize() ; }
}
