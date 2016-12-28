/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.store.nodetable;

import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.junit.BuildTestLib ;
import org.seaborne.tdb2.setup.StoreParams ;
import org.seaborne.tdb2.setup.StoreParamsBuilder ;

public class TestNodeTable extends AbstractTestNodeTable
{
    @Override
    protected NodeTable createEmptyNodeTable()
    {
        StoreParams params = 
            StoreParamsBuilder.create()
                .nodeId2NodeCacheSize(10)
                .node2NodeIdCacheSize(10)
                .nodeMissCacheSize(10).build() ;
        return BuildTestLib.makeNodeTable(Location.mem(), "test", params) ;
    }
}
