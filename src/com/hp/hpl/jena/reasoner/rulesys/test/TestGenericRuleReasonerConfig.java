/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestGenericRuleReasonerConfig.java,v 1.2 2008-01-22 15:59:06 chris-dollin Exp $
*/

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.List;

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
        testLoadsSingleRuleViaRuleSetString( "jms" );
        testLoadsSingleRuleViaRuleSetString( "jr" );
        }

    private void testLoadsSingleRuleViaRuleSetString( String ns )
        {
        String rule = "[R: (?x rdf:type eg:Thing) -> (?x eg:thing true)]";
        List rules = Rule.parseRules( rule );
        Resource r = resourceInModel( "x <ns>:ruleSet _x; _x <ns>:hasRule '<it>'".replace( "<ns>", ns ).replace( "<it>", rule.replace( " ", "\\s" ) ) );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, grr.getRules() );
        }
    }

