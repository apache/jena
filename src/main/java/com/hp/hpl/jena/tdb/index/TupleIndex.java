/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import java.util.Iterator;

import org.openjena.atlas.lib.Tuple ;



import com.hp.hpl.jena.sparql.core.Closeable;

import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.store.NodeId;

public interface TupleIndex extends Sync, Closeable
{
    /** Insert a tuple - return true if it was really added, false if it was a duplicate */
    public boolean add(Tuple<NodeId> tuple) ;

    /** Delete a tuple - return true if it was deleted, false if it didn't exist */
    public boolean delete(Tuple<NodeId> tuple) ; 
    
    public String getLabel() ; 
    //public ColumnMap getColMap() { return colMap ;  }
    
    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any.
     *  Input pattern in natural order, not index order.
     */

    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern) ;
    
    /** return an iterator of everything */
    public Iterator<Tuple<NodeId>> all() ;
    
    /** Weight a pattern - specified in normal order (not index order) */
    public int weight(Tuple<NodeId> pattern) ;

    /** Length of tuple supported */
    public int getTupleLength() ;

    /** Size of index (number of slots). May be an estimate and not exact. -1 for unknown.  */
    public long size() ;

    /** Answer whether empty or not */
    public boolean isEmpty() ;
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