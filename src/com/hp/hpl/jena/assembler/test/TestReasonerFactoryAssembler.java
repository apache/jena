/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestReasonerFactoryAssembler.java,v 1.1 2006-01-05 13:40:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.dig.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.*;

public class TestReasonerFactoryAssembler extends AssemblerTestBase
    {
    private final Assembler ASSEMBLER = new ReasonerFactoryAssembler();

    public TestReasonerFactoryAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return ReasonerFactoryAssembler.class; }

    public void testReasonerFactoryAssemblerType()
        { testDemandsMinimalType( new ReasonerFactoryAssembler(), JA.ReasonerFactory );  }

    public void testCreateReasonerFactory()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory" );
        assertInstanceOf( GenericRuleReasonerFactory.class, ASSEMBLER.create( root ) );
        }
    
    public void testStandardReasonerURLs()
        {
        testReasonerURL( GenericRuleReasonerFactory.class, GenericRuleReasonerFactory.URI );
        testReasonerURL( TransitiveReasonerFactory.class, TransitiveReasonerFactory.URI );
        testReasonerURL( RDFSRuleReasonerFactory.class, RDFSRuleReasonerFactory.URI );
        testReasonerURL( OWLFBRuleReasonerFactory.class, OWLFBRuleReasonerFactory.URI );
        testReasonerURL( DAMLMicroReasonerFactory.class, DAMLMicroReasonerFactory.URI );
        testReasonerURL( DIGReasonerFactory.class, DIGReasonerFactory.URI );
        testReasonerURL( OWLMicroReasonerFactory.class, OWLMicroReasonerFactory.URI );
        testReasonerURL( OWLMiniReasonerFactory.class, OWLMiniReasonerFactory.URI );
        }
    
    public void testBadReasonerURLFails()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:reasonerURL bad:URL" );
        try 
            { ASSEMBLER.create( root ); 
            fail( "should detected unknown reasoner" ); }
        catch (UnknownReasonerException e) 
            { assertEquals( resource( "bad:URL" ), e.getURL() ); }
        }

    public void testMultipleURLsFails()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:reasonerURL bad:URL; x ja:reasonerURL another:bad/URL" );
        try 
            { ASSEMBLER.create( root ); 
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
            { ASSEMBLER.create( new FixedObjectAssembler( rules ), root );
            fail( "only GenericRuleReasoners can have attached rules" ); }
        catch (CannotHaveRulesException e )
            {
            }
        }
    
    public void testSchema()
        {
        }
    
    public void testSingleRules()
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:rules S" );
        String ruleStringA = "[rdfs2:  (?x ?p ?y), (?p rdfs:domain ?c) -> (?x rdf:type ?c)]";
        final RuleSet rules = RuleSet.create( ruleStringA );
        Assembler mock = new AssemblerBase() 
            {
            public Object create( Assembler a, Resource root )
                {
                assertEquals( root, resource( "S" ) );
                return rules; 
                }
            };
        ReasonerFactory r = (ReasonerFactory) ASSEMBLER.create( mock, root );
        GenericRuleReasoner grr = (GenericRuleReasoner) r.create( null );
        assertEquals( new HashSet( rules.getRules() ), new HashSet( grr.getRules() ) );
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
            public Object create( Assembler a, Resource root )
                {
                if (root.equals( resource( "S" ) )) return rulesA;
                if (root.equals( resource( "T" ) )) return rulesB;
                throw new RuntimeException( "unknown resource in mock: " + root );
                }
            };
        ReasonerFactory r = (ReasonerFactory) ASSEMBLER.create( mock, root );
        GenericRuleReasoner grr = (GenericRuleReasoner) r.create( null );
        HashSet wanted = new HashSet();
        wanted.addAll( rulesA.getRules() );
        wanted.addAll( rulesB.getRules() );
        assertEquals( wanted, new HashSet( grr.getRules() ) );
        }
    
    protected void testReasonerURL( Class wanted, String string )
        {
        Resource root = resourceInModel( "x rdf:type ja:ReasonerFactory; x ja:reasonerURL " + string );
        ReasonerFactory rf = (ReasonerFactory) ASSEMBLER.create( root );
        assertInstanceOf( wanted, rf );
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