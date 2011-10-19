/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestOntModelSpecAssembler.java,v 1.1 2009-06-29 08:55:53 castagna Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.lang.reflect.Field;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.assembler.exceptions.ReasonerClashException;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.CannotCreateException;

import junit.framework.*;

public class TestOntModelSpecAssembler extends AssemblerTestBase
    {
    public TestOntModelSpecAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return OntModelSpecAssembler.class; }

    public void testOntModelSpecAssemblerType()
        { testDemandsMinimalType( new OntModelSpecAssembler(), JA.OntModelSpec );  }
    
    public static TestSuite suite()
        {
        TestSuite result = new TestSuite();
        result.addTestSuite( TestOntModelSpecAssembler.class );
        addParameterisedTests( result );
        return result;
        }
    
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
    
    protected void testBuiltinSpec( OntModelSpec ontModelSpec, String specName )
        {
        testBuiltinSpecAsRootName( ontModelSpec, specName );
        testBuiltinSpecAsLikeTarget( ontModelSpec, specName );
        }

    private void testBuiltinSpecAsLikeTarget( OntModelSpec ontModelSpec, String specName )
        {
        Resource rr = resourceInModel( "_x rdf:type ja:OntModelSpec; _x ja:likeBuiltinSpec <OM>".replaceAll( "<OM>", JA.getURI() + specName ) );
        assertEquals( ontModelSpec, new OntModelSpecAssembler().open( rr ) );
        }

    private void testBuiltinSpecAsRootName( OntModelSpec ontModelSpec, String specName )
        {
        Resource root = resourceInModel( JA.getURI() + specName + " rdf:type ja:OntModelSpec" );
        assertEquals( ontModelSpec, new OntModelSpecAssembler().open( root ) );
        }
    
    protected static Test createTest( final OntModelSpec spec, final String name )
        {
        return new TestOntModelSpecAssembler( name )
            {
            @Override
            public void runBare()
                { testBuiltinSpec( spec, name ); }
            };
        }
    
    public void testOntModelSpecVocabulary()
        {
        assertDomain( JA.OntModelSpec, JA.ontLanguage );
        assertDomain( JA.OntModelSpec, JA.documentManager );
        assertDomain( JA.OntModelSpec, JA.likeBuiltinSpec );
        }
    
    public void testCreateFreshDocumentManager() 
        { 
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel( "x rdf:type ja:OntModelSpec; x ja:documentManager y" );
        OntDocumentManager dm = new OntDocumentManager();
        NamedObjectAssembler mock = new NamedObjectAssembler( resource( "y" ), dm );
        OntModelSpec om = (OntModelSpec) a.open( mock, root );
        assertSame( dm, om.getDocumentManager() );
        }
    
    public void testUseSpecifiedReasoner()
        {
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel( "x rdf:type ja:OntModelSpec; x ja:reasonerFactory R" );
        ReasonerFactory rf = new FakeReasonerFactory();
        NamedObjectAssembler mock = new NamedObjectAssembler( resource( "R" ), rf );
        OntModelSpec om = (OntModelSpec) a.open( mock, root );
        assertSame( rf, om.getReasonerFactory() );
        }
    
    public void testUseSpecifiedImpliedReasoner()
        {
        testUsedSpecifiedImpliedReasoner( OWLFBRuleReasonerFactory.URI );
        testUsedSpecifiedImpliedReasoner( RDFSRuleReasonerFactory.URI );
        }

    private void testUsedSpecifiedImpliedReasoner( String R )
        {
        ReasonerFactory rf = ReasonerFactoryAssembler.getReasonerFactoryByURL( resource( "x" ), resource( R ) );
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel( "x rdf:type ja:OntModelSpec; x ja:reasonerURL " + R );
        Assembler mock = new FixedObjectAssembler( "(should not be this string)" );
        OntModelSpec om = (OntModelSpec) a.open( mock, root );
        assertSame( rf, om.getReasonerFactory() );
        }
    
    public void testDetectsClashingImpliedAndExplicitReasoners()
        {
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel( "x rdf:type ja:OntModelSpec; x ja:reasonerURL R; x ja:reasonerFactory F" );
        Assembler mock = new FixedObjectAssembler( "(should not be this string)" );
        try { a.open( mock, root ); fail( "should detect reasoner clash" ); }
        catch (ReasonerClashException e) { pass(); }
        }
    
    public void testUseSpecifiedLanguage()
        {
        testSpecifiedLanguage( ProfileRegistry.DAML_LANG );
        testSpecifiedLanguage( ProfileRegistry.OWL_DL_LANG );
        testSpecifiedLanguage( ProfileRegistry.OWL_LANG );
        testSpecifiedLanguage( ProfileRegistry.OWL_LITE_LANG );
        testSpecifiedLanguage( ProfileRegistry.RDFS_LANG );
        }

    private void testSpecifiedLanguage( String lang )
        {
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel( "x rdf:type ja:OntModelSpec; x ja:ontLanguage " + lang );
        OntModelSpec om = (OntModelSpec) a.open( root );
        assertEquals( lang, om.getLanguage() );
        }
    
    public void testSpecifiedModelGetter()
        {
        Assembler a = new OntModelSpecAssembler();
        ModelGetter getter = new ModelGetter() 
            { 
            @Override
            public Model getModel( String URL ) { return null; }

            @Override
            public Model getModel( String URL, ModelReader loadIfAbsent )
                { throw new CannotCreateException( URL ); }
            };
        NamedObjectAssembler mock = new NamedObjectAssembler( resource( "source" ), getter );
        Resource root = resourceInModel( "x rdf:type ja:OntModelSpec; x ja:importSource source" );
        OntModelSpec om = (OntModelSpec) a.open( mock, root );
        assertSame( getter, om.getImportModelGetter() );
        }

    private static final class FakeReasonerFactory implements ReasonerFactory
        {
        @Override
        public Reasoner create( Resource configuration )
            { return null; }

        @Override
        public Model getCapabilities()
            { return null; }

        @Override
        public String getURI()
            { return null; }
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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