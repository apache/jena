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

package org.apache.jena.tdb1.setup;

import org.apache.jena.tdb1.base.file.FileFactory;
import org.apache.jena.tdb1.base.file.FileSet;
import org.apache.jena.tdb1.base.objectfile.ObjectFile;
import org.apache.jena.tdb1.base.record.RecordFactory;
import org.apache.jena.tdb1.index.RangeIndex;
import org.apache.jena.tdb1.index.RangeIndexBuilder;
import org.apache.jena.tdb1.lib.ColumnMap;
import org.apache.jena.tdb1.store.tupletable.TupleIndex;
import org.apache.jena.tdb1.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb1.sys.SystemTDB;

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

