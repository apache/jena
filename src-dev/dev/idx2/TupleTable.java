/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import java.util.Iterator;

import lib.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Closeable;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

/** A TupleTable is a set of TupleIndexes.  The first TupleIndex is the "primary" index and must exist */
public class TupleTable implements Sync, Closeable
{
    private static Logger log = LoggerFactory.getLogger(TupleTable.class) ;
    
    private TupleIndex[] indexes = null ;
    private Location location ;

    private int len ;
    private RecordFactory factory ;
    
    protected TupleTable(int N, TupleIndex[] indexes, RecordFactory factory, Location location)
    {
        this.len = N ;
        this.indexes = indexes ;
        this.factory = factory ;
        this.location = location ;
        if ( indexes[0] == null )
            throw new TDBException("TupleTable: no primary index") ; 
        
    }
    
    /** Insert a tuple - return true if it was really added, false if it was a duplicate */
    public boolean add( Tuple<NodeId> t) 
    { 
        if ( len != t.size() )
            throw new TDBException(String.format("Mismatch: inserting tuple of length %d into a table of tuples of length %d", t.size(), len)) ;

        for ( int i = 0 ; i < len ; i++ )
        {
            if ( indexes[i] == null ) continue ;
            if ( ! indexes[i].add(t) )
            {
                if ( i == 0 )
                {
                    duplicate(t) ;
                    return false ;
                }
                throw new TDBException("Secondary index duplicate: "+t) ;
            }
        }
        return true ;
    }

    private void duplicate(Tuple<NodeId> t)
    { }

    /** Delete a tuple - return true if it was deleted, false if it didn't exist */
    public boolean delete( Tuple<NodeId> t ) 
    { 
        if ( len != t.size() )
            throw new TDBException(String.format("Mismatch: deleting tuple of length %d from a table of tuples of length %d", t.size(), len)) ;

        for ( int i = 0 ; i < len ; i++ )
        {
            if ( indexes[i] == null ) continue ;
            if ( ! indexes[i].delete(t) )
            {
                if ( i == 0 )
                {
                    duplicate(t) ;
                    return false ;
                }
                throw new TDBException("Secondary index duplicate: "+t) ;
            }
        }
        return true ;

    }

    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any */
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern)
    {
        if ( len != pattern.size() )
            throw new TDBException(String.format("Mismatch: finding tuple of length %d in a table of tuples of length %d", pattern.size(), len)) ;
        
        NodeId[] pattern2 = new NodeId[pattern.size()] ;
        int numSlots = 0 ;
        // Canonical form. 
        for ( int i = 0 ; i < pattern.size() ; i++ )
        {
            pattern2[i] = pattern.get(i) ;
            if ( pattern2[i] == NodeId.NodeIdAny )
                pattern2[i] = null ;
            if ( pattern2[i] != null )
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
                //Tuple<NodeId> mapped = idx.getColMap().map(pattern) ;
                Tuple<NodeId> mapped = pattern ;
                int w = idx.weight(mapped) ;
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

        return index.findOrScan(pattern) ;
    }
    
    @Override
    final public void close()
    {
        for ( int i = 0 ; i < indexes.length ; i++ )
        {
            TupleIndex idx = indexes[i] ;
            if ( idx != null )
                idx.close();
        }
    }
    
    public void dumpIndexes()
    {
        for ( int i = 0 ; i < indexes.length ; i++ )
        {
            TupleIndex idx = indexes[i] ;
            if ( idx != null )
                ;
                //idx.dump();
        }
        
    }
    
    @Override
    public void sync(boolean force)
    {
        for ( int i = 0 ; i < indexes.length ; i++ )
        {
            TupleIndex idx = indexes[i] ;
            if ( idx != null )
                idx.sync(force) ;
        }
    }

    // Getters and setters - use with care,
    public Location getLocation()                       { return location ; }
    public TupleIndex getIndex(int i)                   { return indexes[i] ; }
    public void setTupleIndex(int i, TupleIndex index)  { indexes[i] = index ; }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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