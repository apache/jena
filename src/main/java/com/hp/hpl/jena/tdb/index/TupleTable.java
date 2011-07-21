/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import static java.lang.String.format ;

import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.util.Iterator ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;
import org.openjena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** A TupleTable is a set of TupleIndexes.  The first TupleIndex is the "primary" index and must exist */
public class TupleTable implements Sync, Closeable
{
    private static Logger log = LoggerFactory.getLogger(TupleTable.class) ;
    
    private final TupleIndex[] indexes ;
    private final int tupleLen ;
    
    public TupleTable(int tupleLen, TupleIndex[] indexes)
    {
        this.tupleLen = tupleLen ;
        this.indexes = indexes ;
        if ( indexes[0] == null )
            throw new TDBException("TupleTable: no primary index") ;
        for ( TupleIndex index : indexes )
        {
            if ( index != null && index.getTupleLength() != tupleLen )
                throw new TDBException("Incompatible index: "+index.getLabel()) ;
        }
        
    }
    
    /** Insert a tuple - return true if it was really added, false if it was a duplicate */
    public boolean add( Tuple<NodeId> t) 
    { 
        if ( tupleLen != t.size() )
            throw new TDBException(format("Mismatch: inserting tuple of length %d into a table of tuples of length %d", t.size(), tupleLen)) ;

        for ( int i = 0 ; i < indexes.length ; i++ )
        {
            if ( indexes[i] == null ) continue ;
            if ( ! indexes[i].add(t) )
            {
                if ( i == 0 )
                {
                    duplicate(t) ;
                    return false ;
                }
                try {
                    OutputStream f = new FileOutputStream("LOG") ;
                    IndentedWriter w = new IndentedWriter(f) ;
                    ( (BPlusTree) ((TupleIndexRecord)indexes[i]).getRangeIndex() ).dump(w) ;
                    w.flush() ;
                    f.flush() ;
                    f.close() ;
                } catch ( IOException ex ) {}
                throw new TDBException(format("Secondary index duplicate: %s -> %s",indexes[i].getLabel(), t)) ;
            }
        }
        return true ;
    }

    protected void duplicate(Tuple<NodeId> t)
    { }

    /** Delete a tuple - return true if it was deleted, false if it didn't exist */
    public boolean delete( Tuple<NodeId> t ) 
    { 
        if ( tupleLen != t.size() )
            throw new TDBException(format("Mismatch: deleting tuple of length %d from a table of tuples of length %d", t.size(), tupleLen)) ;

        for ( int i = 0 ; i < indexes.length ; i++ )
        {
            if ( indexes[i] == null ) continue ;
            // Use return boolean
            indexes[i].delete(t) ;
//            if ( ! indexes[i].delete(t) )
//            {
//                if ( i == 0 )
//                {
//                    duplicate(t) ;
//                    return false ;
//                }
//                throw new TDBException("Secondary index duplicate: "+t) ;
//            }
        }
        return true ;

    }

    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any */
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern)
    {
//        for ( NodeId n : pattern.tuple() )
//        {
//            if ( n == null )
//                log.warn("find(Tuple<NodeId> pattern): Null found: "+pattern) ;
//        }
        
        if ( tupleLen != pattern.size() )
            throw new TDBException(format("Mismatch: finding tuple of length %d in a table of tuples of length %d", pattern.size(), tupleLen)) ;
        
        int numSlots = 0 ;
        // Canonical form. 
        for ( int i = 0 ; i < tupleLen ; i++ )
        {
            NodeId x = pattern.get(i) ;
            if ( ! NodeId.isAny(x) )
                numSlots++ ;
        }

        if ( numSlots == 0 )
            return indexes[0].all() ;
        
        int indexNumSlots = 0 ;
        TupleIndex index = null ;
        for ( int i = 0 ; i < indexes.length ; i++ )
        {
            TupleIndex idx = indexes[i] ;
            if ( idx != null )
            {
                int w = idx.weight(pattern) ;
                if ( w > indexNumSlots )
                {
                    indexNumSlots = w ;
                    index = idx ; 
                }
            }
        }
        
        if ( index == null )
            // No index at all.  Scan.
            index = indexes[0] ;

        return index.find(pattern) ;
    }
    
    @Override
    final public void close()
    {
        for ( TupleIndex idx : indexes )
        {
            if ( idx != null )
                idx.close();
        }
    }
    
//    public void dumpIndexes()
//    {
//        for ( TupleIndex idx : indexes )
//        {
//            if ( idx != null )
//                ;
//                //idx.dump();
//        }
//        
//    }
    
    @Override
    public void sync()
    {
        for ( TupleIndex idx : indexes )
        {
            if ( idx != null )
                idx.sync() ;
        }
    }

    public boolean isEmpty()        { return indexes[0].isEmpty() ; }
    
    public void clear()
    {
        for ( TupleIndex idx : indexes )
        {
            if ( idx != null )
                idx.clear() ;
        }
    }
    
    public long size()
    {
        return indexes[0].size() ;
    }
    
    /** Get i'th index */ 
    public TupleIndex getIndex(int i)                   { return indexes[i] ; }
    
    /** Get all indexes - for code that maipulates internal structures directly - use with care */ 
    public TupleIndex[] getIndexes()                    { return indexes ; }
    
    /** Get the width of tuples in indexes in this table */
    public int getTupleLen()                            { return tupleLen ; }

    /** Set index - for code that maipulates internal structures directly - use with care */ 
    public void setTupleIndex(int i, TupleIndex index)
    {
        if ( index != null && index.getTupleLength() != tupleLen )
            throw new TDBException("Incompatible index: "+index.getLabel()) ;
        indexes[i] = index ;
    }

    /** Number of indexes on this tuple table */
    public int numIndexes()                             { return indexes.length ; }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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