/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestGraphRDBFactory.java,v 1.2 2003-05-08 15:19:27 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.test;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;

import junit.framework.*;

/**
 	@author hedgehog
    
    Test the RDB graph factory, based on the abstract test class. We track the
    current graph factory so that we can discard all the graphs we create during
    the test.
*/

public class TestGraphRDBFactory extends AbstractTestGraphMaker
    {
    /**
        A clean test connection for all the graph factories.
    */
    IDBConnection connection = TestConnection.makeAndCleanTestConnection();
    
    public TestGraphRDBFactory( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestGraphRDBFactory.class ); }

    /**
        The current factory object, or null when there isn't one.
     */
    private GraphRDBMaker current;
    
    /**
        Invent a new factory on the connection, record it, and return it.    
    */
    public GraphMaker getGraphFactory()
        { return current = new GraphRDBMaker( connection ); }    
        
    /**
        Run the parent teardown, and then remove all the freshly created graphs.
    */
    public void tearDown()
        {
        super.tearDown();
        if (current != null) current.removeAll();
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