/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestFactory.java,v 1.4 2003-05-02 15:30:36 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

/**
 	@author kers
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.db.test.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.*;

import java.util.*;

import java.sql.SQLException;

import junit.framework.*;

public class TestFactory extends GraphTestBase
    {
    public TestFactory( String name )
        { super( name ); };
        
    public static TestSuite suite()
        { return new TestSuite( TestFactory.class ); }   

    private TrackingFactory factory;
    private IDBConnection connection;
    
    public void setUp()
        {
        connection = TestConnection.makeTestConnection();
        factory = new TrackingFactory( connection );
        }
        
    private static class TrackingFactory extends GraphRDBFactory
        {
        Set names = new HashSet();
        
        public TrackingFactory( IDBConnection c ) { super( c ); }
        
        public Graph createGraph( String name )
            {
            Graph result = super.createGraph( name );
            names.add( name );
            return result;
            }
        }
        
    public void tearDown()
        {
        Iterator it = factory.names.iterator();
        while (it.hasNext())
            {
            String name = (String) it.next();
            try
                {
                GraphRDB g = (GraphRDB) factory.openGraph( name );
                g.remove();
                g.close();
                }
            catch (DoesNotExistException e)
                {}
            }
        
//         try { connection.cleanDB(); connection.close(); }
//         catch (SQLException s) { throw new JenaException( s ); }
        }
        
    public void testFactory()
        {
        Graph g = Factory.createDefaultGraph();
        }
        
    public void testCanRemoveOpenedGraph()
        {
        String name = "jena_testing_remove";
        GraphRDB g = (GraphRDB) factory.createGraph( name );     
        g.close();
        GraphRDB g2 = (GraphRDB) factory.openGraph( name );  
        g2.remove();
        g2.close();
        }
        
    public void testCannotCreateTwice()
        {
        String name = "jena_testing_benjamin";
        GraphRDB g = (GraphRDB) factory.createGraph( name );
        g.close();
        try
            {
            factory.createGraph( name );
            fail( "should not be able to re-create " + name );
            }
        catch (AlreadyExistsException a)
            { /* good - that's what we expected */ }
        }
        
    public void testCreatePersistentGraph()
        {
        Graph g = factory.createGraph( "xxx" );
        ((GraphRDB) g).remove();
        g.close();
        }
        
    public void testCannotOpenNonExistentGraph()
        {
        String name = "beetle";
        try 
            { 
            Graph g = factory.openGraph( name );
            g.close();
            fail( "should not be able to open " + name );
            }
        catch (DoesNotExistException e)
            { /* find - that's what we expect */ }
        }
        
    public void testOpenPersistentGraph()
        {
        Graph g = factory.createGraph( "xxx" );
        g.close();
        Graph g2 = factory.openGraph( "xxx" );
        g2.close();
        }
        
    public void testPersistent()
        {
        Graph triples = graphWith( "x R y" );
        Graph g = factory.createGraph( "alpha" );
        g.getBulkUpdateHandler().add( triples );
        g.close();
        Graph g2 = factory.openGraph( "alpha" );
        assertTrue( "should retrieve triples", triples.isIsomorphicWith( g2 ) );
        g2.close();
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