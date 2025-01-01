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

package org.apache.jena.query.text;

import java.util.Arrays ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Before ;
import org.junit.Test ;

/**
 * This class defines a setup configuration for a dataset that uses a keyword analyzer with a Lucene index.
 */
public class TestDatasetWithKeywordAnalyzer extends AbstractTestDatasetWithAnalyzer {
    @Override
    @Before
    public void before() {
        init("text:KeywordAnalyzer");
    }
    
    @Test
    public void testKeywordAnalyzerDoesNotSplitTokensAtSpace() {
        final String testName = "testKeywordAnalyzerDoesNotSplitTokensAtSpace";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + testName + ">",
                "  rdfs:label 'EC1V 9BE'",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( rdfs:label 'EC1V' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        doTestSearch(turtle, queryString, expectedURIs);
    }
    
    @Test
    public void testKeywordAnalyzerMatchesWholeField() {
        final String testName = "testKeywordAnalyzerMatchesWholeField";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + testName + ">",
                "  rdfs:label 'EC2V 9BE'",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( rdfs:label '\"EC2V 9BE\"' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + testName)) ;
        doTestSearch(turtle, queryString, expectedURIs);
    }
}
