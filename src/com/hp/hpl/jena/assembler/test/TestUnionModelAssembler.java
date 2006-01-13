/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestUnionModelAssembler.java,v 1.4 2006-01-13 08:38:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.AddDeniedException;

public class TestUnionModelAssembler extends AssemblerTestBase
    {
    public TestUnionModelAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return UnionModelAssembler.class; }

    public void testUnionModelAssemblerType()
        { testDemandsMinimalType( new UnionModelAssembler(), JA.UnionModel );  }

    public void testUnionVocabulary()
        {
        assertSubclassOf( JA.UnionModel, JA.Model );
        assertDomain( JA.UnionModel, JA.subModel );
        assertRange( JA.Model, JA.subModel );
        assertDomain( JA.UnionModel, JA.rootModel );
        assertRange( JA.Model, JA.rootModel );
        }

    public void testCreatesMultiUnion() 
        {
        Resource root = resourceInModel( "x rdf:type ja:UnionModel" );
        Assembler a = new UnionModelAssembler();
        Model m = a.openModel( root );
        assertInstanceOf( MultiUnion.class, m.getGraph() );
        checkImmutable( m );
        }

    private void checkImmutable( Model m )
        {
        try { m.add( statement( "S P O" ) ); fail( "should be immutable" ); }
        catch (AddDeniedException e) { pass(); }
        }
    
    static class SmudgeAssembler extends AssemblerBase 
        {
        Map map = new HashMap();
        
        public SmudgeAssembler add( String name, Model m )
            {
            map.put( resource( name ), m );
            return this;
            }

        public Model openModel( Resource root, Mode mode )
            { return (Model) open( this, root, mode ); }
        
        public Object open( Assembler a, Resource root, Mode irrelevant )
            { return (Model) map.get( root ); }
        }
    
    public void testCreatesUnionWithSubModels()
        {
        Resource root = resourceInModel( "x rdf:type ja:UnionModel; x ja:subModel A; x ja:subModel B" );
        Assembler a = new UnionModelAssembler();
        Model modelA = model( "" ), modelB = model( "" );
        Set expected = new HashSet(); expected.add( modelA.getGraph() ); expected.add( modelB.getGraph() );
        Assembler mock = new SmudgeAssembler().add( "A", modelA ).add( "B", modelB );
        Model m = (Model) a.open( mock, root );
        assertInstanceOf( MultiUnion.class, m.getGraph() );
        MultiUnion mu = (MultiUnion) m.getGraph();
        List L = mu.getSubGraphs();
        assertEquals( expected, new HashSet( L ) );
        checkImmutable( m );
        }
    
    public void testSubModelsCheckObject()
        {
        Resource root = resourceInModel( "x rdf:type ja:UnionModel; x ja:subModel 'A'" );
        Assembler a = new UnionModelAssembler();
        try 
            { 
            a.open( root ); 
            fail( "should trap unsuitable object" );
            }
        catch (BadObjectException e) 
            { 
            assertEquals( resource( "x" ), e.getRoot() ); 
            assertEquals( rdfNode( empty, "'A'" ), e.getObject() );
            }
        }
    
    public void testCreatesUnionWithBaseModel()
        {
        Resource root = resourceInModel( "x rdf:type ja:UnionModel; x ja:subModel A; x ja:rootModel B" );
        Assembler a = new UnionModelAssembler();
        Model modelA = model( "" ), modelB = model( "" );
        Set expected = new HashSet(); expected.add( modelA.getGraph() ); 
        Assembler mock = new SmudgeAssembler().add( "A", modelA ).add( "B", modelB );
        Model m = (Model) a.open( mock, root );
        assertInstanceOf( MultiUnion.class, m.getGraph() );
    //
        MultiUnion mu = (MultiUnion) m.getGraph();
        assertSame( modelB.getGraph(), mu.getBaseGraph() );
        assertEquals( listOfOne( modelA.getGraph() ), mu.getSubGraphs() );
        m.add( statement( "a P b" ) );
        assertIsoModels( model( "a P b" ), modelB );
        }
    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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