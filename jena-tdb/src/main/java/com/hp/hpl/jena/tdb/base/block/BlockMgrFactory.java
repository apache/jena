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

package com.hp.hpl.jena.tdb.base.block;

import org.apache.jena.atlas.lib.FileOps ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.file.BlockAccess ;
import com.hp.hpl.jena.tdb.base.file.BlockAccessDirect ;
import com.hp.hpl.jena.tdb.base.file.BlockAccessMapped ;
import com.hp.hpl.jena.tdb.base.file.BlockAccessMem ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;


public class BlockMgrFactory
{
    // This isn't always helpful so be careful if setting the default to "true".
    // Sometimes the tracking is too strict
    //     e.g. transactions keep blocks and not release them down the layers.
    //     But journal layers over a tracked BlockMgr is this is on. 
    public /*final*/ static boolean AddTracker = false ;
    
    public static BlockMgr tracker(BlockMgr blockMgr)
    {
        if ( blockMgr instanceof BlockMgrTracker ) return blockMgr ;
        return BlockMgrTracker.track(blockMgr) ;
    }
    
    /** Add a tracker if the system default is to do so */
    private static BlockMgr track(BlockMgr blockMgr)
    {
        if ( ! AddTracker ) return blockMgr ;
        return tracker(blockMgr) ;
    }
    
    public static BlockMgr create(FileSet fileSet, String ext, int blockSize, int readBlockCacheSize, int writeBlockCacheSize)
    {
        if ( fileSet.isMem() )
            return createMem(fileSet.filename(ext), blockSize) ;
        else
            return createFile(fileSet.filename(ext), blockSize, readBlockCacheSize, writeBlockCacheSize) ;
    }
    
    /** Create an in-memory block manager */ 
    public static BlockMgr createMem(String indexName, int blockSize)
    {
        BlockAccess file = new BlockAccessMem(indexName, blockSize) ;
        BlockMgr blockMgr = new BlockMgrFileAccess(file, blockSize) ;
        blockMgr = new BlockMgrFreeChain(blockMgr) ;

        // Small cache - testing.
        //blockMgr = new BlockMgrCache(indexName, 3, 3, blockMgr) ;
        
        return track(blockMgr) ;
    }
    
    /** Create a BlockMgr backed by a file */
    public static BlockMgr createFile(String filename, int blockSize, int readBlockCacheSize, int writeBlockCacheSize)
    {
        switch ( SystemTDB.fileMode() )
        {
            case mapped:
                return createMMapFile(filename, blockSize) ;
            case direct:
                return createStdFile(filename, blockSize, readBlockCacheSize, writeBlockCacheSize) ;
        }
        throw new TDBException("Unknown file mode: "+SystemTDB.fileMode()) ;
    }        

    /** Create a NIO Block Manager */
    public static BlockMgr createMMapFile(String filename, int blockSize)
    {
        BlockAccess file = new BlockAccessMapped(filename, blockSize) ;
        BlockMgr blockMgr =  wrapFileAccess(file, blockSize) ;
        return track(blockMgr) ;
    }
    
    /** Create a Block Manager using direct access (and a cache) */
    public static BlockMgr createStdFile(String filename, int blockSize, int readBlockCacheSize, int writeBlockCacheSize)
    {
        BlockAccess file = new BlockAccessDirect(filename, blockSize) ;
        BlockMgr blockMgr =  wrapFileAccess(file, blockSize) ;

        String fn = FileOps.basename(filename) ;
        
        blockMgr = BlockMgrCache.create(fn, readBlockCacheSize, writeBlockCacheSize, blockMgr) ;
        return track(blockMgr) ;
    }
    
    /** Create a Block Manager using direct access, no caching, no nothing. */
    public static BlockMgr createStdFileNoCache(String filename, int blockSize)
    {
        BlockAccess blockAccess = new BlockAccessDirect(filename, blockSize) ;
        BlockMgr blockMgr = new BlockMgrFileAccess(blockAccess, blockSize) ;
        return blockMgr ;
    }
    
    private static BlockMgr wrapFileAccess(BlockAccess blockAccess, int blockSize)
    {
        BlockMgr blockMgr = new BlockMgrFileAccess(blockAccess, blockSize) ;
        // This is a temporary fix to the problem 
        blockMgr = new BlockMgrFreeChain(blockMgr) ;
        return blockMgr ;
        
    }
}
