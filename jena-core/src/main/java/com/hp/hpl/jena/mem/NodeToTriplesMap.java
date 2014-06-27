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

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.*;
import com.hp.hpl.jena.util.iterator.*;

/**
	NodeToTriplesMap: a map from nodes to sets of triples.
	Subclasses must override at least one of useXXXInFilter methods.
*/
public class NodeToTriplesMap extends NodeToTriplesMapBase 
    {    
    public NodeToTriplesMap( Field indexField, Field f2, Field f3 )
        { super( indexField, f2, f3 ); }

    @Override public boolean add( Triple t ) 
        {
        Object o = getIndexField( t );
        OpenSetBunch s = (OpenSetBunch) bunchMap.get( o );
        if (s == null) bunchMap.put( o, s = createSetBunch() );
        if (s.baseSet().add( t )) { size += 1; return true; } else return false; 
        }

    private static class OpenSetBunch extends SetBunch
        {
        private static final TripleBunch empty = new ArrayBunch();
        
        public OpenSetBunch()
            { super( empty ); }
        
        public Set<Triple> baseSet()
            { return elements; }
        }
    
    private OpenSetBunch createSetBunch()
        { return new OpenSetBunch(); }
    
    @Override public boolean remove( Triple t )
        { 
        Object o = getIndexField( t );
        OpenSetBunch s = (OpenSetBunch) bunchMap.get( o );
        if (s == null)
            return false;
        else
            {
            Set<Triple> base = s.baseSet();
            boolean result = base.remove( t );
            if (result) size -= 1;
            if (base.isEmpty()) bunchMap.remove( o );
            return result;
        	} 
        }
    
    @Override public ExtendedIterator<Triple> iterator( Object o, HashCommon.NotifyEmpty container )
        {
        TripleBunch b = bunchMap.get( o );
        return b == null ? NullIterator.<Triple>instance() : b.iterator();
        }
    
    @Override public boolean contains( Triple t )
        { 
        TripleBunch s = bunchMap.get( getIndexField( t ) );
        return s == null ? false : s.contains( t );
        }

    protected static boolean equalsObjectOK( Triple t )
        { 
        Node o = t.getObject();
        return o.isLiteral() ? o.getLiteralDatatype() == null : true;
        }

    @Override
    public boolean containsBySameValueAs( Triple t )
        { return equalsObjectOK( t ) ? contains( t ) : slowContains( t ); }
    
    protected boolean slowContains( Triple t )
        { 
        TripleBunch s = bunchMap.get( getIndexField( t ) );
        if (s == null)
            return false;
        else
            {
            Iterator<Triple> it = s.iterator();
            while (it.hasNext()) if (t.matches( it.next() )) return true;
            return false;
            }
        }
    
    public ExtendedIterator<Triple> iterateAll( Triple pattern )
        {
        return
            indexField.filterOn( pattern )
            .and( f2.filterOn( pattern ) )
            .and( f3.filterOn( pattern ) )
            .filterKeep( iterateAll() )
            ;
        }

    @Override public ExtendedIterator<Triple> iterator( Node index, Node n2, Node n3 )
        {
        TripleBunch s = bunchMap.get( index.getIndexingValue() );
        return s == null
            ? NullIterator.<Triple>instance()
            : f2.filterOn( n2 ).and( f3.filterOn( n3 ) )
                .filterKeep( s.iterator() )
            ;
        }

    /**
        Answer an iterator over all the triples that are indexed by the item <code>y</code>.
        Note that <code>y</code> need not be a Node (because of indexing values).
    */
    @Override public Iterator<Triple> iteratorForIndexed( Object y )
        { return get( y ).iterator();  }
    
    /** 
        @see com.hp.hpl.jena.mem.Temp#get(java.lang.Object)
    */
    private TripleBunch get( Object y )
        { return bunchMap.get( y ); }
    }
