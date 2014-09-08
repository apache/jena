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

package com.hp.hpl.jena.tdb.index.factories ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.PlainFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexFactory ;
import com.hp.hpl.jena.tdb.index.IndexParams ;
import com.hp.hpl.jena.tdb.index.ext.ExtHash ;
import com.hp.hpl.jena.tdb.sys.Names ;

/**
 * Index factory for extensible hash tables. Only an index, not a range index
 */

public class IndexFactoryExtHash implements IndexFactory {
    public IndexFactoryExtHash() {}

    @Override
    public Index createIndex(FileSet fileset, RecordFactory recordFactory, IndexParams idxParams) {
        String fnDictionary = fileset.filename(Names.extHashExt) ;
        PlainFile dictionary = FileFactory.createPlainFileDisk(fnDictionary) ;

        String fnBuckets = fileset.filename(Names.extHashBucketExt) ;
        BlockMgr mgr = createBlockMgr(fnBuckets, idxParams) ;
        ExtHash eHash = new ExtHash(dictionary, recordFactory, mgr) ;
        return eHash ;
    }

    protected BlockMgr createBlockMgr(String filename, IndexParams idxParams) {
        return BlockMgrFactory.createFile(filename, idxParams) ;
    }
}
