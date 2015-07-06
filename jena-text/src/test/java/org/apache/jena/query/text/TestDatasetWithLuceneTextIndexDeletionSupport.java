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

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestDatasetWithLuceneTextIndexDeletionSupport extends AbstractTestDatasetWithLuceneTextIndexDeletionSupport {

    @Before
    public void before() {
        init();
    }

    @Test
    public void testInsertDeleteOneTriple(){
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "myResource>",
                "  rdfs:label 'My first resource'",
                "."
        );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( rdfs:label 'first' ) .",
                "}"
        );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList("http://example.org/data/resource/myResource")) ;
        doTestSearch(turtle, queryString, expectedURIs);

        String updateString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "DELETE DATA { ",
                "    <" + RESOURCE_BASE + "myResource> rdfs:label 'My first resource'",
                "}"
        );
        doUpdate(updateString);

        queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( rdfs:label 'first' ) .",
                "}"
        );
        doTestNoResult(dataset, "", queryString);
    }

    @Test
    public void testInsert2WithSameLabelAndDelete1(){
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "object1>",
                "  rdfs:label 'The same label'",
                ".",
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "object2>",
                "  rdfs:label 'The same label'",
                "."
        );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( rdfs:label 'same' ) .",
                "}"
        );
        Set<String> expectedURIs = new HashSet<String>() ;
        expectedURIs.addAll( Arrays.asList("http://example.org/data/resource/object1",
                                           "http://example.org/data/resource/object2")) ;
        doTestSearch(turtle, queryString, expectedURIs);

        String updateString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "DELETE DATA { ",
                "    <" + RESOURCE_BASE + "object1> rdfs:label 'The same label'",
                "}"
        );
        doUpdate(updateString);

        expectedURIs = new HashSet<String>() ;
        expectedURIs.addAll( Arrays.asList("http://example.org/data/resource/object2")) ;
        doTestQuery(dataset, "", queryString, expectedURIs, 1);
    }
}
