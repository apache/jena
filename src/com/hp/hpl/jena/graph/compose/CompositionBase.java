/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            4 Mar 2003
 * Filename           $RCSfile: CompositionBase.java,v $
 * Revision           $Revision: 1.11 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 11:52:02 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.graph.compose;


// Imports
///////////////
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.util.IteratorCollection;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;


/**
 * <p>
 * Base class for graphs that are composed of multiple sub-graphs.  This is to provide
 * a home for shared functionality that was previously in {@link Dyadic} before
 * refactoring.
 * </p>
 *
 * @author Ian Dickinson, moved kers' code from Dyadic to this class, added commentage
 * @author Chris Dollin (kers)
 * @version CVS $Id: CompositionBase.java,v 1.11 2005-02-21 11:52:02 andy_seaborne Exp $
 */
public abstract class CompositionBase
    extends GraphBase
{
    /**
     * <p>
     * Answer the number of triples in this graph
     * </p>
     * 
     * @return The integer triple count
     * @see com.hp.hpl.jena.graph.Graph#size()
     */
    public int graphBaseSize()
        { return countIterator( GraphUtil.findAll( this ) ); }             
      
    /**
     * <p>
     * Helper to throw an unsupported operation exception. For users whose brains
     * have been infected by perl.
     * </p>
     * 
     * @param message
     * @exception Throws {@link UnsupportedOperationException}
     */
    protected void die( String message )
        { throw new UnsupportedOperationException( message ); }

    /**
     * <p>
     * Answer a {@link Filter} that will reject any element that is a member of iterator i.
     * As a side-effect, i will be closed. 
     * </p>
     * 
     * @param i A closable iterator
     * @return A Filter that will accept any object not a member of i.
     */
    public static Filter reject( final ClosableIterator i )
        {
        final Set suppress = IteratorCollection.iteratorToSet( i );
        return new Filter()
            { public boolean accept( Object o ) { return !suppress.contains( o ); } };
        }
        
    /**
     * <p>
     * Answer an iterator over the elements of iterator a that are not members of iterator b.
     * As a side-effect, iterator b will be closed.
     * </p>
     * 
     * @param a An iterator that will be filtered by rejecting the elements of b
     * @param b A closable iterator 
     * @return The iteration of elements in a but not in b.
     */
    public static ClosableIterator butNot( final ClosableIterator a, final ClosableIterator b )
        {
        return new FilterIterator( reject( b ), a );
        }
        
    /**
     * <p>
     * Answer an iterator that will record every element delived by <code>next()</code> in
     * the set <code>seen</code>. 
     * </p>
     * 
     * @param i A closable iterator
     * @param seen A set that will record each element of i in turn
     * @return An iterator that records the elements of i.
     */
    public static ExtendedIterator recording( final ClosableIterator i, final Set seen )
        {
        return new NiceIterator()
            {
            public void remove()
                { i.remove(); }
            
            public boolean hasNext()
                { return i.hasNext(); }    
            
            public Object next()
                { Object x = i.next(); 
                try { seen.add( x ); } catch (OutOfMemoryError e) { throw e; } return x; }  
                
            public void close()
                { i.close(); }
            };
        }
        
    //static final Object absent = new Object();
    
    /**
     * <p>
     * Answer an iterator over the elements of iterator i that are not in the set <code>seen</code>. 
     * </p>
     * 
     * @param i An extended iterator
     * @param seen A set of objects
     * @return An iterator over the elements of i that are not in the set <code>seen</code>.
     */
    public static ExtendedIterator rejecting( final ExtendedIterator i, final Set seen )
        {
        Filter seenFilter = new Filter()
            { public boolean accept( Object x ) { return seen.contains( x ); } };
        return i.filterDrop( seenFilter );
        }
        
    /**
         Answer an iterator over the elements of <code>i</code> that are not in
         the graph <code>seen</code>.
    */
    public static ExtendedIterator rejecting( final ExtendedIterator i, final Graph seen )
        {
        Filter seenFilter = new Filter()
            { public boolean accept( Object x ) { return seen.contains( (Triple) x ); } };
        return i.filterDrop( seenFilter );
        }
    
    /**
     * <p>
     * Answer the number of items in the closable iterator i. As a side effect, i
     * is closed.
     * </p>
     * 
     * @param i A closable iterator
     * @return The number of elements of i
     */
    public static int countIterator( ClosableIterator i )
        {
        try { int n = 0; while (i.hasNext()) { n += 1; i.next(); } return n; }
        finally { i.close(); }
        }
  
    /**
     * <p>
     * Answer a {@link Filter} that will accept any object that is an element of 
     * iterator i.  As a side-effect, i will be evaluated and closed. 
     * </p>
     * 
     * @param i A closable iterator 
     * @return A Filter that will accept any object in iterator i.
     */
    public static Filter ifIn( final ClosableIterator i )
        {
        final Set allow = IteratorCollection.iteratorToSet( i );
        return new Filter()
            { public boolean accept( Object x ) { return allow.contains( x ); } };
        }
        
    /**
     * <p>
     * Answer a {@link Filter} that will accept any triple that is an edge of 
     * graph g. 
     * </p>
     * 
     * @param g A graph 
     * @return A Filter that will accept any triple that is an edge in g.
     */
    public static Filter ifIn( final Graph g )
        {
        return new Filter()
            { public boolean accept( Object x ) { return g.contains( (Triple) x ); } };
        }
        

    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

