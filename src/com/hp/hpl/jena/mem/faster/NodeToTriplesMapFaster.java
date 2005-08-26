/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NodeToTriplesMapFaster.java,v 1.12 2005-08-26 12:48:49 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.mem.NodeToTriplesMapBase;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.util.iterator.*;

public class NodeToTriplesMapFaster extends NodeToTriplesMapBase
    {    
    public NodeToTriplesMapFaster( Field indexField, Field f2, Field f3 )
       { super( indexField, f2, f3 ); }
    
    protected static abstract class Bunch
        {
        public abstract boolean contains( Triple t );
        public abstract int size();
        public abstract void add( Triple t );
        public abstract void remove( Triple t );
        public abstract ExtendedIterator iterator();
        public abstract void app( Domain d, StageElement next, MatchOrBind s );
        }
    
    protected static class ArrayBunch extends Bunch
        {
        public ArrayBunch()
            {}
        
        protected int size = 0;
        protected Triple [] elements = new Triple[9];

        public boolean contains( Triple t )
            {
            for (int i = 0; i < size; i += 1)
                if (t.equals( elements[i])) return true;
            return false;
            }

        public int size()
            { return size; }

        public void add( Triple t )
            { elements[size++] = t; }

        public void remove( Triple t )
            { 
            for (int i = 0; i < size; i += 1)
                {
                if (t.equals( elements[i] ))
                    { elements[i] = elements[--size];
                    return; }
                }
            }
        
        public void app( Domain d, StageElement next, MatchOrBind s )
            {
            for (int i = 0; i < size; i += 1)
                if (s.matches( elements[i] )) next.run( d );
            }
        
        public ExtendedIterator iterator()
            {
            return new NiceIterator()
                {
                protected int i = 0;
                
                public boolean hasNext()
                    { return i < size; }
            
                public Object next()
                    {
                    if (!hasNext()) throw new BrokenException( "ARGH" );
                    return elements[i++]; 
                    }
                
                public void remove()
                    {
                    if (i == size)
                        size -= 1;
                    else
                        elements[--i] = elements[--size];
                    }
                };
            }
        }
    
    protected static class SetBunch extends Bunch
        {
        protected Set elements = new HashSet(20);
        
        public SetBunch( Bunch b )
            { 
            for (Iterator it = b.iterator(); it.hasNext();) 
                elements.add( it.next() );
            }

        public boolean contains( Triple t )
            { return elements.contains( t ); }

        public int size()
            { return elements.size(); }

        public void add( Triple t )
            { elements.add( t ); }

        public void remove( Triple t )
            { elements.remove( t ); }

        public ExtendedIterator iterator()
            { return WrappedIterator.create( elements.iterator() ); }        
        
        public void app( Domain d, StageElement next, MatchOrBind s )
            {
            Iterator it = iterator();
            while (it.hasNext())
                if (s.matches( (Triple) it.next() )) next.run( d );
            }
        }
    
    /**
        Add <code>t</code> to this NTM; the node <code>o</code> <i>must</i>
        be the index node of the triple. Answer <code>true</code> iff the triple
        was not previously in the set, ie, it really truly has been added. 
    */
    public boolean add( Triple t ) 
       {
       Object o = getIndexField( t );
       Bunch s = (Bunch) map.get( o );
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
       Bunch s = (Bunch) map.get( o );
       if (s == null || !s.contains( t ))
           return false;
       else
           {
           s.remove( t );
           size -= 1;
           if (size == 0) map.put( o, null );
           return true;
        } 
    }
    
    /**
        Answer an iterator over all the triples in this NTM which have index node
        <code>o</code>.
    */
    public Iterator iterator( Object o ) 
       {
       Bunch s = (Bunch) map.get( o );
       return s == null ? NullIterator.instance : s.iterator();
       }
    
    /**
        Answer true iff this NTM contains the concrete triple <code>t</code>.
    */
    public boolean contains( Triple t )
       { 
       Bunch s = (Bunch) map.get( getIndexField( t ) );
       return s == null ? false : s.contains( t );
       }
    
    /**
        Answer an iterator over all the triples in this NTM which match
        <code>pattern</code>. The index field of this NTM is guaranteed
        concrete in the pattern.
    */
    public ExtendedIterator iterator( Node index, Node n2, Node n3 )
       {
       Bunch s = (Bunch) map.get( index.getIndexingValue() );
       return s == null
           ? NullIterator.instance
           : f2.filterOn( n2 ).and( f3.filterOn( n3 ) )
               .filterKeep( s.iterator() )
           ;
       }    

    static abstract class MatchOrBind
        {
        public static MatchOrBind createSP( final ProcessedTriple Q )
            {
            return new MatchOrBind()
                {
                protected Domain d;
                protected final QueryNode S = Q.S;
                protected final QueryNode P = Q.P;
                
                public MatchOrBind reset( Domain d )
                    { this.d = d; return this; }
                
                public boolean matches( Triple t )
                    {
                    return 
                        S.matchOrBind( d, t.getSubject() )
                        && P.matchOrBind( d, t.getPredicate() )
                        ;
                    }
                };
            }
        
        public static MatchOrBind createPO( final ProcessedTriple Q )
            {
            return new MatchOrBind()
                {
                protected Domain d;
                protected final QueryNode P = Q.P;
                protected final QueryNode O = Q.O;
                
                public MatchOrBind reset( Domain d )
                    { this.d = d; return this; }
                
                public boolean matches( Triple t )
                    {
                    return 
                        P.matchOrBind( d, t.getPredicate() )
                        && O.matchOrBind( d, t.getObject() )
                        ;
                    }
                };
            }   
        public abstract boolean matches( Triple t );
        
        public abstract MatchOrBind reset( Domain d );
        }
    
    public Applyer createFixedOApplyer( final ProcessedTriple Q )
        {        
        final Bunch ss = (Bunch) map.get( Q.O.node.getIndexingValue() );
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
                Bunch c = (Bunch) map.get( pt.O.finder( d ).getIndexingValue() );
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
                Bunch c = (Bunch) map.get( pt.S.finder( d ) );
                if (c != null) c.app( d, next, x.reset( d ) );
                }
            };
        }

    public Applyer createFixedSApplyer( final ProcessedTriple Q )
        {
        final Bunch ss = (Bunch) map.get( Q.S.node );
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
    
    /**
        Answer an iterator over all the triples in this NTM which are 
        accepted by <code>pattern</code>.
    */
    public ExtendedIterator iterateAll( Node index, Node n2, Node n3 )
       {
       return
           indexField.filterOn( index )
           .and( f2.filterOn( n2 ) )
           .and( f3.filterOn( n3 ) )
           .filterKeep( iterator() )
           ;
       }
    
    /**
       Answer an iterator over all the triples in this NTM.
    */
    public ExtendedIterator iterator()
      {
      final Iterator nodes = domain();
      return new NiceIterator()
          {
          private Iterator current = NullIterator.instance;
          
          public Object next()
              {
              if (hasNext() == false) noElements( "NodeToTriples iterator" );
              return current.next();
              }
          
          public boolean hasNext()
              {
              while (true)
                  {
                  if (current.hasNext()) return true;
                  if (nodes.hasNext() == false) return false;
                  current = iterator( nodes.next() );
                  }
              }
          
          public void remove()
              {
              current.remove();
              }
          };
      }

    public Bunch get( Object index )
        { return (Bunch) map.get( index ); }
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