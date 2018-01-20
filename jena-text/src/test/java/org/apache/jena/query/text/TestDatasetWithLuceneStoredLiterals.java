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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Reader ;
import java.io.StringReader ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.LinkedList ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.text.assembler.TextAssembler ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public class TestDatasetWithLuceneStoredLiterals extends AbstractTestDatasetWithTextIndex {

    private static final String SPEC_BASE = "http://example.org/spec#";
    private static final String SPEC_ROOT_LOCAL = "lucene_text_dataset";
    private static final String SPEC_ROOT_URI = SPEC_BASE + SPEC_ROOT_LOCAL;
    private static final String SPEC;
    static {
        SPEC = StrUtils.strjoinNL(
                    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ",
                    "prefix ja:   <http://jena.hpl.hp.com/2005/11/Assembler#> ",
                    "prefix tdb:  <http://jena.hpl.hp.com/2008/tdb#>",
                    "prefix text: <http://jena.apache.org/text#>",
                    "prefix :     <" + SPEC_BASE + ">",
                    "",
                    "[] ja:loadClass    \"org.apache.jena.query.text.TextQuery\" .",
                    "text:TextDataset      rdfs:subClassOf   ja:RDFDataset .",
                    "text:TextIndexLucene  rdfs:subClassOf   text:TextIndex .",

                    ":" + SPEC_ROOT_LOCAL,
                    "    a              text:TextDataset ;",
                    "    text:dataset   :dataset ;",
                    "    text:index     :indexLucene ;",
                    "    .",
                    "",
                    ":dataset",
                    "    a               ja:RDFDataset ;",
                    "    ja:defaultGraph :graph ;",
                    ".",
                    ":graph",
                    "    a               ja:MemoryModel ;",
                    ".",
                    "",
                    ":indexLucene",
                    "    a text:TextIndexLucene ;",
                    "    text:directory \"mem\" ;",
                    "    text:storeValues true ;",
                    "    text:entityMap :entMap ;",
                    "    .",
                    "",
                    ":entMap",
                    "    a text:EntityMap ;",
                    "    text:entityField      \"uri\" ;",
                    "    text:defaultField     \"label\" ;",
                    "    text:langField     \"lang\" ;",
                    "    text:map (",
                    "         [ text:field \"label\" ; text:predicate rdfs:label ]",
                    "         [ text:field \"comment\" ; text:predicate rdfs:comment ]",
                    "         ) ."
                    );
    }

    @Before
    public void before() {
        Reader reader = new StringReader(SPEC);
        Model specModel = ModelFactory.createDefaultModel();
        specModel.read(reader, "", "TURTLE");
        TextAssembler.init();
        Resource root = specModel.getResource(SPEC_ROOT_URI);
        dataset = (Dataset) Assembler.general.open(root);
    }

    @After
    public void after() {
        dataset.close();
    }

    protected Map<String,Literal> doTestSearchWithLiterals(String turtle, String queryString, Set<String> expectedEntityURIs) {
        Model model = dataset.getDefaultModel();
        Reader reader = new StringReader(turtle);
        dataset.begin(ReadWrite.WRITE);
        model.read(reader, "", "TURTLE");
        dataset.commit();

        Map<String,Literal> literals = new HashMap<>();
        Query query = QueryFactory.create(queryString) ;
        dataset.begin(ReadWrite.READ);
        try(QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet results = qexec.execSelect() ;
            assertEquals(expectedEntityURIs.size() > 0, results.hasNext());
            int count;
            for (count=0; results.hasNext(); count++) {
                QuerySolution soln = results.nextSolution();
                String entityUri = soln.getResource("s").getURI();
                assertTrue(expectedEntityURIs.contains(entityUri));
                Literal literal = soln.getLiteral("literal");
                assertNotNull(literal);
                literals.put(entityUri, literal);
            }
            assertEquals(expectedEntityURIs.size(), count);
        }
        finally {
            dataset.end() ;
        }
        return literals;
    }

    protected List<Node> doTestSearchWithLiteralsMultiple(String turtle, String queryString, String expectedEntityURI) {
        List<Node> literals = new LinkedList<>();
        Model model = dataset.getDefaultModel();
        Reader reader = new StringReader(turtle);
        dataset.begin(ReadWrite.WRITE);
        model.read(reader, "", "TURTLE");
        dataset.commit();

        Query query = QueryFactory.create(queryString) ;
        dataset.begin(ReadWrite.READ);
        try(QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet results = qexec.execSelect() ;
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                String entityURI = soln.getResource("s").getURI();
                assertEquals(expectedEntityURI, entityURI);
                Literal literal = soln.getLiteral("literal");
                assertNotNull(literal);
                literals.add(literal.asNode());
            }
        }
        finally {
            dataset.end() ;
        }
        return literals;
    }


    @Test
    public void testLiteralValue() {
        // test basic capturing of the literal value in a variable
        final String turtle = PF_DATA ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?literal",
                "WHERE {",
                "    (?s ?score ?literal) text:query ('text') .",
                "}"
                );

        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( R_S1 ) ) ;
        Map<String,Literal> literals = doTestSearchWithLiterals(turtle, queryString, expectedURIs);
        Literal value = literals.get(R_S1);
        assertNotNull(value);
        assertEquals(NodeFactory.createLiteral("text"), value.asNode());
    }

    @Test
    public void testLiteralValueNonDefaultField() {
        // test basic capturing of the literal value in a variable
        final String testName = "testLiteralValueNonDefaultField";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + testName + ">",
                "  rdfs:comment 'a text comment'",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?literal",
                "WHERE {",
                "    (?s ?score ?literal) text:query (rdfs:comment 'text') .",
                "}"
                );

        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( RESOURCE_BASE + testName ) ) ;
        Map<String,Literal> literals = doTestSearchWithLiterals(turtle, queryString, expectedURIs);
        Literal value = literals.get(RESOURCE_BASE + testName);
        assertNotNull(value);
        assertEquals(NodeFactory.createLiteral("a text comment"), value.asNode());
    }

    @Test
    public void testLiteralValueWithLanguage() {
        // test capturing of the literal value in a variable, with language tag
        final String testName = "testLiteralValueWithLanguage";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + testName + ">",
                "  rdfs:label 'English language text'@en",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?literal",
                "WHERE {",
                "    (?s ?score ?literal) text:query ('text') .",
                "}"
                );

        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( RESOURCE_BASE + testName ) ) ;
        Map<String,Literal> literals = doTestSearchWithLiterals(turtle, queryString, expectedURIs);
        Literal value = literals.get( RESOURCE_BASE + testName );
        assertNotNull(value);
        assertEquals(NodeFactory.createLiteral("English language text", "en"), value.asNode());
    }

    @Test
    public void testLiteralValueWithDatatype() {
        // test capturing of the literal value in a variable, with datatype
        final String testName = "testLiteralValueWithDatatype";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + testName + ">",
                "  rdfs:comment true",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?literal",
                "WHERE {",
                "    (?s ?score ?literal) text:query (rdfs:comment 'true') .",
                "}"
                );

        Set<String> expectedURIs = new HashSet<>();
        expectedURIs.addAll( Arrays.asList( RESOURCE_BASE + testName ) ) ;
        Map<String,Literal> literals = doTestSearchWithLiterals(turtle, queryString, expectedURIs);
        Literal value = literals.get( RESOURCE_BASE + testName );
        assertNotNull(value);
        assertEquals(NodeFactory.createLiteral("true", XSDDatatype.XSDboolean), value.asNode());
    }

    @Test
    public void testLiteralValueMultiple() {
        // test capturing of multiple matching literal values in a variable
        final String testName = "testLiteralValueMultiple";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + testName + ">",
                "  rdfs:comment 'a nontext comment', 'another nontext comment'",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?literal",
                "WHERE {",
                "    (?s ?score ?literal) text:query (rdfs:comment 'nontext') .",
                "}"
                );

        String expectedURI = RESOURCE_BASE + testName;
        List<Node> literals = doTestSearchWithLiteralsMultiple(turtle, queryString, expectedURI);

        assertEquals(2, literals.size());
        assertTrue(literals.contains(NodeFactory.createLiteral("a nontext comment")));
        assertTrue(literals.contains(NodeFactory.createLiteral("another nontext comment")));
    }

    @Test
    public void testLiteralValueMultipleBoundSubject() {
        // test capturing of multiple matching literal values in a variable, when using bound subject
        final String testName = "testLiteralValueMultipleBoundSubject";
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + testName + ">",
                "  rdfs:comment 'a nontext comment', 'another nontext comment'",
                ".",
                "<" + RESOURCE_BASE + "irrelevant>",
                "  rdfs:comment 'an irrelevant nontext comment'",
                "."
                );
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?literal",
                "WHERE {",
                "    BIND(<" + RESOURCE_BASE + testName + "> AS ?s)",
                "    (?s ?score ?literal) text:query (rdfs:comment 'nontext') .",
                "}"
                );

        String expectedURI = RESOURCE_BASE + testName;
        List<Node> literals = doTestSearchWithLiteralsMultiple(turtle, queryString, expectedURI);

        assertEquals(2, literals.size());
        assertTrue(literals.contains(NodeFactory.createLiteral("a nontext comment")));
        assertTrue(literals.contains(NodeFactory.createLiteral("another nontext comment")));
    }

}
