/*
  (c) Copyright 2004, Chris Dollin
  [See end of file]
  $Id: TestModelExtract.java,v 1.2 2004-08-09 13:31:43 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestSuite;

/**
 @author hedgehog
*/
public class TestModelExtract extends ModelTestBase
    {
    protected static final StatementBoundary sbTrue = new StatementBoundaryBase()
        { 
        public boolean stopAt( Statement s ) { return true; } 
        };
        
    protected static final StatementBoundary sbFalse = new StatementBoundaryBase()
        { 
        public boolean stopAt( Statement s ) { return false; }
        };

    public TestModelExtract( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelExtract.class ); }

    static class MockModelExtract extends ModelExtract
        {
        Node root;
        Graph result;
        Graph subject;
       
        public MockModelExtract( StatementBoundary b )
            { super( b ); }
            
        public StatementBoundary getStatementBoundary()
            { return boundary; }
        
        protected GraphExtract getGraphExtract( TripleBoundary b )
            {
            return new GraphExtract( b )
                {
                public Graph extractInto( Graph toUpdate, Node n, Graph source )
                    {
                    root = n;
                    return result = super.extractInto( toUpdate, n, subject = source );
                    }
                };
            }
        }
    
    public void testAsTripleBoundary()
        {
        Model m = ModelFactory.createDefaultModel();
        assertTrue( sbTrue.asTripleBoundary( m ).stopAt( triple( "x R y" ) ) );
        assertFalse( sbFalse.asTripleBoundary( m ).stopAt( triple( "x R y" ) ) );
        }
    
    public void testStatementTripleBoundaryAnon()
        {
        TripleBoundary anon = TripleBoundary.stopAtAnonObject;
        assertSame( anon, new StatementTripleBoundary( anon ).asTripleBoundary( null ) );
        assertFalse( new StatementTripleBoundary( anon ).stopAt( statement( "s P o" ) ) );
        assertTrue( new StatementTripleBoundary( anon ).stopAt( statement( "s P _o" ) ) );
        }
    
    public void testStatementContinueWith()
        {
        StatementBoundary sb = new StatementBoundaryBase()
             { public boolean continueWith( Statement s ) { return false; } };
        assertTrue( sb.stopAt( statement( "x pings y" ) ) );
        }
    
    public void testStatementTripleBoundaryNowhere()
        {
        TripleBoundary nowhere = TripleBoundary.stopNowhere;
        assertSame( nowhere, new StatementTripleBoundary( nowhere ).asTripleBoundary( null ) );
        assertFalse( new StatementTripleBoundary( nowhere ).stopAt( statement( "s P _o" ) ) );
        assertFalse( new StatementTripleBoundary( nowhere ).stopAt( statement( "s P o" ) ) );
        }
    public void testRemembersBoundary()
        {
        assertSame( sbTrue, new MockModelExtract( sbTrue ).getStatementBoundary() );
        assertSame( sbFalse, new MockModelExtract( sbFalse ).getStatementBoundary() );
        }
    
    public void testInvokesExtract()
        {
        MockModelExtract mock = new MockModelExtract( sbTrue );
        Model source = modelWithStatements( "a R b" );
        Model m = mock.extract( resource( "a" ), source );
        assertEquals( node( "a" ), mock.root );
        assertSame( mock.result, m.getGraph() );
        assertSame( mock.subject, source.getGraph() );
        }

    /* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.model.StatementBoundary#stopAt(com.hp.hpl.jena.rdf.model.Statement)
     */
    public boolean stopAt( Statement s )
        {
        // TODO Auto-generated method stub
        return false;
        }

    /* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.model.StatementBoundary#asTripleBoundary(com.hp.hpl.jena.rdf.model.Model)
     */
    public TripleBoundary asTripleBoundary( Model m )
        {
        // TODO Auto-generated method stub
        return null;
        }
    }


/*
    (c) Copyright 2004, Chris Dollin
    All rights reserved. Provided AS IS. Redistribution only by written consent.
    Work in progress - could easily break; that's your problem, not mine.
*/