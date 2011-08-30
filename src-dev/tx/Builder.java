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

package tx;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public final class Builder
{
    // See SetupTDB
    // This already has many statics.
    // Reuse with defaults.
    
    // Yet another attempt to have one place to "build things"
    // See DatasetBuilderStd.
    
    // Use standard defaults.
    // Go back and find all other attempts and remove. 
    
    // Become a library of builders - take code from DatasetBuilderStd
    
    
    //static BlockMgrBuilder blkMgrBuilder = new BlockMgrBuilderStd() ;
    
    public static Index createIndex(FileSet fileset, RecordFactory recordFactory)
    {
        return createRangeIndex(fileset, recordFactory) ;
    }

    public static RangeIndex createRangeIndex(FileSet fileset, RecordFactory recordFactory)
    {
        if ( fileset.isMem() )
            return createRangeIndex$(fileset, recordFactory) ;
        else
            return createRangeIndexMem$(fileset, recordFactory) ;
    }
    
    private static RangeIndex createRangeIndex$(FileSet fileset, RecordFactory recordFactory)
    {
        return createBPlusTree$(fileset, recordFactory) ;
    }
    
    private static RangeIndex createRangeIndexMem$(FileSet fileset, RecordFactory recordFactory)
    {
        return createBPlusTreeMem$(fileset, recordFactory) ;
    }

    public static BPlusTree createBPlusTree(FileSet fileset, RecordFactory recordFactory)
    {
        if ( fileset.isMem() )
            return createBPlusTreeMem$(fileset, recordFactory) ;
        else
            return createBPlusTree$(fileset, recordFactory) ;
        
    }
    
    private static BPlusTree createBPlusTree$(FileSet fileset, RecordFactory recordFactory)
    {
        int order = BPlusTreeParams.calcOrder(params.blockSize , recordFactory) ;
        BPlusTreeParams bptParams = new BPlusTreeParams(order, recordFactory) ;
        BlockMgr blkMgrRecords = createBlockMgr$(fileset, Names.bptExtRecords, recordFactory) ;
        BlockMgr blkMgrNodes = createBlockMgr$(fileset, Names.bptExtTree, recordFactory) ;
        BPlusTree bpt =BPlusTree.attach(bptParams, blkMgrNodes, blkMgrRecords) ;
        return bpt ;
    }

    private static BPlusTree createBPlusTreeMem$(FileSet fileset, RecordFactory recordFactory)
    {
        int order = BPlusTreeParams.calcOrder(params.memBlockSize , recordFactory) ;
        BPlusTreeParams bptParams = new BPlusTreeParams(order, recordFactory) ;
        BlockMgr blkMgrRecords = createBlockMgrMem$(fileset, Names.bptExtRecords, recordFactory) ;
        BlockMgr blkMgrNodes = createBlockMgrMem$(fileset, Names.bptExtTree, recordFactory) ;
        BPlusTree bpt = BPlusTree.attach(bptParams, blkMgrNodes, blkMgrRecords) ;
        return bpt ;
    }
    
    private static BlockMgr createBlockMgr$(FileSet fileset, String ext, RecordFactory recordFactory)
    {
        return BlockMgrFactory.create(fileset, ext, params.blockSize, params.readCacheSize, params.writeCacheSize) ;
    }
    
    private static BlockMgr createBlockMgrMem$(FileSet fileset, String ext, RecordFactory recordFactory)
    {
        return BlockMgrFactory.createMem(fileset.getBasename(), params.memBlockSize) ;
    }
    
    public static ObjectFile createObjectFile(FileSet fileset)
    {
        String x = fileset.filename(Names.extNodeData) ;
        if ( fileset.isMem() )
            return FileFactory.createObjectFileMem(x) ;
        else
            return FileFactory.createObjectFileDisk(x) ;
    }
    
    private static Params params = new Params() ;
    
    // The standard setting
    private static class Params
    {
         final int blockSize = SystemTDB.BlockSize ;
         final int memBlockSize = SystemTDB.BlockSizeTestMem ;
         final int readCacheSize = SystemTDB.BlockReadCacheSize ;
         final int writeCacheSize = SystemTDB.BlockWriteCacheSize ;
    }
    
}



