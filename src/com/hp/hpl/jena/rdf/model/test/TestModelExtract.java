/*
  (c) Copyright 2004, Chris Dollin
  [See end of file]
  $Id: TestModelExtract.java,v 1.1 2004-08-07 15:45:58 chris-dollin Exp $
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
    protected static final StatementBoundary sbTrue = new StatementBoundary()
        { 
        public boolean stopAt( Statement s ) { return true; } 
        public TripleBoundary asTripleBoundary( Model m ) { return ModelExtract.convert( m, this ); }
        };
        
    protected static final StatementBoundary sbFalse = new StatementBoundary()
        { 
        public boolean stopAt( Statement s ) { return false; }
        public TripleBoundary asTripleBoundary( Model m ) { return ModelExtract.convert( m, this ); }
        };

    public TestModelExtract(String name)
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
    }


/*
    (c) Copyright 2004, Chris Dollin
    All rights reserved. Provided AS IS. Redistribution only by written consent.
    Work in progress - could easily break; that's your problem, not mine.
*/