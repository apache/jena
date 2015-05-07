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

package org.apache.jena.tdb.setup;

import org.apache.jena.tdb.base.file.FileSet ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.index.IndexBuilder ;
import org.apache.jena.tdb.index.RangeIndexBuilder ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** A general way to make TDB storage dataset graphs : not for transactional datasets.
 * Old code. Unused and made inaccessible.  Kept for now for reference.
 * @see DatasetBuilderStd
 */ 

/*public*/ class DatasetBuilderBasic //implements DatasetBuilder
{
    private static final Logger log = LoggerFactory.getLogger(DatasetBuilderBasic.class) ;
    
    private NodeTableBuilder nodeTableBuilder ;
    private StoreParams params ;
    
    private /*public*/ DatasetBuilderBasic(IndexBuilder indexBuilder, RangeIndexBuilder rangeIndexBuilder)
    {
        ObjectFileBuilder objectFileBuilder = new BuilderStdDB.ObjectFileBuilderStd()  ;
        nodeTableBuilder    = new BuilderStdDB.NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
    }

    protected NodeTable makeNodeTable(Location location, String indexNode2Id, String indexId2Node, 
                                      int sizeNode2NodeIdCache, int sizeNodeId2NodeCache, int sizeNodeMissCache)
    {
        FileSet fsNodeToId = new FileSet(location, indexNode2Id) ;
        FileSet fsId2Node = new FileSet(location, indexId2Node) ;
        NodeTable nt = nodeTableBuilder.buildNodeTable(fsNodeToId, fsId2Node, params) ;
        return nt ;
    }
}
