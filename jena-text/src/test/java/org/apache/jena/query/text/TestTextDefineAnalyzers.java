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

import static org.junit.Assert.assertTrue;

import java.io.Reader ;
import java.io.StringReader ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.text.assembler.TextAssembler ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public class TestTextDefineAnalyzers extends AbstractTestDatasetWithTextIndexBase {

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
                    "    a                     tdb:DatasetTDB ;",
                    "    tdb:location          \"--mem--\" ;",
                    "    tdb:unionDefaultGraph true ;",
                    ".",
                    "",
                    ":indexLucene",
                    "    a text:TextIndexLucene ;",
                    "    text:directory \"mem\" ;",
                    "    text:storeValues true ;",
                    "    text:analyzer [",
                    "         a text:DefinedAnalyzer ;",
                    "         text:useAnalyzer :configuredAnalyzer ] ;",
                    "    text:defineAnalyzers (",
                    "         [ text:defineAnalyzer :configuredAnalyzer ;",
                    "           text:analyzer [",
                    "                a text:ConfigurableAnalyzer ;",
                    "                text:tokenizer :ngram ;",
                    "                text:filters ( :asciiff text:LowerCaseFilter ) ] ]",
                    "         [ text:defineTokenizer :ngram ;",
                    "           text:tokenizer [",
                    "                a text:GenericTokenizer ;",
                    "                text:class \"org.apache.lucene.analysis.ngram.NGramTokenizer\" ;",
                    "                text:params (",
                    "                     [ text:paramName \"minGram\" ;",
                    "                       text:paramType text:TypeInt ;",
                    "                       text:paramValue 3 ]",
                    "                     [ text:paramName \"maxGram\" ;",
                    "                       text:paramType text:TypeInt ;",
                    "                       text:paramValue 7 ]",
                    "                     ) ] ]",
                    "         [ text:defineFilter :asciiff ;",
                    "           text:filter [",
                    "                a text:GenericFilter ;",
                    "                text:class \"org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter\" ;",
                    "                text:params (",
                    "                     [ text:paramName \"preserveOriginal\" ;",
                    "                       text:paramType text:TypeBoolean ;",
                    "                       text:paramValue true ]",
                    "                     ) ] ]",
                    "         ) ;",
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
                    "         ) ."
                    );
    }

    @Before
    public void before() {
        Reader reader = new StringReader(SPEC);
        System.out.println(">>>>");        
        System.out.println(SPEC);        
        System.out.println("<<<<");
        Model specModel = ModelFactory.createDefaultModel();
        specModel.read(reader, "", "TURTLE");
        TextAssembler.init();
        Resource root = specModel.getResource(SPEC_ROOT_URI);
        try {
            dataset = (Dataset) Assembler.general.open(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Test
    public void testTextQueryDefAnalyzers1() {
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
        
        // execution reaches here in the event that the assembler machinery
        // has executed without errors and generated a usable dataset
        // usage of the runtime machinery is tested elsewhere
        assertTrue(true);
    }
}
