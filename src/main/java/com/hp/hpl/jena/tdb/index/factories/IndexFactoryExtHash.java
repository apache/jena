/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * (c) Copyright 2010 IBM Corp. All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.factories;

import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.file.FileFactory;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.PlainFile;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexFactory;
import com.hp.hpl.jena.tdb.index.ext.ExtHash;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SetupTDB;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Index factory for extendible hash tables.
 *  Only an index, not a range index
 */

public class IndexFactoryExtHash implements IndexFactory
{
    private final int blockSize ;

    public IndexFactoryExtHash()
    { this(SystemTDB.BlockSize) ; }
    
    public IndexFactoryExtHash(int blockSize)
    {
        this.blockSize = blockSize ;
    }
    
    @Override
    public Index createIndex(FileSet fileset, RecordFactory recordFactory)
    {
        String fnDictionary = fileset.filename(Names.extHashExt) ;
        PlainFile dictionary = FileFactory.createPlainFileDisk(fnDictionary) ;
        
        String fnBuckets = fileset.filename(Names.extHashBucketExt) ;
        BlockMgr mgr =  createBlockMgr(fnBuckets, blockSize) ;
        ExtHash eHash = new ExtHash(dictionary, recordFactory, mgr) ;
        return eHash ;
    }
    
    protected BlockMgr createBlockMgr(String filename, int blockSize)
    {
        return BlockMgrFactory.createFile(filename, blockSize, 
                                          SetupTDB.systemInfo.getBlockReadCacheSize(), //SystemTDB.BlockReadCacheSize,
                                          SetupTDB.systemInfo.getBlockWriteCacheSize()) ; //SystemTDB.BlockWriteCacheSize) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * (c) Copyright 2010 IBM Corp. All rights reserved.
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