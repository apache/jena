/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import java.util.Iterator ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;

public class RangeIndexWrapper implements RangeIndex
{
    private final RangeIndex rIndex ;
    
    public RangeIndexWrapper(RangeIndex rIdx) { this.rIndex = rIdx ; }
    
    @Override
    public Record find(Record record)
    { return rIndex.find(record) ; }
    
    @Override
    public boolean contains(Record record)
    { return rIndex.contains(record) ; }
    
    @Override
    public Record minKey()
    { return rIndex.minKey() ; }
    
    @Override
    public Record maxKey()
    { return rIndex.maxKey() ; }
    
    @Override
    public boolean add(Record record)
    { return rIndex.add(record) ; }
    
    @Override
    public boolean delete(Record record)
    { return rIndex.delete(record) ; }
    
//  public Record addAndReturnOld(Record record)
//  { return bpt.addAndReturnOld(record) ; }
  
//    public Record deleteAndReturnOld(Record record)
//    { return bpt.deleteAndReturnOld(record) ; }
    
    @Override
    public Iterator<Record> iterator()
    { return rIndex.iterator() ; }
    
    @Override
    public Iterator<Record> iterator(Record minRec, Record maxRec)
    { return rIndex.iterator(minRec, maxRec) ; }
    
    @Override
    public boolean isEmpty()
    { return rIndex.isEmpty() ; }
    
    @Override
    public void clear()
    { rIndex.clear() ; }
    
    @Override
    public void sync()
    { rIndex.sync() ; }
    
    @Override
    public void close()
    { rIndex.close() ; }

    public RangeIndex getWrapped()
    { return rIndex ; }
    
    @Override
    public RecordFactory getRecordFactory()
    { return rIndex.getRecordFactory() ; }

    @Override
    public void check()
    { rIndex.check() ; }

    @Override
    public long size()
    { return rIndex.size() ; }

    @Override
    public long sessionTripleCount()
    { return rIndex.sessionTripleCount() ; }
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