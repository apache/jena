/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NodeToTriplesMapFaster.java,v 1.1 2009-06-29 08:55:55 castagna Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.shared.JenaException;
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
    @Override public boolean add( Triple t ) 
       {
       Object o = getIndexField( t );
       TripleBunch s = bunchMap.get( o );
       if (s == null) bunchMap.put( o, s = new ArrayBunch() );
       if (s.contains( t ))
           return false;
       else
           {
           if (s.size() == 9 && s instanceof ArrayBunch)
               bunchMap.put( o, s = new HashedTripleBunch( s ) );
           s.add( t );
           size += 1; 
           return true; 
           } 
       }
    
    /**
        Remove <code>t</code> from this NTM. Answer <code>true</code> iff the 
        triple was previously in the set, ie, it really truly has been removed. 
    */
    @Override public boolean remove( Triple t )
       { 
       Object o = getIndexField( t );
       TripleBunch s = bunchMap.get( o );
       if (s == null || !s.contains( t ))
           return false;
       else
           {
           s.remove( t );
           size -= 1;
           if (s.size() == 0) bunchMap.remove( o );
           return true;
        } 
    }
    
    /**
        Answer an iterator over all the triples in this NTM which have index node
        <code>o</code>.
    */
    @Override public Iterator<Triple> iterator( Object o, HashCommon.NotifyEmpty container ) 
       {
       // System.err.println( ">> BOINK" ); // if (true) throw new JenaException( "BOINK" );
       TripleBunch s = bunchMap.get( o );
       return s == null ? NullIterator.<Triple>instance() : s.iterator( container );
       }
    
    public class NotifyMe implements HashCommon.NotifyEmpty
        {
        protected final Object key;
        
        public NotifyMe( Object key )
            { this.key = key; }
        
        // TODO fix the way this interacts (badly) with iteration and CMEs.
        @Override
        public void emptied()
            { if (false) throw new JenaException( "BOOM" ); /* System.err.println( ">> OOPS" ); */ bunchMap.remove( key ); }
        }
    
    /**
        Answer true iff this NTM contains the concrete triple <code>t</code>.
    */
    @Override public boolean contains( Triple t )
       { 
       TripleBunch s = bunchMap.get( getIndexField( t ) );
       return s == null ? false :  s.contains( t );
       }    
    
    @Override public boolean containsBySameValueAs( Triple t )
       { 
       TripleBunch s = bunchMap.get( getIndexField( t ) );
       return s == null ? false :  s.containsBySameValueAs( t );
       }
    
    /**
        Answer an iterator over all the triples in this NTM which match
        <code>pattern</code>. The index field of this NTM is guaranteed
        concrete in the pattern.
    */
    @Override public ExtendedIterator<Triple> iterator( Node index, Node n2, Node n3 )
       {
       Object indexValue = index.getIndexingValue();
       TripleBunch s = bunchMap.get( indexValue );
//       System.err.println( ">> ntmf::iterator: " + (s == null ? (Object) "None" : s.getClass()) );
       return s == null
           ? NullIterator.<Triple>instance()
           : f2.filterOn( n2 ).and( f3.filterOn( n3 ) )
               .filterKeep( s.iterator( new NotifyMe( indexValue ) ) )
           ;
       }    

    public Applyer createFixedOApplyer( final ProcessedTriple Q )
        {        
        final TripleBunch ss = bunchMap.get( Q.O.node.getIndexingValue() );
        if (ss == null)
            return Applyer.empty;
        else
            {
            return new Applyer() 
                {
                final MatchOrBind x = MatchOrBind.createSP( Q );
                
                @Override
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
            
            @Override public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                TripleBunch c = bunchMap.get( pt.O.finder( d ).getIndexingValue() );
                if (c != null) c.app( d, next, x.reset( d ) );
                }
            };
        }
    
    public Applyer createBoundSApplyer( final ProcessedTriple pt )
        {
        return new Applyer()
            {
            final MatchOrBind x = MatchOrBind.createPO( pt );
            
            @Override public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                TripleBunch c = bunchMap.get( pt.S.finder( d ) );
                if (c != null) c.app( d, next, x.reset( d ) );
                }
            };
        }

    public Applyer createFixedSApplyer( final ProcessedTriple Q )
        {
        final TripleBunch ss = bunchMap.get( Q.S.node );
        if (ss == null)
            return Applyer.empty;
        else
            {
            return new Applyer() 
                {
                final MatchOrBind x = MatchOrBind.createPO( Q );
                
                @Override public void applyToTriples( Domain d, Matcher m, StageElement next )
                    { ss.app( d, next, x.reset( d ) ); }
                };
            }
        }
       
    protected TripleBunch get( Object index )
        { return bunchMap.get( index ); }
    
    /**
     Answer an iterator over all the triples that are indexed by the item <code>y</code>.
        Note that <code>y</code> need not be a Node (because of indexing values).
    */
    @Override public Iterator<Triple> iteratorForIndexed( Object y )
        { return get( y ).iterator();  }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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