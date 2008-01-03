/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestModelContent.java,v 1.8 2008-01-03 09:33:04 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.TransactionAbortedException;
import com.hp.hpl.jena.rdf.model.*;

public class TestModelContent extends AssemblerTestBase
    {
    public TestModelContent( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return null; }
    
    public void testMemoryModelLoadsSingleContent()
        {
        testModelLoadsSingleContent( Assembler.memoryModel, JA.MemoryModel );
        }
    
    public void testMemoryModelLoadsMultipleContent()
        {
        testModelLoadsMultipleContent( Assembler.memoryModel, JA.MemoryModel );
        }
    
    public void testDefaultModelLoadsSingleContent()
        {
        testModelLoadsSingleContent( Assembler.defaultModel, JA.DefaultModel );
        }
    
    public void testDefaultModelLoadsMultipleContent()
        {
        testModelLoadsMultipleContent( Assembler.defaultModel, JA.DefaultModel );
        }
    
    public void testInfModelLoadsContent()
        {
        testModelLoadsMultipleContent( Assembler.infModel, JA.InfModel );
        }

    public void testContentTransactionsNone()
        {
        final List history = new ArrayList();
        final Model expected = model( "_x rdf:value '17'xsd:integer" );
        Assembler a = new MockTransactionModel( history, expected, false, true );
        Resource root = resourceInModel
            ( "x rdf:type ja:Model; x ja:content y; y rdf:type ja:Content; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17.'" );
        try { a.open( Assembler.content, root  ); }
        catch (RuntimeException e) {}
        }
    
    public void testContentTransactionsCommit()
        {
        final List history = new ArrayList();
        final Model expected = model( "_x rdf:value '17'xsd:integer" );
        Assembler a = new MockTransactionModel( history, expected, true, false );
        Resource root = resourceInModel
            ( "x rdf:type ja:Model; x ja:content y; y rdf:type ja:Content; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17.'" );
        Model m = (Model) a.open( Assembler.content, root  );
        assertEquals( listOfStrings( "supports[true] begin add commit" ), history );
        assertIsoModels( expected, m );
        }

    public void testContentTransactionsAbort()
        {
        final List history = new ArrayList();
        final Model expected = model( "_x rdf:value '17'xsd:integer" );
        final Model toDeliver = model( "" ).add( expected );
        Assembler a = new MockTransactionModel( history, toDeliver, true, true );
        try
            {
            Resource root = resourceInModel
                ( "x rdf:type ja:Model; x ja:content y; y rdf:type ja:Content"
                + "; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17.'" );
            a.open( Assembler.content, root  );
            fail( "should throw (wrapped) failing exception" );
            }
        catch (TransactionAbortedException  e)
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( listOfStrings( "supports[true] begin add abort" ), history );
            assertIsoModels( expected, toDeliver );
            }        
        }
    
    protected void testModelLoadsSingleContent( Assembler a, Resource type )
        {
        Resource root = resourceInModel
            ( "x rdf:type " + type + "; x ja:content y; y rdf:type ja:Content"
            + "; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17.'" );
        Model m = (Model) a.open( Assembler.content, root );
        assertIsoModels( model( "_x rdf:value '17'xsd:integer" ), m );
        }

    protected void testModelLoadsMultipleContent( Assembler a, Resource type )
        {
        Model m = (Model) a.open( Assembler.content, resourceInModel
                ( "x rdf:type " + type + "; x ja:content y; x ja:content z"
                + "; y rdf:type ja:Content; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17.'"
                + "; z rdf:type ja:Content; z rdf:type ja:LiteralContent; z ja:literalContent '_:x\\srdf:value\\s42.'" ) );
        assertIsoModels( model( "_x rdf:value '17'xsd:integer; _y rdf:value '42'xsd:integer" ), m );
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