/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestQueryRDB.java,v 1.4 2003-08-27 12:56:20 andy_seaborne Exp $
*/

package com.hp.hpl.jena.db.test;

import com.hp.hpl.jena.graph.query.test.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;

/**
    Apply the abstract query tests to an RDB graph.
 	@author kers
*/
public class TestQueryRDB extends AbstractTestQuery
    {
    public TestQueryRDB( String name )
        { super( name ); }

	public static TestSuite suite()
        { return new TestSuite( TestQueryRDB.class ); }     
     
    private IDBConnection theConnection;
    private int count = 0;
    
    public void setUp()
        {
        theConnection = TestConnection.makeAndCleanTestConnection();
        super.setUp();
        }
        
    public void tearDown()
        {
        try { theConnection.close(); }
        catch (Exception e) { throw new JenaException( e ); }
        }
        
    public Graph getGraph()
        { 
        return new GraphRDB
            (
            theConnection,
            "testGraph-" + count ++, 
            theConnection.getDefaultModelProperties().getGraph(), 
            GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS, 
            true
            );
        }

    }

        



/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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