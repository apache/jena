/*
  (c) Copyright 2004 Hewlett-Packard Development Company, LP
  All rights reserved.
  [See end of file]
  $Id: TestModelExtract.java,v 1.3 2004-12-06 13:50:25 andy_seaborne Exp $
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
 * (c) Copyright 2000, 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
