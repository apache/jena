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

import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.OntConfigs;
import org.apache.jena.ontapi.common.OntPersonalities;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.rulesys.OWLFBRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.OWLMicroReasonerFactory;
import org.apache.jena.reasoner.rulesys.OWLMiniReasonerFactory;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;

import java.util.Objects;

/**
 * Encapsulates a description of the components of an ontology model.
 * Contains OWL2, OWL1 and RDFS specifications:
 * <ul>
 *     <li>OWL2 (DL &amp; FULL): full support last version of OWL</li>
 *     <li>OWL2 (EL): OWL2 Existential Logic subset</li>
 *     <li>OWL2 (QL): OWL2 Query Language subset</li>
 *     <li>OWL2 (RL): OWL2 Rule Language subset</li>
 *     <li>OWL1 (DL &amp; FULL): does not support some language construct from OWL2, such as {@code OntDataRange.UnionOf};
 *     supposed to be compatible with legacy Jena's OntModel</li>
 *     <li>OWL1 LITE: does not support some language construct from OWL2 and OWL1, such as {@code OntClass.UnionOf};
 *     supposed to be compatible with old Jena's OntModel</li>
 *     <li>RDFS: supports only RDFS language: rdf:Property, rdf:Class, rdfs:subClassOf, etc</li>
 * </ul>
 *
 * @see <a href="https://www.w3.org/TR/owl2-syntax/">OWL v2</a>
 * @see <a href="https://www.w3.org/TR/owl2-profiles/#OWL_2_EL">OWL 2 EL</a>
 * @see <a href="https://www.w3.org/TR/owl2-profiles/#OWL_2_QL">OWL 2 QL</a>
 * @see <a href="https://www.w3.org/TR/owl2-profiles/#OWL_2_RL">OWL 2 RL</a>
 * @see <a href="https://www.w3.org/TR/2008/WD-owl11-syntax-20080108/">OWL v1.1</a>
 * @see <a href="https://www.w3.org/TR/owl-guide/">OWL v1</a>
 * @see <a href="https://www.w3.org/TR/2004/REC-owl-features-20040210/#s3">OWL1 Lite</a>
 */
@SuppressWarnings("ClassCanBeRecord")
public class OntSpecification {

    private static final OntPersonality OWL2_FULL_PERSONALITY =
            OntPersonalities.OWL2_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_CONFIG)
                    .build();

    private static final OntPersonality OWL2_DL_PERSONALITY =
            OntPersonalities.OWL2_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_CONFIG)
                    .build();

    private static final OntPersonality OWL2_EL_PERSONALITY =
            OntPersonalities.OWL2_EL_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_EL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_EL_CONFIG)
                    .build();

    private static final OntPersonality OWL2_QL_PERSONALITY =
            OntPersonalities.OWL2_QL_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_QL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_QL_CONFIG)
                    .build();

    private static final OntPersonality OWL2_RL_PERSONALITY =
            OntPersonalities.OWL2_RL_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_RL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_RL_CONFIG)
                    .build();

    private static final OntPersonality OWL1_DL_PERSONALITY =
            OntPersonalities.OWL1_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL1_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL1_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL1_PUNNINGS)
                    .setConfig(OntConfigs.OWL1_CONFIG)
                    .build();

    private static final OntPersonality OWL1_FULL_PERSONALITY =
            OntPersonalities.OWL1_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL1_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL1_RESERVED)
                    .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
                    .setConfig(OntConfigs.OWL1_CONFIG)
                    .build();

    private static final OntPersonality OWL1_LITE_PERSONALITY =
            OntPersonalities.OWL1_LITE_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL1_LITE_BUILTINS)
                    .setReserved(OntPersonalities.OWL1_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL1_PUNNINGS)
                    .setConfig(OntConfigs.OWL1_LITE_CONFIG)
                    .build();

    private static final OntPersonality RDFS_PERSONALITY =
            OntPersonalities.RDFS_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.RDFS_BUILTINS)
                    .setReserved(OntPersonalities.RDFS_RESERVED)
                    .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
                    .setConfig(OntConfigs.RDFS_CONFIG)
                    .build();

    /*
     * *****************************************************************************************************************
     * OWL 2 DL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL2 ontology models that are stored in memory
     * and use fast but incomplete builtin OWL inference engine for additional entailments
     * that handles hierarchy.
     * It supports {@code rdfs:subClassOf}, {@code rdfs:subPropertyOf} and class-individuals hierarchy,
     * otherwise it behaves like a regular {@link #OWL2_DL_MEM}.
     */
    public static final OntSpecification OWL2_DL_MEM_BUILTIN_RDFS_INF = new OntSpecification(
            OntPersonalities.OWL2_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_CONFIG.setTrue(OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT))
                    .build(),
            null
    );

    /**
     * A specification for OWL2 DL ontology models
     * that are stored in memory and do no additional entailment reasoning.
     */
    public static final OntSpecification OWL2_DL_MEM = new OntSpecification(
            OWL2_DL_PERSONALITY, null
    );

    /**
     * A specification for OWL2 DL ontology models
     * that are stored in memory and use the RDFS inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_DL_MEM_RDFS_INF = new OntSpecification(
            OWL2_DL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 DL ontology models
     * that are stored in memory and use the transitive inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_DL_MEM_TRANS_INF = new OntSpecification(
            OWL2_DL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 DL ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     */
    public static final OntSpecification OWL2_DL_MEM_RULES_INF = new OntSpecification(
            OWL2_DL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 2 FULL
     * *****************************************************************************************************************
     */

    /**
     * A specification for Ontology models that are stored in memory and do no additional entailment reasoning.
     * Full support for the OWL v2 specification.
     */
    public static final OntSpecification OWL2_FULL_MEM = new OntSpecification(
            OWL2_FULL_PERSONALITY, null
    );

    /**
     * A specification for OWL2 models that are stored in memory and use the RDFS inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_FULL_MEM_RDFS_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 models that are stored in memory and use the transitive inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_FULL_MEM_TRANS_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     */
    public static final OntSpecification OWL2_FULL_MEM_RULES_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 ontology models
     * that are stored in memory and use the micro OWL rules inference engine for additional entailments.
     */
    public static final OntSpecification OWL2_FULL_MEM_MICRO_RULES_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, OWLMicroReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 ontology models
     * that are stored in memory and use the mini OWL rules inference engine for additional entailments.
     */
    public static final OntSpecification OWL2_FULL_MEM_MINI_RULES_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, OWLMiniReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 2 EL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL2 EL Ontology models that are stored in memory and do no additional entailment reasoning.
     */
    public static final OntSpecification OWL2_EL_MEM = new OntSpecification(
            OWL2_EL_PERSONALITY, null
    );

    /**
     * A specification for OWL2 EL models that are stored in memory
     * and use the RDFS inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_EL_MEM_RDFS_INF = new OntSpecification(
            OWL2_EL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 EL models that are stored in memory
     * and use the transitive inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_EL_MEM_TRANS_INF = new OntSpecification(
            OWL2_EL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 EL ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     */
    public static final OntSpecification OWL2_EL_MEM_RULES_INF = new OntSpecification(
            OWL2_EL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 2 QL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL2 QL Ontology models that are stored in memory and do no additional entailment reasoning.
     */
    public static final OntSpecification OWL2_QL_MEM = new OntSpecification(
            OWL2_QL_PERSONALITY, null
    );

    /**
     * A specification for OWL2 QL models that are stored in memory
     * and use the RDFS inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_QL_MEM_RDFS_INF = new OntSpecification(
            OWL2_QL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 QL models that are stored in memory
     * and use the transitive inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_QL_MEM_TRANS_INF = new OntSpecification(
            OWL2_QL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 QL ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     */
    public static final OntSpecification OWL2_QL_MEM_RULES_INF = new OntSpecification(
            OWL2_QL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 2 RL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL2 RL Ontology models that are stored in memory and do no additional entailment reasoning.
     */
    public static final OntSpecification OWL2_RL_MEM = new OntSpecification(
            OWL2_RL_PERSONALITY, null
    );

    /**
     * A specification for OWL2 RL models that are stored in memory
     * and use the RDFS inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_RL_MEM_RDFS_INF = new OntSpecification(
            OWL2_RL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 RL models that are stored in memory
     * and use the transitive inferencer for additional entailments.
     */
    public static final OntSpecification OWL2_RL_MEM_TRANS_INF = new OntSpecification(
            OWL2_RL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 RL ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     */
    public static final OntSpecification OWL2_RL_MEM_RULES_INF = new OntSpecification(
            OWL2_RL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 1 DL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL1 DL ontology models
     * that are stored in memory and do no additional entailment reasoning.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL DL v1.1 specification.
     */
    public static final OntSpecification OWL1_DL_MEM = new OntSpecification(
            OWL1_DL_PERSONALITY, null
    );

    /**
     * A specification for OWL1 DL ontology models
     * that are stored in memory and use the RDFS inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_DL_MEM_RDFS_INF = new OntSpecification(
            OWL1_DL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 DL ontology models
     * that are stored in memory and use the transitive inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_DL_MEM_TRANS_INF = new OntSpecification(
            OWL1_DL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 DL ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_DL_MEM_RULES_INF = new OntSpecification(
            OWL1_DL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 1 FULL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and do no additional entailment reasoning.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_FULL_MEM = new OntSpecification(
            OWL1_FULL_PERSONALITY, null
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the RDFS inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_FULL_MEM_RDFS_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the transitive inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_FULL_MEM_TRANS_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_FULL_MEM_RULES_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the micro OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_FULL_MEM_MICRO_RULES_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, OWLMicroReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the mini OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     */
    public static final OntSpecification OWL1_FULL_MEM_MINI_RULES_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, OWLMiniReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 1 LITE
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL1 Lite ontology models
     * that are stored in memory and do no additional entailment reasoning.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1 Lite specification.
     */
    public static final OntSpecification OWL1_LITE_MEM = new OntSpecification(
            OWL1_LITE_PERSONALITY, null
    );

    /**
     * A specification for OWL1 Lite ontology models
     * that are stored in memory and use the RDFS inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1 Lite specification.
     */
    public static final OntSpecification OWL1_LITE_MEM_RDFS_INF = new OntSpecification(
            OWL1_LITE_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 Lite ontology models
     * that are stored in memory and use the transitive inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1 Lite specification.
     */
    public static final OntSpecification OWL1_LITE_MEM_TRANS_INF = new OntSpecification(
            OWL1_LITE_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 Lite ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1 Lite specification.
     */
    public static final OntSpecification OWL1_LITE_MEM_RULES_INF = new OntSpecification(
            OWL1_LITE_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * RDFS
     * *****************************************************************************************************************
     */

    /**
     * A specification for RDFS models that are stored in memory and do no additional entailment reasoning.
     * Supposed to be a replacement for the original legacy Jena interpretation of RDFS specification.
     */
    public static final OntSpecification RDFS_MEM = new OntSpecification(
            RDFS_PERSONALITY, null
    );

    /**
     * A specification for RDFS ontology models that are stored in memory
     * and use the RDFS inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of RDFS specification.
     */
    public static final OntSpecification RDFS_MEM_RDFS_INF = new OntSpecification(
            RDFS_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for RDFS ontology models that are stored in memory
     * and use the transitive reasoner for entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of RDFS specification.
     */
    public static final OntSpecification RDFS_MEM_TRANS_INF = new OntSpecification(
            RDFS_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );


    private final OntPersonality personality;
    private final ReasonerFactory reasonerFactory;

    public OntSpecification(OntPersonality personality, ReasonerFactory reasonerFactory) {
        this.personality = Objects.requireNonNull(personality);
        this.reasonerFactory = reasonerFactory;
    }

    public OntPersonality getPersonality() {
        return personality;
    }

    public ReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public OntConfig getConfig() {
        return personality.getConfig();
    }
}
