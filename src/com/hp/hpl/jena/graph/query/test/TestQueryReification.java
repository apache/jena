/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestQueryReification.java,v 1.2 2006-03-22 13:53:36 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.*;

public class TestQueryReification extends QueryTestBase
    {
    public TestQueryReification( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestQueryReification.class ); }
    
    public Graph getGraph()
        { return Factory.createDefaultGraph(); }
    
    public Graph getGraph( String triples )
        { return graphAdd( getGraph(), triples ); }
    
    public void testS()
        {  
        Graph g = geGraphSPO();
        Query q = new Query().addMatch( Query.X, RDF.Nodes.subject, Query.S );
        Iterator it = q.executeBindings( g, new Node[] {Query.X, Query.S} ).mapWith( select(1) );
        assertEquals( nodeSet( "S" ), iteratorToSet( it ) );
        }
    public void testP()
        {  
        Graph g = geGraphSPO();
        Query q = new Query().addMatch( Query.X, RDF.Nodes.predicate, Query.P );
        Iterator it = q.executeBindings( g, new Node[] {Query.X, Query.P} ).mapWith( select(1) );
        assertEquals( nodeSet( "P" ), iteratorToSet( it ) );
        }
    
    public void testO()
        {  
        Graph g = geGraphSPO();
        Query q = new Query().addMatch( Query.X, RDF.Nodes.object, Query.O );
        Iterator it = q.executeBindings( g, new Node[] {Query.X, Query.O} ).mapWith( select(1) );
        assertEquals( nodeSet( "O" ), iteratorToSet( it ) );
        }
    
    protected Graph geGraphSPO()
        {
        return getGraph( "_x rdf:subject S; _x rdf:predicate P; _x rdf:object O; _x rdf:type rdf:Statement" );
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