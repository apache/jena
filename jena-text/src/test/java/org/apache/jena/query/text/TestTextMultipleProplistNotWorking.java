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

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.query.*;
import org.apache.jena.query.text.assembler.TextAssembler;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// GH-2094
public class TestTextMultipleProplistNotWorking extends AbstractTestDatasetWithTextIndexBase {

    private static final String SPEC_ROOT_URI = "http://example.org/spec#lucene_text_dataset";
    private static final String SPEC;

    protected static final String TURTLE_PROLOG2 = """
            @prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix mt: <http://id.example.test/vocab/#> .
            @prefix mx: <http://id.example.test/mx/#> .
            """;

    protected static final String QUERY_PROLOG2 = """
                    prefix res:  <http://example.org/resource#>
                    prefix spec: <http://example.org/spec#>
                    prefix mt: <http://id.example.test/vocab/#>
                    prefix text: <http://jena.apache.org/text#>
                    prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    prefix skos: <http://www.w3.org/2004/02/skos/core#>
                    """;
    static {
        SPEC = """
                @prefix :        <http://example.org/spec#> .
                @prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
                @prefix tdb2:    <http://jena.apache.org/2016/tdb#> .
                @prefix ja:      <http://jena.hpl.hp.com/2005/11/Assembler#> .
                @prefix text:    <http://jena.apache.org/text#> .
                @prefix fuseki:  <http://jena.apache.org/fuseki#> .
                @prefix mt:      <http://id.example.test/vocab/#> .
                @prefix mx:      <http://id.example.test/mx/#> .

                [] ja:loadClass       "org.apache.jena.query.text.TextQuery" .
                text:TextDataset      rdfs:subClassOf   ja:RDFDataset .
                text:TextIndexLucene  rdfs:subClassOf   text:TextIndex .

                :lucene_text_dataset
                    a text:TextDataset ;
                    text:dataset   <#dataset> ;
                    text:index     <#indexLucene> ;
                    .

                # A TDB dataset used for RDF storage
                <#dataset>
                    a tdb2:DatasetTDB2 ;
                    tdb2:location  "--mem--" ;
                    .

                # Text index description

                <#indexLucene>
                    a text:TextIndexLucene ;
                    text:storeValues true ;
                    text:directory "mem" ;
                    text:multilingualSupport true ;
                    text:defineAnalyzers (
                      [ text:addLang "en-01" ;
                        text:searchFor ( "en-01" "en-02" ) ;
                        text:analyzer [ a text:StandardAnalyzer ]
                      ]

                      [ text:addLang "en-02" ;
                        text:searchFor ( "en-01" "en-02" ) ;
                        text:analyzer [ a text:StandardAnalyzer ]
                      ] ) ;
                    text:entityMap <#entMap> ;
                        text:propLists (
                        [ text:propListProp mt:defQuery ;
                          text:props (
                             rdfs:label
                             mt:altLabel
                             mt:alt_label
                             mx:alt_label
                             ) ;
                        ]
                        [ text:propListProp mt:includeNotes ;
                          text:props (
                               rdfs:label
                               mt:altLabel
                               mt:alt_label
                               mx:alt_label
                               mt:note
                             ) ;
                        ]
                    ) ;
                     .

                <#entMap>
                    a text:EntityMap ;
                    text:defaultField     "comment" ;
                    text:entityField      "uri" ;
                    text:uidField         "uid" ;
                    text:langField        "lang" ;
                    text:graphField       "graph" ;
                    text:map (
                [ text:field "comment" ; text:predicate rdfs:comment ]
                         [ text:field "ftext" ; text:predicate rdfs:label ]
                         [ text:field "ftext" ; text:predicate mt:altLabel ]
                         [ text:field "ftext" ; text:predicate mt:alt_label ]
                         [ text:field "ftext" ; text:predicate mx:alt_label ]
                         [ text:field "note" ; text:predicate mt:note ]
                         ) .
                """;
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
    public void test01TextPropNotWorkingInSomeCases() {
        final String turtleA = TURTLE_PROLOG2 +"""
                <http://id.example.test/1>
                  rdfs:label "beer"@en ;
                  mt:altLabel "pint"@en ;
                  mx:alt_label "pivečko"@cs ;
                  mt:note "Booze is a pleasure"@en,"C hlast je slast"@cs .

                <http://id.example.test/2>
                  mt:alt_label "ale"@en,"burgundy"@en ;
                  rdfs:label "wine "@en ;
                  mt:altLabel "champagne"@en ;
                  mx:alt_label "víno"@cs ;
                  mt:note "Red or white"@en, "Červené či bílé"@cs .

                <http://id.example.test/3>
                  mt:alt_label"Scotch"@en ;
                  rdfs:comment"Johnnie Walker red label "@en .
                """;

        putTurtleInModel(turtleA, null) ;
        String queryString = QUERY_PROLOG2+"""
                SELECT ?s ?lit
                WHERE {
                  (?s ?sc ?lit) text:query ( mt:includeNotes 'red booze' ) .
                }
                """;
        String queryStringMtNote = QUERY_PROLOG2 + """
                SELECT ?s ?lit
                WHERE {
                  (?s ?sc ?lit)  text:query ( mt:note "red booze" ) .
                }
                """;

        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList("http://id.example.test/1", "http://id.example.test/2")) ;

        Map<String, Literal> literals = doTestSearchWithLiterals(queryString, expectedURIs) ;

        Map<String,Literal> literalsFromMtNote = doTestSearchWithLiterals(queryStringMtNote,expectedURIs);
        assertEquals(2, literals.size());

        Literal value = literals.get("http://id.example.test/1");
        assertNotNull(value);

        Literal valueNote = literalsFromMtNote.get("http://id.example.test/1");
        assertNotNull(valueNote);

        value = literals.get("http://id.example.test/2");
        assertNotNull(value);

        valueNote = literalsFromMtNote.get("http://id.example.test/2");
        assertNotNull(valueNote);
    }

    @Test
    public void test02TextPropNotWorkingInSomeCases() {
        final String turtleA = TURTLE_PROLOG2+"""
                <http://id.example.test/1>
                  rdfs:label "beer"@en-01 ;
                  mt:altLabel "pint"@en-01 ;
                  mx:alt_label "pivečko"@cs ;
                  mt:note "Booze is a pleasure"@en-01, "Chlast je slast"@cs .

                <http://id.example.test/2>
                  mt:alt_label "ale"@en-01 , "burgundy"@en-01 ;
                  rdfs:label "wine"@en-01 ;
                  mt:altLabel "champagne"@en-01 ;
                  mx:alt_label "víno"@cs ;
                  mt:note  "Red or white"@en-01, "Červené či bílé"@cs .

                <http://id.example.test/3>
                  mt:alt_label "Scotch"@en-01 ;
                  rdfs:comment "Johnnie Walker red label "@en-01 .
               """;

        putTurtleInModel(turtleA, null) ;
        String queryString = QUERY_PROLOG2+"""
                SELECT ?s ?lit
                WHERE {
                  (?s ?sc ?lit) text:query ( mt:includeNotes "red booze"@en-02 ) .
                }
                """;
        String queryStringMtNote = QUERY_PROLOG2+"""
                SELECT ?s ?lit
                WHERE {
                  (?s ?sc ?lit)  text:query ( mt:note "red booze"@en-02 ) .
                }
                """;

        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList("http://id.example.test/1", "http://id.example.test/2")) ;

        Map<String, Literal> literals = doTestSearchWithLiterals(queryString, expectedURIs) ;

        Map<String,Literal> literalsFromMtNote = doTestSearchWithLiterals(queryStringMtNote,expectedURIs);
        assertEquals(2, literals.size());

        Literal value = literals.get("http://id.example.test/1");
        assertNotNull(value);

        Literal valueNote = literalsFromMtNote.get("http://id.example.test/1");
        assertNotNull(valueNote);

        value = literals.get("http://id.example.test/2");
        assertNotNull(value);

        valueNote = literalsFromMtNote.get("http://id.example.test/2");
        assertNotNull(valueNote);
    }
}
