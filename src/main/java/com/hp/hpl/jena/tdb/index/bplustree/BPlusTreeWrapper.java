/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import java.util.Iterator ;

import com.hp.hpl.jena.tdb.base.record.Record ;

public class BPlusTreeWrapper // implement BPlusTree
{
    private final BPlusTree bpt ;
    
    BPlusTreeWrapper(BPlusTree bpt) { this.bpt = bpt ; }
    
    public Record find(Record record)
    { return bpt.find(record) ; }
    
    public boolean contains(Record record)
    { return bpt.contains(record) ; }
    
    public Record minKey()
    { return bpt.minKey() ; }
    
    public Record maxKey()
    { return bpt.maxKey() ; }
    
    public boolean add(Record record)
    { return bpt.add(record) ; }
    
    public Record addAndReturnOld(Record record)
    { return bpt.addAndReturnOld(record) ; }
    
    public boolean delete(Record record)
    { return bpt.delete(record) ; }
    
    public Record deleteAndReturnOld(Record record)
    { return bpt.deleteAndReturnOld(record) ; }
    
    public Iterator<Record> iterator()
    { return bpt.iterator() ; }
    
    public Iterator<Record> iterator(Record minRec, Record maxRec)
    { return bpt.iterator(minRec, maxRec) ; }
    
    public boolean isEmpty()
    { return bpt.isEmpty() ; }
    
    public void clear()
    { bpt.clear() ; }
    
    public void sync()
    { bpt.sync() ; }
    
    public void close()
    { bpt.close() ; }
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