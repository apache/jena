/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionSet.java,v 1.1 2005-10-18 15:18:16 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

/**
    A revamped version of the regression set-operation tests.
    @author kers
*/
public class NewRegressionSet extends NewRegressionBase
    {
    public NewRegressionSet( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionSet.class );  }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    
    public void setUp()
        { 
        m = getModel();
        }
    
    public void testUnion()
        {
        Model m1 = getModel();
        Model m2 = getModel();
        modelAdd( m1, "a P b; w R x" );
        modelAdd( m2, "w R x; y S z" );
        Model um = m1.union( m2 );
        assertFalse( m1.containsAll( m2 ) );
        assertFalse( m2.containsAll( m1 ) );
        assertTrue( um.containsAll( m1 ) );
        assertTrue( um.containsAll( m2 ) );
        for (StmtIterator it = um.listStatements(); it.hasNext();)
            {
            Statement s = it.nextStatement();
            assertTrue( m1.contains( s ) || m2.contains( s ) );
            }
        for (StmtIterator it = m1.listStatements(); it.hasNext();)
            assertTrue( um.contains( it.nextStatement() ) );
        for (StmtIterator it = m2.listStatements(); it.hasNext();)
            assertTrue( um.contains( it.nextStatement() ) );
        assertTrue( um.containsAll( m1.listStatements() ) );
        assertTrue( um.containsAll( m2.listStatements() ) );
        }
    
    public void testIntersection()
        {
        Model m1 = getModel();
        Model m2 = getModel();
        modelAdd( m1, "a P b; w R x" );
        modelAdd( m2, "w R x; y S z" );
        Model im = m1.intersection( m2 );
        assertFalse( m1.containsAll( m2 ) );
        assertFalse( m2.containsAll( m1 ) );
        assertTrue( m1.containsAll( im ) );
        assertTrue( m2.containsAll( im ) );
        for (StmtIterator it = im.listStatements(); it.hasNext();)
            {
            Statement s = it.nextStatement();
            assertTrue( m1.contains( s ) && m2.contains( s ) );
            }
        for (StmtIterator it = im.listStatements(); it.hasNext();)
            assertTrue( m1.contains( it.nextStatement() ) );
        for (StmtIterator it = im.listStatements(); it.hasNext();)
            assertTrue( m2.contains( it.nextStatement() ) );
        assertTrue( m1.containsAll( im.listStatements() ) );
        assertTrue( m2.containsAll( im.listStatements() ) );
        }
    
    public void testDifference()
        {
        Model m1 = getModel();
        Model m2 = getModel();
        modelAdd( m1, "a P b; w R x" );
        modelAdd( m2, "w R x; y S z" );
        Model dm = m1.difference( m2 );
        for (StmtIterator it = dm.listStatements(); it.hasNext();)
            {
            Statement s = it.nextStatement();
            assertTrue( m1.contains( s ) && !m2.contains( s ) );
            }
        for (StmtIterator it = m1.union( m2 ).listStatements(); it.hasNext(); )
            {
            Statement s = it.nextStatement();
            assertEquals( m1.contains( s ) && !m2.contains( s ), dm.contains( s ) );
            }
        assertTrue( dm.containsAny( m1 ) );
        assertTrue( dm.containsAny( m1.listStatements() ) );
        assertFalse( dm.containsAny( m2 ) );
        assertFalse( dm.containsAny( m2.listStatements() ) );
        assertTrue( m1.containsAll( dm ) );
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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