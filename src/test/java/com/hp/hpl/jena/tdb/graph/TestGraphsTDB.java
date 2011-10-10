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

package com.hp.hpl.jena.tdb.graph;

import org.junit.BeforeClass ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.graph.GraphsTests ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestGraphsTDB extends GraphsTests
{
    private static Dataset ds ;
    @BeforeClass public static void setupClass()
    {
        SystemTDB.defaultOptimizer = ReorderLib.identity() ;
        ds = TDBFactory.createDataset() ;
        
    }
    @Override
    protected Dataset createDataset()
    {
        return ds ;
    }
}
