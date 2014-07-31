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

package com.hp.hpl.jena.query;

import java.util.Calendar ;
import java.util.Iterator ;
import java.util.TimeZone ;

import org.apache.jena.iri.IRIFactory ;
import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.UpdateExecutionFactory ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.XSD ;

/**
 * Tests for the {@link ParameterizedSparqlString}
 * 
 */
public class TestParameterizedSparqlString {

    private void test(ParameterizedSparqlString query, String[] expected, String[] notExpected) {
        // System.out.println("Raw Command:");
        // System.out.println(query.getCommandText());
        String command = query.toString();
        // System.out.println("Injected Command:");
        // System.out.println(command);
        for (String x : expected) {
            Assert.assertTrue(command.contains(x));
        }
        for (String x : notExpected) {
            Assert.assertFalse(command.contains(x));
        }
    }

    private Query testAsQuery(ParameterizedSparqlString query) {
        return query.asQuery();
    }

    private UpdateRequest testAsUpdate(ParameterizedSparqlString update) {
        return update.asUpdate();
    }

    @Test
    public void test_param_string_constructor_1() {
        // Test empty constructor
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        Assert.assertEquals("", query.getCommandText());
    }

    @Test
    public void test_param_string_constructor_2() {
        // Test constructor with null command - null command should map to empty
        // command automagically
        ParameterizedSparqlString query = new ParameterizedSparqlString((String) null);
        Assert.assertEquals("", query.getCommandText());
    }

    @Test
    public void test_param_string_constructor_3() {
        // Test constructor with base URI
        ParameterizedSparqlString query = new ParameterizedSparqlString("", "http://example.org");
        Assert.assertEquals("http://example.org", query.getBaseUri());
    }

    @Test
    public void test_param_string_constructor_4() {
        // Test constructor with predefined parameters
        QuerySolutionMap map = new QuerySolutionMap();
        Resource r = ResourceFactory.createResource("http://example.org");
        map.add("s", r);
        ParameterizedSparqlString query = new ParameterizedSparqlString("", map);

        Assert.assertEquals(r.asNode(), query.getParam("s"));
    }

    @Test
    public void test_param_string_constructor_5() {
        // Test constructor with predefined parameters - variant of constructor
        // that does not require command text
        QuerySolutionMap map = new QuerySolutionMap();
        Resource r = ResourceFactory.createResource("http://example.org");
        map.add("s", r);
        ParameterizedSparqlString query = new ParameterizedSparqlString(map);

        Assert.assertEquals(r.asNode(), query.getParam("s"));
    }

    @Test
    public void test_param_string_constructor_6() {
        // Test constructor with predefined parameters
        QuerySolutionMap map = new QuerySolutionMap();
        Resource r = ResourceFactory.createResource("http://example.org");
        map.add("s", r);
        Literal l = ResourceFactory.createPlainLiteral("example");
        map.add("o", l);
        ParameterizedSparqlString query = new ParameterizedSparqlString("", map);

        Assert.assertEquals(r.asNode(), query.getParam("s"));
        Assert.assertEquals(l.asNode(), query.getParam("o"));
    }

    @Test
    public void test_param_string_constructor_7() {
        // Test constructor with predefined parameters - variant of constructor
        // that does not require command text
        QuerySolutionMap map = new QuerySolutionMap();
        Resource r = ResourceFactory.createResource("http://example.org");
        map.add("s", r);
        Literal l = ResourceFactory.createPlainLiteral("example");
        map.add("o", l);
        ParameterizedSparqlString query = new ParameterizedSparqlString(map);

        Assert.assertEquals(r.asNode(), query.getParam("s"));
        Assert.assertEquals(l.asNode(), query.getParam("o"));
    }

    @Test
    public void test_param_string_constructor_8() {
        // Test constructors with predefined prefixes
        PrefixMappingImpl prefixes = new PrefixMappingImpl();
        prefixes.setNsPrefix("ex", "http://example.org");
        ParameterizedSparqlString query = new ParameterizedSparqlString("", prefixes);

        Assert.assertEquals(prefixes.getNsPrefixURI("ex"), query.getNsPrefixURI("ex"));
    }

    @Test
    public void test_param_string_constructor_9() {
        // Test constructors with predefined prefixes - variant of constructor
        // that does not require command text
        PrefixMappingImpl prefixes = new PrefixMappingImpl();
        prefixes.setNsPrefix("ex", "http://example.org");
        ParameterizedSparqlString query = new ParameterizedSparqlString(prefixes);

        Assert.assertEquals(prefixes.getNsPrefixURI("ex"), query.getNsPrefixURI("ex"));
    }

    @Test
    public void test_param_string_iri_1() {
        // Test simple injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");

        test(query, new String[] { "<http://example.org>" }, new String[] { "?s" });
    }

    @Test
    public void test_param_string_iri_2() {
        // Test simple injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("p", "http://example.org");

        test(query, new String[] { "<http://example.org>" }, new String[] { "?p" });
    }

    @Test
    public void test_param_string_iri_3() {
        // Test simple injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("o", "http://example.org");

        test(query, new String[] { "<http://example.org>" }, new String[] { "?o" });
    }

    @Test
    public void test_param_string_iri_4() {
        // Test simple injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o . ?s a ?type }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");

        test(query, new String[] { "<http://example.org>" }, new String[] { "?s" });
    }

    @Test
    public void test_param_string_iri_5() {
        // Test simple injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");

        test(query, new String[] { "<http://example.org>", "<http://predicate>" }, new String[] { "?s", "?p" });
    }

    @Test
    public void test_param_string_bnode_1() {
        // Test Blank Node injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "_:blankNodeID");

        test(query, new String[] { "<_:blankNodeID>" }, new String[] { "?s" });
    }

    @Test
    public void test_param_string_bnode_2() {
        // Test Blank Node injenction
        String cmdText = "INSERT { GRAPH <target> { ?node a:p ?o . } } WHERE { ?node a:p ?o . }";
        ParameterizedSparqlString update = new ParameterizedSparqlString(cmdText);
        update.setIri("node", "_:blankNodeID");

        test(update, new String[] { "<_:blankNodeID>" }, new String[] { "?node" });
    }

    @Test
    public void test_param_string_bnode_3() {
        // Test case related to treatment of blank nodes when injecting into
        // SPARQL updates using _: syntax

        Model model = ModelFactory.createDefaultModel();
        Resource bnode = model.createResource();
        bnode.addProperty(RDF.type, OWL.Thing);
        Assert.assertEquals(1, model.size());

        Dataset ds = DatasetFactory.create(model);

        // Use a parameterized query to check the data can be found
        ParameterizedSparqlString pq = new ParameterizedSparqlString();
        pq.setCommandText("SELECT * WHERE { ?s ?p ?o }");
        pq.setIri("s", "_:" + bnode.getId());
        Query q = pq.asQuery();
        try(QueryExecution qe = QueryExecutionFactory.create(q, ds)) {
            ResultSet rset = qe.execSelect();
            Assert.assertEquals(1, ResultSetFormatter.consume(rset));
        }

        // Use a parameterized update to modify the data
        ParameterizedSparqlString s = new ParameterizedSparqlString();
        s.setCommandText("INSERT { ?o ?p ?s } WHERE { ?s ?p ?o }");
        s.setIri("s", "_:" + bnode.getId());
        UpdateRequest query = s.asUpdate();
        UpdateProcessor proc = UpdateExecutionFactory.create(query, GraphStoreFactory.create(ds));
        proc.execute();

        // This should be true because this was present in the intial model set
        // up
        Assert.assertEquals(1, model.listStatements(bnode, null, (RDFNode) null).toList().size());
        // This should return 0 because the INSERT should result in a new blank
        // node being created rather than the existing one being reused becaue
        // of the semantics of blank nodes usage in templates
        Assert.assertEquals(0, model.listStatements(null, null, bnode).toList().size());
    }

    @Test
    public void test_param_string_mixed_1() {
        // Test simple injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");
        query.setLiteral("o", true);

        test(query, new String[] { "<http://example.org>", "<http://predicate>", "true" }, new String[] { "?s", "?p", "?o" });
    }

    @Test
    public void test_param_string_string_1() {
        // Test regular string injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");
        query.setLiteral("o", "test");

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"test\" . }", query.toString());
    }

    @Test
    public void test_param_string_string_2() {
        // Test a string with quotes
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");
        query.setLiteral("o", "A \"test\" string");

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"A \\\"test\\\" string\" . }",
                query.toString());
    }

    @Test
    public void test_param_string_string_3() {
        // Test a string with a $
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");
        query.setLiteral("o", "Show me the $!");

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"Show me the $!\" . }", query.toString());
    }

    @Test
    public void test_param_string_string_4() {
        // Test a string with a newline
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");
        query.setLiteral("o", "A multi\nline string");

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"A multi\\nline string\" . }",
                query.toString());
    }

    @Test
    public void test_param_string_string_5() {
        // Test a string with a tab
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");
        query.setLiteral("o", "A tabby\tstring");

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"A tabby\\tstring\" . }", query.toString());
    }

    @Test
    public void test_param_string_string_6() {
        // Test a string with a single quote
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");
        query.setLiteral("o", "A test's test");

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"A test\\'s test\" . }", query.toString());
    }

    @Test
    public void test_param_string_string_7() {
        // Test a string with a backslash
        String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("p", "http://predicate");
        query.setLiteral("o", "test a\\b");

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"test a\\\\b\" . }", query.toString());
    }

    @Test
    public void test_param_string_boolean_1() {
        // Test boolean injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", true);

        // We don't expect #boolean as booleans should be formatted as plain
        // literals
        test(query, new String[] { "true" }, new String[] { "?o", XSD.xboolean.toString() });
    }

    @Test
    public void test_param_string_boolean_2() {
        // Test boolean injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", false);

        // We don't expect #boolean as booleans should be formatted as plain
        // literals
        test(query, new String[] { "false" }, new String[] { "?o", XSD.xboolean.toString() });
    }

    @Test
    public void test_param_string_boolean_3() {
        // Test invalid boolean injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xboolean.toString()));

        // We expect #boolean as booleans with invalid lexical values should not
        // be formatted as plain literals
        test(query, new String[] { "xyz", XSD.xboolean.toString() }, new String[] { "?o" });
    }

    @Test
    public void test_param_string_boolean_4() {
        // Test boolean injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, true);

        // We don't expect #boolean as booleans should be formatted as plain
        // literals
        test(query, new String[] { "true" }, new String[] { "? ", XSD.xboolean.toString() });
    }

    @Test
    public void test_param_string_boolean_5() {
        // Test boolean injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, false);

        // We don't expect #boolean as booleans should be formatted as plain
        // literals
        test(query, new String[] { "false" }, new String[] { "? ", XSD.xboolean.toString() });
    }

    @Test
    public void test_param_string_boolean_6() {
        // Test invalid boolean injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xboolean.toString()));

        // We expect #boolean as booleans with invalid lexical values should not
        // be formatted as plain literals
        test(query, new String[] { "xyz", XSD.xboolean.toString() }, new String[] { "? " });
    }

    @Test
    public void test_param_string_int_1() {
        // Test integer injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", 1234);

        // We don't expect #integer as integers should be formatted as typed
        // literals
        test(query, new String[] { "1234" }, new String[] { "?o", XSD.integer.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_int_2() {
        // Test long integer injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", 3000000000l);

        // We don't expect #integer as integers should be formatted as typed
        // literals
        test(query, new String[] { "3000000000" }, new String[] { "?o", XSD.integer.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_int_3() {
        // Test invalid integer injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.integer.toString()));

        // We do expect #integer as invalid integers should be formatted with
        // their type
        test(query, new String[] { "xyz", XSD.integer.toString() }, new String[] { "?o" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_int_4() {
        // Test integer injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, 1234);

        // We don't expect #integer as integers should be formatted as typed
        // literals
        test(query, new String[] { "1234" }, new String[] { "? ", XSD.integer.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_int_5() {
        // Test long integer injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, 3000000000l);

        // We don't expect #integer as integers should be formatted as typed
        // literals
        test(query, new String[] { "3000000000" }, new String[] { "? ", XSD.integer.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_int_6() {
        // Test invalid integer injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.integer.toString()));

        // We do expect #integer as invalid integers should be formatted with
        // their type
        test(query, new String[] { "xyz", XSD.integer.toString() }, new String[] { "? " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_double_1() {
        // Test double injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", 123.4);

        // We expect #double as doubles without exponents cannot be formatted as
        // plain literals
        test(query, new String[] { "123.4", XSD.xdouble.toString() }, new String[] { "?o" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_double_2() {
        // Test double injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", 123.0e5);

        // We don't expect #double as we expected doubles to be formatted as
        // plain literals
        test(query, new String[] { "1.23E7" }, new String[] { "?o", XSD.xdouble.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_double_3() {
        // Test invalid double injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xdouble.toString()));

        // We expect #double as invalid doubles cannot be formatted as plain
        // literals
        test(query, new String[] { "xyz", XSD.xdouble.toString() }, new String[] { "?o" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_double_4() {
        // Test double injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, 123.4);

        // We expect #double as doubles without exponents cannot be formatted as
        // plain literals
        test(query, new String[] { "123.4", XSD.xdouble.toString() }, new String[] { "? " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_double_5() {
        // Test double injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, 123.0e5);

        // We don't expect #double as we expected doubles to be formatted as
        // plain literals
        test(query, new String[] { "1.23E7" }, new String[] { "? ", XSD.xdouble.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_double_6() {
        // Test invalid double injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xdouble.toString()));

        // We expect #double as invalid doubles cannot be formatted as plain
        // literals
        test(query, new String[] { "xyz", XSD.xdouble.toString() }, new String[] { "? " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_float_1() {
        // Test float injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", 123.4f);

        // We expect #float as floats should be formatted as typed literals
        test(query, new String[] { "123.4", XSD.xfloat.toString() }, new String[] { "?o" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_float_2() {
        // Test float injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, 123.4f);

        // We expect #float as floats should be formatted as typed literals
        test(query, new String[] { "123.4", XSD.xfloat.toString() }, new String[] { "? " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_date_1() {
        // Test date injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        Calendar dt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        dt.set(2012, 1, 24, 12, 0, 0);
        query.setLiteral("o", dt);

        // We expect #dateTime as dateTime should be formatted as typed literals
        test(query, new String[] { "2012-02-24T12:00:00", XSD.dateTime.toString() }, new String[] { "?o" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_date_2() {
        // Test invalid date injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.dateTime.toString()));

        // We expect #dateTime as dateTime should be formatted as typed literals
        test(query, new String[] { "xyz", XSD.dateTime.toString() }, new String[] { "?o" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_date_3() {
        // Test date injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        Calendar dt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        dt.set(2012, 1, 24, 12, 0, 0);
        query.setLiteral(0, dt);

        // We expect #dateTime as dateTime should be formatted as typed literals
        test(query, new String[] { "2012-02-24T12:00:00", XSD.dateTime.toString() }, new String[] { "? " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_date_4() {
        // Test invalid date injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.dateTime.toString()));

        // We expect #dateTime as dateTime should be formatted as typed literals
        test(query, new String[] { "xyz", XSD.dateTime.toString() }, new String[] { "? " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_lang_1() {
        // Test lang literal injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", "hello", "en");

        test(query, new String[] { "hello", "@en" }, new String[] { "?o" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_lang_2() {
        // Test lang literal injection
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("o", "bonjour", "fr");

        test(query, new String[] { "bonjour", "@fr" }, new String[] { "?o" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_lang_3() {
        // Test lang literal injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, "hello", "en");

        test(query, new String[] { "hello", "@en" }, new String[] { "? " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_lang_4() {
        // Test lang literal injection
        String cmdText = "SELECT * WHERE { ?s ?p ? }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral(0, "bonjour", "fr");

        test(query, new String[] { "bonjour", "@fr" }, new String[] { "? " });
        testAsQuery(query);
    }

    @Test(expected = QueryException.class)
    public void test_param_string_bad_1() {
        // Test bad input - not a valid query
        String cmdText = "Not a query";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);

        testAsQuery(query);
    }

    @Test(expected = QueryException.class)
    public void test_param_string_simple_bad_1() {
        // Test bad input - injecting the parameter makes the query invalid
        String cmdText = "SELECT ?s WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");

        test(query, new String[] { "<http://example.org>" }, new String[] { "?s" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_precedence_1() {
        // Test simple injection precedence
        // Setting parameter multiple times just overrides the existing setting
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setIri("s", "http://alternate.org");

        test(query, new String[] { "<http://alternate.org>" }, new String[] { "?s", "<http://example.org>" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_precedence_2() {
        // Test simple injection precedence
        // Setting parameter multiple times just overrides the existing setting
        String cmdText = "SELECT * WHERE { ? ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri(0, "http://example.org");
        query.setIri(0, "http://alternate.org");

        test(query, new String[] { "<http://alternate.org>" }, new String[] { "? ", "<http://example.org>" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_name_collision_1() {
        // Test name collision
        // The parameter we inject has a name which is a prefix of another
        // variable name, only the
        // actual name should be injected to
        String cmdText = "SELECT * WHERE { ?a ?ab ?abc }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("a", "http://example.org");

        // In the not expected list we need the whitespace after ?a as otherwise
        // the test will give a
        // false negative since obviously we should still have ?ab and ?abc
        // present
        test(query, new String[] { "<http://example.org>", "?ab", "?abc" }, new String[] { "?a " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_name_collision_2() {
        // Test name collision
        // The parameter we inject has a name which is a prefix of another
        // variable name, only the
        // actual name should be injected to
        String cmdText = "SELECT * WHERE { ?abc ?ab ?a. }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("a", "http://example.org");

        // In the not expected list we need the whitespace after ?a as otherwise
        // the test will give a
        // false negative since obviously we should still have ?ab and ?abc
        // present
        test(query, new String[] { "<http://example.org>", "?ab", "?abc" }, new String[] { "?a " });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_name_collision_3() {
        // Test name collision
        // In this test the parameter we inject has a name which collides with a
        // term used
        // in a prefix in the query
        String cmdText = "PREFIX ex: <http://example.org/vocab#> SELECT * WHERE { ?s ex:name ?name }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setLiteral("name", "Bob");

        // In the expected list we want to see Bob, in the not expected list we
        // don't want to see
        // ex:Bob since that would be a bad variable insertion
        test(query, new String[] { "Bob" }, new String[] { "?name", "ex:Bob" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_clear_1() {
        // Test clearing of parameter
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.clearParam("s");

        test(query, new String[] { "?s" }, new String[] { "<http://example.org>" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_clear_2() {
        // Test clearing of parameter
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.clearParams();

        test(query, new String[] { "?s" }, new String[] { "<http://example.org>" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_clear_3() {
        // Test indirect clearing of parameter by setting param to null
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri("s", "http://example.org");
        query.setParam("s", (Node) null);

        test(query, new String[] { "?s" }, new String[] { "<http://example.org>" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_clear_4() {
        // Test clearing of parameter
        String cmdText = "SELECT * WHERE { ? ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri(0, "http://example.org");
        query.clearParam(0);

        test(query, new String[] { "? " }, new String[] { "<http://example.org>" });
    }

    @Test
    public void test_param_string_clear_5() {
        // Test clearing of parameter
        String cmdText = "SELECT * WHERE { ? ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri(0, "http://example.org");
        query.clearParams();

        test(query, new String[] { "? " }, new String[] { "<http://example.org>" });
    }

    @Test
    public void test_param_string_clear_6() {
        // Test indirect clearing of parameter by setting param to null
        String cmdText = "SELECT * WHERE { ? ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setIri(0, "http://example.org");
        query.setParam(0, (Node) null);

        test(query, new String[] { "? " }, new String[] { "<http://example.org>" });
    }

    @Test
    public void test_param_string_prefixes_1() {
        // Test prefixes are prepended
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setNsPrefix("ex", "http://example.org");

        test(query, new String[] { "PREFIX", "ex:", "<http://example.org>" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_prefixes_2() {
        // Test prefixes are prepended
        String cmdText = "SELECT * WHERE { ?s ex:predicate ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setNsPrefix("ex", "http://example.org");

        test(query, new String[] { "PREFIX", "ex:", "<http://example.org>", "ex:predicate" }, new String[] {});
        testAsQuery(query);
    }

    @Test(expected = QueryException.class)
    public void test_param_string_prefixes_bad_1() {
        // Test bad input - using a prefix without defining prefix
        String cmdText = "SELECT * WHERE { ?s ex:predicate ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);

        // Testing with an PName using an undefined prefix in the string
        // Should fail on parsing
        test(query, new String[] { "ex:predicate" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_base_1() {
        // Test base is prepended
        String cmdText = "SELECT * WHERE { ?s <#predicate> ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setBaseUri("http://example.org");

        test(query, new String[] { "BASE", "<http://example.org>" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_base_bad_1() {
        // Test questionable input - using relative URI without defining base
        // ARQ accepts this, not sure if this is a way to disable this as this
        // test should
        // ideally be expecting a QueryException
        String cmdText = "SELECT * WHERE { ?s <#predicate> ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);

        test(query, new String[] {}, new String[] { "BASE", "<http://example.org>" });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_copy_1() {
        // Test copying - copying always copies command text
        String cmdText = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        ParameterizedSparqlString copy = query.copy();

        Assert.assertEquals(cmdText, copy.getCommandText());
    }

    @Test
    public void test_param_string_copy_2() {
        // Test copying - copying and changing a parameter changes only one
        // instance
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.setIri("x", "http://example.org/original");
        ParameterizedSparqlString copy = query.copy();
        copy.setIri("x", "http://example.org/copy");

        Assert.assertEquals("http://example.org/original", query.getParam("x").toString());
        Assert.assertFalse("http://example.org/copy".equals(query.getParam("x").toString()));

        Assert.assertEquals("http://example.org/copy", copy.getParam("x").toString());
        Assert.assertFalse("http://example.org/original".equals(copy.getParam("x").toString()));
    }

    @Test
    public void test_param_string_copy_3() {
        // Test copying - copying should copy prefixes
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.setNsPrefix("ex", "http://example.org");
        ParameterizedSparqlString copy = query.copy();

        Assert.assertEquals("http://example.org", copy.getNsPrefixURI("ex"));
    }

    @Test
    public void test_param_string_copy_4() {
        // Test copying - copying should copy base URI
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.setBaseUri("http://example.org");
        ParameterizedSparqlString copy = query.copy();

        Assert.assertEquals("http://example.org", copy.getBaseUri());
    }

    @Test
    public void test_param_string_copy_5() {
        // Test selective copying - copying without copying parameters
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.setIri("x", "http://example.org/original");
        ParameterizedSparqlString copy = query.copy(false);

        Assert.assertEquals("http://example.org/original", query.getParam("x").toString());
        Assert.assertEquals(null, copy.getParam("x"));
    }

    @Test
    public void test_param_string_copy_6() {
        // Test selective copying - copying without copying prefixes
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.setNsPrefix("ex", "http://example.org");
        ParameterizedSparqlString copy = query.copy(true, true, false);

        Assert.assertFalse("http://example.org".equals(copy.getNsPrefixURI("ex")));
    }

    @Test
    public void test_param_string_copy_7() {
        // Test copying - copying and changing a parameter changes only one
        // instance
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.setIri(0, "http://example.org/original");
        ParameterizedSparqlString copy = query.copy();
        copy.setIri(0, "http://example.org/copy");

        Assert.assertEquals("http://example.org/original", query.getParam(0).toString());
        Assert.assertFalse("http://example.org/copy".equals(query.getParam(0).toString()));

        Assert.assertEquals("http://example.org/copy", copy.getParam(0).toString());
        Assert.assertFalse("http://example.org/original".equals(copy.getParam(0).toString()));
    }

    @Test
    public void test_param_string_copy_8() {
        // Test selective copying - copying without copying parameters
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.setIri(0, "http://example.org/original");
        ParameterizedSparqlString copy = query.copy(false);

        Assert.assertEquals("http://example.org/original", query.getParam(0).toString());
        Assert.assertEquals(null, copy.getParam(0));
    }

    @Test
    public void test_param_string_append_1() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ?o }");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "?o" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_2() {
        // Test appending simple types
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.append(true);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "true" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_3() {
        // Test appending simple types
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.append(123);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "123" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_4() {
        // Test appending simple types
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.append(123l);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "123" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_5() {
        // Test appending simple types
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.append(123.0e5);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "1.23E7" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_iri_1() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ");
        query.appendIri("http://example.org");
        query.append(" ?o }");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "<http://example.org>", "?o" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_iri_2() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ");
        query.appendIri(IRIFactory.iriImplementation().construct("http://example.org"));
        query.append(" ?o }");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "<http://example.org>", "?o" }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_boolean_1() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.appendLiteral(true);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "true" }, new String[] { XSD.xboolean.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_boolean_2() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.appendLiteral("xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xboolean.toString()));
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "xyz", XSD.xboolean.toString() }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_integer_1() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.appendLiteral(123);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "123" }, new String[] { XSD.integer.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_integer_2() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.appendLiteral("xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.integer.toString()));
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "xyz", XSD.integer.toString() }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_integer_3() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.appendLiteral(123l);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "123" }, new String[] { XSD.integer.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_double_1() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.appendLiteral(123.0e5);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "1.23E7" }, new String[] { XSD.xdouble.toString() });
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_double_2() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.appendLiteral(1.23d);
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "1.23", XSD.xdouble.toString() }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_append_double_3() {
        // Test appending text
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT *");
        query.append('\n');
        query.append("WHERE { ?s ?p ");
        query.appendLiteral("xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xdouble.toString()));
        query.append("}");

        test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "xyz", XSD.xdouble.toString() }, new String[] {});
        testAsQuery(query);
    }

    @Test
    public void test_param_string_positional_1() {
        // Test positional parameters
        ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.append("SELECT * WHERE { ? ?p ?o }");
        query.setParam(0, NodeFactory.createURI("http://example.org"));

        test(query, new String[] { "<http://example.org>" }, new String[] { "? " });
    }

    @Test
    public void test_param_string_positional_2() {
        // Test regular string injection
        String cmdText = "SELECT * WHERE { ? ? ? . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setParam(0, NodeFactory.createURI("http://example.org"));
        query.setParam(1, NodeFactory.createURI("http://predicate"));
        query.setParam(2, NodeFactory.createLiteral("test"));

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"test\" . }", query.toString());
    }

    @Test
    public void test_param_string_positional_3() {
        // Test regular string injection
        String cmdText = "SELECT * WHERE { ? ? ? . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setParam(0, NodeFactory.createLiteral("with ? mark"));
        query.setParam(1, NodeFactory.createURI("http://predicate"));
        query.setParam(2, NodeFactory.createLiteral("test"));

        Assert.assertEquals("SELECT * WHERE { \"with ? mark\" <http://predicate> \"test\" . }", query.toString());
    }

    @Test
    public void test_param_string_positional_4() {
        // Test regular string injection
        String cmdText = "SELECT * WHERE { ? ? ? . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setParam(0, NodeFactory.createLiteral("with ? mark"));
        query.setParam(1, NodeFactory.createLiteral("with ? mark"));
        query.setParam(2, NodeFactory.createLiteral("test"));

        Assert.assertEquals("SELECT * WHERE { \"with ? mark\" \"with ? mark\" \"test\" . }", query.toString());
    }

    @Test
    public void test_param_string_positional_5() {
        // Test regular string injection
        String cmdText = "SELECT * WHERE { ? ? ?. }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setParam(0, NodeFactory.createURI("http://example.org"));
        query.setParam(1, NodeFactory.createURI("http://predicate"));
        query.setParam(2, NodeFactory.createLiteral("test"));

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"test\". }", query.toString());
    }

    @Test
    public void test_param_string_positional_6() {
        // Test regular string injection
        String cmdText = "SELECT * WHERE { ? ? ?; ?p ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setParam(0, NodeFactory.createURI("http://example.org"));
        query.setParam(1, NodeFactory.createURI("http://predicate"));
        query.setParam(2, NodeFactory.createLiteral("test"));

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"test\"; ?p ?o . }", query.toString());
    }

    @Test
    public void test_param_string_positional_7() {
        // Test regular string injection
        String cmdText = "SELECT * WHERE { ? ? ?, ?o . }";
        ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
        query.setParam(0, NodeFactory.createURI("http://example.org"));
        query.setParam(1, NodeFactory.createURI("http://predicate"));
        query.setParam(2, NodeFactory.createLiteral("test"));

        Assert.assertEquals("SELECT * WHERE { <http://example.org> <http://predicate> \"test\", ?o . }", query.toString());
    }

    @Test
    public void test_param_string_positional_eligible_1() {
        // Test detection of eligible parameters
        String cmdText = "SELECT * WHERE { ?s ?p ? . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(cmdText);

        Iterator<Integer> iter = pss.getEligiblePositionalParameters();
        int count = 0;
        while (iter.hasNext()) {
            count++;
            iter.next();
        }
        Assert.assertEquals(1, count);
    }

    @Test
    public void test_param_string_positional_eligible_2() {
        // Test detection of eligible parameters
        String cmdText = "SELECT * WHERE { ? ? ? . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(cmdText);

        Iterator<Integer> iter = pss.getEligiblePositionalParameters();
        int count = 0;
        while (iter.hasNext()) {
            count++;
            iter.next();
        }
        Assert.assertEquals(3, count);
    }

    @Test
    public void test_param_string_positional_eligible_3() {
        // Test detection of eligible parameters
        String cmdText = "SELECT * WHERE { ?s ?p ?; ?p1 ?, ?. }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(cmdText);

        Iterator<Integer> iter = pss.getEligiblePositionalParameters();
        int count = 0;
        while (iter.hasNext()) {
            count++;
            iter.next();
        }
        Assert.assertEquals(3, count);
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_01() {
        // This injection is prevented by forbidding the > character in URIs
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ?var2 . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setIri("var2", "hello> } ; DROP ALL ; INSERT DATA { <s> <p> <goodbye>");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_02() {
        // This injection is prevented by forbidding the > character in URIs
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ?var2 . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setIri("var2", "hello> } ; DROP ALL ; INSERT DATA { <s> <p> <goodbye");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test
    public void test_param_string_injection_03() {
        // This injection attempt results in a valid update but a failed
        // injection
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ?var2 . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var2", "hello\" } ; DROP ALL ; INSERT DATA { <s> <p> <goodbye>");

        UpdateRequest updates = pss.asUpdate();
        Assert.assertEquals(1, updates.getOperations().size());
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_04() {
        // This injection is prevented by forbidding the > character in URIs
        String str = "PREFIX : <http://example/>\nSELECT * WHERE { <s> <p> ?var2 . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setIri("var2", "hello> . ?s ?p ?o");

        Query q = pss.asQuery();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test
    public void test_param_string_injection_05() {
        // This injection attempt results in a valid query but a failed
        // injection
        String str = "PREFIX : <http://example/>\nSELECT * WHERE { <s> <p> ?var2 . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var2", "hello\" . ?s ?p ?o");

        Query q = pss.asQuery();
        Element el = q.getQueryPattern();
        if (el instanceof ElementTriplesBlock) {
            Assert.assertEquals(1, ((ElementTriplesBlock) q.getQueryPattern()).getPattern().size());
        } else if (el instanceof ElementGroup) {
            Assert.assertEquals(1, ((ElementGroup) el).getElements().size());
            el = ((ElementGroup) el).getElements().get(0);
            if (el instanceof ElementTriplesBlock) {
                Assert.assertEquals(1, ((ElementTriplesBlock) el).getPattern().size());
            }
        }
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_06() {
        // This injection attempt is prevented by forbidding injection to a
        // variable parameter immediately surrounded by quotes
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> '?var' }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", "hello' . } ; DROP ALL ; INSERT DATA { <s> <p> \"goodbye");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_07() {
        // This injection attempt is prevented by forbidding injection of
        // variable parameters immediately surrounded by quotes
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> \"?var\" }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_08() {
        // This injection attempt results in an invalid SPARQL update because
        // you end up with a double quoted literal inside a single quoted
        // literal
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> '?var' }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", "' . } ; DROP ALL ; INSERT DATA { <s> <p> <o> }#");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test
    public void test_param_string_injection_09() {
        // This injection attempt using comments results in a valid SPARQL
        // update but a failed injection because the attempt to use comments
        // ends up being a valid string literal within quotes
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ?var }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", "\" . } ; DROP ALL ; INSERT DATA { <s> <p> <o> }#");

        UpdateRequest updates = pss.asUpdate();
        Assert.assertEquals(1, updates.getOperations().size());
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_10() {
        // This injection attempt tries to chain together injections to achieve
        // an attack, the first
        // injection appears innocuous and is an attempt to set up an actual
        // injection vector
        // The injection is prevented because a ?var directly surrounded by
        // quotes is always flagged as
        // subject to injection because pre-injection validation happens before
        // each variable is injected
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ?var }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", "a");
        pss.setLiteral("var2", "b");

        // Figure out which variable will be injected first
        @SuppressWarnings("deprecation")
        String first = pss.getVars().next();
        String second = first.equals("var") ? "var2" : "var";

        pss.setLiteral(first, "?" + second);
        pss.setLiteral(second, " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_11() {
        // This is a variant on placing a variable bound to a literal inside a
        // literal resulting in an injection, we are now able to detect and
        // prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> \" ?var \" }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_12() {
        // This is a variant on placing a variable bound to a literal inside a
        // literal resulting in an injection, we are now able to detect and
        // prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> \"some text ?var other text\" }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test
    public void test_param_string_injection_13() {
        // This is a variant on placing a variable bound to a literal inside a
        // literal resulting in an injection, we now escape ' so prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ' ?var ' }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", "' . } ; DROP ALL ; INSERT DATA { <s> <p> <o> }#");

        UpdateRequest updates = pss.asUpdate();
        Assert.assertEquals(1, updates.getOperations().size());
    }

    @Test
    public void test_param_string_injection_14() {
        // This is a variant on placing a variable bound to a literal inside a
        // literal resulting in an injection, we now escape ' so prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> 'some text ?var other text' }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", "' . } ; DROP ALL ; INSERT DATA { <s> <p> <o> }#");

        UpdateRequest updates = pss.asUpdate();
        Assert.assertEquals(1, updates.getOperations().size());
    }

    @Test(expected = ARQException.class)
    public void test_param_string_injection_15() {
        // This injection attempt tries to chain together injections to achieve
        // an attack, the first injection appears innocuous and is an attempt to
        // set up an actual injection vector
        // Since we not check out delimiters we are not able to detect and
        // prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ?var }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", "a");
        pss.setLiteral("var2", "b");

        // Figure out which variable will be injected first
        @SuppressWarnings("deprecation")
        String first = pss.getVars().next();
        String second = first.equals("var") ? "var2" : "var";

        pss.setLiteral(first, " ?" + second + " ");
        pss.setLiteral(second, " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test
    public void test_param_string_non_injection_01() {
        // This test checks that a legitimate injection of a literal to a
        // variable that occurs between two other literals is permitted
        // Btw this is not a valid query but it serves to illustrate the case
        String str = "SELECT * { \"subject\" ?var \"object\" . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("var", "predicate");

        pss.toString();
    }

    @Test(expected = ARQException.class)
    public void test_param_string_positional_injection_01() {
        // This injection is prevented by forbidding the > character in URIs
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ?v . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setIri(0, "hello> } ; DROP ALL ; INSERT DATA { <s> <p> <goodbye>");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test(expected = ARQException.class)
    public void test_param_string_positional_injection_02() {
        // This injection is prevented by forbidding the > character in URIs
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ? . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setIri(0, "hello> } ; DROP ALL ; INSERT DATA { <s> <p> <goodbye");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test
    public void test_param_string_positional_injection_03() {
        // This injection attempt results in a valid update but a failed
        // injection
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ? . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "hello\" } ; DROP ALL ; INSERT DATA { <s> <p> <goodbye>");

        UpdateRequest updates = pss.asUpdate();
        Assert.assertEquals(1, updates.getOperations().size());
    }

    @Test(expected = ARQException.class)
    public void test_param_string_positional_injection_04() {
        // This injection is prevented by forbidding the > character in URIs
        String str = "PREFIX : <http://example/>\nSELECT * WHERE { <s> <p> ? . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setIri(0, "hello> . ?s ?p ?o");

        Query q = pss.asQuery();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test
    public void test_param_string_positional_injection_05() {
        // This injection attempt results in a valid query but a failed
        // injection
        String str = "PREFIX : <http://example/>\nSELECT * WHERE { <s> <p> ? . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "hello\" . ?s ?p ?o");

        Query q = pss.asQuery();
        Element el = q.getQueryPattern();
        if (el instanceof ElementTriplesBlock) {
            Assert.assertEquals(1, ((ElementTriplesBlock) q.getQueryPattern()).getPattern().size());
        } else if (el instanceof ElementGroup) {
            Assert.assertEquals(1, ((ElementGroup) el).getElements().size());
            el = ((ElementGroup) el).getElements().get(0);
            if (el instanceof ElementTriplesBlock) {
                Assert.assertEquals(1, ((ElementTriplesBlock) el).getPattern().size());
            }
        }
    }

    @Test
    public void test_param_string_positional_injection_06() {
        // This injection attempt is prevented by forbidding injection to a
        // variable parameter immediately surrounded by quotes
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> '?' }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "hello' . } ; DROP ALL ; INSERT DATA { <s> <p> \"goodbye");

        // In the positional case this does not work because the '?' is not
        // considered an eligible positional parameter due to the lack of
        // subsequent white space or punctuation
        Assert.assertFalse(pss.getEligiblePositionalParameters().hasNext());
    }

    @Test
    public void test_param_string_positional_injection_07() {
        // This injection attempt is prevented by forbidding injection of
        // variable parameters immediately surrounded by quotes
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> \"?\" }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        // In the positional case this does not work because the "?" is not
        // considered an eligible positional parameter due to the lack of
        // subsequent white space or punctuation
        Assert.assertFalse(pss.getEligiblePositionalParameters().hasNext());
    }

    @Test
    public void test_param_string_positional_injection_08() {
        // This injection attempt results in an invalid SPARQL update because
        // you end up with a double quoted literal inside a single quoted
        // literal
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> '?' }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "' . } ; DROP ALL ; INSERT DATA { <s> <p> <o> }#");

        // In the positional case this does not work because the '?' is not
        // considered an eligible positional parameter due to the lack of
        // subsequent white space or punctuation
        Assert.assertFalse(pss.getEligiblePositionalParameters().hasNext());
    }

    @Test
    public void test_param_string_positional_injection_09() {
        // This injection attempt using comments results in a valid SPARQL
        // update but a failed injection because the attempt to use comments
        // ends up being a valid string literal within quotes
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ? }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "\" . } ; DROP ALL ; INSERT DATA { <s> <p> <o> }#");

        UpdateRequest updates = pss.asUpdate();
        Assert.assertEquals(1, updates.getOperations().size());
    }

    @Test
    public void test_param_string_positional_injection_10() {
        // This injection attempt tries to chain together injections to achieve
        // an attack, the first
        // injection appears innocuous and is an attempt to set up an actual
        // injection vector
        // The injection is prevented because a ?var directly surrounded by
        // quotes is always flagged as
        // subject to injection because pre-injection validation happens before
        // each variable is injected
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ? }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "?");
        pss.setLiteral(1, " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        // In the positional parameter case this should fail because there
        // is only one eligible positional parameter in the string and we cannot
        // introduce additional ones via chained injection
        Iterator<Integer> params = pss.getEligiblePositionalParameters();
        Assert.assertTrue(params.hasNext());
        params.next();
        Assert.assertFalse(params.hasNext());

        UpdateRequest u = pss.asUpdate();
        Assert.assertEquals(1, u.getOperations().size());
    }

    @Test(expected = ARQException.class)
    public void test_param_string_positional_injection_11() {
        // This is a variant on placing a variable bound to a literal inside a
        // literal resulting in an injection, we are now able to detect and
        // prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> \" ? \" }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test(expected = ARQException.class)
    public void test_param_string_positional_injection_12() {
        // This is a variant on placing a variable bound to a literal inside a
        // literal resulting in an injection, we are now able to detect and
        // prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> \"some text ? other text\" }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        UpdateRequest updates = pss.asUpdate();
        Assert.fail("Attempt to do SPARQL injection should result in an exception");
    }

    @Test
    public void test_param_string_positional_injection_13() {
        // This is a variant on placing a variable bound to a literal inside a
        // literal resulting in an injection, we now escape ' so prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ' ? ' }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "' . } ; DROP ALL ; INSERT DATA { <s> <p> <o> }#");

        UpdateRequest updates = pss.asUpdate();
        Assert.assertEquals(1, updates.getOperations().size());
    }

    @Test
    public void test_param_string_positional_injection_14() {
        // This is a variant on placing a variable bound to a literal inside a
        // literal resulting in an injection, we now escape ' so prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> 'some text ? other text' }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "' . } ; DROP ALL ; INSERT DATA { <s> <p> <o> }#");

        UpdateRequest updates = pss.asUpdate();
        Assert.assertEquals(1, updates.getOperations().size());
    }

    @Test
    public void test_param_string_positional_injection_15() {
        // This injection attempt tries to chain together injections to achieve
        // an attack, the first injection appears innocuous and is an attempt to
        // set up an actual injection vector
        // Since we not check out delimiters we are not able to detect and
        // prevent this
        String str = "PREFIX : <http://example/>\nINSERT DATA { <s> <p> ? }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, " ? ");
        pss.setLiteral(1, " . } ; DROP ALL ; INSERT DATA { <s> <p> ");

        // In the positional parameter case this should fail because there
        // is only one eligible positional parameter in the string and we cannot
        // introduce additional ones via chained injection
        Iterator<Integer> params = pss.getEligiblePositionalParameters();
        Assert.assertTrue(params.hasNext());
        params.next();
        Assert.assertFalse(params.hasNext());

        UpdateRequest u = pss.asUpdate();
        Assert.assertEquals(1, u.getOperations().size());
    }

    @Test
    public void test_param_string_positional_non_injection_01() {
        // This test checks that a legitimate injection of a literal to a
        // variable that occurs between two other literals is permitted
        // Btw this is not a valid query but it serves to illustrate the case
        String str = "SELECT * { \"subject\" ? \"object\" . }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral(0, "predicate");

        pss.toString();
    }

    @Test
    public void test_param_string_bug_01() {
        // Tests a bug reported with setting literals
        String str = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("o", "has$sign");

        pss.toString();
    }

    @Test
    public void test_param_string_bug_02() {
        // Tests a bug reported with setting literals
        String str = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("o", "has$1sign");

        pss.toString();
    }

    @Test
    public void test_param_string_bug_03() {
        // Tests a bug reported with setting literals
        String str = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("o", "has$5sign");

        pss.toString();
    }

    @Test
    public void test_param_string_bug_04() {
        // Tests a bug reported with setting literals
        String str = "SELECT * WHERE { ?s ?p ?o }";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(str);
        pss.setLiteral("o", "has $9 sign");

        pss.toString();
    }
}
