/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.core.Closeable;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.lib.Sync;

public interface Index extends Iterable<Record>, Sync, Closeable
{
    /** Find one record - and return the record actually in the index (may have a value part) */
    public Record find(Record record) ;
    
    /** Return whether the index contains the record or not. */
    public boolean contains(Record record) ;
    
    /** Add a record - return true if an insertion was actually needed */
    public boolean add(Record record) ;
    
    /** Delete a record.  Return true if a record was actually removed */
    public boolean delete(Record record) ;
    
//    /** Empty the index */
//    public boolean clear() ;

    /** Iterate over the whole index */ 
    public Iterator<Record> iterator() ;
    
    /** Get the Record factory associated with this index */
    public RecordFactory getRecordFactory() ;
    
    /** Syncrhonize with any persistent storage underlying the index */
    public void sync(boolean force) ;
    
    /** Close the index - can't not be used again through this object */
    public void close() ;
    
    /** Answer whether the index is empty or not.  May return false for unknown or meaningless
     * (e.g. transactional index)  */
    public boolean isEmpty() ;
    
    /** Perform checks on this index */
    public void check() ;
    
    /** Return size if known else return -1 : does not count the peristent storage */
    public long size() ;
    
    /** [testing] Count the nunber of triples in the index 
     * (+1 for succesful insertion, -1 for successful deletion)
     * Return Integer.MIN_VALUE for unknown.
     * "session" has no formal definition.
     */
    
    public long sessionTripleCount() ;
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