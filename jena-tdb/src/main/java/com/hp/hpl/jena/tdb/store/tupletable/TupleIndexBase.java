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

package com.hp.hpl.jena.tdb.store.tupletable;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.Tuple ;


import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public abstract class TupleIndexBase implements TupleIndex
{
    private static final boolean Check = false ;

    protected final ColumnMap colMap ;
    protected final int tupleLength ;

    private final String name ;
    
    protected TupleIndexBase(int N, ColumnMap colMapping, String name)
    {
        this.tupleLength = N ;
        this.colMap = colMapping ;
        this.name = name ;
    }
    
    /** Add tuple worker: Tuple passed in unmapped (untouched) order */
    protected abstract boolean performAdd(Tuple<NodeId> tuple) ;
    
    /** Delete tuple worker: Tuple passed in unmaped (untouched) order */
    protected abstract boolean performDelete(Tuple<NodeId> tuple) ;
    
    /** Find tuples worker: Tuple passed in unmaped (untouched) order */
    protected abstract Iterator<Tuple<NodeId>> performFind(Tuple<NodeId> tuple) ;

    /** Insert a tuple - return true if it was really added, false if it was a duplicate */
    @Override
    public final boolean add(Tuple<NodeId> tuple) 
    { 
        if ( Check )
        {
            if ( tupleLength != tuple.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", tuple.size(), tupleLength)) ;
        }
        return performAdd(tuple) ;
    }
    /** Delete a tuple - return true if it was deleted, false if it didn't exist */
    @Override
    public final boolean delete(Tuple<NodeId> tuple) 
    { 
        if ( Check )
        {
            if ( tupleLength != tuple.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", tuple.size(), tupleLength)) ;
        }

        return performDelete(tuple) ;
    }

    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any.
     *  Input pattern in natural order, not index order.
     */
    @Override
    public final Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern)
    {
        if ( Check )
        {
            if ( tupleLength != pattern.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", pattern.size(), tupleLength)) ;
        } 
        // null to NodeId.NodIdAny ??
        return performFind(pattern) ;
    }
    
    @Override
    public final int weight(Tuple<NodeId> pattern)
    {
        for ( int i = 0 ; i < tupleLength ; i++ )
        {
            NodeId X = colMap.fetchSlot(i, pattern) ;
            if ( undef(X) )
                // End of fixed terms
                return i ;
        }
        return tupleLength ;
    }
    
    @Override
    public final String getMapping()          { return colMap.getLabel() ; }

    @Override
    public final String getName()           { return name ; }

    @Override
    public final int getTupleLength()       { return tupleLength ; }

    @Override
    public final ColumnMap getColumnMap()   { return colMap ;  }
    
    protected final boolean undef(NodeId x)
    { return NodeId.isAny(x) ; }
    
    @Override
    public String toString() { return "index:"+getName() ; }
}
