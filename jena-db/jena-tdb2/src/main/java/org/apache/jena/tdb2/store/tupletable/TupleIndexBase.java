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

import java.util.Iterator ;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.NodeId;

public abstract class TupleIndexBase implements TupleIndex
{
    private static final boolean Check = false ;

    protected final TupleMap tupleMap ;
    protected final int tupleLength ;

    private final String name ;
    
    protected TupleIndexBase(int N, TupleMap indexMapping, String name)
    {
        this.tupleLength = N ;
        this.tupleMap = indexMapping ;
        this.name = name ;
    }
    
    @Override
    public TupleIndex wrapped() {
        return null ;
    }
    
    /** Add tuple worker: Tuple passed in unmapped (untouched) order */
    protected abstract void performAdd(Tuple<NodeId> tuple) ;
    
    /** Delete tuple worker: Tuple passed in unmapped (untouched) order */
    protected abstract void performDelete(Tuple<NodeId> tuple) ;
    
    /** Find tuples worker: Tuple passed in unmaped (untouched) order */
    protected abstract Iterator<Tuple<NodeId>> performFind(Tuple<NodeId> tuple) ;

    /** Insert a tuple */
    @Override
    public final void add(Tuple<NodeId> tuple) 
    { 
        if ( Check ) {
            if ( tupleLength != tuple.len() )
                throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", tuple.len(), tupleLength));
        }
        performAdd(tuple) ;
    }
    /** Delete a tuple */
    @Override
    public final void delete(Tuple<NodeId> tuple) 
    { 
        if ( Check ) {
            if ( tupleLength != tuple.len() )
                throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", tuple.len(), tupleLength));
        }

        performDelete(tuple) ;
    }

    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any.
     *  Input pattern in natural order, not index order.
     */
    @Override
    public final Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern)
    {
        if ( Check ) {
            if ( tupleLength != pattern.len() )
                throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", pattern.len(), tupleLength));
        }
        // null to NodeId.NodIdAny ??
        return performFind(pattern) ;
    }
    
    @Override
    public final int weight(Tuple<NodeId> pattern)
    {
        for ( int i = 0 ; i < tupleLength ; i++ )
        {
            NodeId X = tupleMap.mapSlot(i, pattern) ;
            if ( undef(X) )
                // End of fixed terms
                return i ;
        }
        return tupleLength ;
    }
    
    @Override
    public final String getMappingStr()     { return tupleMap.getLabel() ; }

    @Override
    public final String getName()           { return name ; }

    @Override
    public final int getTupleLength()       { return tupleLength ; }

    @Override
    public final TupleMap getMapping()      { return tupleMap ;  }
    
    protected final boolean undef(NodeId x)
    { return NodeId.isAny(x) ; }
    
    @Override
    public String toString() { return "index:"+getName() ; }
}
