/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.assembler.test;

import java.lang.reflect.Field;
import java.util.List;

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

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return OntModelAssembler.class; }

    public void testOntModelAssemblerType()
        { testDemandsMinimalType( new OntModelAssembler(), JA.OntModel );  }
    
    protected static void addParameterisedTests( TestSuite result ) 
        {
        Field [] fields = OntModelSpec.class.getFields();
            for ( Field f : fields )
            {
                String name = f.getName();
                if ( f.getType() == OntModelSpec.class )
                {
                    try
                    {
                        result.addTest( createTest( (OntModelSpec) f.get( null ), name ) );
                    }
                    catch ( Exception e )
                    {
                        System.err.println( "WARNING: failed to create test for OntModelSpec " + name );
                    }
                }
            }
        }    
    
    protected static Test createTest( final OntModelSpec spec, final String name )
        {
        return new TestOntModelAssembler( name )
            {
            @Override
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
            @Override
            protected Model openEmptyModel( Assembler a, Resource root, Mode irrelevant )
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
    
    public void testSubModels()
        {
        final Model baseModel = model( "a P b" );
        Assembler a = new OntModelAssembler();
        Assembler aa = new ModelAssembler()
            {
            @Override
            protected Model openEmptyModel( Assembler a, Resource root, Mode irrelevant )
                { 
                assertEquals( resource( "y" ), root );
                return baseModel;  
                }
            };
        Object m = a.open( aa, resourceInModel( "x rdf:type ja:OntModel; x ja:subModel y" ) );
        assertInstanceOf( OntModel.class, m );
        OntModel om = (OntModel) m;
        List<OntModel> subModels = om.listSubModels().toList();
        assertEquals( 1, subModels.size() );
        assertSame( baseModel.getGraph(), subModels.get( 0 ).getBaseModel().getGraph() );
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
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM ) ;
        Assembler mock = new NamedObjectAssembler( resource( "y" ), spec );
        OntModel om = (OntModel) a.open( mock, root );
        assertSame( spec, om.getSpecification() );
        }
    }
