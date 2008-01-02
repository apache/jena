/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestRemoveSPO.java,v 1.3 2008-01-02 12:04:42 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import junit.framework.TestSuite;

public class TestRemoveSPO extends ModelTestBase
    {
    public TestRemoveSPO( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestRemoveSPO.class ); }
    
    public void testRemoveSPOReturnsModel()
        {
        Model m = new ModelCom( Factory.createDefaultGraph() );
        assertSame( m, m.remove( resource( "R" ), property( "P" ), rdfNode( m, "17" ) ) );
        }
    
    public void testRemoveSPOCallsGraphDeleteTriple()
        {
        Graph base = Factory.createDefaultGraph();
        final List deleted = new ArrayList();
        Graph wrapped = new WrappedGraph( base )
            { public void delete( Triple t ) { deleted.add( t ); } };
        Model m = new ModelCom( wrapped );
        m.remove( resource( "R" ), property( "P" ), rdfNode( m, "17" ) );
        assertEquals( listOfOne( Triple.create( "R P 17" ) ), deleted );
        }
    }


/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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