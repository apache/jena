/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openjena.atlas.iterator.PeekIterator;

import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;

/** From a stream of RecordBufferPage,  manage the link fields.
 * That is, be a one slot delay so that the "link" field can point to the next page.
 * Be careful about the last block.   
 *
 */
class RecordBufferPageLinker implements Iterator<RecordBufferPage>
{
    PeekIterator<RecordBufferPage> peekIter ;
    
    RecordBufferPage slot = null ;
    
    RecordBufferPageLinker(Iterator<RecordBufferPage> iter)
    {
        if ( ! iter.hasNext() )
        {
            peekIter = null ;
            return ;
        }
        
        peekIter = new PeekIterator<RecordBufferPage>(iter) ;
    }
    
    @Override
    public boolean hasNext()
    {
        if ( slot != null )
            return true ;
        
        if ( peekIter == null )
            return false ;

        if ( ! peekIter.hasNext() )
        {
            peekIter = null ;
            return false ;
        }
        
        slot = peekIter.next() ;
        RecordBufferPage nextSlot = peekIter.peekOrNull() ;
        // If null, no slot ahead so no linkage field to set.
        if ( nextSlot != null )
            // Set the slot to the id of the next one
            slot.setLink(nextSlot.getId()) ;
        return true ;
    }
    
    @Override
    public RecordBufferPage next()
    {
        if ( ! hasNext() ) throw new NoSuchElementException() ;
        RecordBufferPage rbp = slot ;
        slot = null ;
        return rbp ;
    }
    
    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }
}
/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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