/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestTripleField.java,v 1.4 2006-03-22 13:52:22 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.*;
import com.hp.hpl.jena.util.iterator.Filter;

/**
    @author kers
*/
public class TestTripleField extends GraphTestBase
    {
    public TestTripleField( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestTripleField.class ); }
    
    public void testFieldsExistAndAreTyped()
        {
        assertInstanceOf( Triple.Field.class, Triple.Field.getSubject );
        assertInstanceOf( Triple.Field.class, Triple.Field.getObject );
        assertInstanceOf( Triple.Field.class, Triple.Field.getPredicate );        
        }
    
    public void testGetSubject()
        {
        assertEquals( node( "s" ), Field.getSubject.getField( triple( "s p o" ) ) );
        }
    
    public void testGetObject()
        {
        assertEquals( node( "o" ), Field.getObject.getField( triple( "s p o" ) ) );
        }  
    
    public void testGetPredicate()
        {
        assertEquals( node( "p" ), Field.getPredicate.getField( triple( "s p o" ) ) );
        }    
    
    public void testFilterSubject()
        {
        assertTrue( Field.getSubject.filterOn( node( "a" ) ).accept( triple( "a P b" ) ) );
        assertFalse( Field.getSubject.filterOn( node( "x" ) ).accept( triple( "a P b" ) ) );
        }    
    
    public void testFilterObject()
        {
        assertTrue( Field.getObject.filterOn( node( "b" ) ).accept( triple( "a P b" ) ) );
        assertFalse( Field.getObject.filterOn( node( "c" ) ).accept( triple( "a P b" ) ) );
        }
    
    public void testFilterPredicate()
        {
        assertTrue( Field.getPredicate.filterOn( node( "P" ) ).accept( triple( "a P b" ) ) );
        assertFalse( Field.getPredicate.filterOn( node( "Q" ) ).accept( triple( "a P b" ) ) );
        }
    
    public void testFilterByTriple()
        {
        assertTrue( Field.getSubject.filterOn( triple( "s P o" ) ).accept( triple( "s Q p" ) ) );
        assertFalse( Field.getSubject.filterOn( triple( "s P o" ) ).accept( triple( "x Q p" ) ) );
        }
    
    public void testWildcardFilterIsAny()
        {
        assertSame( Filter.any, Field.getSubject.filterOn( triple( "?x R s" ) ) );
        assertSame( Filter.any, Field.getObject.filterOn( triple( "x R ?s" ) ) );
        assertSame( Filter.any, Field.getPredicate.filterOn( triple( "x ?R s" ) ) );
        }
    }


/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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