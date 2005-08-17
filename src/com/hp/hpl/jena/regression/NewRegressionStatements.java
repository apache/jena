/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionStatements.java,v 1.1 2005-08-17 10:56:23 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.regression.Regression.*;

public class NewRegressionStatements extends ModelTestBase
    {
    public NewRegressionStatements( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionStatements.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    static final String subjURI = "http://aldabaran.hpl.hp.com/foo";
    static final String predURI = "http://aldabaran.hpl.hp.com/bar";

    protected Resource r;
    protected Property p;
    
    protected Model m;
    
    public void setUp()
        {
        m = getModel();
        r = m.createResource( subjURI );
        p = m.createProperty( predURI );
        }
    
    public void tearDown()
        { m = null; r = null; p = null; }
    
    public void testCreateStatementTrue()
        {
        Statement s = m.createStatement( r, p, true );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( true, s.getBoolean() );
        }

    public void testCreateStatementByteMax()
        {
        Statement s = m.createStatement( r, p, Byte.MAX_VALUE );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( Byte.MAX_VALUE, s.getByte() );
        }
    
    public void testCreateStatementShortMax()
        {
        Statement s = m.createStatement( r, p, Short.MAX_VALUE );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( Short.MAX_VALUE, s.getShort() );
        }
    
    public void testCreateStatementIntMax()
        {
        Statement s = m.createStatement( r, p, Integer.MAX_VALUE );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( Integer.MAX_VALUE, s.getInt() );
        }
    
    public void testCreateStatementLongMax()
        {
        Statement s = m.createStatement( r, p, Long.MAX_VALUE );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( Long.MAX_VALUE, s.getLong() );
        }
    
    public void testCreateStatementChar()
        {
        Statement s = m.createStatement( r, p, '$' );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( '$', s.getChar() );
        }
    
    public void testCreateStatementFloat()
        {
        Statement s = m.createStatement( r, p, 123.456f );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( 123.456f, s.getFloat(), 0.0005 );
        }
    
    public void testCreateStatementDouble()
        {
        Statement s = m.createStatement( r, p, 12345.67890d );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( 12345.67890d, s.getDouble(), 0.0000005 );
        }
    
    public void testCreateStatementString()
        {
        String string = "this is a plain string", lang = "en";
        Statement s = m.createStatement( r, p, string );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( string, s.getString() );
        assertEquals( lang, m.createStatement( r, p, string, lang ).getLanguage() );
        }
    
    public void testCreateStatementFactory()
        {
        LitTestObj tv = new LitTestObj( Long.MIN_VALUE );
        Statement s = m.createStatement( r, p, tv );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( tv, s.getObject( new LitTestObjF() ) );
        }

    public void testCreateStatementResource()
        {
        Resource tv = m.createResource();
        Statement s = m.createStatement( r, p, tv );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( tv, s.getResource() );
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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