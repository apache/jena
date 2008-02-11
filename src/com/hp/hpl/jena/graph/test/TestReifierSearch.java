/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestReifierSearch.java,v 1.1 2008-02-11 09:32:44 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.ReificationStyle;

public class TestReifierSearch extends ModelTestBase
    {
    public TestReifierSearch( String name )
        { super( name ); }
    
    public void testX()
        {
        Graph g = Factory.createDefaultGraph( ReificationStyle.Standard );
        graphAdd( g, "x rdf:type rdf:Statement" );
        assertContains( "", "x rdf:type rdf:Statement", g );
        assertContains( "", "?? rdf:type rdf:Statement", g );
        assertContains( "", "x ?? rdf:Statement", g );
        assertContains( "", "x rdf:type ??", g );
        }

    }

