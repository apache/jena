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

package org.apache.jena.sparql.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

public class TestAPI
{
    private static final String ns = "http://example/ns#";

    static Model m = GraphFactory.makeJenaDefaultModel();
    static Resource r1 = m.createResource();
    static Property p1 = m.createProperty(ns+"p1");
    static Property p2 = m.createProperty(ns+"p2");
    static Property p3 = m.createProperty(ns+"p3");
    static Model dft = GraphFactory.makeJenaDefaultModel();
    static Resource s = dft.createResource(ns+"s");
    static Property p = dft.createProperty(ns+"p");
    static Resource o = dft.createResource(ns+"o");
    static Resource g1 = dft.createResource(ns+"g1");
    static Dataset d = null;
    static  {
        m.add(r1, p1, "x1");
        m.add(r1, p2, "X2"); // NB Capital
        m.add(r1, p3, "y1");
        dft.add(s, p, o);
        d = DatasetFactory.create(dft);
        d.addNamedModel(g1.getURI(), m);
    }

    @Test
    public void test_API1() {
        String qs = "SELECT * {?s ?p ?o}";
        try (QueryExecution qExec = QueryExecution.model(m).query(qs).build()) {
            ResultSet rs = qExec.execSelect();
            assertTrue(rs.hasNext(), () -> "No results");
            QuerySolution qSoln = rs.nextSolution();
            Resource qr = qSoln.getResource("s");
            Set<Statement> s1 = qr.getModel().listStatements().toSet();
            Set<Statement> s2 = m.listStatements().toSet();
            assertEquals(s1, s2);
        }
    }

    @Test
    public void testSubstitutionConstruct1() {
        QuerySolutionMap init = new QuerySolutionMap();
        init.add("z", m.createLiteral("zzz"));
        String qs = "CONSTRUCT {?s ?p ?z} {?s ?p 'x1'}";
        try (QueryExecution qExec = QueryExecution.model(m).query(qs).substitution(init).build()) {
            Model r = qExec.execConstruct();
            assertTrue(r.size() > 0, () -> "Empty model");
            Property p1 = m.createProperty(ns + "p1");
            assertTrue(r.contains(null, p1, init.get("z")), () -> "Empty model");
        }
    }

    @Test
    public void testSubstitutionConstruct2() {
        QuerySolutionMap init = new QuerySolutionMap();
        init.add("o", m.createLiteral("x1"));
        String qs = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
        try (QueryExecution qExec = QueryExecution.model(m).query(qs).substitution(init).build()) {
            Model r = qExec.execConstruct();
            assertTrue(r.size() > 0, () -> "Empty model");
            Property p1 = m.createProperty(ns + "p1");
            assertTrue(r.contains(null, p1, init.get("x1")), () -> "Empty model");
        }
    }

    @Test
    public void testSubstitution0() {
        QuerySolutionMap smap1 = new QuerySolutionMap();
        QuerySolutionMap smap2 = new QuerySolutionMap();
        smap1.add("o", m.createLiteral("y1"));
        smap2.addAll(smap1);
        assertTrue(smap2.contains("o"));
        smap2.clear();
        assertFalse(smap2.contains("o"));
        assertTrue(smap1.contains("o"));

        QuerySolutionMap smap3 = new QuerySolutionMap();
        smap2.addAll((QuerySolution)smap1);
        assertTrue(smap2.contains("o"));
    }

    @Test
    public void testSubstitution1() {
        QuerySolutionMap init = new QuerySolutionMap();
        init.add("o", m.createLiteral("y1"));
        String qs = "SELECT * {?s ?p ?o}";
        try (QueryExecution qExec = QueryExecution.model(m).query(qs).substitution(init).build()) {
            int count = queryAndCount(qExec);
            assertEquals(1, count, () -> "Initial binding didn't restrict query properly");
        }
    }

    @Test
    public void testSubstitution2() {
        QuerySolutionMap init = new QuerySolutionMap();
        init.add("z", m.createLiteral("zzz"));
        String qs = "SELECT * {?s ?p ?o}";
        try (QueryExecution qExec = QueryExecution.model(m).query(qs).substitution(init).build()) {
            int count = queryAndCount(qExec);
            assertEquals(3, count, "Initial binding restricted query improperly");
        }
    }

    @Test
    public void testSubstitution3() {
        // test requires the substitutions are returned.
        QuerySolutionMap init = new QuerySolutionMap();
        init.add("z", m.createLiteral("zzz"));
        String qs = "SELECT * {?s ?p 'x1'}";
        Query q = QueryFactory.create(qs);
        Query q2 = QueryTransformOps.syntaxSubstitute(q, init.asVarNodeMap());

        try (QueryExecution qExec = QueryExecution.model(m).query(q2).build()) {
            ResultSet rs = qExec.execSelect();
            QuerySolution qSoln = rs.nextSolution();
            assertTrue(qSoln.getLiteral("z").getLexicalForm().equals("zzz"), () -> "Initial setting not set correctly now");
        }
    }

    @Test public void testSubstitution4() {
        // Test derived from report by Holger Knublauch
        String queryString = "PREFIX : <"+ns+">\n" +
                """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            SELECT *
            WHERE {
                ?x :p1 ?z .
                NOT EXISTS {
                    ?x rdfs:label ?z .
                }
            }""";

        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QuerySolutionMap map = new QuerySolutionMap();
        map.add("this", OWL.Thing);

        try ( QueryExecution qExec = QueryExecution.model(m).query(queryString)
                .substitution(map)
                .build() ) {
            ResultSet rs = qExec.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
            }
        }
    }

    @Test public void testSubstitution5() {
        // From JENA-500
        Query query = QueryFactory.create(
                "ASK\n" +
                "WHERE {\n" +
                "    FILTER (?a = <http://constant>) .\n" +
                "}");
        //System.out.println(Algebra.optimize(Algebra.compile(query)).toString());

        Model model = ModelFactory.createDefaultModel();
        model.add(OWL.Thing, RDF.type, OWL.Class);
        QuerySolutionMap substitution = new QuerySolutionMap();
        substitution.add("a", ResourceFactory.createResource("http://constant"));
        try ( QueryExecution qExec = QueryExecution.model(model).query(query).substitution(substitution).build() ) {
            boolean result = qExec.execAsk();
            assertTrue(result);
        }
    }

    @Test public void testSubstitution6() {
        // From JENA-500
        Query query = QueryFactory.create(
                "ASK\n" +
                "WHERE {\n" +
                "    FILTER (?a = ?b) .\n" +
                "}");
        //System.out.println(Algebra.optimize(Algebra.compile(query)).toString());

        Model model = ModelFactory.createDefaultModel();
        model.add(OWL.Thing, RDF.type, OWL.Class);
        QuerySolutionMap substitution = new QuerySolutionMap();
        substitution.add("a", ResourceFactory.createTypedLiteral(Boolean.TRUE));
        substitution.add("b", ResourceFactory.createTypedLiteral(Boolean.TRUE));
        try ( QueryExecution qExec = QueryExecution.model(model).query(query).substitution(substitution).build() ) {
            boolean result = qExec.execAsk();
            assertTrue(result);
        }
    }

    @Test public void testSubstitution7() {
        // JENA-1354
        Query query = QueryFactory.create("SELECT DISTINCT ?x WHERE {}");
        Dataset ds = DatasetFactory.create();
        QuerySolutionMap substitution = new QuerySolutionMap();
        substitution.add("a", ResourceFactory.createTypedLiteral(Boolean.TRUE));
        try ( QueryExecution qExec = QueryExecution
                .dataset(ds).query(query).substitution(substitution).build() ) {
            assertFalse(qExec.execSelect().next().contains("a"));
        }
    }

    @Test public void testReuseQueryObject1()
    {
        String queryString = "SELECT * {?s ?p ?o}";
        Query q = QueryFactory.create(queryString);

        QueryExecution qExec = QueryExecutionFactory.create(q, m);
        int count = queryAndCount(qExec);
        assertEquals(3, count);

        qExec = QueryExecutionFactory.create(q, m);
        count = queryAndCount(qExec);
        assertEquals(3, count);
    }

    @Test public void testReuseQueryObject2()
    {
        String queryString = "SELECT (count(?o) AS ?c) {?s ?p ?o} GROUP BY ?s";
        Query q = QueryFactory.create(queryString);

        try(QueryExecution qExec = QueryExecutionFactory.create(q, m)) {
            ResultSet rs = qExec.execSelect();
            QuerySolution qs = rs.nextSolution();
            assertEquals(3, qs.getLiteral("c").getInt());
        }

        try(QueryExecution qExec = QueryExecutionFactory.create(q, m)) {
            ResultSet rs = qExec.execSelect();
            QuerySolution qs = rs.nextSolution();
            assertEquals(3, qs.getLiteral("c").getInt());
        }
    }

    @Test public void testConstructRejectsBadTriples1()
    {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { ?o ?p ?s }";
        Query q = QueryFactory.create(queryString);

        QueryExecution qExec = QueryExecutionFactory.create(q, m);

        Model resultModel = qExec.execConstruct();
        assertEquals(0, resultModel.size());
    }

    @Test public void testConstructRejectsBadTriples2()
    {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { ?o ?p ?s }";
        Query q = QueryFactory.create(queryString);

        QueryExecution qExec = QueryExecutionFactory.create(q, m);

        Iterator<Triple> ts = qExec.execConstructTriples();
        long count = 0;
        while (ts.hasNext()) {
            count++;
            ts.next();
        }
        assertEquals(0, count);
    }


    // ARQ Construct Quad Tests:
    // Two types of query strings: a) construct triple string; b) construct quad string;
    // Two kinds of query methods: 1) execTriples(); 2) execQuads();

    // Test a)+1)
    @Test public void testARQConstructQuad_a_1() {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Triple> iterTriples = qExec.execConstructTriples();
        Model result = ModelFactory.createDefaultModel();
        while (iterTriples.hasNext()) {
            Triple triple = iterTriples.next();
            Statement stmt = result.asStatement(triple);
            if ( stmt != null )
                result.add(stmt);
        }
        assertEquals(3, result.size());
        assertTrue(m.isIsomorphicWith(result));
    }

    // Test b)+2)
    @Test public void testARQConstructQuad_b_2() {
        String queryString = "CONSTRUCT { GRAPH ?g1 {?s ?p ?o} } WHERE { ?s ?p ?o. GRAPH ?g1 {?s1 ?p1 'x1'} }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Quad> iterQuads = qExec.execConstructQuads();
        DatasetGraph result = DatasetGraphFactory.create();
        long count = 0;
        while (iterQuads.hasNext()) {
            count++;
            Quad quad = iterQuads.next();
            result.add(quad);
        }

        DatasetGraph expected = DatasetGraphFactory.create();
        expected.add(g1.asNode(), s.asNode(), p.asNode(), o.asNode());

        assertEquals(1, count);
        assertTrue(IsoMatcher.isomorphic( expected, result) );

    }

    // Test a)+2): Quads constructed in the default graph
    @Test public void testARQConstructQuad_a_2() {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Quad> iterQuads = qExec.execConstructQuads();
        DatasetGraph result = DatasetGraphFactory.create();
        long count = 0;
        while (iterQuads.hasNext()) {
            count++;
            result.add( iterQuads.next() );
        }
        DatasetGraph expected = DatasetGraphFactory.create();
        expected.add(Quad.defaultGraphNodeGenerated, s.asNode(), p.asNode(), o.asNode());
        assertEquals(1, count);
        assertTrue(IsoMatcher.isomorphic( expected, result) );

    }

    // Test b)+1): Projection on default graph, ignoring constructing named graphs
    @Test public void testARQConstructQuad_b_1() {
        String queryString = "CONSTRUCT { ?s ?p ?o GRAPH ?g1 { ?s1 ?p1 ?o1 } } WHERE { ?s ?p ?o. GRAPH ?g1 { ?s1 ?p1 ?o1 } }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qExec = QueryExecutionFactory.create(q, d);
        Iterator<Triple> iterTriples = qExec.execConstructTriples();
        Model result = ModelFactory.createDefaultModel();
        while (iterTriples.hasNext()) {
            Triple triple = iterTriples.next();
            Statement stmt = result.asStatement(triple);
            if ( stmt != null )
                result.add(stmt);
        }
        assertEquals(1, result.size());
        assertTrue(dft.isIsomorphicWith(result));
    }

    @Test public void testARQConstructQuad_bnodes() {
        String queryString = "PREFIX : <http://example/> CONSTRUCT { :s :p :o GRAPH _:a { :s :p :o1 } } WHERE { }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qExec = QueryExecutionFactory.create(q, d);
        Dataset ds = qExec.execConstructDataset();
        assertEquals(1, Iter.count(ds.asDatasetGraph().listGraphNodes()));
        Node n = ds.asDatasetGraph().listGraphNodes().next();
        assertTrue(n.isBlank());
        Graph g = ds.asDatasetGraph().getGraph(n);
        assertNotNull(g);
        assertFalse(g.isEmpty());
   }

    // Allow duplicated quads in execConstructQuads()
    @Test public void testARQConstructQuad_Duplicate_1() {
        String queryString = "CONSTRUCT { GRAPH ?g1 {?s ?p ?o} } WHERE { ?s ?p ?o. GRAPH ?g1 {?s1 ?p1 ?o1} }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Quad> iterQuads = qExec.execConstructQuads();
        long count = 0;
        Quad expected = Quad.create( g1.asNode(), s.asNode(), p.asNode(), o.asNode());
        while (iterQuads.hasNext()) {
            count++;
            Quad quad = iterQuads.next();
            assertEquals(expected, quad);
        }
        assertEquals(3, count); // 3 duplicated quads
    }

    // No duplicated quads in execConstructDataset()
    @Test public void testARQConstructQuad_Duplicate_2() {
        String queryString = "CONSTRUCT { GRAPH ?g1 {?s ?p ?o} } WHERE { ?s ?p ?o. GRAPH ?g1 {?s1 ?p1 ?o1} }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Dataset result = qExec.execConstructDataset();

        DatasetGraph expected = DatasetGraphFactory.create();
        expected.add(g1.asNode(), s.asNode(), p.asNode(), o.asNode());
        assertEquals(1, result.asDatasetGraph().size());
        assertTrue(IsoMatcher.isomorphic( expected, result.asDatasetGraph()) );
    }

    // Allow duplicated template quads in execConstructQuads()
    @Test public void testARQConstructQuad_Duplicate_3() {
        String queryString = "CONSTRUCT { GRAPH ?g1 {?s ?p ?o} GRAPH ?g1 {?s ?p ?o} } WHERE { ?s ?p ?o. GRAPH ?g1 {?s1 ?p1 ?o1} }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Quad> iterQuads = qExec.execConstructQuads();
        long count = 0;
        Quad expected = Quad.create( g1.asNode(), s.asNode(), p.asNode(), o.asNode());
        while (iterQuads.hasNext()) {
            count++;
            Quad quad = iterQuads.next();
            assertEquals(expected, quad);
        }
        assertEquals(6, count); // 6 duplicated quads
    }

    // Allow duplicated template quads in execConstructQuads()
    @Test public void testARQConstructQuad_Prefix() {
        String queryString = "PREFIX :   <http://example/ns#> CONSTRUCT { GRAPH :g1 { ?s :p ?o} } WHERE { ?s ?p ?o }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Quad> iterQuads = qExec.execConstructQuads();
        DatasetGraph result = DatasetGraphFactory.create();
        long count = 0;
        while (iterQuads.hasNext()) {
            count++;
            Quad qd = iterQuads.next();
            result.add(qd);
        }

        DatasetGraph expected = DatasetGraphFactory.create();
        expected.add(g1.asNode(), s.asNode(), p.asNode(), o.asNode());

        assertEquals(1, count);
        assertTrue(IsoMatcher.isomorphic( expected, result) );

    }

    // Test construct triple short form:
    @Test public void testARQConstructQuad_ShortForm_1() {
        String queryString = "CONSTRUCT WHERE {?s ?p ?o }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Model result = ModelFactory.createDefaultModel();
        qExec.execConstruct(result);

        assertEquals(1, result.size());
        assertTrue(dft.isIsomorphicWith(result));
    }

    // Test construct quad short form:
    @Test public void testARQConstructQuad_ShortForm_2() {
        String queryString = "CONSTRUCT WHERE { GRAPH ?g {?s ?p ?o} }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);
        Dataset result = qExec.execConstructDataset();

        Dataset expected = DatasetFactory.createTxnMem();
        expected.addNamedModel(g1.getURI(), m);

        assertTrue(IsoMatcher.isomorphic( expected.asDatasetGraph(), result.asDatasetGraph()) );
    }

    // Test construct triple and quad short form:
    @Test public void testARQConstructQuad_ShortForm_3() {
        String queryString = "CONSTRUCT WHERE { ?s ?p ?o. GRAPH ?g1 {?s1 ?p1 ?o1} }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);
        Dataset result = qExec.execConstructDataset();

        assertTrue(IsoMatcher.isomorphic( d.asDatasetGraph(), result.asDatasetGraph()) );
    }

    // Test bad construct quad short form:
    @Test public void testARQConstructQuad_ShortForm_bad() {
        String queryString = "CONSTRUCT WHERE { GRAPH ?g {?s ?p ?o. FILTER isIRI(?o)}  }";
        try {
            QueryFactory.create(queryString, Syntax.syntaxARQ);
        }catch (QueryParseException e){
            return;
        }
        fail("Short form of construct quad MUST be simple graph patterns!");
    }

    @Test public void testResultSetCloseableGood() {
        String queryString = "SELECT * { ?s ?p ?o. }";
        Query q = QueryFactory.create(queryString);
        QueryExecution qExec = QueryExecutionFactory.create(q, d);
        try (ResultSetCloseable rs = ResultSetFactory.closeableResultSet(qExec) ) {
            int x = ResultSetFormatter.consume(rs);
            assertEquals(1,x);
        }
    }

    @Test
    public void testResultSetCloseableBad() {
        String queryString = "ASK { ?s ?p ?o. }";
        Query q = QueryFactory.create(queryString);
        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        assertThrows(QueryException.class, ()-> {
            try (ResultSetCloseable rs = ResultSetFactory.closeableResultSet(qExec) ) {
                // No consume
                int x = ResultSetFormatter.consume(rs);
            }
        });
    }

    private int queryAndCount(String queryString) {
        QueryExecution qExec = QueryExecution.model(m).query(queryString).build();
        return queryAndCount(qExec);
    }

    private int queryAndCount(QueryExecution qExec) {
        try {
            ResultSet rs = qExec.execSelect();
            return ResultSetFormatter.consume(rs);
        }
        finally {
            qExec.close();
        }
    }

    /**
     * Test that a JSON query returns an array with the correct size, given a pre-populated model.
     */
    @Test public void testExecJson() {
        // JENA-632
        Query query = QueryFactory.create("JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } "
                + "WHERE { ?s ?p ?o }", Syntax.syntaxARQ);

        try ( QueryExecution qexec = QueryExecutionFactory.create(query, m) ) {
            JsonArray jsonArray = qexec.execJson();
            assertNotNull( jsonArray );
            assertEquals(3, jsonArray.size());
        }
    }

    /**
     * Test that a JSON query returns an array with the correct data values, given a pre-populated
     * model.
     */
    @Test public void testExecJsonItems() {
        // JENA-632
        Model model = ModelFactory.createDefaultModel();
        {
            Resource r = model.createResource(AnonId.create("first"));
            Property p = model.getProperty("");
            RDFNode node = ResourceFactory.createTypedLiteral("123", XSDDatatype.XSDdecimal);
            model.add(r, p, node);
            r = model.createResource(AnonId.create("second"));
            p = model.getProperty("");
            node = ResourceFactory.createTypedLiteral("abc", XSDDatatype.XSDstring);
            model.add(r, p, node);
            r = model.createResource(AnonId.create("third"));
            p = model.getProperty("");
            node = ResourceFactory.createLangLiteral("def", "en");
            model.add(r, p, node);
        }
        Query query = QueryFactory.create("JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } "
                + "WHERE { ?s ?p ?o }", Syntax.syntaxARQ);
        try ( QueryExecution qexec = QueryExecutionFactory.create(query, model) ) {
            Iterator<JsonObject> execJsonItems = qexec.execJsonItems();
            int size = 0;
            while(execJsonItems.hasNext()) {
                JsonObject next = execJsonItems.next();
                if (next.get("s").toString().contains("first")) {
                    assertEquals(123, next.get("o").getAsNumber().value().intValue());
                } else if (next.get("s").toString().contains("second")) {
                    assertEquals("abc", next.get("o").getAsString().value());
                } else if (next.get("s").toString().contains("third")) {
                    assertEquals("def", next.get("o").getAsString().value());
                }
                size++;
            }
            assertEquals(3, size);
        }
    }
}
