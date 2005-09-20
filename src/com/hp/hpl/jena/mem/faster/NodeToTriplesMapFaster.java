/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NodeToTriplesMapFaster.java,v 1.18 2005-09-20 08:19:43 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.mem.ArrayBunch;
import com.hp.hpl.jena.mem.SetBunch;
import com.hp.hpl.jena.mem.TripleBunch;
import com.hp.hpl.jena.mem.MatchOrBind;
import com.hp.hpl.jena.mem.NodeToTriplesMapBase;
import com.hp.hpl.jena.util.iterator.*;

public class NodeToTriplesMapFaster extends NodeToTriplesMapBase
    {    
    public NodeToTriplesMapFaster( Field indexField, Field f2, Field f3 )
       { super( indexField, f2, f3 ); }
    
    /**
        Add <code>t</code> to this NTM; the node <code>o</code> <i>must</i>
        be the index node of the triple. Answer <code>true</code> iff the triple
        was not previously in the set, ie, it really truly has been added. 
    */
    public boolean add( Triple t ) 
       {
       Object o = getIndexField( t );
       TripleBunch s = (TripleBunch) map.get( o );
       if (s == null) map.put( o, s = new ArrayBunch() );
       if (s.contains( t ))
           return false;
       else
           {
           if (s.size() == 9)
               map.put( o, s = new SetBunch( s ) );
           s.add( t );
           size += 1; 
           return true; 
           } 
       }
    
    /**
        Remove <code>t</code> from this NTM. Answer <code>true</code> iff the 
        triple was previously in the set, ie, it really truly has been removed. 
    */
    public boolean remove( Triple t )
       { 
       Object o = getIndexField( t );
       TripleBunch s = (TripleBunch) map.get( o );
       if (s == null || !s.contains( t ))
           return false;
       else
           {
           s.remove( t );
           size -= 1;
           if (s.size() == 0) map.remove( o );
           return true;
        } 
    }
    
    /**
        Answer an iterator over all the triples in this NTM which have index node
        <code>o</code>.
    */
    public Iterator iterator( Object o ) 
       {
       TripleBunch s = (TripleBunch) map.get( o );
       return s == null ? NullIterator.instance : s.iterator();
       }
    
    /**
        Answer true iff this NTM contains the concrete triple <code>t</code>.
    */
    public boolean contains( Triple t )
       { 
       TripleBunch s = (TripleBunch) map.get( getIndexField( t ) );
       return s == null ? false :  s.contains( t );
       }    
    
    public boolean containsBySameValueAs( Triple t )
       { 
       TripleBunch s = (TripleBunch) map.get( getIndexField( t ) );
       return s == null ? false :  s.containsBySameValueAs( t );
       }
    
    /**
        Answer an iterator over all the triples in this NTM which match
        <code>pattern</code>. The index field of this NTM is guaranteed
        concrete in the pattern.
    */
    public ExtendedIterator iterator( Node index, Node n2, Node n3 )
       {
       TripleBunch s = (TripleBunch) map.get( index.getIndexingValue() );
       return s == null
           ? NullIterator.instance
           : f2.filterOn( n2 ).and( f3.filterOn( n3 ) )
               .filterKeep( s.iterator() )
           ;
       }    

    public Applyer createFixedOApplyer( final ProcessedTriple Q )
        {        
        final TripleBunch ss = (TripleBunch) map.get( Q.O.node.getIndexingValue() );
        if (ss == null)
            return Applyer.empty;
        else
            {
            return new Applyer() 
                {
                final MatchOrBind x = MatchOrBind.createSP( Q );
                
                public void applyToTriples( Domain d, Matcher m, StageElement next )
                    { ss.app( d, next, x.reset( d ) ); }
                };
            }
        }

    public Applyer createBoundOApplyer( final ProcessedTriple pt )
        {        
        return new Applyer()
            {
            final MatchOrBind x = MatchOrBind.createSP( pt );
            
            public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                TripleBunch c = (TripleBunch) map.get( pt.O.finder( d ).getIndexingValue() );
                if (c != null) c.app( d, next, x.reset( d ) );
                }
            };
        }
    
    public Applyer createBoundSApplyer( final ProcessedTriple pt )
        {
        return new Applyer()
            {
            final MatchOrBind x = MatchOrBind.createPO( pt );
            
            public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                TripleBunch c = (TripleBunch) map.get( pt.S.finder( d ) );
                if (c != null) c.app( d, next, x.reset( d ) );
                }
            };
        }

    public Applyer createFixedSApplyer( final ProcessedTriple Q )
        {
        final TripleBunch ss = (TripleBunch) map.get( Q.S.node );
        if (ss == null)
            return Applyer.empty;
        else
            {
            return new Applyer() 
                {
                final MatchOrBind x = MatchOrBind.createPO( Q );
                
                public void applyToTriples( Domain d, Matcher m, StageElement next )
                    { ss.app( d, next, x.reset( d ) ); }
                };
            }
        }
       
    protected TripleBunch get( Object index )
        { return (TripleBunch) map.get( index ); }
    
    /**
     Answer an iterator over all the triples that are indexed by the item <code>y</code>.
        Note that <code>y</code> need not be a Node (because of indexing values).
    */
    public Iterator iteratorForIndexed( Object y )
        { return get( y ).iterator();  }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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