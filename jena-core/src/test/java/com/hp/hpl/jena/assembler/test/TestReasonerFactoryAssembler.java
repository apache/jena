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

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.*;

public class TestReasonerFactoryAssembler extends AssemblerTestBase
    {
    private final Assembler ASSEMBLER = new ReasonerFactoryAssembler();

    public TestReasonerFactoryAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return ReasonerFactoryAssembler.class; }

    public void testReasonerFactoryAssemblerType()
        { testDemandsMinimalType( new ReasonerFactoryAssembler(), JA.ReasonerFactory );  }

    public void testCreateReasonerFactory()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory" );
        assertInstanceOf( GenericRuleReasonerFactory.class, ASSEMBLER.open( root ) );
        }

    public void testStandardReasonerURLs()
        {
        testReasonerURL( GenericRuleReasonerFactory.class, GenericRuleReasonerFactory.URI );
        testReasonerURL( TransitiveReasonerFactory.class, TransitiveReasonerFactory.URI );
        testReasonerURL( RDFSRuleReasonerFactory.class, RDFSRuleReasonerFactory.URI );
        testReasonerURL( OWLFBRuleReasonerFactory.class, OWLFBRuleReasonerFactory.URI );
        testReasonerURL( OWLMicroReasonerFactory.class, OWLMicroReasonerFactory.URI );
        testReasonerURL( OWLMiniReasonerFactory.class, OWLMiniReasonerFactory.URI );
        }

    public void testBadReasonerURLFails()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:reasonerURL bad:URL" );
        try
            { ASSEMBLER.open( root );
            fail( "should detected unknown reasoner" ); }
        catch (UnknownReasonerException e)
            { assertEquals( resource( "bad:URL" ), e.getURL() ); }
        }

    public static class MockBase implements ReasonerFactory
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

    public static class MockFactory extends MockBase implements ReasonerFactory
        {
        public static final MockFactory instance = new MockFactory();

        public static ReasonerFactory theInstance() { return instance; }
        }

    public void testReasonerClassThrowsIfClassNotFound()
        {
        String description = "x rdf:type ja:ReasonerFactory; x ja:reasonerClass java:noSuchClass";
        Resource root = resourceInModel( description );
        try
            { ASSEMBLER.open( root ); fail( "should trap missing class noSuchClass" ); }
        catch (CannotLoadClassException e)
            { assertEquals( "noSuchClass", e.getClassName() ); }
        }

    public void testReasonerClassThrowsIfClassNotFactory()
        {
        String description = "x rdf:type ja:ReasonerFactory; x ja:reasonerClass java:java.util.ArrayList";
        Resource root = resourceInModel( description );
        try
            { ASSEMBLER.open( root ); fail( "should trap non-ReasonerFactory ArrayList" ); }
        catch (NotExpectedTypeException e)
            {
            assertEquals( root, e.getRoot() );
            assertEquals( ReasonerFactory.class, e.getExpectedType() );
            assertEquals( ArrayList.class, e.getActualType() );
            }
        }

    public void testReasonerClassUsesTheInstance()
        {
        String description = "x rdf:type ja:ReasonerFactory; x ja:reasonerClass java:";
        String MockName = MockFactory.class.getName();
        Resource root = resourceInModel( description + MockName );
        assertEquals( MockFactory.instance, ASSEMBLER.open( root ) );
        }

    public void testReasonerClassInstantiatesIfNoInstance()
        {
        String description = "x rdf:type ja:ReasonerFactory; x ja:reasonerClass java:";
        String MockName = MockBase.class.getName();
        Resource root = resourceInModel( description + MockName );
        assertInstanceOf( MockBase.class, ASSEMBLER.open( root ) );
        assertNotSame( MockFactory.instance, ASSEMBLER.open( root ) );
        }

    public void testMultipleURLsFails()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:reasonerURL bad:URL; x ja:reasonerURL another:bad/URL" );
        try
            { ASSEMBLER.open( root );
            fail( "should detected multiple reasoner URLs" ); }
        catch (NotUniqueException e)
            { assertEquals( JA.reasonerURL, e.getProperty() );
            assertEquals( root, e.getRoot() ); }
        }

    public void testOnlyGenericReasonerCanHaveRules()
        {
        String url = TransitiveReasonerFactory.URI;
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:rule '[->(a\\sP\\sb)]'; x ja:reasonerURL " + url );
        String ruleStringA = "[rdfs2:  (?x ?p ?y), (?p rdfs:domain ?c) -> (?x rdf:type ?c)]";
        final RuleSet rules = RuleSet.create( ruleStringA );
        try
            { ASSEMBLER.open( new FixedObjectAssembler( rules ), root );
            fail( "only GenericRuleReasoners can have attached rules" ); }
        catch (CannotHaveRulesException e )
            {
            }
        }

    public void testSchema()
        {
        Model schema = model( "P rdf:type owl:ObjectProperty" );
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:schema S" );
        Assembler sub = new NamedObjectAssembler( resource( "S" ), schema );
        ReasonerFactory rf = (ReasonerFactory) ASSEMBLER.open( sub, root );
        Reasoner r = rf.create( null );
        assertIsomorphic( schema.getGraph(), ((FBRuleReasoner) r).getBoundSchema() );
        }

    public void testSingleRules()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:rules S" );
        String ruleStringA = "[rdfs2:  (?x ?p ?y), (?p rdfs:domain ?c) -> (?x rdf:type ?c)]";
        final RuleSet rules = RuleSet.create( ruleStringA );
        Assembler mock = new AssemblerBase()
            {
            @Override
            public Object open( Assembler a, Resource root, Mode irrelevant )
                {
                assertEquals( root, resource( "S" ) );
                return rules;
                }
            };
        ReasonerFactory r = (ReasonerFactory) ASSEMBLER.open( mock, root );
        GenericRuleReasoner grr = (GenericRuleReasoner) r.create( null );
        assertEquals( new HashSet<>( rules.getRules() ), new HashSet<>( grr.getRules() ) );
        }

    public void testMultipleRules()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:rules S; x ja:rules T" );
        String ruleStringA = "[rdfs2:  (?x ?p ?y), (?p rdfs:domain ?c) -> (?x rdf:type ?c)]";
        String ruleStringB = "[rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]";
        final RuleSet rulesA = RuleSet.create( ruleStringA );
        final RuleSet rulesB = RuleSet.create( ruleStringB );
        Assembler mock = new AssemblerBase()
            {
            @Override
            public Object open( Assembler a, Resource root, Mode irrelevant )
                {
                if (root.equals( resource( "S" ) )) return rulesA;
                if (root.equals( resource( "T" ) )) return rulesB;
                throw new RuntimeException( "unknown resource in mock: " + root );
                }
            };
        ReasonerFactory r = (ReasonerFactory) ASSEMBLER.open( mock, root );
        GenericRuleReasoner grr = (GenericRuleReasoner) r.create( null );
        HashSet<Rule> wanted = new HashSet<>();
        wanted.addAll( rulesA.getRules() );
        wanted.addAll( rulesB.getRules() );
        assertEquals( wanted, new HashSet<>( grr.getRules() ) );
        }

    protected void testReasonerURL( Class<?> wanted, String string )
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:reasonerURL " + string );
        ReasonerFactory rf = (ReasonerFactory) ASSEMBLER.open( root );
        assertInstanceOf( wanted, rf );
        }
    }
