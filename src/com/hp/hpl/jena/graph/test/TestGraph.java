/*
  (c) Copyright 2002, 2003 Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestGraph.java,v 1.11 2003-05-13 14:59:45 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

/**
    @author kers
<br>
    even more extended testcase code
*/

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.graph.*;

import junit.framework.*;

public class TestGraph extends GraphTestBase
    { 
	public TestGraph( String name )
		{ super( name ); }

    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite();
        result.addTest( TestDefaultGraph.suite() );
        result.addTest( TestStandardGraph.suite() );
        return result;
        }
        
    public static class TestDefaultGraph extends AbstractTestGraph
        {
        public TestDefaultGraph( String name ) { super( name ); }
        public static TestSuite suite() { return new TestSuite( TestDefaultGraph.class ); }
        public Graph getGraph() { return new GraphMem(); }
        }
        
    public static class TestStandardGraph extends AbstractTestGraph
        {
        public TestStandardGraph( String name ) { super( name ); }
        public static TestSuite suite() { return new TestSuite( TestStandardGraph.class ); }
        public Graph getGraph() { return new GraphMem( Reifier.Standard ); }
        }
        
    }

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
