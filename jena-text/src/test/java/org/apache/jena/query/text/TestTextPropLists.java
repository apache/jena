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

import java.io.File ;
import java.io.Reader ;
import java.io.StringReader ;
import java.util.Arrays ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.text.assembler.TextAssembler ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

/**
 * This class defines a setup configuration for a dataset that uses a standard analyzer with a Lucene index.
 */
public class TestTextPropLists extends AbstractTestDatasetWithTextIndexBase {
    private static final String INDEX_PATH = "target/test/TestDatasetWithLuceneIndex";
    private static final File indexDir = new File(INDEX_PATH);
    
    private static final String RES_BASE = "http://example.org/resource#";
    private static final String SPEC_BASE = "http://example.org/spec#";
    private static final String SPEC_ROOT_LOCAL = "lucene_text_dataset";
    private static final String SPEC_ROOT_URI = SPEC_BASE + SPEC_ROOT_LOCAL;

    protected static final String TURTLE_PROLOG = 
            StrUtils.strjoinNL(
                    "@prefix  res:  <" + RES_BASE + "> .",
                    "@prefix  spec: <" + SPEC_BASE + "> .",
                    "@prefix text: <http://jena.apache.org/text#> .",
                    "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
                    "@prefix skos: <http://www.w3.org/2004/02/skos/core#> ."
                    );    
    protected static final String QUERY_PROLOG = 
            StrUtils.strjoinNL(
                    "prefix res:  <" + RES_BASE + "> ",
                    "prefix spec: <" + SPEC_BASE + "> ",
                    "prefix text: <http://jena.apache.org/text#> ",
                    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ",
                    "prefix skos: <http://www.w3.org/2004/02/skos/core#> "
                    );

    private static final String SPEC;
    static {
        SPEC = StrUtils.strjoinNL(
                    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ",
                    "prefix skos: <http://www.w3.org/2004/02/skos/core#> ",
                    "prefix ja:   <http://jena.hpl.hp.com/2005/11/Assembler#> ",
                    "prefix tdb:  <http://jena.hpl.hp.com/2008/tdb#> ",
                    "prefix text: <http://jena.apache.org/text#> ",
                    "prefix spec: <" + SPEC_BASE + "> ",
                    "",
                    "[] ja:loadClass       \"org.apache.jena.query.text.TextQuery\" .",
                    "text:TextDataset      rdfs:subClassOf   ja:RDFDataset .",
                    "text:TextIndexLucene  rdfs:subClassOf   text:TextIndex .",
                    
                    "spec:" + SPEC_ROOT_LOCAL,
                    "    a              text:TextDataset ;",
                    "    text:dataset   spec:dataset ;",
                    "    text:index     spec:indexLucene ;",
                    "    .",
                    "",
                    "spec:dataset",
                    "    a               ja:RDFDataset ;",
                    "    ja:defaultGraph spec:graph ;",
                    ".",
                    "spec:graph",
                    "    a               ja:MemoryModel ;",
                    ".",
                    "",
                    "spec:indexLucene",
                    "    a text:TextIndexLucene ;",
                    "    text:directory   \"mem\" ;",
                    "    text:storeValues true ;",
                    "    text:entityMap   spec:entMap ;",
                    "#    text:multilingualSupport true ;", 
                    "    text:propLists (",
                    "      [ text:propListProp spec:labels ;",
                    "        text:props ( skos:prefLabel ",
                    "                     skos:altLabel ",
                    "                     rdfs:label ) ;",
                    "      ]",
                    "      [ text:propListProp spec:workStmts ;",
                    "        text:props ( rdfs:comment ",
                    "                     spec:workColophon ",
                    "                     spec:workAuthorshipStatement ",
                    "                     spec:workEditionStatement ) ;",
                    "      ]",
                    "    )",                    
                    "    .",
                    "",
                    "spec:entMap",
                    "    a text:EntityMap ;",
                    "    text:entityField      \"uri\" ;",
                    "    text:defaultField     \"label\" ;",
                    "    text:langField        \"lang\" ;",
                    "    text:graphField       \"graph\" ;",
                    "    text:map (",
                    "         [ text:field \"label\" ; text:predicate rdfs:label ]",
                    "         [ text:field \"altLabel\" ; text:predicate skos:altLabel ]",
                    "         [ text:field \"prefLabel\" ; text:predicate skos:prefLabel ]",
                    "         [ text:field \"comment\" ; text:predicate rdfs:comment ]",
                    "         [ text:field \"workAuthorshipStatement\" ; text:predicate spec:workAuthorshipStatement ]",
                    "         [ text:field \"workEditionStatement\" ; text:predicate spec:workEditionStatement ]",
                    "         [ text:field \"workColophon\" ; text:predicate spec:workColophon ]",
                    "         ) ."
                    );
    }      
    
    public void init() {
        Reader reader = new StringReader(SPEC);
        Model specModel = ModelFactory.createDefaultModel();
        specModel.read(reader, "", "TURTLE");
        TextAssembler.init();            
        indexDir.mkdirs();
        Resource root = specModel.getResource(SPEC_ROOT_URI);
        dataset = (Dataset) Assembler.general.open(root);
    }
    
    
    public void deleteOldFiles() {
        dataset.close();
        if (indexDir.exists()) TextSearchUtil.emptyAndDeleteDirectory(indexDir);
    }    

    @Before
    public void beforeClass() {
        init();
    }    
    
    @After
    public void afterClass() {
        deleteOldFiles();
    }
    
    @Test
    public void testForSanity01() {
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "res:oneThing rdfs:label 'bar the barfoo foo'",
                "."
                );
        String qyString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query 'bar' .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(RES_BASE+"oneThing")) ;
        doTestSearch(turtle, qyString, expectedURIs);
    }
    
    @Test
    public void testForSanity02() {
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "res:oneThing rdfs:label 'bar the barfoo foo'",
                "."
                );
        // the standard analyzer not to have 'the' as a stop word
        String qyString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( rdfs:label 'bar' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(RES_BASE+"oneThing")) ;
        doTestSearch(turtle, qyString, expectedURIs);
    }
    
    @Test
    public void testForSanity03() {
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "res:oneThing skos:prefLabel 'bar the barfoo foo'",
                "."
                );
        // the standard analyzer not to have 'the' as a stop word
        String qyString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    ?s text:query ( skos:prefLabel 'bar' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(RES_BASE+"oneThing")) ;
        doTestSearch(turtle, qyString, expectedURIs);
    }
    
    @Test
    public void testSingleTextProp01() {
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "res:oneThing skos:altLabel 'bar is surely the barfoo foo for me and you'",
                "."
                );
        // the standard analyzer not to have 'the' as a stop word
        String qyString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    (?ss ?sc ?lit ?g ?s) text:query ( skos:altLabel 'surely' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(SKOS.getURI()+"altLabel")) ;
        doTestSearch(turtle, qyString, expectedURIs);
    }
    
    @Test
    public void testSingleTextProp02() {
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "res:oneThing skos:altLabel 'bar is surely the barfoo foo for me and you'",
                "."
                );
        // the standard analyzer not to have 'the' as a stop word
        String qyString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    (?ss ?sc ?lit ?g ?s) text:query ( skos:altLabel 'surely' 10 'highlight:' ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(SKOS.getURI()+"altLabel")) ;
        doTestSearch(turtle, qyString, expectedURIs);
    }
    
    @Test
    public void testListTextProp01() {
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "res:oneThing skos:prefLabel \"bar the barfoo foo is hidden\"",
                "."
                );
        String qyString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "SELECT ?s",
                "WHERE {",
                "    (?ss ?sc ?lit ?g ?s) text:query ( spec:labels 'foo' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(SKOS.getURI()+"prefLabel")) ;
        doTestSearch(turtle, qyString, expectedURIs);
    }
    
    @Test
    public void testListTextProp02() {
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "",
                "res:oneThing skos:prefLabel \"bar the barfoo foo is hidden\" ",
                ".",
                "res:twoThing skos:altLabel \"there is no bar to the hidden foo of the flow\" ",
                ".",
                "res:threeThing rdfs:label \"if there had been a f o o then it would not bar a hit\" ",
                "."
                );
        String qyString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "",
                "SELECT ?s",
                "WHERE {",
                "    (?ss ?sc ?lit ?g ?s) text:query ( spec:labels 'foo' 10 ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(SKOS.getURI()+"prefLabel", SKOS.getURI()+"altLabel")) ;
        doTestSearch(turtle, qyString, expectedURIs);
    }
    
    @Test
    public void testListTextProp03() {
        final String turtle = StrUtils.strjoinNL(
                TURTLE_PROLOG,
                "",
                "res:oneThing skos:prefLabel \"bar the barfoo foo is hidden\" ",
                ".",
                "res:twoThing skos:altLabel \"there is no bar to the hidden foo of the flow\" ",
                ".",
                "res:threeThing rdfs:label \"if there had been a f o o then it would not bar a hit\" ",
                "."
                );
        String qyString = StrUtils.strjoinNL(
                QUERY_PROLOG,
                "",
                "SELECT ?s",
                "WHERE {",
                "    (?ss ?sc ?lit ?g ?s) text:query ( spec:labels 'foo' 10 'highlight:' ) .",
                "}"
                );
        Set<String> expectedURIs = new HashSet<>() ;
        expectedURIs.addAll( Arrays.asList(SKOS.getURI()+"prefLabel", SKOS.getURI()+"altLabel")) ;
        doTestSearch(turtle, qyString, expectedURIs);
    }
}
