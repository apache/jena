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

package org.apache.jena.riot.lang.rdfxml.converted_legacy;

import static org.apache.jena.riot.lang.rdfxml.converted_legacy.LibTestARP1.negativeTest;
import static org.apache.jena.riot.lang.rdfxml.converted_legacy.LibTestARP1.positiveTest;
import static org.apache.jena.riot.lang.rdfxml.converted_legacy.LibTestARP1.warningTest;

import org.junit.Test;

/**
 * The ARP test suite run on a local legacy copy of the RDF 1.0 WG test suite
 * (updated for RDF 1.1).
 */
public class TestAPR1_W3C_WG {
    @Test
    public void test_amp_in_url_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.nt");
    }

    @Test
    public void test_datatypes_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.nt");
    }

    @Test
    public void test_datatypes_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.nt");
    }

    @Test
    public void test_error_rdf_containers_syntax_vs_schema_001() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error001.rdf");
    }

    @Test
    public void test_error_rdf_containers_syntax_vs_schema_002() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error002.rdf");
    }

    @Test
    public void test_error_rdfms_abouteach_001() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error001.rdf");
    }

    @Test
    public void test_error_rdfms_abouteach_002() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error002.rdf");
    }

    @Test
    public void test_error_rdfms_difference_between_ID_and_about_1() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#error1",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/error1.rdf");
    }

    @Test
    public void test_error_rdfms_empty_property_elements_001() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error001.rdf");
    }

    @Test
    public void test_error_rdfms_empty_property_elements_002() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error002.rdf");
    }

    @Test
    public void test_error_rdfms_empty_property_elements_003() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error003.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_id_001() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error001.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_id_002() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error002.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_id_003() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error003.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_id_004() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error004.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_id_005() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error005.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_id_006() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error006.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_id_007() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error007",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error007.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__001() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-001.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__002() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-002.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__003() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-003.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__004() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-004.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__005() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-005.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__006() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-006.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__007() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-007",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-007.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__008() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-008",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-008.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__009() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-009",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-009.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__010() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-010",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-010.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__011() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-011",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-011.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__012() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-012",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-012.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__013() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-013",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-013.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__014() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-014",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-014.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__015() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-015",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-015.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__016() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-016",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-016.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__017() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-017",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-017.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__018() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-018",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-018.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__019() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-019",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-019.rdf");
    }

    @Test
    public void test_error_rdfms_rdf_names_use__020() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-020",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-020.rdf");
    }

    @Test
    public void test_error_rdfms_syntax_incomplete_001() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error001.rdf");
    }

    @Test
    public void test_error_rdfms_syntax_incomplete_002() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error002.rdf");
    }

    @Test
    public void test_error_rdfms_syntax_incomplete_003() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error003.rdf");
    }

    @Test
    public void test_error_rdfms_syntax_incomplete_004() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error004.rdf");
    }

    @Test
    public void test_error_rdfms_syntax_incomplete_005() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error005.rdf");
    }

    @Test
    public void test_error_rdfms_syntax_incomplete_006() {
        negativeTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error006.rdf");
    }

    @Test
    public void test_rdf_charmod_literals_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.nt");
    }

    @Test
    public void test_rdf_charmod_uris_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.nt");
    }

    @Test
    public void test_rdf_charmod_uris_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.nt");
    }

    @Test
    public void test_rdf_containers_syntax_vs_schema_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.nt");
    }

    @Test
    public void test_rdf_containers_syntax_vs_schema_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.nt");
    }

    @Test
    public void test_rdf_containers_syntax_vs_schema_003() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.nt");
    }

    @Test
    public void test_rdf_containers_syntax_vs_schema_004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.nt");
    }

    @Test
    public void test_rdf_containers_syntax_vs_schema_006() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.nt");
    }

    @Test
    public void test_rdf_containers_syntax_vs_schema_007() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test007",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.nt");
    }

    @Test
    public void test_rdf_containers_syntax_vs_schema_008() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test008",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.nt");
    }

    @Test
    public void test_rdf_element_not_mandatory_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0003() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0005() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0006() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0009() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0009",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0010() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0010",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0011() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0011",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0012() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0012",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0013() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0013",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.nt");
    }

    @Test
    public void test_rdf_ns_prefix_confusion_0014() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0014",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.nt");
    }

    @Test
    public void test_rdfms_difference_between_ID_and_about_1() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test1",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.nt");
    }

    @Test
    public void test_rdfms_difference_between_ID_and_about_2() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test2",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.nt");
    }

    @Test
    public void test_rdfms_duplicate_member_props_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_003() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_005() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_006() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_007() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test007",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_008() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test008",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_009() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test009",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_010() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test010",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_011() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test011",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_012() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test012",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_013() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test013",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_014() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test014",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_015() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test015",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.nt");
    }

    @Test
    public void test_rdfms_identity_anon_resources_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.nt");
    }

    @Test
    public void test_rdfms_identity_anon_resources_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.nt");
    }

    @Test
    public void test_rdfms_identity_anon_resources_003() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.nt");
    }

    @Test
    public void test_rdfms_identity_anon_resources_004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.nt");
    }

    @Test
    public void test_rdfms_identity_anon_resources_005() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.nt");
    }

    @Test
    public void test_rdfms_not_id_and_resource_attr_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.nt");
    }

    @Test
    public void test_rdfms_not_id_and_resource_attr_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.nt");
    }

    @Test
    public void test_rdfms_not_id_and_resource_attr_004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.nt");
    }

    @Test
    public void test_rdfms_not_id_and_resource_attr_005() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.nt");
    }

    @Test
    public void test_rdfms_para196_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__003() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__005() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__006() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__007() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-007",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__008() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-008",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__009() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-009",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__010() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-010",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__011() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-011",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__012() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-012",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__013() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-013",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__014() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-014",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__015() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-015",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__016() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-016",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__017() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-017",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__018() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-018",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__019() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-019",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__020() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-020",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__021() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-021",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__022() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-022",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__023() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-023",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__024() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-024",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__025() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-025",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__026() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-026",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__027() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-027",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__028() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-028",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__029() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-029",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__030() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-030",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__031() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-031",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__032() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-032",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__033() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-033",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__034() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-034",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__035() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-035",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__036() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-036",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.nt");
    }

    @Test
    public void test_rdfms_rdf_names_use__037() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-037",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.nt");
    }

    @Test
    public void test_rdfms_reification_required_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.nt");
    }

    @Test
    public void test_rdfms_seq_representation_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.nt");
    }

    @Test
    public void test_rdfms_syntax_incomplete_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.nt");
    }

    @Test
    public void test_rdfms_syntax_incomplete_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.nt");
    }

    @Test
    public void test_rdfms_syntax_incomplete_003() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.nt");
    }

    @Test
    public void test_rdfms_syntax_incomplete_004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.nt");
    }

    @Test
    public void test_rdfms_uri_substructure_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.nt");
    }

    @Test
    public void test_rdfms_xml_literal_namespaces_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.nt");
    }

    @Test
    public void test_rdfms_xml_literal_namespaces_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.nt");
    }

    @Test
    public void test_rdfms_xmllang_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.nt");
    }

    @Test
    public void test_rdfms_xmllang_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.nt");
    }

    @Test
    public void test_rdfms_xmllang_003() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.nt");
    }

    @Test
    public void test_rdfms_xmllang_004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.nt");
    }

    @Test
    public void test_rdfms_xmllang_005() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test005",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.nt");
    }

    @Test
    public void test_rdfms_xmllang_006() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.nt");
    }

    @Test
    public void test_rdfs_domain_and_range_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.nt");
    }

    @Test
    public void test_rdfs_domain_and_range_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.nt");
    }

    @Test
    public void test_unrecognised_xml_attributes_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.nt");
    }

    @Test
    public void test_unrecognised_xml_attributes_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.nt");
    }

    @Test
    public void test_warn_rdfms_rdf_names_use__001() {
        warningTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-001",
                    "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.rdf",
                    "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.nt");
    }

    @Test
    public void test_warn_rdfms_rdf_names_use__002() {
        warningTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-002",
                    "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.rdf",
                    "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.nt");
    }

    @Test
    public void test_warn_rdfms_rdf_names_use__003() {
        warningTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-003",
                    "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.rdf",
                    "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.nt");
    }

    @Test
    public void test_xml_canon_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.nt");
    }

    @Test
    public void test_xmlbase_001() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test001",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.nt");
    }

    @Test
    public void test_xmlbase_002() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test002",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.nt");
    }

    @Test
    public void test_xmlbase_003() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test003",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.nt");
    }

    @Test
    public void test_xmlbase_004() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test004",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.nt");
    }

    @Test
    public void test_xmlbase_006() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test006",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.nt");
    }

    @Test
    public void test_xmlbase_007() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test007",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.nt");
    }

    @Test
    public void test_xmlbase_008() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test008",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.nt");
    }

    @Test
    public void test_xmlbase_009() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test009",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.nt");
    }

    @Test
    public void test_xmlbase_010() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test010",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.nt");
    }

    @Test
    public void test_xmlbase_011() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test011",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.nt");
    }

    @Test
    public void test_xmlbase_013() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test013",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.nt");
    }

    @Test
    public void test_xmlbase_014() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test014",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.nt");
    }
}