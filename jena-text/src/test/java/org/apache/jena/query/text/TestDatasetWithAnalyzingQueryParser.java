/**
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

package org.apache.jena.query.text;

import java.util.Set ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl;
import org.junit.AfterClass;
import org.junit.Before ;
import org.junit.BeforeClass;
import org.junit.Test ;

/**
 * This class defines a setup configuration for a dataset that uses an ASCII folding lowercase keyword analyzer with a Lucene index.
 */
public class TestDatasetWithAnalyzingQueryParser extends TestDatasetWithConfigurableAnalyzer {

    private static String loggerLevel;

    /** Suppress a warning message - see {@link TextIndexLucene#parseQuery} */
    @BeforeClass public static void beforeClass() {
        loggerLevel = LogCtl.getLevel(TextIndexLucene.class);
        LogCtl.setLevel(TextIndexLucene.class, "ERROR");
    }
    @AfterClass public static void afterClass() {
        LogCtl.setLevel(TextIndexLucene.class, loggerLevel);
    }

    @Override
    @Before
    public void before() {
        init(StrUtils.strjoinNL(
            "text:ConfigurableAnalyzer ;",
            "text:tokenizer text:KeywordTokenizer ;",
            "text:filters (text:ASCIIFoldingFilter text:LowerCaseFilter)"
        ), "text:AnalyzingQueryParser");
    }

    @Test
    public void testAnalyzingQueryParserAnalyzesWildcards() {
        final String testName = "testAnalyzingQueryParserAnalyzesWildcards";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + testName + ">",
                "  rdfs:label 'éducation'@fr",
                ".",
                "<" + RESOURCE_BASE + "irrelevant>",
                "  rdfs:label 'déjà vu'@fr",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( rdfs:label 'édu*' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = Set.of(RESOURCE_BASE + testName);
        doTestSearch(turtle, queryString, expectedURIs);
    }
}
