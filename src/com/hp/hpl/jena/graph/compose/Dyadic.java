/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Dyadic.java,v 1.1 2003-02-21 15:45:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.util.iterator.*;

import java.io.*;
import java.util.*;

/**
    @author kers
*/

public abstract class Dyadic extends GraphBase implements Graph 
	{
	public abstract void add( Triple t );

	public abstract void delete( Triple t );
    
	public abstract ExtendedIterator find( TripleMatch t ); 
	
    public QueryHandler queryHandler() 
        { return new SimpleQueryHandler( this ); }
        
    private Reifier reifier;
    
    public Reifier getReifier()
        {
        if (reifier == null)  reifier = new SimpleReifier( this );
        return reifier; 
        }
    
	public ExtendedIterator find( Node s, Node p, Node o ) 
		{ return find( new StandardTripleMatch( s, p, o ) ); }
		
	public ClosableIterator findAll()
		{ return find( null, null, null ); }

	public int size()
		{ return countIterator( findAll() ); }		

	public int capabilities() 
		{ return ADD | DELETE | SIZE;  }

/* */
    public void die( String message )
        { throw new UnsupportedOperationException( message ); }
/* */

	protected Graph L;
	protected Graph R;
	
	public Dyadic( Graph L, Graph R )
		{
		this.L = L;
		this.R = R;
		}

    public void close()
    	{
    	L.close();
    	R.close();
    	}
 
    public boolean mightContain( Graph other )
        {
        return other == this || L.mightContain( other ) || R.mightContain( other );
        }
 				
/*
	useful shared functionality.
*/
	private static HashSet hashSet( ClosableIterator x )
		{
		HashSet result = new HashSet();
		while (x.hasNext()) result.add( x.next() );
		return result;
		}
		
	protected static Filter reject( final ClosableIterator it )
		{
		final HashSet suppress = hashSet( it );
		return new Filter()
			{ public boolean accept( Object o ) { return suppress.contains( o ) == false; } };
		}
		
	public static ClosableIterator butNot( final ClosableIterator a, final ClosableIterator b )
		{
		return new FilterIterator( reject( b ), a );
		}
		
	public ExtendedIterator recording( final ClosableIterator it, final HashSet seen )
		{
		return new NiceIterator()
			{
			public void remove()
				{ it.remove(); }
			
			public boolean hasNext()
				{ return it.hasNext(); }	
			
			public Object next()
				{ Object x = it.next(); seen.add( x ); return x; }	
				
			public void close()
				{ it.close(); }
			};
		}
		
	static final Object absent = new Object();
	
	public ExtendedIterator rejecting( final ExtendedIterator it, final HashSet seen )
		{
        Filter seenFilter = new Filter()
            { public boolean accept( Object x ) { return seen.contains( x ); } };
        return it .filterDrop ( seenFilter );
		}
		
    protected int countIterator( ClosableIterator them )
        {
        try { int n = 0; while (them.hasNext()) { n += 1; them.next(); } return n; }
        finally { them.close(); }
        }
  
    public boolean contains( Triple t )
      {
      return contains( t.getSubject(), t.getPredicate(), t.getObject() );
      }             	    
      
    protected static Filter ifIn( final ClosableIterator it )
        {
        final HashSet allow = hashSet( it );
        return new Filter()
            { public boolean accept( Object o ) { return allow.contains( o ); } };
        }
        
     public static Filter ifIn( final Graph g )
        {
        return new Filter()
            { public boolean accept( Object x ) { return g.contains( (Triple) x ); } };
        }
        
    public Union union( Graph X )
        { return new Union( this, X ); }
	}

/*
    (c) Copyright Hewlett-Packard Company 2002
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
