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

package com.hp.hpl.jena.tdb.base.block ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.file.* ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class BlockMgrFactory {
    // This isn't always helpful so be careful if setting the default to "true".
    // Sometimes the tracking is too strict
    // e.g. transactions keep blocks and not release them down the layers.
    public/* final */static boolean AddTracker = false ;

    public static BlockMgr tracker(BlockMgr blockMgr) {
        if ( blockMgr instanceof BlockMgrTracker )
            return blockMgr ;
        return BlockMgrTracker.track(blockMgr) ;
    }

    /** Add a tracker if the system default is to do so */
    private static BlockMgr track(BlockMgr blockMgr) {
        if ( !AddTracker )
            return blockMgr ;
        return tracker(blockMgr) ;
    }

    public static BlockMgr create(FileSet fileSet, String ext, BlockParams params) {
        return create(fileSet, ext,
                      params.getFileMode(),
                      params.getBlockSize(),
                      params.getBlockReadCacheSize(),
                      params.getBlockWriteCacheSize()) ;
    }
    
    public static BlockMgr create(FileSet fileSet, String ext, int blockSize, int readBlockCacheSize, int writeBlockCacheSize) {
        return create(fileSet, ext, null, blockSize, readBlockCacheSize, writeBlockCacheSize) ;
    }

    // XXX Deprecate?
    public static BlockMgr create(FileSet fileSet, String ext, FileMode fileMode, int blockSize, int readBlockCacheSize, int writeBlockCacheSize) {
        if ( fileSet.isMem() )
            return createMem(fileSet.filename(ext), blockSize) ;
        else
            return createFile(fileSet.filename(ext), fileMode, blockSize, readBlockCacheSize, writeBlockCacheSize) ;
    }

    /** Create an in-memory block manager */
    public static BlockMgr createMem(String indexName, int blockSize) {
        BlockAccess file = new BlockAccessMem(indexName, blockSize) ;
        BlockMgr blockMgr = new BlockMgrFileAccess(file, blockSize) ;
        blockMgr = new BlockMgrFreeChain(blockMgr) ;
        // Small cache - testing.
        // blockMgr = new BlockMgrCache(indexName, 3, 3, blockMgr) ;
        return track(blockMgr) ;
    }

    /** Create a BlockMgr backed by a real file */
    public static BlockMgr createFile(String filename, BlockParams params) {
        return createFile(filename, 
                          params.getFileMode(), params.getBlockSize(),
                          params.getBlockReadCacheSize(), params.getBlockWriteCacheSize()) ;
    }

        /** Create a BlockMgr backed by a real file */
    public static BlockMgr createFile(String filename, FileMode fileMode, int blockSize, int readBlockCacheSize, int writeBlockCacheSize) {
        if ( fileMode == null )
            fileMode = SystemTDB.fileMode() ;
        switch (fileMode) {
            case mapped :
                return createMMapFile(filename, blockSize) ;
            case direct :
                return createStdFile(filename, blockSize, readBlockCacheSize, writeBlockCacheSize) ;
        }
        throw new TDBException("Unknown file mode: " + fileMode) ;
    }

    /** Create a NIO Block Manager */
    public static BlockMgr createMMapFile(String filename, int blockSize) {
        BlockAccess file = new BlockAccessMapped(filename, blockSize) ;
        BlockMgr blockMgr = wrapFileAccess(file, blockSize) ;
        return track(blockMgr) ;
    }

    /** Create a Block Manager using direct access (and a cache) */
    public static BlockMgr createStdFile(String filename, int blockSize, int readBlockCacheSize, int writeBlockCacheSize) {
        BlockAccess file = new BlockAccessDirect(filename, blockSize) ;
        BlockMgr blockMgr = wrapFileAccess(file, blockSize) ;
        blockMgr = addCache(blockMgr, readBlockCacheSize, writeBlockCacheSize) ;
        return track(blockMgr) ;
    }

    /** Create a Block Manager using direct access, no caching, no nothing. */
    public static BlockMgr createStdFileNoCache(String filename, int blockSize) {
        BlockAccess blockAccess = new BlockAccessDirect(filename, blockSize) ;
        BlockMgr blockMgr = new BlockMgrFileAccess(blockAccess, blockSize) ;
        return blockMgr ;
    }

    /**
     * Add a caching layer to a BlockMgr.
     * <p>
     * This does not make sense for memory BlockMgr or for memory mapper files.
     * This function always add the cache.
     * 
     * @see #addCache(BlockMgr, FileSet, FileMode, int, int)
     */
    public static BlockMgr addCache(BlockMgr blockMgr, int readBlockCacheSize, int writeBlockCacheSize) {
        if ( blockMgr instanceof BlockMgrCache )
            Log.warn(BlockMgrFactory.class, "BlockMgr already has a cache: " + blockMgr.getLabel()) ;
        return BlockMgrCache.create(readBlockCacheSize, writeBlockCacheSize, blockMgr) ;
    }

    /**
     * Add a caching layer to a BlockMgr if appropriate. This does not make
     * sense for memory BlockMgr or for memory mapper files. These are skipped.
     */
    public static BlockMgr addCache(BlockMgr blockMgr, FileSet fileSet, FileMode fileMode, int readBlockCacheSize, int writeBlockCacheSize) {
        if ( fileSet.isMem() )
            return blockMgr ;
        if ( fileMode == null )
            fileMode = SystemTDB.fileMode() ;
        if ( fileMode == FileMode.mapped )
            return blockMgr ;
        return addCache(blockMgr, readBlockCacheSize, writeBlockCacheSize) ;
    }

    private static BlockMgr wrapFileAccess(BlockAccess blockAccess, int blockSize) {
        BlockMgr blockMgr = new BlockMgrFileAccess(blockAccess, blockSize) ;
        // This is a temporary fix to the problem
        blockMgr = new BlockMgrFreeChain(blockMgr) ;
        return blockMgr ;
    }
}
