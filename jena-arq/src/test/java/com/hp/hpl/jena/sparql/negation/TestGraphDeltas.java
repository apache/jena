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

package com.hp.hpl.jena.sparql.negation;

import java.io.StringReader ;
import java.util.Arrays ;
import java.util.Collection ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.AfterClass ;
import org.junit.Assert ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

/**
 * Tests for calculating graph deltas using SPARQL
 * 
 */
@RunWith(Parameterized.class)
public class TestGraphDeltas {

    private static final String testData = StrUtils.strjoinNL("<http://r1> <http://r1> <http://r1> .",
            "<http://r2> <http://r2> <http://r2> .");

    private static final String testData2 = StrUtils.strjoinNL("<http://r1> <http://r1> <http://r1> , <http://r2> .",
            "<http://r2> <http://r2> <http://r2> .");

    private static final String testData3 = StrUtils.strjoinNL("<http://r1> <http://r1> <http://r1> , \"value\" .",
            "<http://r2> <http://r2> <http://r2> , 1234 .");

    private static final String testData4 = StrUtils.strjoinNL(
            "<http://r1> <http://r1> <http://r1> , \"value\" , 1234, 123e4, 123.4, true, false .",
            "<http://r2> <http://r2> <http://r2> .");

    private static final String MinusQuery = StrUtils.strjoinNL
        ("SELECT *",
         "{",
         "  GRAPH <http://a>",
         "  {",
         "     ?s ?p ?o .",
         "  }",
         "  MINUS",
         "  {",
         "    GRAPH <http://b> { ?s ?p ?o }",
         "  }",
         "}");
    
    private static final String OptionalSameTermQuery1 = StrUtils.strjoinNL
        ("SELECT *",
         "{",
         "  GRAPH <http://a>",
         "  {",
         "     ?s ?p ?o .",
         "  }",
         "  OPTIONAL",
         "  {",
         "     GRAPH <http://b> { ?s0 ?p0 ?o0 . }",
         "     FILTER (SAMETERM(?s, ?s0) && SAMETERM(?p, ?p0) && SAMETERM(?o, ?o0))",
         "  }",
         "  FILTER(!BOUND(?s0))",
         "}");

    private static final String OptionalSameTermQuery2 = StrUtils.strjoinNL
        ("SELECT *",
         "{",
         "  GRAPH <http://a>",
         "  {",
         "    ?s ?p ?o .",
         "  }",
         "  OPTIONAL",
         "  {",
         "    GRAPH <http://b> { ?s ?p ?o0 . }",
         "    FILTER (SAMETERM(?o, ?o0))",
         "  }",
         "  FILTER(!BOUND(?o0))",
         "}");

    private static final String NotExistsQuery = StrUtils.strjoinNL
        ("SELECT *",
         "{",
         "  GRAPH <http://a>",
         "  {",
         "     ?s ?p ?o .",
         "  }",
         "  FILTER NOT EXISTS { GRAPH <http://b> { ?s ?p ?o } }",
         "}");


    @AfterClass
    public static void afterTests() {
        ARQ.getContext().set(ARQ.optimization, true);
    }

    static boolean[] $enabled = new boolean[] { true }, $disabled = new boolean[] { false };

    /**
     * Data for parameters
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { $enabled }, { $disabled } });
    }

    /**
     * Creates new tests
     * 
     * @param optimized
     *            Whether to enable the ARQ optimizer
     */
    public TestGraphDeltas(boolean[] optimized) {
        ARQ.getContext().set(ARQ.optimization, optimized[0]);
    }

    private void testQuery(Dataset ds, String query, String queryName, int differences) {
        try(QueryExecution qe = QueryExecutionFactory.create(query, ds)) {
            ResultSetRewindable results = ResultSetFactory.makeRewindable(qe.execSelect());
            Assert.assertEquals(queryName + " gave incorrect results", differences, results.size());
        }
    }

    /**
     * Tests the delta queries which calculate deltas correctly i.e. finds
     * triples in {@code a} which are not present in {@code b}
     * 
     * @param a
     *            Model A
     * @param b
     *            Model B
     * @param differences
     *            Expected number of differences
     */
    private void testDeltas(Model a, Model b, int differences) {
        Dataset ds = DatasetFactory.createMem();
        ds.addNamedModel("http://a", a);
        ds.addNamedModel("http://b", b);

        this.testQuery(ds, MinusQuery, "Minus", differences);
        this.testQuery(ds, OptionalSameTermQuery1, "OptionalSameTerm1", differences);
        this.testQuery(ds, OptionalSameTermQuery2, "OptionalSameTerm2", differences);
        this.testQuery(ds, NotExistsQuery, "NotExists", differences);
    }

    /**
     * Tests graph deltas calculate with SPARQL
     */
    @Test
    public void sparql_graph_delta_01() {
        Model a = ModelFactory.createDefaultModel();
        Model b = ModelFactory.createDefaultModel();

        a.read(new StringReader(testData), null, "TTL");
        b.read(new StringReader(testData), null, "TTL");

        this.testDeltas(a, b, 0);
        this.testDeltas(b, a, 0);
    }

    /**
     * Tests graph deltas calculate with SPARQL
     */
    @Test
    public void sparql_graph_delta_02() {
        Model a = ModelFactory.createDefaultModel();
        Model b = ModelFactory.createDefaultModel();

        a.read(new StringReader(testData), null, "TTL");
        b.read(new StringReader(testData2), null, "TTL");

        this.testDeltas(a, b, 0);
        this.testDeltas(b, a, 1);
    }

    /**
     * Tests graph deltas calculate with SPARQL
     */
    @Test
    public void sparql_graph_delta_03() {
        Model a = ModelFactory.createDefaultModel();
        Model b = ModelFactory.createDefaultModel();

        a.read(new StringReader(testData), null, "TTL");
        b.read(new StringReader(testData2), null, "TTL");
        b.removeAll(b.createResource("http://r1"), null, null);
        Assert.assertEquals(1, b.size());

        this.testDeltas(a, b, 1);
        this.testDeltas(b, a, 0);
    }

    /**
     * Tests graph deltas calculate with SPARQL
     */
    @Test
    public void sparql_graph_delta_04() {
        Model a = ModelFactory.createDefaultModel();
        Model b = ModelFactory.createDefaultModel();

        a.read(new StringReader(testData), null, "TTL");

        this.testDeltas(a, b, 2);
        this.testDeltas(b, a, 0);
    }

    /**
     * Tests graph deltas calculate with SPARQL
     */
    @Test
    public void sparql_graph_delta_05() {
        Model a = ModelFactory.createDefaultModel();
        Model b = ModelFactory.createDefaultModel();

        a.read(new StringReader(testData3), null, "TTL");
        b.read(new StringReader(testData), null, "TTL");

        this.testDeltas(a, b, 2);
        this.testDeltas(b, a, 0);
    }

    /**
     * Tests graph deltas calculate with SPARQL
     */
    @Test
    public void sparql_graph_delta_06() {
        Model a = ModelFactory.createDefaultModel();
        Model b = ModelFactory.createDefaultModel();

        a.read(new StringReader(testData4), null, "TTL");
        b.read(new StringReader(testData), null, "TTL");

        this.testDeltas(a, b, 6);
        this.testDeltas(b, a, 0);
    }
}
