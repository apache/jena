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

package org.apache.jena.ontapi;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.ontapi.common.OntPersonalities;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OntSpecificationAssemblerTest {

    @Test
    public void testAssemblerWithBuiltinSpecification() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        
                        :spec a oa:OntSpecification ;
                            oa:specificationName "OWL1_LITE_MEM_RDFS_INF" .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#spec");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(OntSpecification.class, result);
        var spec = (OntSpecification) result;
        Assertions.assertSame(OntSpecification.OWL1_LITE_MEM_RDFS_INF, spec);
    }

    @Test
    public void testAssemblerWithWrongSpecification() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        
                        :spec a oa:OntSpecification ;
                            oa:specificationName "XXX" .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#spec");
        Assertions.assertThrows(AssemblerException.class, () -> Assembler.general().open(root));
    }

    @Test
    public void testAssemblerWithBuiltinSpecificationAndPersonality() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        
                        :spec a oa:OntSpecification ;
                            oa:specificationName "OWL1_LITE_MEM_RDFS_INF" ;
                            oa:personalityName "RDFS_PERSONALITY" ;
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#spec");
        Assertions.assertThrows(AssemblerException.class, () -> Assembler.general().open(root));
    }


    @Test
    public void testAssemblerWithBuiltinSpecificationAndReasonerURL() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        
                        :reasoner a oa:ReasonerFactory ;
                            oa:reasonerURL <http://jena.hpl.hp.com/2003/RDFSExptRuleReasoner> .
                        
                        :spec a oa:OntSpecification ;
                            oa:personalityName "RDFS_PERSONALITY" ;
                            oa:reasonerFactory :reasoner .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#spec");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(OntSpecification.class, result);
        var spec = (OntSpecification) result;

        Assertions.assertInstanceOf(RDFSRuleReasonerFactory.class, spec.getReasonerFactory());
        Assertions.assertEquals(OntPersonalities.RDFS_PERSONALITY, spec.getPersonality());
    }

    @Test
    public void testAssemblerWithReasonerURLAndRule() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        
                        :simple-rules a oa:RuleSet ;
                            oa:rule "[rdfs1: (?a rdfs:subClassOf ?b) -> (?a rdf:type owl:Class)]" .
                        
                        :reasoner a oa:ReasonerFactory ;
                            oa:reasonerURL <http://jena.hpl.hp.com/2003/GenericRuleReasoner> ;
                            oa:rules :simple-rules .
                        
                        :spec a oa:OntSpecification ;
                            oa:reasonerFactory :reasoner .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#spec");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(OntSpecification.class, result);
        var spec = (OntSpecification) result;

        Assertions.assertEquals("http://jena.hpl.hp.com/2003/GenericRuleReasoner", spec.getReasonerFactory().getURI());
        Assertions.assertEquals(OntPersonalities.OWL2_DL_PERSONALITY, spec.getPersonality());

        var reasoner = spec.getReasonerFactory().create(null);
        Assertions.assertInstanceOf(GenericRuleReasoner.class, reasoner);
        GenericRuleReasoner grr = (GenericRuleReasoner) reasoner;

        List<Rule> rules = grr.getRules();
        Assertions.assertTrue(rules.stream().anyMatch(r -> r.getName().equals("rdfs1")));
    }

    @Test
    public void testAssemblerWithReasonerURLAndSchema() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix owl: <http://www.w3.org/2002/07/owl#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
                        
                        :content a ja:Content ;
                            ja:literalContent ""\"
                                @prefix : <http://ex.com#> .
                                @prefix owl: <http://www.w3.org/2002/07/owl#> .
                                @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
                                :Person a owl:Class .
                                :Employee a owl:Class ; rdfs:subClassOf :Person .
                            ""\" ;
                            ja:contentEncoding "TTL" .
                        
                        :schema a ja:DefaultModel ;
                            ja:content :content .
                        
                        :reasoner a oa:ReasonerFactory ;
                            oa:reasonerURL <http://jena.hpl.hp.com/2003/OWLFBRuleReasoner> ;
                            oa:schema :schema .
                        
                        :spec a oa:OntSpecification ;
                            oa:reasonerFactory :reasoner .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#spec");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(OntSpecification.class, result);
        var spec = (OntSpecification) result;

        Assertions.assertEquals("http://jena.hpl.hp.com/2003/OWLFBRuleReasoner", spec.getReasonerFactory().getURI());
        Assertions.assertEquals(OntPersonalities.OWL2_DL_PERSONALITY, spec.getPersonality());
    }

    @Test
    public void testAssemblerWithReasonerClass() {
        var m = RDFIOTestUtils.readStringAsModel(
                String.format("""
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        
                        :reasoner a oa:ReasonerFactory ;
                            oa:reasonerClass "%s" .
                        
                        :spec a oa:OntSpecification ;
                            oa:reasonerFactory :reasoner .
                        """, MockFactory.class.getName()), "ttl"
        );
        var root = m.getResource("http://ex.com#spec");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(OntSpecification.class, result);
        var spec = (OntSpecification) result;

        Assertions.assertEquals("test", spec.getReasonerFactory().getURI());
        Assertions.assertEquals(OntPersonalities.OWL2_DL_PERSONALITY, spec.getPersonality());

        var reasoner = spec.getReasonerFactory().create(null);
        Assertions.assertInstanceOf(RDFSRuleReasoner.class, reasoner);
    }

    @SuppressWarnings("unused")
    public static class MockFactory implements ReasonerFactory {
        @Override
        public Reasoner create(Resource configuration) {
            return new RDFSRuleReasoner(this);
        }

        @Override
        public Model getCapabilities() {
            return null;
        }

        @Override
        public String getURI() {
            return "test";
        }
    }

}
