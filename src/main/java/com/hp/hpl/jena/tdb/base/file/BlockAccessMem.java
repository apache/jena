/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import static java.lang.String.format ;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/**
 * File access layer that simulates a disk in-memory - for testing, not written for efficiency.
 * There is a safe mode, whereby blocks are copied in and out to guarantee no writing to an unallocated block.
 * This is very inefficient but a better simulation of a disk.
 * 
 * @See BlockAccessByteArray
 */

public class BlockAccessMem implements BlockAccess
{
    public static boolean SafeMode = true ;
    static final boolean Checking = true ;
    boolean fileClosed = false ;
    private List<Block> blocks = new ArrayList<Block>() ;
    private final boolean safeModeThisMgr ;
    protected final int blockSize ;
    private final String label ;
    
    public BlockAccessMem(String label, int blockSize)
    {
        this(label, blockSize, SafeMode) ;
    }
    
    private BlockAccessMem(String label, int blockSize, boolean b)
    {
        this.blockSize = blockSize ;
        this.label = label ;
        safeModeThisMgr = b ;
    }

    @Override
    public Block allocate(int blkSize)
    {
        if ( blkSize > 0 && blkSize != this.blockSize )
            throw new FileException("Fixed blocksize only: request= "+blkSize+" / fixed size="+this.blockSize) ;
        
        int x = blocks.size() ;
        ByteBuffer bb = ByteBuffer.allocate(blkSize) ;
        Block block = new Block(x, bb) ;
        blocks.add(block) ;
        return block;
    }

    @Override
    public Block read(long id)
    {
        check(id) ;
        Block blk = blocks.get((int)id) ;
        if ( safeModeThisMgr ) 
            return blk.replicate() ;
        else
            return blk ;
    }

    @Override
    public void write(Block block)
    {
        check(block) ;
        if ( safeModeThisMgr )
            block = block.replicate() ;
        // Memory isn't scaling to multi gigabytes.
        blocks.set(block.getId().intValue(), block) ;
    }
    
    @Override
    public boolean isEmpty()
    {
        return blocks.isEmpty() ;
    }

    @Override
    public boolean valid(long id)
    {
        return id >= 0 && id < blocks.size() ;
    }

    @Override
    public void close()
    {
        fileClosed = true ;
        //blocks = null ;
    }
    
    @Override
    public void sync()
    {}
    
    private void check(Block block)
    {
        check(block.getId()) ;
        check(block.getByteBuffer()) ;
    }

    private void check(long id)
    {
        if ( id > Integer.MAX_VALUE )
            throw new FileException("BlockAccessMem: Bounds exception (id large than an int): "+id) ;
        if ( !Checking ) return ;
        if ( id < 0 || id >= blocks.size() )
            throw new FileException("BlockAccessMem: Bounds exception: "+id) ;
    }

    private void check(ByteBuffer bb)
    {
        if ( !Checking ) return ;
        if ( bb.capacity() != blockSize )
            throw new FileException(format("FileAccessMem: Wrong size block.  Expected=%d : actual=%d", blockSize, bb.capacity())) ;
        if ( bb.order() != SystemTDB.NetworkOrder )
            throw new FileException("BlockMgrMem: Wrong byte order") ;
    }

    @Override
    public String getLabel()
    {
        return label ;
    }
}
/*
 * (c) Copyright 2011 Epimorphics Ltd.
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