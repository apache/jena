/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrWrapper ;

// Recycle blocks.
public class BlockMgrFreeChain extends BlockMgrWrapper
{
    // Could keep Pair<Integer, ByteBuffer>
    List<Integer> freeBlocks = new ArrayList<Integer>() ;
    List<Integer> allocBlocks = new ArrayList<Integer>() ;
    
    public BlockMgrFreeChain(BlockMgr blockMgr)
    {
        super(blockMgr) ;
    }

    @Override
    public int allocateId()
    {
        if ( freeBlocks.size() > 0 )
        {
            Integer id = freeBlocks.remove(freeBlocks.size()-1) ;
            allocBlocks.add(id) ;
            return id ;
        }
        return super.allocateId() ;
    }

    @Override
    public ByteBuffer allocateBuffer(int id)
    {
        // Usually first (and only)
        Iterator<Integer> iter = allocBlocks.iterator() ;
        
        for ( ; iter.hasNext() ; ) 
        {
            Integer allocId = iter.next() ;
            if ( allocId == id )
            {
                iter.remove() ;
                return super.get(id) ;
            }
        }
        throw new TDBException("Failed to find allocated id '"+id+"' in alloc list") ;
    }

    @Override
    public void freeBlock(int id)
    {
        freeBlocks.add(id) ;
    }

    @Override
    public boolean valid(int id)
    {
        for ( Integer id2 : freeBlocks ) 
        {
            if ( id2 == id )
                return true ; 
        }
        for ( Integer id2 : allocBlocks ) 
        {
            if ( id2 == id )
                return true ; 
        }
        
        return super.valid(id) ;
    }

    @Override
    public void sync()
    {
        super.sync() ;
        
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