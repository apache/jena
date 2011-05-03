/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.io.File;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.FileAccess ;
import com.hp.hpl.jena.tdb.base.file.FileAccessMem ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.sys.SystemTDB;


public class BlockMgrFactory
{
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
        FileAccess file = new FileAccessMem(blockSize) ;
        BlockMgr blockMgr = new BlockMgrFileAccess(file, blockSize) ;
        // Small cache - testing.
        //blockMgr = new BlockMgrCache(indexName, 3, 3, blockMgr) ;
        return blockMgr ;
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
        return new BlockMgrMapped(filename, blockSize) ;
    }
    
    /** Create a Block Manager using direct access (and a cache) */
    public static BlockMgr createStdFile(String filename, int blockSize, int readBlockCacheSize, int writeBlockCacheSize)
    {
        BlockMgr blockMgr = new BlockMgrDirect(filename, blockSize) ;
        String fn = filename ;
        
        int j = filename.lastIndexOf(File.separatorChar) ;
        if ( j > 0 )
            fn = filename.substring(j+1) ;
        
        blockMgr = new BlockMgrCache(fn, readBlockCacheSize, writeBlockCacheSize, blockMgr) ;
        return blockMgr ;
    }
    
    /** Create a Block Manager using direct access */
    public static BlockMgr createStdFileNoCache(String filename, int blockSize)
    {
        BlockMgr blockMgr = new BlockMgrDirect(filename, blockSize) ;
        return blockMgr ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */