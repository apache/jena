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

package org.apache.jena.graph;

import java.util.function.Predicate;

import org.apache.jena.atlas.lib.Tuple;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;

/**
    Triples are the basis for RDF statements; they have a subject, predicate, and
    object field (all nodes) and express the notion that the relationship named
    by the predicate holds between the subject and the object.
 */
public class Triple extends Tuple<Node>
    {    

	public Triple( Node s, Node p, Node o ) 
        {
        super(s, p, o);
        if (s == null) throw new UnsupportedOperationException( "subject cannot be null" );
        if (p == null) throw new UnsupportedOperationException( "predicate cannot be null" );
        if (o == null) throw new UnsupportedOperationException( "object cannot be null" );
        }
	
	/**
	    A triple-iterator with no elements.
	    @deprecated Use {@link NullIterator#instance()}
	*/
	@Deprecated
	public static final ExtendedIterator<Triple> None = NullIterator.instance() ;
	
    /**
        return a human-readable string "subject @predicate object" describing the triple
    */
	@Override
    public String toString()
        { return toString( PrefixMapping.Standard ); }
    
    public String toString( PrefixMapping pm )
       {
	   return tuple[0].toString( pm, true ) 
            + " @" + tuple[1].toString( pm, true ) 
            + " " + tuple[2].toString( pm, true );
	   }
    
    /**
        @return the subject of the triple
    */
	public final Node getSubject() 
        { return tuple[0]; }
    
    /**
        @return the predicate of the triple
    */
	public final Node getPredicate() 
        { return tuple[1]; }
    
    /**
        @return the object of the triple
    */
	public final Node getObject() 
        { return tuple[2]; }

	/** Return subject or null, not Node.ANY */ 
    public Node getMatchSubject()
        { return anyToNull( tuple[0] ); }
    
    /** Return predicate or null, not Node.ANY */ 
    public Node getMatchPredicate()
        { return anyToNull( tuple[1] ); }
        
    /** Return object or null, not Node.ANY */ 
    public Node getMatchObject()
        { return anyToNull( tuple[2] ); }
        
    private static Node anyToNull( Node n )
        { return Node.ANY.equals( n ) ? null : n; }

    private static Node nullToAny( Node n )
        { return n == null ? Node.ANY : n; }        
        
    public boolean isConcrete()
        { return tuple[0].isConcrete() && tuple[1].isConcrete() && tuple[2].isConcrete(); }
        
    /** 
         Answer true if <code>o</code> is a Triple with the same subject, predicate,
         and object as this triple.
    */
	@Override
    public boolean equals(Object o) 
        { return o instanceof Triple && ((Triple) o).sameAs( tuple[0], tuple[1], tuple[2] ); }
    
    /** 
        Answer true iff this triple has subject s, predicate p, and object o.
    */    
    public boolean sameAs( Node s, Node p, Node o )
        { return tuple[0].equals( s ) && tuple[1].equals( p ) && tuple[2].equals( o ); }
        
    /** Does this triple, used as a pattern match, the other triple (usually a ground triple) */ 
    public boolean matches( Triple other )
        { return other.matchedBy( tuple[0], tuple[1], tuple[2]  ); }
        
    public boolean matches( Node s, Node p, Node o )
        { return tuple[0].matches( s ) && tuple[1].matches( p ) && tuple[2].matches( o ); }
        
    private boolean matchedBy( Node s, Node p, Node o )
        { return s.matches( tuple[0] ) && p.matches( tuple[1] ) && o.matches( tuple[2] ); }
        
    public boolean subjectMatches( Node s )
        { return tuple[0].matches( s ); }
        
    public boolean predicateMatches( Node p )
        { return tuple[1].matches( p ); }
        
    public boolean objectMatches( Node o )
        { return tuple[2].matches( o ); }
        
    /**
        The hash-code of a triple is the hash-codes of its components munged
        together: see hashCode(S, P, O).
    */
    @Override
    public int hashCode() 
        { return hashCode( tuple[0], tuple[1], tuple[2] ); }
    
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
        
        public abstract Predicate<Triple> filterOn( Node n );
        
        public final Predicate<Triple> filterOn( Triple t )
            { return filterOn( getField( t ) ); }
        
        protected static final Predicate<Triple> anyTriple = t -> true;
        
        public static final Field fieldSubject = new Field() 
            { 
            @Override public Node getField( Triple t ) 
                { return t.tuple[0]; }
            
            @Override public Predicate<Triple> filterOn( final Node n )
                { 
                return n.isConcrete() 
                    ? x -> n.equals( x.tuple[0] )
                    : anyTriple
                    ;
                }
            };
            
        public static final Field fieldObject = new Field() 
            { 
            @Override public Node getField( Triple t ) 
                { return t.tuple[2]; } 
            
            @Override public Predicate<Triple> filterOn( final Node n )
                { return n.isConcrete() 
                    ? x -> n.sameValueAs( x.tuple[2] )
                    : anyTriple; 
                }
            };
        
        public static final Field fieldPredicate = new Field() 
            { 
            @Override public Node getField( Triple t ) 
                { return t.tuple[1]; } 
            
            @Override public Predicate<Triple> filterOn( final Node n )
                { return n.isConcrete()
                    ? x -> n.equals( x.tuple[1] )
                    : anyTriple; 
                }
            };
        }
    }
