/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestReifier.java,v 1.12 2003-08-05 14:34:08 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.test;

import java.util.ArrayList;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;

import junit.framework.*;

/**
    Derived from the original reifier tests, and then folded back in by using an
    abstract test base class.
    @author kers, csayers.
*/

public class TestReifier extends AbstractTestReifier  {
	private ArrayList theGraphs = new ArrayList();
	private IDBConnection theConnection;

	public TestReifier(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(TestReifier.class);
	}

	public void setUp() 
        { theConnection = TestConnection.makeAndCleanTestConnection(); }

	public void tearDown() throws Exception 
        {
		theConnection.cleanDB();
		theGraphs.clear();
		theConnection.close();
        }

	public Graph getGraph( Reifier.Style style ) {
		GraphRDB g = new GraphRDB
            (
            theConnection, 
            "name" + theGraphs.size(), 
            theConnection.getDefaultModelProperties().getGraph(), 
            GraphRDB.styleRDB( style ), 
            true 
            );
		theGraphs.add( g );
		return g;		
	   }
       
    public Graph getGraph()
        { return getGraph( Reifier.Convenient ); }
        
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
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
