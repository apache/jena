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

package com.hp.hpl.jena.tdb.index;

import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.setup.BlockMgrBuilder ;
import com.hp.hpl.jena.tdb.setup.StoreParams ;

public class IndexFactory {
    private static BlockMgrBuilder   blockMgrBuilder   = new BuilderStdIndex.BlockMgrBuilderStd() ;
    private static RangeIndexBuilder rangeIndexBuilder = new BuilderStdIndex.RangeIndexBuilderStd(blockMgrBuilder,
                                                                                               blockMgrBuilder) ;
    private static IndexBuilder      indexBuilder      = new BuilderStdIndex.IndexBuilderStd(blockMgrBuilder,
                                                                                          blockMgrBuilder) ;
    
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
        return new BuilderStdIndex.RangeIndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
    }
    
    
    public static RangeIndex buildRangeIndexMem(RecordFactory recordFactory) {
        FileSet fileSet = FileSet.mem() ;
        return buildRangeIndex(fileSet, recordFactory) ;
    }
    
    public static RangeIndex buildRangeIndex(Location location, String indexName, RecordFactory recordFactory) {
        FileSet fileset = new FileSet(location, indexName) ;
        return buildRangeIndex(fileset, recordFactory) ;
    }
    
    public static RangeIndex buildRangeIndex(Location location, String indexName, RecordFactory recordFactory, IndexParams indexParams) {
        FileSet fileset = new FileSet(location, indexName) ;
        return buildRangeIndex(fileset, recordFactory, indexParams) ;
    }

    public static RangeIndex buildRangeIndex(FileSet fileset, RecordFactory recordFactory) {
        IndexParams indexParams = StoreParams.getDftStoreParams() ;
        return buildRangeIndex(fileset, recordFactory, indexParams) ;
    }
        
    public static RangeIndex buildRangeIndex(FileSet fileset, RecordFactory recordFactory, IndexParams indexParams) {
        return rangeIndexBuilder.buildRangeIndex(fileset, recordFactory, indexParams) ; 
    }
    
    public static Index buildIndexMem(RecordFactory recordFactory) {
        FileSet fileSet = FileSet.mem() ;
        return buildIndex(fileSet, recordFactory) ;
    }

    public static Index buildIndex(FileSet fileset, RecordFactory recordFactory) {
        IndexParams indexParams = StoreParams.getDftStoreParams() ;
        return buildIndex(fileset, recordFactory, indexParams) ;
    }
    
    public static Index buildIndex(FileSet fileset, RecordFactory recordFactory, IndexParams indexParams) {
        return indexBuilder.buildIndex(fileset, recordFactory, indexParams) ; 
    }
}

