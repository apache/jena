/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionAddModel.java,v 1.2 2006-03-22 13:52:54 andy_seaborne Exp $
*/

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionAddModel extends ModelTestBase
    {
    public NewRegressionAddModel( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionAddModel.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    
    public void setUp()
        { m = getModel(); }
    
    public void tearDown()
        { m = null; }
    
    public void testAddByIterator()
        {
        Model m1 = getModel();
        Model m2 = getModel();
        modelAdd( m1, "a P b; c P d; x Q 1; y Q 2" );
        m2.add( m1.listStatements() );
        assertEquals( m1.size(), m2.size() );
        assertSameStatements( m1, m2 );
        m1.add( m1.createResource(), RDF.value, m1.createResource() );
        m1.add( m1.createResource(), RDF.value, m1.createResource() );
        m1.add( m1.createResource(), RDF.value, m1.createResource() );
        StmtIterator s = m1.listStatements();
        m2.remove( s.nextStatement() ).remove( s );
        assertEquals( 0, m2.size() );
        }

    public void testAddByModel()
        {
        Model m1 = getModel(), m2 = getModel();
        modelAdd( m1, "a P b; c P d; x Q 1; y Q 2" );
        m2.add( m1 );
        assertEquals( m1.size(), m2.size() );
        assertSameStatements( m1, m2 );
        }

    public void testRemoveByModel()
        {
        Model m1 = getModel(), m2 = getModel();
        modelAdd( m1, "a P b; c P d; x Q 1; y Q 2" );
        m2.add( m1 ).remove( m1 );
        assertEquals( 0, m2.size() );
        assertFalse( m2.listStatements().hasNext() );
        }
    
    protected void assertSameStatements( Model m1, Model m2 )
        {
        assertContainsAll( m1, m2 );
        assertContainsAll( m2, m1 );
        }
    
    protected void assertContainsAll( Model m1, Model m2 )
        {
        for (StmtIterator s = m2.listStatements(); s.hasNext();)
            assertTrue( m1.contains( s.nextStatement() ) );
        }    
    }


/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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