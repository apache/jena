/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: NodeToTriplesMap.java,v 1.45 2007-01-02 11:52:20 andy_seaborne Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.*;
import com.hp.hpl.jena.util.iterator.*;

/**
	NodeToTriplesMap: a map from nodes to sets of triples.
	Subclasses must override at least one of useXXXInFilter methods.
	@author kers
*/
public class NodeToTriplesMap extends NodeToTriplesMapBase 
    {    
    public NodeToTriplesMap( Field indexField, Field f2, Field f3 )
        { super( indexField, f2, f3 ); }

    /** 
     	@see com.hp.hpl.jena.mem.Temp#add(com.hp.hpl.jena.graph.Triple)
    */
    public boolean add( Triple t ) 
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
        
        public Set baseSet()
            { return elements; }
        }
    
    private OpenSetBunch createSetBunch()
        { return new OpenSetBunch(); }
    
    /** 
     	@see com.hp.hpl.jena.mem.Temp#remove(com.hp.hpl.jena.graph.Triple)
    */
    public boolean remove( Triple t )
        { 
        Object o = getIndexField( t );
        OpenSetBunch s = (OpenSetBunch) bunchMap.get( o );
        if (s == null)
            return false;
        else
            {
            Set base = s.baseSet();
            boolean result = base.remove( t );
            if (result) size -= 1;
            if (base.isEmpty()) bunchMap.remove( o );
            return result;
        	} 
        }
    
    public Iterator iterator( Object o, HashCommon.NotifyEmpty container )
        {
        TripleBunch b = bunchMap.get( o );
        return b == null ? NullIterator.instance : b.iterator();
        }
    
    /** 
     	@see com.hp.hpl.jena.mem.Temp#contains(com.hp.hpl.jena.graph.Triple)
    */
    public boolean contains( Triple t )
        { 
        TripleBunch s = bunchMap.get( getIndexField( t ) );
        return s == null ? false : s.contains( t );
        }

    protected static boolean equalsObjectOK( Triple t )
        { 
        Node o = t.getObject();
        return o.isLiteral() ? o.getLiteralDatatype() == null : true;
        }

    public boolean containsBySameValueAs( Triple t )
        { return equalsObjectOK( t ) ? contains( t ) : slowContains( t ); }
    
    protected boolean slowContains( Triple t )
        { 
        TripleBunch s = bunchMap.get( getIndexField( t ) );
        if (s == null)
            return false;
        else
            {
            Iterator it = s.iterator();
            while (it.hasNext())
                if (t.matches( (Triple) it.next() )) return true;
            return false;
            }
        }
    
    /** 
     	@see com.hp.hpl.jena.mem.Temp#iterateAll(com.hp.hpl.jena.graph.Triple)
    */
    public ExtendedIterator iterateAll( Triple pattern )
        {
        return
            indexField.filterOn( pattern )
            .and( f2.filterOn( pattern ) )
            .and( f3.filterOn( pattern ) )
            .filterKeep( iterateAll() )
            ;
        }

    public ExtendedIterator iterator( Node index, Node n2, Node n3 )
        {
        TripleBunch s = bunchMap.get( index.getIndexingValue() );
        return s == null
            ? NullIterator.instance
            : f2.filterOn( n2 ).and( f3.filterOn( n3 ) )
                .filterKeep( s.iterator() )
            ;
        }

    /**
        Answer an iterator over all the triples that are indexed by the item <code>y</code>.
        Note that <code>y</code> need not be a Node (because of indexing values).
    */
    public Iterator iteratorForIndexed( Object y )
        { return get( y ).iterator();  }
    
    /** 
        @see com.hp.hpl.jena.mem.Temp#get(java.lang.Object)
    */
    private TripleBunch get( Object y )
        { return bunchMap.get( y ); }
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/