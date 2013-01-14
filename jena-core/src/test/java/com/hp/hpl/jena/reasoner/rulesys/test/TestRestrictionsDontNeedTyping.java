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

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.*;

/**
    Test that restriction inference works even when the restriction isn't given an 
    explicit type (ie we're not caught in a forward rule -> backward rule
    layering problem).
*/
public class TestRestrictionsDontNeedTyping extends ModelTestBase
    {
    static final Property ANY = null;
    
    public TestRestrictionsDontNeedTyping( String name )
        { super( name ); }

    public void testAllValuesFromFullRules()
        { testAllValuesFrom( OntModelSpec.OWL_MEM_RULE_INF ); }

    public void testAllValuesFromMiniRules()
        { testAllValuesFrom( OntModelSpec.OWL_MEM_MINI_RULE_INF ); }

    public void testAllValuesFromMicroRules()
        {
        /* micro doesn't support this anyway */
        // testAllValuesFrom( OntModelSpec.OWL_MEM_MICRO_RULE_INF ); 
        }

    private void testAllValuesFrom( OntModelSpec owlSpec )
        {
        Model m = model( "V owl:equivalentClass _R; _R owl:onProperty P; _R owl:allValuesFrom T; X rdf:type V; X P t" );
        OntModel ont = ModelFactory.createOntologyModel( owlSpec, m );
        assertTrue( ont.contains( resource( "t" ), RDF.type, resource( "T" ) ) );
        }
    
    public void testSomeValuesFromMiniRules()
        { testSomeValuesFrom( OntModelSpec.OWL_MEM_MINI_RULE_INF ); }

    public void testSomeValuesFromMicroRules()
        { testSomeValuesFrom( OntModelSpec.OWL_MEM_MICRO_RULE_INF ); }

    public void testSomeValuesFromFullRules()
        { testSomeValuesFrom( OntModelSpec.OWL_MEM_RULE_INF ); }

    private void testSomeValuesFrom( OntModelSpec owlSpec )
        {
        Model m = model( "V owl:equivalentClass _R; _R owl:onProperty P; _R owl:someValuesFrom T; X P t; t rdf:type T" );
        OntModel ont = ModelFactory.createOntologyModel( owlSpec, m );
        assertTrue( ont.contains( resource( "X" ), RDF.type, resource( "V" ) ) );
        }
    
    public void testCardinalityFullRules()
        { testCardinality( OntModelSpec.OWL_MEM_RULE_INF ); }
    
//    public void testCardinalityMiniRules()
//        { testCardinality( OntModelSpec.OWL_MEM_MINI_RULE_INF ); }
//    
//    public void testCardinalityMicroRules()
//        { testCardinality( OntModelSpec.OWL_MEM_MICRO_RULE_INF ); }

    private void testCardinality( OntModelSpec owlSpec )
        {
        Model m = model( "V owl:equivalentClass _R; _R rdf:type owl:Restriction; _R owl:onProperty P; _R owl:cardinality 1; X rdf:type V" );
        OntModel ont = ModelFactory.createOntologyModel( owlSpec, m );
        assertEquals( 1, ont.listStatements( resource( "X" ), property( "P" ), ANY ).toList().size() );
        }
    
    Model model( String statements )
        { 
        Model result = ModelFactory.createDefaultModel();
        result.setNsPrefixes( PrefixMapping.Extended );
        return modelAdd( result, statements );
        }
    }
