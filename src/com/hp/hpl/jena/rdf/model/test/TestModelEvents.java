/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestModelEvents.java,v 1.13 2003-07-29 08:38:12 chris-dollin Exp $
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
        
    public void testChangedListener()
        {
        ChangedListener CL = new ChangedListener();
        model.register( CL );
        assertFalse( CL.hasChanged() );
        model.add( statement( model, "S P O" ) );
        assertTrue( CL.hasChanged() );
        assertFalse( CL.hasChanged() );
        model.remove( statement( model, "ab CD ef" ) );
        assertTrue( CL.hasChanged() );
        model.add( statements( model, "gh IJ kl" ) );
        assertTrue( CL.hasChanged() );
        model.remove( statements( model, "mn OP qr" ) );
        assertTrue( CL.hasChanged() );
        model.add( asIterator( statements( model, "st UV wx" ) ) );
        assertTrue( CL.hasChanged() );
        assertFalse( CL.hasChanged() );
        model.remove( asIterator( statements( model, "yz AB cd" ) ) );
        assertTrue( CL.hasChanged() );
        model.add( modelWithStatements( "ef GH ij" ) );
        assertTrue( CL.hasChanged() );
        model.remove( modelWithStatements( "kl MN op" ) );
        assertTrue( CL.hasChanged() );
        model.add( Arrays.asList( statements( model, "rs TU vw" ) ) );
        assertTrue( CL.hasChanged() );
        model.remove( Arrays.asList( statements( model, "xy wh q" ) ) );
        assertTrue( CL.hasChanged() );
        }

    /**
        Local test class to see that a StatementListener funnels all the changes through
        add/remove a single statement
    */
    public static class WatchStatementListener extends StatementListener
        {
        List statements = new ArrayList();
        String addOrRem = "<unset>";
        
        public List contents()
            { try { return statements; } finally { statements = new ArrayList(); } }
        
        public String getAddOrRem()
            { return addOrRem; }
                
        public void addedStatement( Statement s )
            { statements.add( s ); addOrRem = "add"; }
            
        public void removedStatement( Statement s )
            { statements.add( s ); addOrRem = "rem"; }
        }
        
    public void another( Map m, Object x )
        {
        Integer n = (Integer) m.get( x );
        if (n == null) n = new Integer(0);
        m.put( x, new Integer( n.intValue() + 1 ) ); 
        }
    public Map asBag( List l )
        {
        Map result = new HashMap();
        for (int i = 0; i < l.size(); i += 1) another( result, l.get(i) );
        return result;    
        }       
        
    public void assertSameBag( List wanted, List got )
        { assertEquals( asBag( wanted ), asBag( got ) ); }
        
    public void testGot( WatchStatementListener sl, String how, String template )
        {
        assertSameBag( Arrays.asList( statements( model, template ) ), sl.contents() );
        assertEquals( how, sl.getAddOrRem() );
        assertTrue( sl.contents().size() == 0 );
        }
        
    public void testTripleListener()
        {
        WatchStatementListener sl = new WatchStatementListener();    
        model.register( sl );
        model.add( statement( model, "b C d" ) );
        testGot( sl, "add", "b C d" );
        model.remove( statement( model, "e F g" ) );
        testGot( sl, "rem", "e F g" );
    /* */    
        model.add( statements( model, "h I j; k L m" ) );
        testGot( sl, "add", "h I j; k L m" );
        model.remove( statements( model, "n O p; q R s" ) );
        testGot( sl, "rem", "n O p; q R s" );
    /* */    
        model.add( Arrays.asList( statements( model, "t U v; w X y" ) ) );
        testGot( sl, "add", "t U v; w X y" );
        model.remove( Arrays.asList( statements( model, "z A b; c D e" ) ) );
        testGot( sl, "rem", "z A b; c D e" );
    /* */    
        model.add( asIterator( statements( model, "f G h; i J k" ) ) );
        testGot( sl, "add", "f G h; i J k" );
        model.remove( asIterator( statements( model, "l M n; o P q" ) ) );
        testGot( sl, "rem", "l M n; o P q" );
    /* */
        model.add( modelWithStatements( "r S t; u V w; x Y z" ) );
        testGot( sl, "add", "r S t; u V w; x Y z" );
        model.remove( modelWithStatements( "a E i; o U y" ) );
        testGot( sl, "rem", "a E i; o U y" );
        }
        

    static class OL extends ObjectListener
        {
        private Object recorded;
        private String how;
        
        public void added( Object x )
            { recorded = x; how = "add"; }
            
        public void removed( Object x )
            { recorded = x; how = "rem"; }
        
        private Object comparable( Object x )
            {
            if (x instanceof Statement []) return Arrays.asList( (Statement []) x );
            if (x instanceof Iterator) return iteratorToList( (Iterator) x );
            return x;
            }    
            
        public void recent( String wantHow, Object value ) 
            { 
            assertEquals( comparable( value ), comparable( recorded ) ); 
            assertEquals( wantHow, how );
            recorded = how = null;
            }
        }
        
    public void testObjectListener()
        {
        OL ll = new OL();
        model.register( ll );
        Statement s = statement( model, "aa BB cc" ), s2 = statement( model, "dd EE ff" );
        model.add( s );
        ll.recent( "add", s );
        model.remove( s2 );
        ll.recent( "rem", s2 );
    /* */
        List sList = Arrays.asList( statements( model, "gg HH ii; jj KK ll" ) );
        model.add( sList );
        ll.recent( "add", sList );
        List sList2 = Arrays.asList( statements( model, "mm NN oo; pp QQ rr; ss TT uu" ) );
        model.remove( sList2 );
        ll.recent( "rem", sList2 );
    /* */
        Model m1 = modelWithStatements( "vv WW xx; yy ZZ aa" );
        model.add( m1 );
        ll.recent( "add", m1 );
        Model m2 = modelWithStatements( "a B g; d E z" );
        model.remove( m2 );
        ll.recent( "rem", m2 );
    /* */
        Statement [] sa1 = statements( model, "th i k; l m n" );
        model.add( sa1 );
        ll.recent( "add", sa1 );
        Statement [] sa2 = statements( model, "x o p; r u ch" );
        model.remove( sa2 );
        ll.recent( "rem", sa2 );
    /* */
        Statement [] si1 = statements( model, "u ph ch; psi om eh" );
        model.add( asIterator( si1 ) );
        ll.recent( "add", asIterator( si1 ) );
        Statement [] si2 = statements( model, "at last the; end of these; tests ok guv" );
        model.remove( asIterator( si2 ) );
        ll.recent( "rem", asIterator( si2 ) );
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