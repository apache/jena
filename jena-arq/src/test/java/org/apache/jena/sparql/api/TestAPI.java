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

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetCloseable;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestAPI
{
    private static final String ns = "http://example/ns#" ;

    static Model m = GraphFactory.makeJenaDefaultModel() ;
    static Resource r1 = m.createResource() ;
    static Property p1 = m.createProperty(ns+"p1") ;
    static Property p2 = m.createProperty(ns+"p2") ;
    static Property p3 = m.createProperty(ns+"p3") ;
    static Model dft = GraphFactory.makeJenaDefaultModel() ;
    static Resource s = dft.createResource(ns+"s") ;
    static Property p = dft.createProperty(ns+"p") ;
    static Resource o = dft.createResource(ns+"o") ;
    static Resource g1 = dft.createResource(ns+"g1") ;
    static Dataset d = null;
    static  {
        m.add(r1, p1, "x1") ;
        m.add(r1, p2, "X2") ; // NB Capital
        m.add(r1, p3, "y1") ;
        dft.add(s, p, o) ;
        d = DatasetFactory.create(dft);
        d.addNamedModel(g1.getURI(), m);
    }

    @Test public void testInitialBindingsConstruct1()
    {
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("z", m.createLiteral("zzz"));
        String qs = "CONSTRUCT {?s ?p ?z} {?s ?p 'x1'}";
        try ( QueryExecution qExec = QueryExecution.create()
                .query(qs)
                .model(m)
                .initialBinding(init)
                .build() ) {
            Model r = qExec.execConstruct() ;
            assertTrue("Empty model", r.size() > 0 ) ;
            Property p1 = m.createProperty(ns+"p1") ;
            assertTrue("Empty model", r.contains(null,p1, init.get("z"))) ;
        }
    }

    @Test public void testInitialBindingsConstruct2()
    {
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("o", m.createLiteral("x1"));
        String qs = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
        try ( QueryExecution qExec = QueryExecution.create().query(qs).model(m)
                .initialBinding(init)
                .build() ) {
            Model r = qExec.execConstruct() ;
            assertTrue("Empty model", r.size() > 0 ) ;
            Property p1 = m.createProperty(ns+"p1") ;
            assertTrue("Empty model", r.contains(null, p1, init.get("x1"))) ;
        }
    }

    // The original test (see commented out "assertSame) in the test is now bogus.
    // DatasetImpl no longer caches the default model as that caused problems.
    //
    // This is testing that the model for the resource in the result is the
    // same object as the model supplied to the query.
    // "Same" here means "same contents" including blank nodes.
    //
    // it used to be that this tested whether they were the same object.
    // That is dubious and no longer true even for DatasetImpl (the default mode
    // is not cached but recreated on demand so there are no problems with
    // transaction boundaries).
    //
    // Left as an active test so the assumption is tested (it has been true for
    // many years).
    //
    // Using the Resource.getXXX and Resource.listXXX operations is dubious if
    // there are named graphs and that has always been the case.

    @Test public void test_API1()
    {
        String qs = "SELECT * {?s ?p ?o}";
        try ( QueryExecution qExec = QueryExecution.create().query(qs).model(m)
                .build() ) {
            ResultSet rs = qExec.execSelect() ;
            assertTrue("No results", rs.hasNext()) ;
            QuerySolution qSoln = rs.nextSolution() ;
            Resource qr = qSoln.getResource("s") ;
            //assertSame("Not the same model as queried", qr.getModel(), m) ;
            Set<Statement> s1 = qr.getModel().listStatements().toSet() ;
            Set<Statement> s2 = m.listStatements().toSet() ;
            assertEquals(s1,s2) ;
        }
    }

    @Test public void testInitialBindings0()
    {
        QuerySolutionMap smap1 = new QuerySolutionMap() ;
        QuerySolutionMap smap2 = new QuerySolutionMap() ;
        smap1.add("o", m.createLiteral("y1"));
        smap2.addAll(smap1) ;
        assertTrue(smap2.contains("o")) ;
        smap2.clear() ;
        assertFalse(smap2.contains("o")) ;
        assertTrue(smap1.contains("o")) ;

        QuerySolutionMap smap3 = new QuerySolutionMap() ;
        smap2.addAll((QuerySolution)smap1) ;
        assertTrue(smap2.contains("o")) ;
    }

    @Test public void testInitialBindings1()
    {
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("o", m.createLiteral("y1"));
        String qs = "SELECT * {?s ?p ?o}";
        try ( QueryExecution qExec = QueryExecution.create().query(qs).model(m)
                .initialBinding(init)
                .build() ) {
            int count = queryAndCount(qExec) ;
            assertEquals("Initial binding didn't restrict query properly", 1, count) ;
        }
    }

    @Test public void testInitialBindings2()
    {
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("z", m.createLiteral("zzz"));
        String qs = "SELECT * {?s ?p ?o}";
        try ( QueryExecution qExec = QueryExecution.create().query(qs).model(m)
                .initialBinding(init)
                .build() ) {
            int count = queryAndCount(qExec) ;
            assertEquals("Initial binding restricted query improperly", 3, count) ;
        }
    }

    @Test public void testInitialBindings3()
    {
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("z", m.createLiteral("zzz"));
        String qs = "SELECT * {?s ?p 'x1'}";
        try ( QueryExecution qExec = QueryExecution.create().query(qs).model(m)
                .initialBinding(init)
                .build() ) {
            ResultSet rs = qExec.execSelect() ;
            QuerySolution qSoln= rs.nextSolution() ;
            assertTrue("Initial setting not set correctly now", qSoln.getLiteral("z").getLexicalForm().equals("zzz")) ;
        }
    }

    @Test public void testInitialBindings4()
    {
        // Test derived from report by Holger Knublauch
        String queryString =
            "PREFIX : <"+ns+">\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT * \n" +
            "WHERE { \n" +
            "    ?x :p1 ?z ." +
            "    NOT EXISTS { \n" +
            "        ?x rdfs:label ?z . \n" +
            "    }\n" +
            "}";

        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QuerySolutionMap map = new QuerySolutionMap();
        map.add("this", OWL.Thing);

        try ( QueryExecution qExec = QueryExecution.create().query(queryString).model(m)
                .initialBinding(map)
                .build() ) {
            ResultSet rs = qExec.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
            }
        }
    }

    /**
     * Initial binding substitution happens before optimization so initial bindings can make a semantically always false query into one that can return true
     */
    @Test public void testInitialBindings5() {
        // From JENA-500
        Query query = QueryFactory.create(
                "ASK\n" +
                "WHERE {\n" +
                "    FILTER (?a = <http://constant>) .\n" +
                "}");
        //System.out.println(Algebra.optimize(Algebra.compile(query)).toString());

        Model model = ModelFactory.createDefaultModel();
        model.add(OWL.Thing, RDF.type, OWL.Class);
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add("a", ResourceFactory.createResource("http://constant"));
        try ( QueryExecution qExec = QueryExecution.create().query(query).model(model).initialBinding(initialBinding).build() ) {
            boolean result = qExec.execAsk();
            assertTrue(result);
        }
    }

    /**
     * Initial binding substitution happens before optimization so initial bindings can make a semantically always false query into one that can return true
     */
    @Test public void testInitialBindings6() {
        // From JENA-500
        Query query = QueryFactory.create(
                "ASK\n" +
                "WHERE {\n" +
                "    FILTER (?a = ?b) .\n" +
                "}");
        //System.out.println(Algebra.optimize(Algebra.compile(query)).toString());

        Model model = ModelFactory.createDefaultModel();
        model.add(OWL.Thing, RDF.type, OWL.Class);
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add("a", ResourceFactory.createTypedLiteral(Boolean.TRUE));
        initialBinding.add("b", ResourceFactory.createTypedLiteral(Boolean.TRUE));
        try ( QueryExecution qExec = QueryExecution.create().query(query).model(model).initialBinding(initialBinding).build() ) {
            boolean result = qExec.execAsk();
            assertTrue(result);
        }
    }

    @Test public void testInitialBindings7() {
        // JENA-1354
        Query query = QueryFactory.create("SELECT DISTINCT ?x WHERE {}");
        Dataset ds = DatasetFactory.create();
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add("a", ResourceFactory.createTypedLiteral(Boolean.TRUE));
        try ( QueryExecution qexec = QueryExecutionFactory.create(query, ds, initialBinding) ) {
            assertFalse(qexec.execSelect().next().contains("a"));
        }
    }


    @Test public void testReuseQueryObject1()
    {
        String queryString = "SELECT * {?s ?p ?o}";
        Query q = QueryFactory.create(queryString) ;

        QueryExecution qExec = QueryExecutionFactory.create(q, m) ;
        int count = queryAndCount(qExec) ;
        assertEquals(3, count) ;

        qExec = QueryExecutionFactory.create(q, m) ;
        count = queryAndCount(qExec) ;
        assertEquals(3, count) ;
    }

    @Test public void testReuseQueryObject2()
    {
        String queryString = "SELECT (count(?o) AS ?c) {?s ?p ?o} GROUP BY ?s";
        Query q = QueryFactory.create(queryString) ;

        try(QueryExecution qExec = QueryExecutionFactory.create(q, m)) {
            ResultSet rs = qExec.execSelect() ;
            QuerySolution qs = rs.nextSolution() ;
            assertEquals(3, qs.getLiteral("c").getInt()) ;
        }

        try(QueryExecution qExec = QueryExecutionFactory.create(q, m)) {
            ResultSet rs = qExec.execSelect() ;
            QuerySolution qs = rs.nextSolution() ;
            assertEquals(3, qs.getLiteral("c").getInt()) ;
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


//    // Execute a test both with and without regex optimization enabled
//    // Check the number of results
//    private void XexecRegexTest(int expected, String queryString)
//    {
//        Object b = ARQ.getContext().get(ARQ.enableRegexConstraintsOpt) ;
//        try {
//            ARQ.getContext().set(ARQ.enableRegexConstraintsOpt, "false") ;
//            int count1 = queryAndCount(queryString) ;
//            ARQ.getContext().set(ARQ.enableRegexConstraintsOpt, "true") ;
//            int count2 = queryAndCount(queryString) ;
//            assertEquals("Different number of results", count1, count2) ;
//            if ( expected >= 0 )
//                assertEquals("Unexpected number of results", expected, count1) ;
//        } finally {
//            ARQ.getContext().set(ARQ.enableRegexConstraintsOpt, b) ;
//        }
//    }

    // ARQ Construct Quad Tests:
    // Two types of query strings: a) construct triple string; b) construct quad string;
    // Two kinds of query methods: 1) execTriples(); 2) execQuads();

    // Test a)+1)
    @Test public void testARQConstructQuad_a_1() {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Triple> ts = qExec.execConstructTriples();
        Model result = ModelFactory.createDefaultModel();
        while (ts.hasNext()) {
            Triple t = ts.next();
            Statement stmt = ModelUtils.tripleToStatement(result, t);
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

        Iterator<Quad> ts = qExec.execConstructQuads();
        DatasetGraph result = DatasetGraphFactory.create();
        long count = 0;
        while (ts.hasNext()) {
            count++;
            Quad qd = ts.next();
            result.add(qd);
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

        Iterator<Quad> ts = qExec.execConstructQuads();
        DatasetGraph result = DatasetGraphFactory.create();
        long count = 0;
        while (ts.hasNext()) {
            count++;
            result.add( ts.next() );
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
        Iterator<Triple> ts = qExec.execConstructTriples();
        Model result = ModelFactory.createDefaultModel();
        while (ts.hasNext()) {
            Triple t = ts.next();
            Statement stmt = ModelUtils.tripleToStatement(result, t);
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
        Dataset ds = qExec.execConstructDataset() ;
        assertEquals(1, Iter.count(ds.asDatasetGraph().listGraphNodes())) ;
        Node n = ds.asDatasetGraph().listGraphNodes().next();
        assertTrue(n.isBlank());
        Graph g = ds.asDatasetGraph().getGraph(n) ;
        assertNotNull(g) ;
        assertFalse(g.isEmpty()) ;
   }

    // Allow duplicated quads in execConstructQuads()
    @Test public void testARQConstructQuad_Duplicate_1() {
        String queryString = "CONSTRUCT { GRAPH ?g1 {?s ?p ?o} } WHERE { ?s ?p ?o. GRAPH ?g1 {?s1 ?p1 ?o1} }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Quad> ts = qExec.execConstructQuads();
        long count = 0;
        Quad expected = Quad.create( g1.asNode(), s.asNode(), p.asNode(), o.asNode());
        while (ts.hasNext()) {
            count++;
            Quad qd = ts.next();
            assertEquals(expected, qd);
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

        Iterator<Quad> ts = qExec.execConstructQuads();
        long count = 0;
        Quad expected = Quad.create( g1.asNode(), s.asNode(), p.asNode(), o.asNode());
        while (ts.hasNext()) {
            count++;
            Quad qd = ts.next();
            assertEquals(expected, qd);
        }
        assertEquals(6, count); // 6 duplicated quads
    }

    // Allow duplicated template quads in execConstructQuads()
    @Test public void testARQConstructQuad_Prefix() {
        String queryString = "PREFIX :   <http://example/ns#> CONSTRUCT { GRAPH :g1 { ?s :p ?o} } WHERE { ?s ?p ?o }";
        Query q = QueryFactory.create(queryString, Syntax.syntaxARQ);

        QueryExecution qExec = QueryExecutionFactory.create(q, d);

        Iterator<Quad> quads = qExec.execConstructQuads();
        DatasetGraph result = DatasetGraphFactory.create();
        long count = 0;
        while (quads.hasNext()) {
            count++;
            Quad qd = quads.next();
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

    @Test(expected=IllegalArgumentException.class)
    public void testResultSetCloseableBad() {
        String queryString = "ASK { ?s ?p ?o. }";
        Query q = QueryFactory.create(queryString);
        QueryExecution qExec = QueryExecutionFactory.create(q, d);
        try (ResultSetCloseable rs = ResultSetFactory.closeableResultSet(qExec) ) {
            int x = ResultSetFormatter.consume(rs);
            assertEquals(1,x);
        }
    }

    private int queryAndCount(String queryString) {
        QueryExecution qExec = QueryExecution.create().query(queryString).model(m).build();
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
