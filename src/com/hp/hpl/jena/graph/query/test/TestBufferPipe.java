/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestBufferPipe.java,v 1.1 2004-12-01 10:46:47 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.query.BufferPipe;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.shared.JenaException;

/**
     TestBufferPipe
     @author kers
*/
public class TestBufferPipe extends GraphTestBase
    {
    public TestBufferPipe( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestBufferPipe.class ); }

    public void testEmpty()
        {
        Pipe p = new BufferPipe();
        p.close();
        assertFalse( p.hasNext() );
        }
    
    public void testNonEmpty()
        {
        Pipe p = new BufferPipe();
        p.put( new Domain( 0 ) );
        p.close();
        assertTrue( p.hasNext() );
        p.get();
        assertFalse( p.hasNext() );
        }
    
    public void testExceptions()
        {
//        Pipe p = new BufferPipe();
//        JenaException bang = new JenaException( "bang" );
//        p.close( bang );
//        try { p.get(); fail( "bang disappeared" ); }
//        catch (Exception e) { fail( "bango" + e ); }
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