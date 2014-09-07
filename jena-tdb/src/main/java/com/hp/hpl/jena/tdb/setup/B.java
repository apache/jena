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

import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexParams ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.setup.BuilderIndex.BlockMgrBuilderStd ;

public class B {
    // c.f. setupTDB
    
    public static IndexBuilder createIndexBuilderMem() { 
        return createIndexBuilder(createRangeIndexBuilderMem()) ;
    }
    
    public static IndexBuilder createIndexBuilder(final RangeIndexBuilder other) 
    { 
        return new IndexBuilder() {
            @Override
            public Index buildIndex(FileSet fileSet, RecordFactory recordfactory, IndexParams indexParams) {
                return other.buildRangeIndex(fileSet, recordfactory, indexParams) ;
            }
        } ;
    }

    public static RangeIndexBuilder createRangeIndexBuilderMem() {
        BlockMgrBuilder blockMgrBuilderNodes = new BlockMgrBuilderStd() ;
        BlockMgrBuilder blockMgrBuilderRecords = new BlockMgrBuilderStd() ;
        return new BuilderIndex.RangeIndexBuilderStd(blockMgrBuilderNodes, blockMgrBuilderRecords) ;
    }
    
    // XXX Merge with com.hp.hpl.jena.tdb.index.IndexFactory
    
    public static RangeIndex buildRangeIndexMem(RecordFactory recordFactory) {
        FileSet fileSet = FileSet.mem() ;
        return buildRangeIndex(fileSet, recordFactory) ;
    }
    
    public static RangeIndex buildRangeIndex(FileSet fileset, RecordFactory recordFactory) {
        IndexParams indexParams = SystemParams.getDftSystemParams() ;
        return buildRangeIndex(fileset, recordFactory, indexParams) ;
    }
        
    public static RangeIndex buildRangeIndex(FileSet fileset, RecordFactory recordFactory, IndexParams indexParams) {
        BlockMgrBuilder nodeBld = new BuilderIndex.BlockMgrBuilderStd() ;
        BlockMgrBuilder leavesBld = new BuilderIndex.BlockMgrBuilderStd() ;
        RangeIndexBuilder builder = new BuilderIndex.RangeIndexBuilderStd(nodeBld, leavesBld) ;
        return builder.buildRangeIndex(fileset, recordFactory, indexParams) ; 
    }
    
    public static Index buildIndexMem(RecordFactory recordFactory) {
        FileSet fileSet = FileSet.mem() ;
        return buildIndex(fileSet, recordFactory) ;
    }

    public static Index buildIndex(FileSet fileset, RecordFactory recordFactory) {
        IndexParams indexParams = SystemParams.getDftSystemParams() ;
        return buildIndex(fileset, recordFactory, indexParams) ;
    }
    
    public static Index buildIndex(FileSet fileset, RecordFactory recordFactory, IndexParams indexParams) {
        BlockMgrBuilder nodeBld = new BuilderIndex.BlockMgrBuilderStd() ;
        BlockMgrBuilder leavesBld = new BuilderIndex.BlockMgrBuilderStd() ;
        IndexBuilder builder = new BuilderIndex.IndexBuilderStd(nodeBld, leavesBld) ;
        return builder.buildIndex(fileset, recordFactory, indexParams) ; 
    }
    
//    public static NodeTable buildNodeTable(FileSet fileset, SystemParams params) {
//        BlockMgrBuilder nodeBld = new Builder.BlockMgrBuilderStd() ;
//        BlockMgrBuilder leavesBld = new Builder.BlockMgrBuilderStd() ;
//        
//        NodeTableBuilder ntb = new Builder.NodeTableBuilderStd(null, null) ;
//        
//        
//        IndexBuilder builder = new Builder.IndexBuilderStd(nodeBld, leavesBld) ;
//        FileSet filesetIdx = new FileSet(params) ;
//        FileSet filesetObjFile = new FileSet(params.
//                                                                          
//        return ntb.buildNodeTable(fileset, 
//                                  fileset, 
//                                  params.getNode2NodeIdCacheSize(),
//                                  params.getNodeId2NodeCacheSize(),
//                                  params.getNodeMissCacheSize()) ;
//    }
}

