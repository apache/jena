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

package org.apache.jena.ontapi.assemblers;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.assemblers.ReasonerFactoryAssembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.assembler.exceptions.ReasonerClashException;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.common.OntPersonalities;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.ReasonerFactory;

import java.util.Map;
import java.util.Objects;

public class OntSpecificationAssembler extends AssemblerBase {

    /**
     * examples:
     * <pre>{@code
     * @prefix : <http://ex.com#> .
     * @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
     * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
     * <p>
     * :spec a oa:OntSpecification ;
     *      oa:specificationName "OWL1_LITE_MEM_RDFS_INF" .
     * }</pre>
     * <pre>{@code
     * @prefix : <http://ex.com#> .
     * @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
     *
     * :reasoner a oa:ReasonerFactory ;
     *      oa:reasonerURL <http://jena.hpl.hp.com/2003/RDFSExptRuleReasoner> .
     *
     * :spec a oa:OntSpecification ;
     *      oa:personalityName "RDFS_PERSONALITY" ;
     *      oa:reasonerFactory :reasoner .
     * }</pre>
     * <pre>{@code
     * @prefix : <http://ex.com#> .
     * @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
     * @prefix owl: <http://www.w3.org/2002/07/owl#> .
     * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
     * @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
     *
     * :content a ja:Content ;
     *      ja:literalContent """
     *          @prefix : <http://ex.com#> .
     *          @prefix owl: <http://www.w3.org/2002/07/owl#> .
     *          @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
     *          :Person a owl:Class .
     *          :Employee a owl:Class ; rdfs:subClassOf :Person .
     *      """ ;
     *      ja:contentEncoding "TTL" .
     *
     * :schema a ja:DefaultModel ;
     *      ja:content :content .
     *
     * :reasoner a oa:ReasonerFactory ;
     *      oa:reasonerURL <http://jena.hpl.hp.com/2003/OWLFBRuleReasoner> ;
     *      oa:schema :schema .
     *
     * :spec a oa:OntSpecification ;
     *      oa:reasonerFactory :reasoner .
     * }</pre>
     */
    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        checkType(root, OA.OntSpecification);

        var specificationName = getLiteralString(root, OA.specificationName);
        var personalityName = getLiteralString(root, OA.personalityName);
        if (specificationName != null) {
            if (personalityName != null) {
                throw new AssemblerException(root,
                        "Both personality and specification are specified. There should be only one."
                );
            }
            var s = Components.BUILTIN_SPECIFICATIONS.get(specificationName);
            if (s == null) {
                throw new AssemblerException(root, "Unknown specification: '" + specificationName + "'. " +
                        "Must be one of the following:\n" + String.join("\n", Components.BUILTIN_SPECIFICATIONS.keySet())
                );
            }
            return s;
        }

        OntPersonality p;
        if (personalityName != null) {
            p = Components.BUILTIN_PERSONALITIES.get(personalityName);
            if (p == null) {
                throw new AssemblerException(root, "Unknown personality: '" + personalityName + "'. " +
                        "Must be one of the following:\n" + String.join("\n", Components.BUILTIN_PERSONALITIES.keySet())
                );
            }
        } else {
            p = Objects.requireNonNull(Components.BUILTIN_PERSONALITIES.get("OWL2_DL_PERSONALITY"));
        }

        var r = getReasonerFactory(a, root);
        return new OntSpecification(p, r);
    }

    private ReasonerFactory getReasonerFactory(Assembler a, Resource root) {
        Resource rf = getUniqueResource(root, OA.reasonerFactory);
        Resource ru = getUniqueResource(root, OA.reasonerURL);
        if (ru != null && rf != null) throw new ReasonerClashException(root);
        if (ru != null) return ReasonerFactoryAssembler.getReasonerFactoryByURL(root, ru);
        return rf == null ? null : (ReasonerFactory) a.open(rf);
    }

    private static String getLiteralString(Resource root, Property p) {
        Statement s = root.getProperty(p);
        if (s == null) {
            return null;
        }
        if (!s.getObject().isLiteral()) {
            throw new AssemblerException(root, "Invalid string property: " + p);
        }
        return s.getString().trim();
    }

    private static class Components {
        private static final Map<String, OntPersonality> BUILTIN_PERSONALITIES = Map.of(
                "OWL2_EL_PERSONALITY", OntPersonalities.OWL2_EL_PERSONALITY,
                "OWL2_RL_PERSONALITY", OntPersonalities.OWL2_RL_PERSONALITY,
                "OWL1_LITE_PERSONALITY", OntPersonalities.OWL1_LITE_PERSONALITY,
                "OWL2_DL_PERSONALITY", OntPersonalities.OWL2_DL_PERSONALITY,
                "OWL1_DL_PERSONALITY", OntPersonalities.OWL1_DL_PERSONALITY,
                "RDFS_PERSONALITY", OntPersonalities.RDFS_PERSONALITY,
                "OWL1_FULL_PERSONALITY", OntPersonalities.OWL1_FULL_PERSONALITY,
                "OWL2_QL_PERSONALITY", OntPersonalities.OWL2_QL_PERSONALITY,
                "OWL2_FULL_PERSONALITY", OntPersonalities.OWL2_FULL_PERSONALITY
        );
        private static final Map<String, OntSpecification> BUILTIN_SPECIFICATIONS = Map.ofEntries(
                Map.entry("OWL1_FULL_MEM", OntSpecification.OWL1_FULL_MEM),
                Map.entry("OWL2_RL_MEM_RDFS_INF", OntSpecification.OWL2_RL_MEM_RDFS_INF),
                Map.entry("OWL1_DL_MEM_RULES_INF", OntSpecification.OWL1_DL_MEM_RULES_INF),
                Map.entry("OWL1_LITE_MEM_TRANS_INF", OntSpecification.OWL1_LITE_MEM_TRANS_INF),
                Map.entry("OWL1_FULL_MEM_RDFS_INF", OntSpecification.OWL1_FULL_MEM_RDFS_INF),
                Map.entry("OWL1_FULL_MEM_MINI_RULES_INF", OntSpecification.OWL1_FULL_MEM_MINI_RULES_INF),
                Map.entry("OWL2_RL_MEM", OntSpecification.OWL2_RL_MEM),
                Map.entry("RDFS_MEM", OntSpecification.RDFS_MEM),
                Map.entry("OWL2_RL_MEM_RULES_INF", OntSpecification.OWL2_RL_MEM_RULES_INF),
                Map.entry("OWL2_DL_MEM_RDFS_INF", OntSpecification.OWL2_DL_MEM_RDFS_INF),
                Map.entry("OWL1_FULL_MEM_RULES_INF", OntSpecification.OWL1_FULL_MEM_RULES_INF),
                Map.entry("OWL2_FULL_MEM_RULES_INF", OntSpecification.OWL2_FULL_MEM_RULES_INF),
                Map.entry("OWL2_FULL_MEM", OntSpecification.OWL2_FULL_MEM),
                Map.entry("OWL2_FULL_MEM_MICRO_RULES_INF", OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF),
                Map.entry("OWL2_FULL_MEM_MINI_RULES_INF", OntSpecification.OWL2_FULL_MEM_MINI_RULES_INF),
                Map.entry("OWL2_QL_MEM_RDFS_INF", OntSpecification.OWL2_QL_MEM_RDFS_INF),
                Map.entry("OWL1_FULL_MEM_TRANS_INF", OntSpecification.OWL1_FULL_MEM_TRANS_INF),
                Map.entry("OWL1_FULL_MEM_MICRO_RULES_INF", OntSpecification.OWL1_FULL_MEM_MICRO_RULES_INF),
                Map.entry("OWL1_DL_MEM_TRANS_INF", OntSpecification.OWL1_DL_MEM_TRANS_INF),
                Map.entry("OWL2_FULL_MEM_TRANS_INF", OntSpecification.OWL2_FULL_MEM_TRANS_INF),
                Map.entry("OWL1_LITE_MEM", OntSpecification.OWL1_LITE_MEM),
                Map.entry("OWL2_EL_MEM_RULES_INF", OntSpecification.OWL2_EL_MEM_RULES_INF),
                Map.entry("OWL1_DL_MEM_RDFS_INF", OntSpecification.OWL1_DL_MEM_RDFS_INF),
                Map.entry("RDFS_MEM_TRANS_INF", OntSpecification.RDFS_MEM_TRANS_INF),
                Map.entry("OWL2_DL_MEM", OntSpecification.OWL2_DL_MEM),
                Map.entry("OWL2_EL_MEM_RDFS_INF", OntSpecification.OWL2_EL_MEM_RDFS_INF),
                Map.entry("OWL2_DL_MEM_TRANS_INF", OntSpecification.OWL2_DL_MEM_TRANS_INF),
                Map.entry("OWL2_FULL_MEM_RDFS_INF", OntSpecification.OWL2_FULL_MEM_RDFS_INF),
                Map.entry("OWL2_EL_MEM", OntSpecification.OWL2_EL_MEM),
                Map.entry("OWL2_EL_MEM_TRANS_INF", OntSpecification.OWL2_EL_MEM_TRANS_INF),
                Map.entry("OWL2_DL_MEM_RULES_INF", OntSpecification.OWL2_DL_MEM_RULES_INF),
                Map.entry("OWL2_QL_MEM_TRANS_INF", OntSpecification.OWL2_QL_MEM_TRANS_INF),
                Map.entry("OWL1_LITE_MEM_RDFS_INF", OntSpecification.OWL1_LITE_MEM_RDFS_INF),
                Map.entry("OWL1_LITE_MEM_RULES_INF", OntSpecification.OWL1_LITE_MEM_RULES_INF),
                Map.entry("OWL1_DL_MEM", OntSpecification.OWL1_DL_MEM),
                Map.entry("OWL2_DL_MEM_BUILTIN_RDFS_INF", OntSpecification.OWL2_DL_MEM_BUILTIN_RDFS_INF),
                Map.entry("OWL2_QL_MEM", OntSpecification.OWL2_QL_MEM),
                Map.entry("RDFS_MEM_RDFS_INF", OntSpecification.RDFS_MEM_RDFS_INF),
                Map.entry("OWL2_RL_MEM_TRANS_INF", OntSpecification.OWL2_RL_MEM_TRANS_INF),
                Map.entry("OWL2_QL_MEM_RULES_INF", OntSpecification.OWL2_QL_MEM_RULES_INF)
        );
    }
}
