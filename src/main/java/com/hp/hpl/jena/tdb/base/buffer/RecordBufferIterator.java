/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.buffer;

import static org.openjena.atlas.lib.Lib.decodeIndex ;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.tdb.base.record.Record;

public class RecordBufferIterator implements Iterator<Record>
{
    private RecordBuffer rBuff ;
    private int nextIdx ;
    private Record slot = null ;
    private final Record maxRec ;
    private final Record minRec ;
    
    RecordBufferIterator(RecordBuffer rBuff)
    { this(rBuff, null, null); }
    
    RecordBufferIterator(RecordBuffer rBuff, Record minRecord, Record maxRecord)
    {
        this.rBuff = rBuff ;
        nextIdx = 0 ;
        minRec = minRecord ;
        if ( minRec != null )
        {
            nextIdx = rBuff.find(minRec) ;
            if ( nextIdx < 0 )
                nextIdx = decodeIndex(nextIdx) ;
        }
        
        maxRec = maxRecord ; 
    }

    private void finish()
    {
        rBuff = null ;
        nextIdx = -99 ;
        slot = null ;
    }
    
    //@Override
    public boolean hasNext()
    {
        if ( slot != null )
            return true ;
        if ( nextIdx < 0 )
            return false ;
        if ( nextIdx >= rBuff.size() )
        {
            finish() ;
            return false ;
        }
        
        slot = rBuff.get(nextIdx) ;
        if ( maxRec != null && Record.keyGE(slot, maxRec) )
        {
            // Finished - now to large
            finish() ;
            return false ;
        }
        nextIdx ++ ;
        return true ;
    }

    //@Override
    public Record next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("RecordBufferIterator") ;
        Record r = slot ;
        slot = null ;
        return r ;
    }

    //@Override
    public void remove()
    { throw new UnsupportedOperationException("RecordBufferIterator.remove") ; }
    
}


/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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