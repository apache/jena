/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestOntModelAssembler.java,v 1.3 2006-01-09 16:02:17 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.lang.reflect.Field;

import junit.framework.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

public class TestOntModelAssembler extends AssemblerTestBase
    {
    public TestOntModelAssembler( String name )
        { super( name ); }

    public static TestSuite suite() 
        {
        TestSuite result = new TestSuite();
        result.addTestSuite( TestOntModelAssembler.class );
        addParameterisedTests( result );
        return result;
        }

    protected Class getAssemblerClass()
        { return OntModelAssembler.class; }

    public void testOntModelAssemblerType()
        { testDemandsMinimalType( new OntModelAssembler(), JA.OntModel );  }
    
    protected static void addParameterisedTests( TestSuite result ) 
        {
        Field [] fields = OntModelSpec.class.getFields();
        for (int i = 0; i < fields.length; i += 1)
            {
            Field f = fields[i];
            String name = f.getName();
            if (f.getType() == OntModelSpec.class) 
                try { result.addTest( createTest( (OntModelSpec) f.get(null), name ) ); }
                catch (Exception e) 
                    {
                    System.err.println( "WARNING: failed to create test for OntModelSpec " + name );
                    }
            }
        }    
    
    protected static Test createTest( final OntModelSpec spec, final String name )
        {
        return new TestOntModelAssembler( name )
            {
            public void runBare()
                { 
                Assembler a = new OntModelAssembler();
                Model m = (Model) a.open( new FixedObjectAssembler( spec ), resourceInModel( "x rdf:type ja:OntModel; x ja:ontModelSpec ja:" + name ) );
                assertInstanceOf( OntModel.class, m );
                OntModel om = (OntModel) m;
                assertSame( spec, om.getSpecification() ); 
                }
            };
        }

    public void testAllDefaults()
        {
        Assembler a = new OntModelAssembler();
        Model m = a.openModel( resourceInModel( "x rdf:type ja:OntModel" ) );
        assertInstanceOf( OntModel.class, m );
        OntModel om = (OntModel) m;
        assertSame( OntModelSpec.OWL_MEM_RDFS_INF, om.getSpecification() );
        }
    
    public void testBaseModel()
        {
        final Model baseModel = model( "a P b" );
        Assembler a = new OntModelAssembler();
        Assembler aa = new ModelAssembler()
            {
            protected Model openModel( Assembler a, Resource root, Mode irrelevant )
                { 
                assertEquals( resource( "y" ), root );
                return baseModel;  
                }
            };
        Object m = a.open( aa, resourceInModel( "x rdf:type ja:OntModel; x ja:baseModel y" ) );
        assertInstanceOf( OntModel.class, m );
        OntModel om = (OntModel) m;
        assertSame( baseModel.getGraph(), om.getBaseModel().getGraph() );
        }
    
    public void testDefaultDocumentManager()
        {
        Assembler a = new OntModelAssembler();
        Resource root = resourceInModel( "x rdf:type ja:OntModel" );
        OntModel om = (OntModel) a.openModel( root );
        assertSame( OntDocumentManager.getInstance(), om.getDocumentManager() );
        }
    
    public void testUsesOntModelSpec()
        {
        Assembler a = new OntModelAssembler();
        Resource root = resourceInModel( "x rdf:type ja:OntModel; x ja:ontModelSpec y" );
        OntModelSpec spec = new OntModelSpec( OntModelSpec.DAML_MEM );
        Assembler mock = new NamedObjectAssembler( resource( "y" ), spec );
        OntModel om = (OntModel) a.open( mock, root );
        assertSame( spec, om.getSpecification() );
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