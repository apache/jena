/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestGraphRDB.java,v 1.5 2003-05-09 10:22:10 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;

/**
 	@author kers
*/
public class TestGraphRDB extends AbstractTestGraph
    {
    public TestGraphRDB(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestGraphRDB.class ); }

    private Graph theGraph;
    private IDBConnection theConnection;
    
    public void setUp()
        {
        theConnection = TestConnection.makeAndCleanTestConnection();
        theGraph = new GraphRDB
            (
            theConnection,
            "bootle", 
            theConnection.getDefaultModelProperties().getGraph(), 
            GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS, 
            true
            );
        }
        
    public void tearDown()
        { 
        theGraph.close();
        try { theConnection.close(); }
        catch (Exception e) { throw new JenaException( e ); }
        }
        
    public Graph getGraph()
        { return theGraph; }

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