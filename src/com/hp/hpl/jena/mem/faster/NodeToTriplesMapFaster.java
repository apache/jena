/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NodeToTriplesMapFaster.java,v 1.5 2005-07-27 16:21:47 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

public class NodeToTriplesMapFaster
    {
    protected final class EmptyApplyer extends Applyer
        {
        public void applyToTriples( Domain d, Matcher m, StageElement next )
            {
            System.err.println( ">> None" );
            }
        }

    /**
    The map from nodes to Set(Triple).
    */
    protected Map map = CollectionFactory.createHashedMap();
    
    protected final Field indexField;
    protected final Field f2;
    protected final Field f3;
    
    public NodeToTriplesMapFaster( Field indexField, Field f2, Field f3 )
       { this.indexField = indexField; this.f2 = f2; this.f3 = f3; }
    
    public Map forTestingOnly_getMap()
       { return map; }
    
    /**
         The number of triples held in this NTM, maintained incrementally 
         (because it's a pain to compute from scratch).
    */
    private int size = 0;
    
    /**
        The nodes which appear in the index position of the stored triples; useful
        for eg listSubjects().
    */
    public Iterator domain()
       { return map.keySet().iterator(); }
    
    protected final Node getIndexField( Triple t )
       { return indexField.getField( t ); }
    
    protected static abstract class Bunch
        {
        public abstract boolean contains( Triple t );
        public abstract int size();
        public abstract void add( Triple t );
        public abstract void remove( Triple t );
        public abstract ExtendedIterator iterator();
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
        }
    
    /**
        Add <code>t</code> to this NTM; the node <code>o</code> <i>must</i>
        be the index node of the triple. Answer <code>true</code> iff the triple
        was not previously in the set, ie, it really truly has been added. 
    */
    public boolean add( Triple t ) 
       {
       Node o = getIndexField( t );
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
       Node o = getIndexField( t );
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
    public Iterator iterator( Node o ) 
       {
       Bunch s = (Bunch) map.get( o );
       return s == null ? NullIterator.instance : s.iterator();
       }
    
    /**
        Clear this NTM; it will contain no triples.
    */
    public void clear() 
       { map.clear(); size = 0; }
    
    public int size()
       { return size; }
    
    public void removedOneViaIterator()
       { size -= 1; }
    
    public boolean isEmpty()
       { return size == 0; }
    
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
       Bunch s = (Bunch) map.get( index );
       return s == null
           ? NullIterator.instance
           : f2.filterOn( n2 ).and( f3.filterOn( n3 ) )
               .filterKeep( s.iterator() )
           ;
       }    

    public Applyer createFixedOApplyer( final ProcessedTriple Q )
        {        
        final Bunch ss = (Bunch) map.get( Q.O.node );
        if (ss == null)
            return new EmptyApplyer();
        else
            {
            return new Applyer() 
                {
                public void applyToTriples( Domain d, Matcher m, StageElement next )
                    {
                    Node sf = Q.S.finder( d ), pf = Q.P.finder( d );
                    Iterator it = ss.iterator();
                    while (it.hasNext())
                        {
                        Triple t = (Triple) it.next();
                        if (sf.matches( t.getSubject() ) && pf.matches( t.getPredicate() ))
                            if (m.match( d, t )) next.run( d );
                        }
                    }
                };
            }
        }

    public Applyer createBoundOApplyer( final ProcessedTriple pt )
        {        
        return new Applyer()
            {
            public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                Bunch c = (Bunch) map.get( pt.O.finder( d ) );
                if (c != null)
                    {
                    Node sf = pt.S.finder( d ), pf = pt.P.finder( d );
                    Iterator it = c.iterator();
                    while (it.hasNext())
                        {
                        Triple t = (Triple) it.next();
                        if (sf.matches( t.getSubject() ) && pf.matches( t.getPredicate() ))
                            if (m.match( d, t )) next.run( d );
                        }
                    }
                }
            };
        }
    
    public Applyer createBoundSApplyer( final ProcessedTriple pt )
        {
        return new Applyer()
            {
            public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                Bunch c = (Bunch) map.get( pt.S.finder( d ) );
                if (c != null)
                    {
                    Node pf = pt.P.finder( d ), of = pt.O.finder( d );
                    Iterator it = c.iterator();
                    while (it.hasNext())
                        {
                        Triple t = (Triple) it.next();
                        if (pf.matches( t.getPredicate() ) && of.matches( t.getObject() ))
                            if (m.match( d, t )) next.run( d );
                        
                        }
                    }
                }
            };
        }
    
    public Applyer createFixedSApplyer( final QueryTriple Q )
        {
        final Bunch ss = (Bunch) map.get( Q.S.node );
        if (ss == null)
            return new EmptyApplyer();
        else
            {
            return new Applyer() 
                {
                public void applyToTriples( Domain d, Matcher m, StageElement next )
                    {
                    Node pf = Q.P.finder( d ), of = Q.O.finder( d );
                    Iterator it = ss.iterator();
                    while (it.hasNext())
                        {
                        Triple t = (Triple) it.next();
                        if (pf.matches( t.getPredicate() ) && of.matches( t.getObject() ))
                            if (m.match( d, t )) next.run( d );
                        }
                    }
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
                  current = iterator( (Node) nodes.next() );
                  }
              }
          
          public void remove()
              {
              current.remove();
              }
          };
      }
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