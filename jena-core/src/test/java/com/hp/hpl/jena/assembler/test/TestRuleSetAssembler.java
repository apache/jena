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
import com.hp.hpl.jena.assembler.assemblers.RuleSetAssembler;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class TestRuleSetAssembler extends AssemblerTestBase
    {
    public TestRuleSetAssembler( String name )
        { super( name ); }

    @Override
    protected Class<? extends Assembler> getAssemblerClass()
        { return RuleSetAssembler.class; }
    
    public void testRuleSetVocabulary()
        {
        assertSubclassOf( JA.RuleSet, JA.HasRules );
        assertDomain( JA.HasRules, JA.rule );
        assertDomain( JA.HasRules, JA.rulesFrom );
        assertDomain( JA.HasRules, JA.rules );
        assertRange( JA.RuleSet, JA.rules );
        }

    public void testRuleSetAssemblerType()
        { testDemandsMinimalType( new RuleSetAssembler(), JA.RuleSet );  }
    
    public void testEmptyRuleSet() 
        { 
        Assembler a = new RuleSetAssembler();
        Resource root = resourceInModel( "x rdf:type ja:RuleSet" );
        assertEquals( RuleSet.empty, a.open( root ) );
        }
    
    public void testSingleRuleString()
        {
        Assembler a = new RuleSetAssembler();
        String ruleString = "[(?a P ?b) -> (?a Q ?b)]";
        Resource root = resourceInModel( "x rdf:type ja:RuleSet; x ja:rule '" + ruleString.replaceAll( " ", "\\\\s" ) + "'" );
        RuleSet rules = (RuleSet) a.open( root );
        Set<Rule> expected = new HashSet<>( Rule.parseRules( ruleString ) );
        assertEquals( expected, new HashSet<>( rules.getRules() ) );
        }
    
    public void testMultipleRuleStrings()
        {
        Assembler a = new RuleSetAssembler();
        String ruleStringA = "[(?a P ?b) -> (?a Q ?b)]";
        String ruleStringB = "[(?a R ?b) -> (?a S ?b)]";
        Resource root = resourceInModel
            ( "x rdf:type ja:RuleSet"
            + "; x ja:rule '" + ruleStringA.replaceAll( " ", "\\\\s" ) + "'" 
            + "; x ja:rule '" + ruleStringB.replaceAll( " ", "\\\\s" ) + "'" 
            );
        RuleSet rules = (RuleSet) a.open( root );
        Set<Rule> expected = new HashSet<>( Rule.parseRules( ruleStringA ) );
        expected.addAll( Rule.parseRules( ruleStringB ) );
        assertEquals( expected, new HashSet<>( rules.getRules() ) );
        }
    
    public void testRulesFrom()
        {
        Assembler a = new RuleSetAssembler();
        String rulesA = file( "example.rules" );
        Resource root = resourceInModel( "x rdf:type ja:RuleSet; x ja:rulesFrom " + rulesA );
        Set<Rule> expected = new HashSet<>( Rule.rulesFromURL( rulesA ) );
        RuleSet rules = (RuleSet) a.open( root );
        assertEquals( expected, new HashSet<>( rules.getRules() ) );
        }
    
    public void testSubRules()
        {
        Assembler a = new RuleSetAssembler();
        String ruleStringA = "[(?a P ?b) -> (?a Q ?b)]";
        Resource root = resourceInModel
            ( "x rdf:type ja:RuleSet; x ja:rules y"
            + "; y rdf:type ja:RuleSet; y ja:rule '" + ruleStringA.replaceAll( " ", "\\\\s" ) + "'" );
        Set<Rule> expected = new HashSet<>( Rule.parseRules( ruleStringA ) );
        RuleSet rules = (RuleSet) a.open( root );
        assertEquals( expected, new HashSet<>( rules.getRules() ) );
        }
    
    public void testTrapsBadRulesObject()
        {
        testTrapsBadRuleObject( "ja:rules", "'y'" );
        testTrapsBadRuleObject( "ja:rulesFrom", "17" );
        testTrapsBadRuleObject( "ja:rule", "aResource" );
        testTrapsBadRuleObject( "ja:rule", "17" );
        testTrapsBadRuleObject( "ja:rule", "'something'xsd:else" );
        }

    private void testTrapsBadRuleObject( String property, String value )
        {
        Assembler a = new RuleSetAssembler();
        Resource root = resourceInModel
            ( "x rdf:type ja:RuleSet; x <property> <value>"
              .replaceAll( "<property>", property ).replaceAll( "<value>", value ) 
            );
        try 
            {
            a.open( root );
            fail( "should trap bad rules object " + value + " for property " + property );
            }
        catch (BadObjectException e) 
            { 
            Model m = e.getRoot().getModel();
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( rdfNode( m, value ), e.getObject() );
            }
        }

    protected static String file( String name )
        { return "file:testing/modelspecs/" + name; }
    }
