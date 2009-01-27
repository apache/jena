/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestRuleSet.java,v 1.9 2009-01-27 10:01:16 chris-dollin Exp $
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
        List<Rule> rules = Arrays.asList( new Rule[] {A, B } );
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