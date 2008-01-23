/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestGenericRuleReasonerConfig.java,v 1.4 2008-01-23 14:48:30 chris-dollin Exp $
*/

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.*;

import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

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
        Resource r = resourceInModel( "x <ns>:ruleSetURL <where>".replace( "<ns>", ns ).replace( "<where>", where ) );
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
        Resource r = resourceInModel( "x <ns>:hasRule '<it>'".replace( "<ns>", ns ).replace( "<it>", rule.replace( " ", "\\s" ) ) );
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
            .replace( "<ns>", ns )
            .replace( "<A>", ruleA.replace( " ", "\\s" ) )
            .replace( "<B>", ruleB.replace( " ", "\\s" ) )
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
        Resource r = resourceInModel( "x <ns>:ruleSet _a; _a <ns>:ruleSetURL <whereA>; _a <ns>:ruleSetURL <whereB>".replace( "<ns>", ns ).replace( "<whereA>", whereA ).replace( "<whereB>", whereB ) );
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

