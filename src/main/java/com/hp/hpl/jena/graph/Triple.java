/*
  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Triple.java,v 1.1 2009-06-29 08:55:45 castagna Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
    Triples are the basis for RDF statements; they have a subject, predicate, and
    object field (all nodes) and express the notion that the relationship named
    by the predicate holds between the subject and the object.
    
    @author Jeremy Carroll, kers
 */
public class Triple implements TripleMatch 
    {    
	private final Node subj, pred, obj;
 
	public Triple( Node s, Node p, Node o ) 
        {
        if (s == null) throw new UnsupportedOperationException( "subject cannot be null" );
        if (p == null) throw new UnsupportedOperationException( "predicate cannot be null" );
        if (o == null) throw new UnsupportedOperationException( "object cannot be null" );
		subj = s;
		pred = p;
		obj = o;
        }
	
	/**
	    A triple-iterator with no elements.
	*/
	public static final ExtendedIterator<Triple> None = new NiceIterator<Triple>();
	
    /**
        return a human-readable string "subject @predicate object" describing the triple
    */
	@Override
    public String toString()
        { return toString( PrefixMapping.Standard ); }
    
    public String toString( PrefixMapping pm )
       {
	   return subj.toString( pm, true ) 
            + " @" + pred.toString( pm, true ) 
            + " " + obj.toString( pm, true );
	   }
    
    /**
        @return the subject of the triple
    */
	public final Node getSubject() 
        { return subj; }
    
    /**
        @return the predicate of the triple
    */
	public final Node getPredicate() 
        { return pred; }
    
    /**
        @return the object of the triple
    */
	public final Node getObject() 
        { return obj; }

    @Override
    public Node getMatchSubject()
        { return anyToNull( subj ); }
    
    public static final Map1<Triple, Node> getSubject = new Map1<Triple, Node>() 
        { @Override
        public Node map1( Triple t ) { return t.getSubject(); } };
        
    public static final Map1<Triple, Node> getPredicate = new Map1<Triple, Node>() 
        { @Override
        public Node map1( Triple t ) { return t.getPredicate(); } };
        
    public static final Map1<Triple, Node> getObject = new Map1<Triple, Node>() 
        { @Override
        public Node map1( Triple t ) { return t.getObject(); } };
        
    @Override
    public Node getMatchPredicate()
        { return anyToNull( pred ); }
        
    @Override
    public Node getMatchObject()
        { return anyToNull( obj ); }
        
    private static Node anyToNull( Node n )
        { return Node.ANY.equals( n ) ? null : n; }

    private static Node nullToAny( Node n )
        { return n == null ? Node.ANY : n; }        
        
    @Override
    public Triple asTriple()
        { return this; }
        
    public boolean isConcrete()
        { return subj.isConcrete() && pred.isConcrete() && obj.isConcrete(); }
        
    /** 
         Answer true if <code>o</code> is a Triple with the same subject, predicate,
         and object as this triple.
    */
	@Override
    public boolean equals(Object o) 
        { return o instanceof Triple && ((Triple) o).sameAs( subj, pred, obj ); }
    
    /** 
        Answer true iff this triple has subject s, predicate p, and object o.
    */    
    public boolean sameAs( Node s, Node p, Node o )
        { return subj.equals( s ) && pred.equals( p ) && obj.equals( o ); }
        
    public boolean matches( Triple other )
        { return other.matchedBy( subj, pred, obj  ); }
        
    public boolean matches( Node s, Node p, Node o )
        { return subj.matches( s ) && pred.matches( p ) && obj.matches( o ); }
        
    private boolean matchedBy( Node s, Node p, Node o )
        { return s.matches( subj ) && p.matches( pred ) && o.matches( obj ); }
        
    public boolean subjectMatches( Node s )
        { return subj.matches( s ); }
        
    public boolean predicateMatches( Node p )
        { return pred.matches( p ); }
        
    public boolean objectMatches( Node o )
        { return obj.matches( o ); }
        
    /**
        The hash-code of a triple is the hash-codes of its components munged
        together: see hashCode(S, P, O).
    */
    @Override
    public int hashCode() 
        { return hashCode( subj, pred, obj ); }
    
    /**
        Return the munged hashCodes of the specified nodes, an exclusive-or of 
        the slightly-shifted component hashcodes; this means (almost) all of the bits 
        count, and the order matters, so (S @P O) has a different hash from 
        (O @P S), etc.
    */    
    public static int hashCode( Node s, Node p, Node o ) 
        { return (s.hashCode() >> 1) ^ p.hashCode() ^ (o.hashCode() << 1); }
    
    /**
        Factory method for creating triples, allows caching opportunities. Attempts
        to use triples from the cache, if any suitable ones exist.
        
        @return a triple with subject=s, predicate=p, object=o
    */
    public static Triple create( Node s, Node p, Node o )
        { 
        Triple already = cache.get( s, p, o );
        return already == null ? cache.put( new Triple( s, p, o ) ) : already;
        }
    
    /**
        The cache of already-created triples.
    */
    protected static TripleCache cache = new TripleCache();
        
    public static Triple createMatch( Node s, Node p, Node o )
        { return Triple.create( nullToAny( s ), nullToAny( p ), nullToAny( o ) ); }
        
    /**
        A Triple that is wildcarded in all fields. 
    */
    public static final Triple ANY = Triple.create( Node.ANY, Node.ANY, Node.ANY );
    
    /**
        A Field is a selector from Triples; it allows selectors to be passed
        around as if they were functions, hooray. 
    */
    public static abstract class Field
        {
        public abstract Node getField( Triple t );
        
        public abstract Filter<Triple> filterOn( Node n );
        
        public final Filter<Triple> filterOn( Triple t )
            { return filterOn( getField( t ) ); }
        
        protected static final Filter<Triple> anyTriple = Filter.any();
        
        public static final Field fieldSubject = new Field() 
            { 
            @Override public Node getField( Triple t ) 
                { return t.subj; }
            
            @Override public Filter<Triple> filterOn( final Node n )
                { 
                return n.isConcrete() 
                    ? new Filter<Triple>() 
                        { @Override public boolean accept( Triple x ) { return n.equals( x.subj ); } }
                    : anyTriple
                    ;
                }
            };
            
        public static final Field fieldObject = new Field() 
            { 
            @Override public Node getField( Triple t ) 
                { return t.obj; } 
            
            @Override public Filter<Triple> filterOn( final Node n )
                { return n.isConcrete() 
                    ? new Filter<Triple>() 
                        { @Override public boolean accept( Triple x ) 
                            { return n.sameValueAs( x.obj ); } }
                    : anyTriple; 
                }
            };
        
        public static final Field fieldPredicate = new Field() 
            { 
            @Override public Node getField( Triple t ) 
                { return t.pred; } 
            
            @Override public Filter<Triple> filterOn( final Node n )
                { return n.isConcrete()
                    ? new Filter<Triple>() 
                        { @Override
                        public boolean accept( Triple x ) { return n.equals( x.pred ); } }
                    : anyTriple; 
                }
            };
        }
    }

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
