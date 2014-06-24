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
	public static final ExtendedIterator<Triple> None = new NiceIterator<>();
	
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

    public static Triple create( Node s, Node p, Node o )
    {
        return new Triple( s, p, o ) ;
    }
    
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
