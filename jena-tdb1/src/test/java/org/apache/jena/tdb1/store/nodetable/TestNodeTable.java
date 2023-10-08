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

package org.apache.jena.tdb1.store.nodetable;

import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.setup.Build;
import org.apache.jena.tdb1.sys.Names;

public class TestNodeTable extends AbstractTestNodeTable
{
    @Override
    protected NodeTable createEmptyNodeTable()
    {
        return Build.makeNodeTable(Location.mem(), Names.indexNode2Id, 10, Names.indexId2Node, 10, 10) ;
    }
}
