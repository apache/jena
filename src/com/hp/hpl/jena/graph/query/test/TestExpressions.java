/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestExpressions.java,v 1.1 2004-07-20 17:17:19 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import junit.framework.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;

/**
 @author hedgehog
 */
public class TestExpressions extends GraphTestBase
    {
    public TestExpressions( String name ) 
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestExpressions.class ); }
    
    public void testSpoo()
        {
        }
    
    public void testPrepareTRUE()
        {
        IndexValues none = new IndexValues() 
            { public Object get( int i ) { return null; } };
        Valuator t = Expression.TRUE.prepare( new Mapping( new Node[0] ) );  
        assertEquals( true, t.evalBool( none ) );    
        }
        
    public void testPrepareFALSE()
        {
        IndexValues none = new IndexValues() 
            { public Object get( int i ) { return null; } };
        Valuator t = Expression.FALSE.prepare( new Mapping( new Node[0] ) );  
        assertEquals( false, t.evalBool( none ) );    
        }
    
    public void testLiterals()
        {
        assertTrue( Expression.TRUE.isConstant() );
        assertTrue( Expression.FALSE.isConstant() );
        assertEquals( Boolean.TRUE, Expression.TRUE.getValue() );
        assertEquals( Boolean.FALSE, Expression.FALSE.getValue() );
        }
    
    }

/*
	(c) Copyright 2004, Hewlett-Packard Development Company, LP
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