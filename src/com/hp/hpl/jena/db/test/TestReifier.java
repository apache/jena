/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestReifier.java,v 1.17 2003-09-22 12:48:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.test;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;

/**
    Derived from the original reifier tests, and then folded back in by using an
    abstract test base class.
    @author kers, csayers.
*/

public class TestReifier extends AbstractTestReifier  {
    
    private int count;
    private Graph properties;
	private IDBConnection con;

	public TestReifier( String name ) 
        { super(name); }

    /** 
        Initialiser required for MetaTestGraph interface.
     */
    public TestReifier( Class graphClass, String name, ReificationStyle style ) 
        { super( name ); }
        
	public static TestSuite suite() {
		return MetaTestGraph.suite( TestReifier.class, LocalGraphRDB.class );
	}
        
    /**
        LocalGraphRDB - an extension of GraphRDB that fixes the connection to 
        TestReifier's connection, passes in the appropriate reification style, uses the
        default properties of the connection, and gives each graph a new name
        exploiting the count.
    
    	@author kers
     */
    public class LocalGraphRDB extends GraphRDB
        {
        public LocalGraphRDB( ReificationStyle style )
            { super( con, "testGraph-" + count ++, properties, styleRDB( style ), true ); }   
        } 
        
	public void setUp() 
        { con = TestConnection.makeAndCleanTestConnection(); 
        properties = con.getDefaultModelProperties().getGraph(); }

	public void tearDown() throws Exception 
        { con.close(); }

    public Graph getGraph( ReificationStyle style )
        { return new LocalGraphRDB( style ); }
        
    }

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
