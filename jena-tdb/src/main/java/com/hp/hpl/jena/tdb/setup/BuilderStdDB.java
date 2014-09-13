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

import org.apache.jena.atlas.lib.ColumnMap ;

import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.RangeIndexBuilder ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTableCache ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTableInline ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTableNative ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class BuilderStdDB {

    public static class TupleIndexBuilderStd implements TupleIndexBuilder
    {
        private final RangeIndexBuilder rangeIndexBuilder ;
    
        public TupleIndexBuilderStd(RangeIndexBuilder rangeIndexBuilder) {
            this.rangeIndexBuilder = rangeIndexBuilder ;
        }
        
        @Override
        public TupleIndex buildTupleIndex(FileSet fileSet, ColumnMap colMap, String name, StoreParams params) {
            RecordFactory recordFactory = new RecordFactory(SystemTDB.SizeOfNodeId * colMap.length(), 0) ;
    
            RangeIndex rIdx = rangeIndexBuilder.buildRangeIndex(fileSet, recordFactory, params) ;
            TupleIndex tIdx = new TupleIndexRecord(colMap.length(), colMap, name, recordFactory, rIdx) ;
            return tIdx ;
        }
    }

    public static class NodeTableBuilderStd implements NodeTableBuilder
    {
        private final IndexBuilder indexBuilder ;
        private final ObjectFileBuilder objectFileBuilder ;
        
        public NodeTableBuilderStd(IndexBuilder indexBuilder, ObjectFileBuilder objectFileBuilder) {
            this.indexBuilder = indexBuilder ;
            this.objectFileBuilder = objectFileBuilder ;
        }
    
        @Override
        public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, StoreParams params) {
            RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
            Index idx = indexBuilder.buildIndex(fsIndex, recordFactory, params) ;
            ObjectFile objectFile = objectFileBuilder.buildObjectFile(fsObjectFile, Names.extNodeData) ;
            NodeTable nodeTable = new NodeTableNative(idx, objectFile) ;
            nodeTable = NodeTableCache.create(nodeTable, 
                                              params.getNode2NodeIdCacheSize(),
                                              params.getNodeId2NodeCacheSize(),
                                              params.getNodeMissCacheSize()) ;
            nodeTable = NodeTableInline.create(nodeTable) ;
            return nodeTable ;
        }
    }

    public static class ObjectFileBuilderStd implements ObjectFileBuilder
    {
        public ObjectFileBuilderStd() { }
        
        @Override
        public ObjectFile buildObjectFile(FileSet fileSet, String ext)
        {
            String filename = fileSet.filename(ext) ;
            if ( fileSet.isMem() )
                return FileFactory.createObjectFileMem(filename) ;
            return FileFactory.createObjectFileDisk(filename) ;
        }
    }

}

