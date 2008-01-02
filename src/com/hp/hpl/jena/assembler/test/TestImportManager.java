/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestImportManager.java,v 1.9 2008-01-02 12:05:55 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class TestImportManager extends AssemblerTestBase
    {
    public TestImportManager( String name )
        { super( name ); }    
    
    static class FixedFileManager extends FileManager
        {
        Map map = new HashMap();
        
        public Model loadModel( String URL )
            {
            Model result = (Model) map.get( URL );
            if (result == null) fail( "no model for " + URL );
            return result;
            }
        
        public FixedFileManager add( String URL, Model m )
            {
            map.put( URL, m );
            return this;
            }
        }

    public void testFollowOwlImports()
        {
        final Model modelToLoad = model( "this hasMarker B5" );
        Model  m = model( "x ja:reasoner y; _x owl:imports eh:/loadMe" );
        FileManager fm = new FixedFileManager().add( "eh:/loadMe", modelToLoad ); 
        Model m2 = new ImportManager().withImports( fm, m );
        assertInstanceOf( MultiUnion.class, m2.getGraph() );
        assertIsoModels( modelToLoad.union( m ), m2 );
        }
    
    public void testFollowJAImports()
        {
        final Model modelToLoad = model( "this hasMarker B5" );
        Model  m = model( "x ja:reasoner y; _x ja:imports eh:/loadMe" );
        FileManager fm = new FixedFileManager().add( "eh:/loadMe", modelToLoad ); 
        Model m2 = new ImportManager().withImports( fm, m );
        assertInstanceOf( MultiUnion.class, m2.getGraph() );
        assertIsoModels( modelToLoad.union( m ), m2 );
        }
    
    public void testImportMayBeLiteral()
        {
        final Model modelToLoad = model( "this hasMarker B5" );
        Model  m = model( "x ja:reasoner y; _x ja:imports 'eh:/loadMe'" );
        FileManager fm = new FixedFileManager().add( "eh:/loadMe", modelToLoad ); 
        Model m2 = new ImportManager().withImports( fm, m );
        assertInstanceOf( MultiUnion.class, m2.getGraph() );
        assertIsoModels( modelToLoad.union( m ), m2 );
        }
    
    public void testBadImportObjectFails()
        {
        testBadImportObjectFails( "_bnode" );
        testBadImportObjectFails( "17" );
        testBadImportObjectFails( "'chat'fr" );
        testBadImportObjectFails( "'chat'xsd:wrong" );
        }

    private void testBadImportObjectFails( String object )
        {
        String string = "x ja:imports " + object;
        Model m = model( string );
        try 
            { 
            new ImportManager().withImports( m );
            fail( "should trap bad import specification " + string );
            }
        catch (BadObjectException e)
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( rdfNode( m, object ), e.getObject() );
            }
        }
    
    public void testFollowOwlImportsDeeply()
        {
        final Model 
            m1 = model( "this hasMarker M1; _x owl:imports M2" ),
            m2 = model( "this hasMarker M2" );
        Model  m = model( "x ja:reasoner y; _x owl:imports M1" );
        FileManager fm = new FixedFileManager() 
            .add( "eh:/M1", m1 ).add( "eh:/M2", m2 );
        Model result = new ImportManager().withImports( fm, m );
        assertInstanceOf( MultiUnion.class, result.getGraph() );
        assertIsoModels( m1.union(m2).union(m), result );
        }
    
    public void testCatchesCircularity()
        {
        final Model 
            m1 = model( "this hasMarker Mx; _x owl:imports My" ),
            m2 = model( "this hasMarker My; _x owl:imports Mx" );
        FileManager fm = new FixedFileManager()
            .add( "eh:/Mx", m1 ).add( "eh:/My", m2 );
        Model result = new ImportManager().withImports( fm, m1 );
        assertIsoModels( m1.union( m2 ), result );
        }
    
    public void testCacheModels()
        {
        ImportManager im = new ImportManager();
        Model spec = model( "_x owl:imports M1" );
        Model m1 = model( "this isModel M1" );
        FileManager withM1 = new FixedFileManager().add( "eh:/M1", m1 );
        Model A = im.withImports( withM1, spec );
        FileManager none = new FixedFileManager();
        Model B = im.withImports( none, spec );
        assertIsoModels( A, B );
        }
    }


/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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