/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.nio.ByteBuffer;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;

import com.hp.hpl.jena.tdb.sys.Session ;

public interface BlockMgr extends Sync, Closeable, Session
{
    /** Allocate an uninitialized slot.  Fill with a .put */ 
    public int allocateId();

    /** Allocate a buffer.  Associate with an id via put(id, buffer) */ 
    public ByteBuffer allocateBuffer(int id) ;
    
    /** Answer whether there are any blocks in the collection being managed */
    public boolean isEmpty() ; 
    
    /** Block size */
    public int blockSize() ;
    
    // Renaming?
    // get->allocate
    // pin and unpin 
    // put->write
    
    /** Fetch a block */
    public ByteBuffer get(int id);
    
    /** Block is no longer being worked on - do not use after this call - get() it again */ 
    public void put(int id, ByteBuffer block);

    /** Announce a block is no longer in use (i.e it's now freed) */ 
    public void freeBlock(int id);
    
    public boolean valid(int id) ;
    
    /** Close the block manager */
    public void close() ;
    
    /** Is this block manager stil usable?  Close block managers can not perform any operations except this one. */  
    public boolean isClosed() ; 
    
    /** Sync the block manager */
    public void sync() ;
    
    /** Signal the start of an update operation */
    public void startUpdate();
    
    /** Signal the completion of an update operation */
    public void finishUpdate();

    /** Signal the start of an update operation */
    public void startRead();
    
    /** Signal the completeion of an update operation */
    public void finishRead();
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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