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
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.shared.BrokenException;

public class TestRuleSet extends AssemblerTestBase
    {
    public TestRuleSet( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { throw new BrokenException( "TestAssemblers does not need this method" ); }
    
    public void testEmpty()
        {
        assertEquals( Collections.EMPTY_LIST, RuleSet.empty.getRules() );
        assertEquals( RuleSet.empty, RuleSet.create( Collections.<Rule>emptyList() ) );
        }
    
    public void testEmptyRuleSet()
        { 
        RuleSet s = RuleSet.create( Collections.<Rule>emptyList() );
        assertEquals( Collections.EMPTY_LIST, s.getRules() );
        assertNotSame( Collections.EMPTY_LIST, s.getRules() );
        }
    
    public void testSingleRuleSet()
        {
        Rule rule = Rule.parseRule( "[(?a P b) -> (?a rdf:type T)]" );
        List<Rule> list = listOfOne( rule );
        RuleSet s = RuleSet.create( list );
        assertEquals( list, s.getRules() );
        assertNotSame( list, s.getRules() );
        }
    
    public void testMultipleRuleSet()
        {
        Rule A = Rule.parseRule( "[(?a P b) -> (?a rdf:type T)]" );
        Rule B = Rule.parseRule( "[(?a Q b) -> (?a rdf:type U)]" );
        List<Rule> rules = Arrays.asList( A, B );
        RuleSet s = RuleSet.create( rules );
        assertEquals( rules, s.getRules() );
        assertNotSame( rules, s.getRules() );
        }
    
    public void testFactoryForString()
        {
        String ruleString = "[(?a P b) -> (?a rdf:type T)]";
        RuleSet s = RuleSet.create( ruleString );
        assertEquals( Rule.parseRules( ruleString ), s.getRules() );
        }
    
    public void testHashAndEquality()
        {
        String A = "[(?x breaks ?y) -> (?y brokenBy ?x)]";
        String B = "[(?a Q b) -> (?a rdf:type U)]";
        RuleSet rsA = RuleSet.create( A ), rsA2 = RuleSet.create( A );
        RuleSet rsB = RuleSet.create( B );
        assertEquals( rsA.getRules(), rsA2.getRules() );
        assertEquals( rsA, rsA2 );
        assertDiffer( rsA, rsB );
        assertEquals( rsA.hashCode(), rsA2.hashCode() );
        assertFalse( rsA.hashCode() == rsB.hashCode() );
        }
    }
