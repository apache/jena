/*
 	(c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestGenericRuleReasonerConfig.java,v 1.6 2008-12-28 19:32:00 andy_seaborne Exp $
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
    
 	@author kers
*/
public class TestGenericRuleReasonerConfig extends AssemblerTestBase
    {
    public TestGenericRuleReasonerConfig( String name )
        { super( name ); }

    protected Model setRequiredPrefixes( Model x )
        {
        x.setNsPrefix( "jr", ReasonerVocabulary.JenaReasonerNS );
        return super.setRequiredPrefixes( x );
        }
    
    public void testLoadsSingleRuleSetViaURL()
        { 
        testLoadsSingleRuleViaURL( "jms" );
        testLoadsSingleRuleViaURL( "jr" );
        }

    private void testLoadsSingleRuleViaURL( String ns )
        {
        String where = "file:testing/modelspecs/example.rules";
        Resource r = resourceInModel( "x <ns>:ruleSetURL <where>".replaceAll( "<ns>", ns ).replaceAll( "<where>", where ) );
        List rules = Rule.rulesFromURL( where );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, grr.getRules() );
        }    
    
    public void testLoadsSingleRuleFromString()
        { 
        testLoadsSingleRuleFromString( "jms" );
        testLoadsSingleRuleFromString( "jr" );
        }

    private void testLoadsSingleRuleFromString( String ns )
        {
        String rule = "[R: (?x rdf:type eg:Thing) -> (?x eg:thing true)]";
        List rules = Rule.parseRules( rule );
        Resource r = resourceInModel( "x <ns>:hasRule '<it>'".replaceAll( "<ns>", ns ).replaceAll( "<it>", rule.replaceAll( " ", "\\\\\\\\s" ) ) );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, grr.getRules() );
        }
    
    public void testLoadsSingleRuleViaRuleSetStringString()
        { 
        testLoadsRulesViaRuleSetStrings( "jms" );
        testLoadsRulesViaRuleSetStrings( "jr" );
        }

    private void testLoadsRulesViaRuleSetStrings( String ns )
        {
        String ruleA = "[R: (?x rdf:type eg:Thing) -> (?x eg:thing true)]";
        String ruleB = "[S: (?x rdf:type eg:Thung) -> (?x eg:thing false)]";
        Set rules = rulesFromTwoStrings( ruleA, ruleB );
        String modelString = "x <ns>:ruleSet _x; _x <ns>:hasRule '<A>'; _x <ns>:hasRule '<B>'"
            .replaceAll( "<ns>", ns )
            .replaceAll( "<A>", ruleA.replaceAll( " ", "\\\\\\\\s" ) )
            .replaceAll( "<B>", ruleB.replaceAll( " ", "\\\\\\\\s" ) )
            ;
        Resource r = resourceInModel( modelString );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, new HashSet( grr.getRules() ) );
        }
    
    public void testLoadsMultipleRuleSetsViaRuleSetNode()
        {
        testLoadsMultipleRuleSetsViaRuleSetNode( "jms" );
        testLoadsMultipleRuleSetsViaRuleSetNode( "jr" );
        }

    private void testLoadsMultipleRuleSetsViaRuleSetNode( String ns )
        {
        String whereA = "file:testing/modelspecs/example.rules";
        String whereB = "file:testing/modelspecs/extra.rules";
        Resource r = resourceInModel( "x <ns>:ruleSet _a; _a <ns>:ruleSetURL <whereA>; _a <ns>:ruleSetURL <whereB>".replaceAll( "<ns>", ns ).replaceAll( "<whereA>", whereA ).replaceAll( "<whereB>", whereB ) );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rulesFromTwoPlaces( whereA, whereB ), new HashSet( grr.getRules() ) );
        }

    private Set rulesFromTwoStrings( String ruleA, String ruleB )
        {
        Set rules = new HashSet( Rule.parseRules( ruleA ) );
        rules.addAll( Rule.parseRules( ruleB ) );
        return rules;
        }

    private Set rulesFromTwoPlaces( String whereA, String whereB )
        {
        Set rules = new HashSet();
        rules.addAll( Rule.rulesFromURL( whereA ) );
        rules.addAll( Rule.rulesFromURL( whereB ) );
        return rules;
        }
    }

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
