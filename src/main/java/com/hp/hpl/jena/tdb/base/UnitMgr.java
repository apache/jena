/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.sys.Session ;

public interface UnitMgr<T> extends Sync, Closeable, Session
{
    /** Fetch, for use for read only */
    public T getRead(int id);
    
    /** Fetch, use for write and read - only inside "update" */
    public T getWrite(int id);

    /** Release a T, unmodified. */
    public void releaseRead(T block) ;
    
    /** Release a T, obtained via getWrite, unmodified. */
    public void releaseWrite(T block) ;

    /** Promote to writeable : it's OK to promote an already writeable T */ 
    public T promote(T block);

    /** T is no longer being worked on - do not use after this call - get() it again */ 
    public void put(T block) ;

    /** Announce a block is no longer in use (i.e it's now freed) */ 
    public void free(Block block);
  
    /** Is this a valid block id? (may be a free block)*/
    public boolean valid(int id) ;
    
//    /** Sync the manager */
//    @Override
//    public void sync() ;
//    
//    /** Signal the start of update operations */
//    @Override
//    public void startUpdate();
//    
//    /** Signal the completion of update operations */
//    @Override
//    public void finishUpdate();
//
//    /** Signal the start of read operations */
//    @Override
//    public void startRead();
//
//    /** Signal the completeion of read operations */
//    @Override
//    public void finishRead();
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