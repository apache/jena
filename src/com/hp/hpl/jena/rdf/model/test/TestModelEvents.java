/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestModelEvents.java,v 1.10 2003-07-28 13:07:47 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.listeners.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

import java.util.*;
import junit.framework.*;

/**
    Tests for model events and listeners.
 	@author kers
*/
public class TestModelEvents extends ModelTestBase
    {
    public TestModelEvents(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestModelEvents.class ); }
        
    protected Model model;
    protected SimpleListener SL;
    
    public void setUp()
        { 
        model = ModelFactory.createDefaultModel(); 
        SL = new SimpleListener();
        }
        
    static class SimpleListener implements ModelChangedListener
        {
        List history = new ArrayList();
        
        public void addedStatement( Statement s )
            { record( "add", s ); }
            
        public void addedStatements( Statement [] statements )
            { record( "add[]", Arrays.asList( statements ) ); }
            
        public void addedStatements( List statements )
            { record( "addList", statements ); }
            
        public void addedStatements( StmtIterator statements )
            { record( "addIterator", iteratorToList( statements ) ); }
            
        public void addedStatements( Model m )
            { record( "addModel", m ); }
            
        public void removedStatements( Statement [] statements )
            { record( "remove[]", Arrays.asList( statements ) ); }
        
       public void removedStatement( Statement s )
            { record( "remove", s ); }
            
        public void removedStatements( List statements )
            { record( "removeList", statements ); }
            
        public void removedStatements( StmtIterator statements )
            { record( "removeIterator", iteratorToList( statements ) ); }
            
        public void removedStatements( Model m )
            { record( "removeModel", m ); }
            
        protected void record( String tag, Object info )
            { history.add( tag ); history.add( info ); }
            
        boolean has( Object [] things ) 
            { return history.equals( Arrays.asList( things ) ); }
            
        void assertHas( Object [] things )
            {
            if (has( things ) == false)
                fail( "expected " + Arrays.asList( things ) + " but got " + history );
            }
        }
        
    public void testRegistrationCompiles()
        {
        assertSame( model, model.register( new SimpleListener() ) );
        }
        
    public void testUnregistrationCompiles()
        {
        model.unregister( new SimpleListener() );
        }
        
    public void testAddSingleStatements()
        {
        Statement S1 = statement( model, "S P O" );
        Statement S2 = statement( model, "A B C" );
        assertFalse( SL.has( new Object [] { "add", S1 } ) );
        model.register( SL );
        model.add( S1 );
        SL.assertHas( new Object[] { "add", S1 } );
        model.add( S2 );
        SL.assertHas( new Object[] { "add", S1, "add", S2 } );
        model.add( S1 );
        SL.assertHas( new Object[] { "add", S1, "add", S2, "add", S1 } );
        }
        
    public void testTwoListeners()
        {
        Statement S = statement( model, "S P O" );
        SimpleListener SL1 = new SimpleListener();
        SimpleListener SL2 = new SimpleListener();
        model.register( SL1 ).register( SL2 );
        model.add( S );
        SL2.assertHas( new Object[] { "add", S } );
        SL1.assertHas( new Object[] { "add", S } );
        }
        
    public void testUnregisterWorks()
        {
        model.register( SL );
        model.unregister( SL );
        model.add( statement( model, "X R Y" ) );
        SL.assertHas( new Object[] {} );
        }
        
    public void testRemoveSingleStatements()
        {
        Statement S = statement( model, "D E F" );
        model.register( SL );
        model.add( S );
        model.remove( S );
        SL.assertHas( new Object[] { "add", S, "remove", S } );
        }
        
    public void testAddInPieces()
        {
        model.register( SL );
        model.add( resource( model, "S" ), property( model, "P" ), resource( model, "O" ) );
        SL.assertHas( new Object[] { "add", statement( model, "S P O") } );
        }

    public void testAddStatementArray()
        {
        model.register( SL );
        Statement [] s = statements( model, "a P b; c Q d" );
        model.add( s );
        SL.assertHas( new Object[] {"add[]", Arrays.asList( s )} );
        }
        
    public void testDeleteStatementArray()
        {
        model.register( SL );
        Statement [] s = statements( model, "a P b; c Q d" );
        model.remove( s );
        SL.assertHas( new Object[] {"remove[]", Arrays.asList( s )} );            
        }
        
    public void testAddStatementList()
        {
        model.register( SL );
        List L = Arrays.asList( statements( model, "b I g; m U g" ) );
        model.add( L );
        SL.assertHas( new Object[] {"addList", L} );
        }
        
    public void testDeleteStatementList()
        {
        model.register( SL );
        List L = Arrays.asList( statements( model, "b I g; m U g" ) );
        model.remove( L );
        SL.assertHas( new Object[] {"removeList", L} );
        }
    
    public void testAddStatementIterator()
        {
        model.register( SL );
        Statement [] sa = statements( model, "x R y; a P b; x R y" );
        StmtIterator it = asIterator( sa );
        model.add( it );
        SL.assertHas( new Object[] {"addIterator", Arrays.asList( sa )} );    
        }
        
    public void testDeleteStatementIterator()
        {
        model.register( SL );
        Statement [] sa = statements( model, "x R y; a P b; x R y" );
        StmtIterator it = asIterator( sa );
        model.remove( it );
        SL.assertHas( new Object[] {"removeIterator", Arrays.asList( sa )} );    
        }
                    
    protected StmtIterator asIterator( Statement [] statements )
        { return new StmtIteratorImpl( Arrays.asList( statements ).iterator() ); }
        
    public void testAddModel()
        {
        model.register( SL );
        Model m = modelWithStatements( "NT beats S; S beats H; H beats D" );
        model.add( m );
        SL.assertHas( new Object[] {"addModel", m} );
        }
        
    public void testDeleteModel()
        {
        model.register( SL );
        Model m = modelWithStatements( "NT beats S; S beats H; H beats D" );
        model.remove( m );
        SL.assertHas( new Object[] {"removeModel", m} );
        }
        
    /**
        Test that the null listener doesn't appear to do anything. Or at least
        doesn't crash ....
    */
    public void testNullListener()
        {
        ModelChangedListener NL = new NullListener();
        model.register( NL );
        model.add( statement( model, "S P O " ) );
        model.remove( statement( model, "X Y Z" ) );
        model.add( statements( model, "a B c; d E f" ) );
        model.remove( statements( model, "g H i; j K l" ) );
        model.add( asIterator( statements( model, "m N o; p Q r" ) ) );
        model.remove( asIterator( statements( model, "s T u; v W x" ) ) );
        model.add( modelWithStatements( "leaves fall softly" ) );
        model.remove( modelWithStatements( "water drips endlessly" ) );
        model.add( Arrays.asList( statements( model, "xx RR yy" ) ) );
        model.remove( Arrays.asList( statements( model, "aa VV rr" ) ) );
        }
        
    public void testChangeListener()
        {
        ModelChangedListener CL = new ChangeListener
        }
 
 public static class TripleListener implements ModelChangedListener
    {
    public void addedStatement( Statement s ) {}
    public void addedStatements( Statement [] statements ) {}
    public void addedStatements( List statements ) {}
    public void addedStatements( StmtIterator statements ) {}
    public void addedStatements( Model m ) {}
    public void removedStatement( Statement s ) {}   
    public void removedStatements( Statement [] statements ) {}
    public void removedStatements( List statements ) {}
    public void removedStatements( StmtIterator statements ) {}
    public void removedStatements( Model m ) {}           
    }

public static class WatchTripleListener extends TripleListener
    {
    public void addedStatement( Statement s )
        {}
        
    public void removedStatement( Statement s )
        {}
    }
               
    public void testTripleListener()
        {
        ModelChangedListener TL = new TripleListener();    
        
        }
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
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