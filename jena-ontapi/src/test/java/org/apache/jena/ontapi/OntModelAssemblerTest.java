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

package org.apache.jena.ontapi;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OntModelAssemblerTest {

    @Test
    public void testAssembleSimpleOntModel() {
        var assembler = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        
                        :model a oa:OntModel .
                        """, "ttl"
        );
        var root = assembler.getResource("http://ex.com#model");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(OntModel.class, result);
        var model = (OntModel) result;
        Assertions.assertEquals(0, model.size());
    }

    @Test
    public void testAssembleOntModelWithBaseModelAndSpec() {
        var assembler = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
                        
                        :spec a oa:OntSpecification ;
                            oa:specificationName "OWL1_LITE_MEM_RDFS_INF" .
                        
                        :base a ja:MemoryModel ;
                            ja:content [
                                ja:literalContent ""\"
                                    @prefix : <http://ex.com#> .
                                    @prefix owl: <http://www.w3.org/2002/07/owl#> .
                                    :C a owl:Class .
                                ""\" ;
                                ja:contentEncoding "TTL"
                            ] .
                        
                        :model a oa:OntModel ;
                            oa:ontModelSpec :spec ;
                            oa:baseModel :base .
                        """, "ttl"
        );
        var root = assembler.getResource("http://ex.com#model");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(OntModel.class, result);
        var model = (OntModel) result;
        Assertions.assertEquals(41, model.size());
    }

    @Test
    public void testAssembleOntModelWithHierarchy() {
        var assembler = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
                        
                        :spec a oa:OntSpecification ;
                            oa:specificationName "OWL1_LITE_MEM_RDFS_INF" .
                        
                        :base1 a ja:MemoryModel ;
                            ja:content [
                                ja:literalContent ""\"
                                    @prefix : <http://ex.com#> .
                                    @prefix owl: <http://www.w3.org/2002/07/owl#> .
                                    : a owl:Ontology .
                                    :A a owl:Class .
                                ""\" ;
                                ja:contentEncoding "TTL"
                            ] .
                        
                        :base2 a ja:MemoryModel ;
                            ja:content [
                                ja:literalContent ""\"
                                    @prefix : <http://ex.com/v1#> .
                                    @prefix owl: <http://www.w3.org/2002/07/owl#> .
                                    : a owl:Ontology .
                                    :B a owl:Class .
                                ""\" ;
                                ja:contentEncoding "TTL"
                            ] .
                        
                        :model1 a oa:OntModel ;
                            oa:baseModel :base1 ;
                            oa:importModels :model2 .
                        :model2 a oa:OntModel ;
                            oa:baseModel :base2 .
                        """, "ttl"
        );
        var root = assembler.getResource("http://ex.com#model1");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(OntModel.class, result);
        var model1 = (OntModel) result;
        var imports = model1.imports().toList();
        Assertions.assertEquals(1, imports.size());
        var model2 = model1.imports().findFirst().orElseThrow();
        Assertions.assertEquals("http://ex.com#", model1.getID().getURI());
        Assertions.assertEquals("http://ex.com/v1#", model2.getID().getURI());
        Assertions.assertEquals(5, model1.size());
        Assertions.assertEquals(2, model2.size());
    }

    @Test
    public void testAssembleWithDocumentManager(@TempDir Path dir) throws IOException {
        var src1 = Files.createTempFile(dir, "wine.", ".ttl");
        RDFIOTestUtils.save(RDFIOTestUtils.loadResourceAsModel("/wine.ttl", Lang.TURTLE), src1, Lang.TURTLE);
        var srcUri1 = src1.toUri();
        var src2 = Files.createTempFile(dir, "food.", ".ttl");
        RDFIOTestUtils.save(RDFIOTestUtils.loadResourceAsModel("/food.ttl", Lang.TURTLE), src2, Lang.TURTLE);
        var srcUri2 = src2.toUri();
        var assembler = RDFIOTestUtils.readStringAsModel(
                String.format("""
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
                        
                        :repo a oa:DocumentGraphRepository ;
                            oa:graph :g .
                        :g a oa:Graph ;
                            oa:graphIRI "urn:food" ;
                            oa:graphLocation "%s" .
                        :base a ja:MemoryModel ;
                            ja:externalContent <%s> .
                        :model a oa:OntModel ;
                            oa:baseModel :base ;
                            oa:documentGraphRepository :repo .
                        """, srcUri2, srcUri1), "ttl"
        );
        var resultModel = Assembler.general().open(assembler.getResource("http://ex.com#model"));
        Assertions.assertInstanceOf(OntModel.class, resultModel);
        var model = (OntModel) resultModel;
        Assertions.assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine", model.getID().getImportsIRI());
        var imports = model.imports().toList();
        Assertions.assertEquals(1, imports.size());
        Assertions.assertEquals("http://www.w3.org/TR/2003/PR-owl-guide-20031209/food", imports.get(0).getID().getImportsIRI());

        var resultRepository = Assembler.general().open(assembler.getResource("http://ex.com#repo"));
        Assertions.assertInstanceOf(DocumentGraphRepository.class, resultRepository);
        var repo = (DocumentGraphRepository) resultRepository;
        Assertions.assertEquals(2, repo.count());
    }
}
