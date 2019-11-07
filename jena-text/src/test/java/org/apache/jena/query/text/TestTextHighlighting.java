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
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.text.assembler.TextAssembler ;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public class TestTextHighlighting extends AbstractTestDatasetWithTextIndexBase {

    private static final String SPEC_BASE = "http://example.org/spec#";
    private static final String SPEC_ROOT_LOCAL = "lucene_text_dataset";
    private static final String SPEC_ROOT_URI = SPEC_BASE + SPEC_ROOT_LOCAL;
    private static final String SPEC;
    static {
        SPEC = StrUtils.strjoinNL(
                    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ",
                    "prefix skos: <http://www.w3.org/2004/02/skos/core#> ",
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
                    "    a                     tdb:DatasetTDB ;",
                    "    tdb:location          \"--mem--\" ;",
                    "    tdb:unionDefaultGraph true ;",
                    ".",
                    "",
                    ":indexLucene",
                    "    a text:TextIndexLucene ;",
                    "    text:directory \"mem\" ;",
                    "    text:storeValues true ;",
                    "    text:multilingualSupport true ;",
                    "    text:entityMap :entMap ;",
                    "    .",
                    "",
                    ":entMap",
                    "    a text:EntityMap ;",
                    "    text:entityField      \"uri\" ;",
                    "    text:defaultField     \"label\" ;",
                    "    text:langField        \"lang\" ;",
                    "    text:graphField       \"graph\" ;",
                    "    text:map (",
                    "         [ text:field \"label\" ; text:predicate rdfs:label ]",
                    "         [ text:field \"comment\" ; text:predicate rdfs:comment ]",
                    "         [ text:field \"prefLabel\" ; text:predicate skos:prefLabel ]",
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

    private void putTurtleInModel(String turtle, String modelName) {
        Model model = modelName != null ? dataset.getNamedModel(modelName) : dataset.getDefaultModel() ;
        Reader reader = new StringReader(turtle) ;
        dataset.begin(ReadWrite.WRITE) ;
        try {
            model.read(reader, "", "TURTLE") ;
            dataset.commit() ;
        }
        finally {
            dataset.end();
        }
    }

    protected Map<String,Literal> doTestSearchWithLiterals(String queryString, Set<String> expectedEntityURIs) {
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
                Literal literal = soln.getLiteral("lit");
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

    @Test
    public void testTextQueryPFHighlighting1() {
        final String turtleA = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "testResultOneInModelA>",
                "  rdfs:label 'bar testResultOne barfoo foo'",
                ".",
                "<" + RESOURCE_BASE + "testResultTwoInModelA>",
                "  rdfs:label 'bar testResultTwo barfoo foo'",
                ".",
                "<" + RESOURCE_BASE + "testResultThreeInModelA>",
                "  rdfs:label 'bar testResultThree barfoo foo'",
                "."
                );
                putTurtleInModel(turtleA, "http://example.org/modelA") ;
        final String turtleB = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "testResultOneInModelB>",
                "  rdfs:label 'bar testResultOne barfoo foo'",
                "."
                );
                putTurtleInModel(turtleB, "http://example.org/modelB") ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?lit",
                "WHERE {",
                "  (?s ?sc ?lit ?g) text:query ( rdfs:label 'testResultThree' 10 \"highlight:\") . ",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + "testResultThreeInModelA")) ;
        
        Map<String, Literal> literals = doTestSearchWithLiterals(queryString, expectedURIs) ;
        assertEquals(1, literals.size());
        
        Literal value = literals.get(RESOURCE_BASE + "testResultThreeInModelA");
        assertNotNull(value);
        assertEquals(NodeFactory.createLiteral("bar ↦testResultThree↤ barfoo foo"), value.asNode());
    }

    @Test
    public void testTextQueryPFHighlighting2() {
        final String turtleA = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "testResultOneInModelA>",
                "  rdfs:label 'bar testResultOne barfoo foo'",
                ".",
                "<" + RESOURCE_BASE + "testResultTwoInModelA>",
                "  rdfs:label 'bar testResultTwo barfoo foo'",
                ".",
                "<" + RESOURCE_BASE + "testResultThreeInModelA>",
                "  rdfs:label 'bar testResultThree barfoo foo'",
                "."
                );
                putTurtleInModel(turtleA, "http://example.org/modelA") ;
        final String turtleB = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "testResultOneInModelB>",
                "  rdfs:label 'bar testResultOne barfoo foo'",
                "."
                );
                putTurtleInModel(turtleB, "http://example.org/modelB") ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?lit",
                "WHERE {",
                "  (?s ?sc ?lit ?g) text:query ( rdfs:label 'testResultOne' 10 \"highlight: s:<em class='hilite'> | e:</em>\") . ",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + "testResultOneInModelA", RESOURCE_BASE + "testResultOneInModelB")) ;
        
        Map<String, Literal> literals = doTestSearchWithLiterals(queryString, expectedURIs) ;
        assertEquals(2, literals.size());
        
        Literal value = literals.get(RESOURCE_BASE + "testResultOneInModelA");
        assertNotNull(value);
        assertEquals(NodeFactory.createLiteral("bar <em class='hilite'>testResultOne</em> barfoo foo"), value.asNode());
        
        value = literals.get(RESOURCE_BASE + "testResultOneInModelB");
        assertNotNull(value);
        assertEquals(NodeFactory.createLiteral("bar <em class='hilite'>testResultOne</em> barfoo foo"), value.asNode());
    }

    @Test
    public void testTextQueryPFHighlighting3() {
        final String turtleA = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "testResultOneInModelA>",
                "  rdfs:label 'bar testResultOne barfoo foo'@en",
                ".",
                "<" + RESOURCE_BASE + "testResultTwoInModelA>",
                "  rdfs:label 'bar testResultTwo barfoo foo'@en",
                ".",
                "<" + RESOURCE_BASE + "testResultThreeInModelA>",
                "  rdfs:label 'bar testResultThree barfoo foo'@en",
                "."
                );
                putTurtleInModel(turtleA, "http://example.org/modelA") ;
        final String turtleB = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "<" + RESOURCE_BASE + "testResultOneInModelB>",
                "  rdfs:label 'bar testResultOne barfoo foo'@en",
                "."
                );
                putTurtleInModel(turtleB, "http://example.org/modelB") ;
        String queryString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s ?lit",
                "WHERE {",
                "  (?s ?sc ?lit ?g) text:query ( rdfs:label 'testResultThree'@en 10 \"highlight:\") . ",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(RESOURCE_BASE + "testResultThreeInModelA")) ;
        
        Map<String, Literal> literals = doTestSearchWithLiterals(queryString, expectedURIs) ;
        assertEquals(1, literals.size());
        
        Literal value = literals.get(RESOURCE_BASE + "testResultThreeInModelA");
        assertNotNull(value);
        assertEquals(NodeFactory.createLiteral("bar ↦testResultThree↤ barfoo foo", "en"), value.asNode());
    }
}
