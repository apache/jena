/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestGenericRuleReasonerConfig.java,v 1.1 2008-01-22 15:15:02 chris-dollin Exp $
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
        Resource r = resourceInModel( "x <ns>:ruleSetURL <it>".replace( "<it>", where ).replace( "<ns>", ns ) );
        List rules = Rule.rulesFromURL( where );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, grr.getRules() );
        }    
    
    public void testLoadsSingleRuleFromString()
        { 
        String rule = "[R: (?x rdf:type eg:Thing) -> (?x eg:thing true)]";
        List rules = Rule.parseRules( rule );
        Resource r = resourceInModel( "x jms:hasRule '<it>'".replace( "<it>", rule.replaceAll( " ", "\\\\s" ) ) );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, grr.getRules() );
        }
    
    public void testLoadsSingleRuleFromXString()
        { 
        String rule = "[R: (?x rdf:type eg:Thing) -> (?x eg:thing true)]";
        List rules = Rule.parseRules( rule );
        Resource r = resourceInModel( "x jms:ruleSet _x; _x jms:hasRule '<it>'".replace( "<it>", rule.replaceAll( " ", "\\\\s" ) ) );
        GenericRuleReasoner grr = new GenericRuleReasoner( null, r );
        assertEquals( rules, grr.getRules() );
        }
    }

