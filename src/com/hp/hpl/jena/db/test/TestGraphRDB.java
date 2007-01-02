/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestGraphRDB.java,v 1.12 2007-01-02 11:50:15 andy_seaborne Exp $
*/

package com.hp.hpl.jena.db.test;

import java.sql.SQLException;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import junit.framework.*;

/**
 	@author kers
*/
public class TestGraphRDB extends MetaTestGraph
    {
    public TestGraphRDB( String name )
        { super( name ); }
    
    public TestGraphRDB( Class graphClass, String name, ReificationStyle style ) 
        { super( graphClass, name, style ); }
        
    public static TestSuite suite()
        { return MetaTestGraph.suite( TestGraphRDB.class, LocalGraphRDB.class ); }

    private IDBConnection con;
    private int count = 0;
    private Graph properties;
    
    public void setUp()
        { con = TestConnection.makeAndCleanTestConnection();
        properties = con.getDefaultModelProperties().getGraph(); }
        
    public void tearDown() throws SQLException
        { con.close(); }
        
    public class LocalGraphRDB extends GraphRDB
        {
        public LocalGraphRDB( ReificationStyle style )
            { super( con, "testGraph-" + count ++, properties, styleRDB( style ), true ); }   
        } 
    
    protected final class GraphRDBWithoutFind extends GraphRDB
        {
        public GraphRDBWithoutFind()
            {
            super( con, "testGraph-" + count ++, properties, styleRDB( ReificationStyle.Minimal ), true );
            }

        public ExtendedIterator graphBaseFind( TripleMatch t )
            { throw new JenaException( "find is Not Allowed" ); }
        
        public void performDelete( Triple t )
            { throw new JenaException( "delete is Not Allowed" ); }
        }
    
    public void testRemoveAllUsesClearNotDelete()
        {
        Graph g = new GraphRDBWithoutFind();
        graphAdd( g, "a P b; c Q d" );
        g.getBulkUpdateHandler().removeAll();
        assertEquals( 0, g.size() );
        }
    }


/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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