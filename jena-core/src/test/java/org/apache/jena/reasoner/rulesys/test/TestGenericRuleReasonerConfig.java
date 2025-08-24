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

package org.apache.jena.reasoner.rulesys.test;

import static org.apache.jena.reasoner.rulesys.Rule.parseRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.assembler.test.AssemblerTestBase;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.ReasonerVocabulary;


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

    public void testRuleLoadingWithOverridenBuiltins() {
        List<Node> savedNode=new ArrayList<>();
        Builtin b= new BaseBuiltin() {
            @Override
            public String getName() {
                return "groo";
            }

            @Override
            public int getArgLength() {
                return 1;
            }

            @Override
            public void headAction(Node[] args, int length, RuleContext context) {
                savedNode.add(getArg(0,args,context));
            }


        };
        BuiltinRegistry r=new OverrideBuiltinRegistry(BuiltinRegistry.theRegistry);
        r.register(b);
        assertEquals(b,r.getImplementation("groo"));
        List<Rule> rules=new ArrayList<>();
        //
        // note that the head action does not appear to fire unless we put a triple in the head as well..  is
        // this expected?
        //
        rules.add(parseRule("[ (?instance rdf:type ?type) -> groo(?type) ]",r));
        GenericRuleReasoner article=new GenericRuleReasoner(rules);
        article.setMode(GenericRuleReasoner.FORWARD_RETE);
        Model input=ModelFactory.createDefaultModel();
        input.add(input.createResource(), RDF.type,input.createResource("http://example.com/Renegade"));
        InfModel output=ModelFactory.createInfModel(article,input);
        output.size(); // not optional, inferences are not run if we don't trigger them
        assertEquals(1,savedNode.size());
        assertEquals("http://example.com/Renegade",savedNode.get(0).getURI());
    }
}
