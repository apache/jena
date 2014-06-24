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

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.*;

import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

/**
    Your eyes will bleed with the number of backslashes required in the substitute
    strings.
*/
public class TestGenericRuleReasonerConfig extends AssemblerTestBase
    {
    public TestGenericRuleReasonerConfig( String name )
        { super( name ); }

    @Override
    protected Model setRequiredPrefixes( Model x )
        {
        x.setNsPrefix( "jr", ReasonerVocabulary.JenaReasonerNS );
        return super.setRequiredPrefixes( x );
        }
    
    public void testLoadsSingleRuleSetViaURL()
        { 
//        testLoadsSingleRuleViaURL( "jms" );
        testLoadsSingleRuleViaURL( "jr" );
        }

    private void testLoadsSingleRuleViaURL( String ns )
        {
        String where = "file:testing/modelspecs/example.rules";
        Resource r = resourceInModel( "x <ns>:ruleSetURL <where>".replaceAll( "<ns>", ns ).replaceAll( "<where>", where ) );
        List<Rule> rules = Rule.rulesFromURL( where );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, grr.getRules() );
        }    
    
    public void testLoadsSingleRuleFromString()
        { 
//        testLoadsSingleRuleFromString( "jms" );
        testLoadsSingleRuleFromString( "jr" );
        }

    private void testLoadsSingleRuleFromString( String ns )
        {
        String rule = "[R: (?x rdf:type eg:Thing) -> (?x eg:thing true)]";
        List<Rule> rules = Rule.parseRules( rule );
        Resource r = resourceInModel( "x <ns>:hasRule '<it>'".replaceAll( "<ns>", ns ).replaceAll( "<it>", rule.replaceAll( " ", "\\\\\\\\s" ) ) );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, grr.getRules() );
        }
    
    public void testLoadsSingleRuleViaRuleSetStringString()
        { 
//        testLoadsRulesViaRuleSetStrings( "jms" );
        testLoadsRulesViaRuleSetStrings( "jr" );
        }

    private void testLoadsRulesViaRuleSetStrings( String ns )
        {
        String ruleA = "[R: (?x rdf:type eg:Thing) -> (?x eg:thing true)]";
        String ruleB = "[S: (?x rdf:type eg:Thung) -> (?x eg:thing false)]";
        Set<Rule> rules = rulesFromTwoStrings( ruleA, ruleB );
        String modelString = "x <ns>:ruleSet _x; _x <ns>:hasRule '<A>'; _x <ns>:hasRule '<B>'"
            .replaceAll( "<ns>", ns )
            .replaceAll( "<A>", ruleA.replaceAll( " ", "\\\\\\\\s" ) )
            .replaceAll( "<B>", ruleB.replaceAll( " ", "\\\\\\\\s" ) )
            ;
        Resource r = resourceInModel( modelString );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, new HashSet<>( grr.getRules() ) );
        }
    
    public void testLoadsMultipleRuleSetsViaRuleSetNode()
        {
//        testLoadsMultipleRuleSetsViaRuleSetNode( "jms" );
        testLoadsMultipleRuleSetsViaRuleSetNode( "jr" );
        }

    private void testLoadsMultipleRuleSetsViaRuleSetNode( String ns )
        {
        String whereA = "file:testing/modelspecs/example.rules";
        String whereB = "file:testing/modelspecs/extra.rules";
        Resource r = resourceInModel( "x <ns>:ruleSet _a; _a <ns>:ruleSetURL <whereA>; _a <ns>:ruleSetURL <whereB>".replaceAll( "<ns>", ns ).replaceAll( "<whereA>", whereA ).replaceAll( "<whereB>", whereB ) );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rulesFromTwoPlaces( whereA, whereB ), new HashSet<>( grr.getRules() ) );
        }

    private Set<Rule> rulesFromTwoStrings( String ruleA, String ruleB )
        {
        Set<Rule> rules = new HashSet<>( Rule.parseRules( ruleA ) );
        rules.addAll( Rule.parseRules( ruleB ) );
        return rules;
        }

    private Set<Rule> rulesFromTwoPlaces( String whereA, String whereB )
        {
        Set<Rule> rules = new HashSet<>();
        rules.addAll( Rule.rulesFromURL( whereA ) );
        rules.addAll( Rule.rulesFromURL( whereB ) );
        return rules;
        }
    }
