/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.tdb2.store.tupletable;

import static java.lang.String.format ;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** A TupleTable is a set of TupleIndexes.  The first TupleIndex is the "primary" index and must exist */
public class TupleTable implements Sync, Closeable
{
    private static Logger log = LoggerFactory.getLogger(TupleTable.class) ;
    
    private final TupleIndex[] indexes ;
    private final TupleIndex   scanAllIndex ;   // Use this index if a complete scan is needed.
    private final int tupleLen ;
    private boolean syncNeeded = false ;
    
    public TupleTable(int tupleLen, TupleIndex[] indexes)
    {
        this.tupleLen = tupleLen ;
        this.indexes = indexes ;
        if ( indexes[0] == null )
            throw new TDBException("TupleTable: no primary index") ;
        for ( TupleIndex index : indexes )
        {
            if ( index != null && index.getTupleLength() != tupleLen )
                throw new TDBException("Incompatible index: "+index.getMappingStr()) ;
        }
        scanAllIndex = chooseScanAllIndex(tupleLen, indexes) ;
    }
    
    /** Choose an index to scan in case we are asked for everything
     * This needs to be ???G for the distinctAdjacent filter in union query to work.
     */
    private static TupleIndex chooseScanAllIndex(int tupleLen, TupleIndex[] indexes)
    {
        if ( tupleLen != 4 )
            return indexes[0] ;
        
        for ( TupleIndex index : indexes )
        {
            // First look for SPOG
            if ( index.getName().equals("SPOG") )
                return index ;
        }
        
        for ( TupleIndex index : indexes )
        {
            // Then look for any ???G
            if ( index.getName().endsWith("G") )
                return index ;
        }
        
        Log.warn(SystemTDB.errlog, "Did not find a ???G index for full scans") ;
        return indexes[0] ;
    }

    /** Insert a tuple */
    public void add(Tuple<NodeId> t) {
        // A "contains test" could be used to avoid needing to hit all
        // the indexes when the triple is already present.
        if ( tupleLen != t.len() )
            throw new TDBException(format("Mismatch: inserting tuple of length %d into a table of tuples of length %d", t.len(), tupleLen)) ;
        for ( int i = 0 ; i < indexes.length ; i++ ) {
            if ( indexes[i] == null ) continue ;
            indexes[i].add(t) ;
            syncNeeded = true ;
        }
    }

    /** Insert tuples */
    public void addAll(List<Tuple<NodeId>> t) {
        // Parallel.
        for ( int i = 0 ; i < indexes.length ; i++ ) {
            if ( indexes[i] == null ) continue ;
            indexes[i].addAll(t) ;
            syncNeeded = true ;
        }
    }

    /** Delete a tuple */
    public void delete( Tuple<NodeId> t ) { 
        if ( tupleLen != t.len() )
            throw new TDBException(format("Mismatch: deleting tuple of length %d from a table of tuples of length %d", t.len(), tupleLen)) ;

        for ( TupleIndex index : indexes ) {
            if ( index == null )
                continue;
            index.delete( t );
        }
    }
    
    /** Delete tuples */
    public void deleteAll(List<Tuple<NodeId>> t) {
        // Parallel.
        for ( int i = 0 ; i < indexes.length ; i++ ) {
            if ( indexes[i] == null ) continue ;
            indexes[i].deleteAll(t) ;
            syncNeeded = true ;
        }
    }

    /** Find all matching tuples - a slot of NodeId.NodeIdAny means match any */
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern) {
        if ( tupleLen != pattern.len() )
            throw new TDBException(format("Mismatch: finding tuple of length %d in a table of tuples of length %d", pattern.len(), tupleLen)) ;
        
        int numSlots = 0 ;
        // Canonical form. 
        for ( int i = 0 ; i < tupleLen ; i++ ) {
            NodeId x = pattern.get(i) ;
            if ( ! NodeId.isAny(x) )
                numSlots++ ;
            if ( NodeId.isDoesNotExist(x))
                return Iter.nullIterator();
        }

        if ( numSlots == 0 )
            return scanAllIndex.all() ;
        
        int indexNumSlots = 0 ;
        TupleIndex index = null ;
        for ( TupleIndex idx : indexes ) {
            if ( idx != null ) {
                int w = idx.weight( pattern );
                if ( w > indexNumSlots ) {
                    indexNumSlots = w;
                    index = idx;
                }
            }
        }
        
        if ( index == null )
            // No index at all.  Scan.
            index = indexes[0] ;
        return index.find(pattern) ;
    }
    
    @Override
    final public void close() {
        for ( TupleIndex idx : indexes ) {
            if ( idx != null )
                idx.close();
        }
    }
    
    @Override
    public void sync() {
        if ( syncNeeded ) {
            for ( TupleIndex idx : indexes ) {
                if ( idx != null )
                    idx.sync() ;
            }
            syncNeeded = false ;
        }
    }

    public boolean isEmpty()        { return indexes[0].isEmpty() ; }
    
    public void clear() {
        for ( TupleIndex idx : indexes ) {
            if ( idx != null )
                idx.clear() ;
        }
        syncNeeded = true ;
    }
    
    public long size() {
        return indexes[0].size() ;
    }
    
    /** Get i'th index */ 
    public TupleIndex getIndex(int i)                   { return indexes[i] ; }
    
    /** Get all indexes - for code that manipulates internal structures directly - use with care */ 
    public TupleIndex[] getIndexes()                    { return indexes ; }
    
    /** Get the width of tuples in indexes in this table */
    public int getTupleLen()                            { return tupleLen ; }

    /** Set index - for code that manipulates internal structures directly - use with care */ 
    public void setTupleIndex(int i, TupleIndex index) {
        if ( index != null && index.getTupleLength() != tupleLen )
            throw new TDBException("Incompatible index: " + index.getMappingStr()) ;
        indexes[i] = index ;
    }

    /** Number of indexes on this tuple table */
    public int numIndexes()                             { return indexes.length ; }
}
