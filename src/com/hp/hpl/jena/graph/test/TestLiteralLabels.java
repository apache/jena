/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestLiteralLabels.java,v 1.7 2008-01-02 12:05:32 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.impl.LiteralLabel;

import junit.framework.*;

/**
    Tests submitted By: Wolfgang Groiss (littlelui) via SF bugtracker, incorporated
    into new test class by kers.
    
    @author kers
*/
public class TestLiteralLabels extends GraphTestBase
    {
    public TestLiteralLabels( String name )
        { super( name );  }

    public static Test suite()
        { return new TestSuite( TestLiteralLabels.class ); }
    
    public void testHashCode()  
        {
        LiteralLabel ll = new LiteralLabel( "test", "", null );
        ll.hashCode();
        }

    public void testHashCode2() 
        {
        LiteralLabel ll = new LiteralLabel( (Object) "test",  "", null );
        ll.hashCode();
        }    
    
    public void testHashCodesForBase64Binary()
        {
        LiteralLabel A = node( "'0123'http://www.w3.org/2001/XMLSchema#base64Binary" ).getLiteral();
        LiteralLabel B = node( "'0123'http://www.w3.org/2001/XMLSchema#base64Binary" ).getLiteral();
        assertEquals( A.hashCode(), B.hashCode() );
        }
    
    public void testHashCodesForHexBinary()
        {
        LiteralLabel A = node( "'0123'http://www.w3.org/2001/XMLSchema#hexBinary" ).getLiteral();
        LiteralLabel B = node( "'0123'http://www.w3.org/2001/XMLSchema#hexBinary" ).getLiteral();
        assertEquals( A.hashCode(), B.hashCode() );
        }

    // AFS
    public void testEquality1()
    {
        LiteralLabel A = new LiteralLabel("xyz") ;
        LiteralLabel B = new LiteralLabel("xyz") ;
        assertTrue(A.equals(B)) ;
        assertTrue(A.sameValueAs(B)) ;
        assertEquals(A.hashCode(), B.hashCode()) ;
    }
    
    public void testEquality2()
    {
        LiteralLabel A = new LiteralLabel("xyz") ;
        LiteralLabel B = new LiteralLabel("XYZ") ;
        assertFalse(A.equals(B)) ;
        assertFalse(A.sameValueAs(B)) ;
    }

    public void testEquality3()
    {
        LiteralLabel A = new LiteralLabel("xyz", "en-us") ;
        LiteralLabel B = new LiteralLabel("xyz", "en-uk") ;
        assertFalse(A.equals(B)) ;
        assertFalse(A.sameValueAs(B)) ;
    }

    public void testEquality4()
    {
        LiteralLabel A = new LiteralLabel("xyz", "en-UK") ;
        LiteralLabel B = new LiteralLabel("xyz", "en-uk") ;
        assertFalse(A.equals(B)) ;
        assertTrue(A.sameValueAs(B)) ;
    }
    
}


/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/