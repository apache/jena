/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestModelAssembler.java,v 1.7 2008-01-02 12:05:57 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.assembler.exceptions.UnknownStyleException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

public class TestModelAssembler extends AssemblerTestBase
    {
    protected static final class FakeModelAssembler extends ModelAssembler
        {
        protected Model openModel( Assembler a, Resource root, Mode mode )
            { return ModelFactory.createDefaultModel(); }
        }

    public TestModelAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return null; }
    
    public void testModelAssemblerVocabulary()
        {
        assertDomain( JA.Model, JA.reificationMode );
        assertRange( JA.ReificationMode, JA.reificationMode );
        assertType( JA.ReificationMode, JA.minimal );
        assertType( JA.ReificationMode, JA.standard );
        assertType( JA.ReificationMode, JA.convenient );
        }

    public void testContent()
        {
//        Resource root = resourceInModel( "x rdf:type ja:DefaultModel; x ja:initialContent c; c ja:quotedContent A; A P B" );
//        root.getModel().write( System.err, "N3"  );
//        Model m = (Model) new FakeModelAssembler().open( new ContentAssembler(), root, Mode.ANY );
//        assertIsoModels( modelWithStatements( "A P B" ), m );
        }
    
    public void testGetsPrefixMappings()
        { 
        Assembler a = new FakeModelAssembler();
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "my", "urn:secret:42/" )
            .setNsPrefix( "your", "urn:public:17#" );
        Resource root = resourceInModel
            ( "x rdf:type ja:DefaultModel; x ja:includes p1; x ja:includes p2"
            + "; p1 rdf:type ja:PrefixMapping; p1 ja:prefix 'my'; p1 ja:namespace 'urn:secret:42/'"
            + "; p2 rdf:type ja:PrefixMapping; p2 ja:prefix 'your'; p2 ja:namespace 'urn:public:17#'" );
        Model m = (Model) a.open( Assembler.prefixMapping, root );
        assertSamePrefixMapping( wanted, m );
        }
    
    public void testGetsStandardReificationMode()
        {
        final List style = new ArrayList();
        Assembler a = new ModelAssembler() 
            {
            protected Model openModel( Assembler a, Resource root, Mode irrelevant )
                {
                style.add( getReificationStyle( root ) );
                return ModelFactory.createDefaultModel(); 
                }
            };
        Model m = a.openModel( resourceInModel( "a rdf:type ja:Model" ) );
        assertEquals( listOfOne( ReificationStyle.Standard ), style );
        }
    
    public void testGetsExplicitReificationMode()
        {
        testGetsStyle( "ja:minimal", ReificationStyle.Minimal );
        testGetsStyle( "ja:standard", ReificationStyle.Standard );
        testGetsStyle( "ja:convenient", ReificationStyle.Convenient );
        }
    
    public void testUnknownStyleFails()
        {
        try
            { 
            testGetsStyle( "unknown", ReificationStyle.Standard );
            fail( "should trap unknown reification style" );
            }
        catch (UnknownStyleException e)
            {
            assertEquals( resource( "unknown" ), e.getStyle() );
            assertEquals( resource( "a" ), e.getRoot() );
            }
        }

    private void testGetsStyle( String styleString, ReificationStyle style )
        {
        final List styles = new ArrayList();
        Assembler a = new ModelAssembler() 
            {
            protected Model openModel( Assembler a, Resource root, Mode irrelevant )
                {
                styles.add( getReificationStyle( root ) );
                return ModelFactory.createDefaultModel(); 
                }
            };
        Model m = a.openModel( resourceInModel( "a rdf:type ja:Model; a ja:reificationMode " + styleString ) );
        assertEquals( listOfOne( style ), styles );
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
