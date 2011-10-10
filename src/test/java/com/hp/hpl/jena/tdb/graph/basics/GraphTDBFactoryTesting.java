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

package com.hp.hpl.jena.tdb.graph.basics;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBPlusTree ;
import com.hp.hpl.jena.tdb.store.GraphTriplesTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Place to put various "making" explicitly for testing */

class GraphTDBFactoryTesting
{
    // This class and FactoryGraphTDB are old ways of making graphs and datasets 
    
    /** Create a graph backed with storage at a location using B+Tree indexes (testing) */
    public static GraphTriplesTDB createBPlusTree(Location location)
    { 
        IndexFactoryBPlusTree idxFactory = new IndexFactoryBPlusTree(SystemTDB.BlockSizeTest) ;
        IndexBuilder builder = new IndexBuilder(idxFactory,idxFactory) ; 
        return FactoryGraphTDB.createGraph(builder, location) ;
    }

    /** Create a graph backed with storage and B+Tree indexes in-memory (testing) */
    public static GraphTriplesTDB createBPlusTreeMem()
    { 
        IndexFactoryBPlusTree idxFactory = new IndexFactoryBPlusTree(SystemTDB.BlockSizeTestMem) ;
        IndexBuilder builder = new IndexBuilder(idxFactory,idxFactory) ; 
        return FactoryGraphTDB.createGraphMem(builder) ;
    }
}
