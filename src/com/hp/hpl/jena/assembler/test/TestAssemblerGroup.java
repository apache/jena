/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestAssemblerGroup.java,v 1.4 2006-03-22 13:52:21 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.assembler.exceptions.NoImplementationException;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.mem.GraphMemBase;
import com.hp.hpl.jena.rdf.model.*;

public class TestAssemblerGroup extends AssemblerTestBase
    {
    public TestAssemblerGroup( String name )
        { super( name );  }

    protected Class getAssemblerClass()
        { return AssemblerGroup.class; }
    
    public void testEmptyAssemblerGroup()
        {
        AssemblerGroup a = AssemblerGroup.create();
        assertInstanceOf( AssemblerGroup.class, a );
        assertEquals( null, a.assemblerFor( resource( "ja:Anything" ) ) );
        checkFailsType( a, "rdf:Resource" );
        }

    protected void checkFailsType( Assembler a, String type )
        {
        try 
            { 
            a.open( resourceInModel( "x rdf:type " + type ) ); 
            fail( "should trap missing implementation" ); 
            }
        catch (NoImplementationException e) 
            { 
            assertEquals( resource( "x" ), e.getRoot() ); 
            assertEquals( JA.Object, e.getType() );
            assertNotNull( e.getAssembler() );
            }
        }
    
    public void testSingletonAssemblerGroup()
        {
        AssemblerGroup a = AssemblerGroup.create();
        assertSame( a, a.implementWith( JA.InfModel, Assembler.infModel ) );
        a.openModel( resourceInModel( "x rdf:type ja:InfModel" ) );
        checkFailsType( a, "js:DefaultModel" );
        }
    
    public void testMultipleAssemblerGroup()
        {
        AssemblerGroup a = AssemblerGroup.create();
        assertSame( a, a.implementWith( JA.InfModel, Assembler.infModel ) );
        assertSame( a, a.implementWith( JA.MemoryModel, Assembler.memoryModel ) );
        assertInstanceOf( InfModel.class, a.openModel( resourceInModel( "x rdf:type ja:InfModel" ) ) );
        assertFalse( a.openModel( resourceInModel( "y rdf:type ja:MemoryModel" ) ) instanceof InfModel );
        checkFailsType( a, "js:DefaultModel" );
        }
    
    public void testImpliedType()
        {
        AssemblerGroup a = AssemblerGroup.create();
        Resource root = resourceInModel( "x ja:reasoner y" );
        Object expected = new Object();
        a.implementWith( JA.InfModel, new NamedObjectAssembler( resource( "x" ), expected ) );
        assertSame( expected, a.open( root ) );
        }
    
    public void testBuiltinGroup()
        {
        AssemblerGroup g = Assembler.general;
        assertInstanceOf( Model.class, g.open( resourceInModel( "x rdf:type ja:DefaultModel" ) ) );
        assertInstanceOf( InfModel.class, g.open( resourceInModel( "x rdf:type ja:InfModel" ) ) );
        assertMemoryModel( g.open( resourceInModel( "x rdf:type ja:MemoryModel" ) ) );
        }
    
    protected void assertMemoryModel( Object object )
        {
        if (object instanceof Model)
            {
            Graph g = ((Model) object).getGraph();
            assertInstanceOf( GraphMemBase.class, g );
            }
        else
            fail( "expected a Model, but got a " + object.getClass() );
        }
    
    public void testPassesSelfIn()
        {
        final AssemblerGroup group = AssemblerGroup.create();
        final Object result = new Object();
        Assembler fake = new AssemblerBase() 
            {
            public Object open( Assembler a, Resource root, Mode irrelevant )
                {
                assertSame( "nested call should pass in assembler group:", group, a );
                return result;
                }
            };
        group.implementWith( JA.Object, fake );
        assertSame( result, group.open( resourceInModel( "x rdf:type ja:Object" ) ) );
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