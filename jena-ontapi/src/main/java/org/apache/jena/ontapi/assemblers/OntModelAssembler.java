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

package org.apache.jena.ontapi.assemblers;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class OntModelAssembler extends AssemblerBase {
    /**
     * examples:
     * <pre>{@code
     * @prefix : <http://ex.com#> .
     * @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
     * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
     * @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
     *
     * :spec a oa:OntSpecification ;
     *      oa:specificationName "OWL1_LITE_MEM_RDFS_INF" .
     *
     * :base a ja:MemoryModel ;
     *      ja:content [
     *          ja:literalContent """
     *              @prefix : <http://ex.com#> .
     *              @prefix owl: <http://www.w3.org/2002/07/owl#> .
     *              :C a owl:Class .
     *          """ ;
     *          ja:contentEncoding "TTL"
     *      ] .
     *
     * :model a oa:OntModel ;
     *      oa:ontModelSpec :spec ;
     *      oa:baseModel :base .
     * }</pre>
     * <pre>{@code
     * @prefix : <http://ex.com#> .
     * @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
     * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
     * @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
     *
     * :repo a oa:DocumentGraphRepository ;
     *      oa:graph :g .
     * :g a oa:Graph ;
     *      oa:graphIRI "urn:food" ;
     *      oa:graphLocation "file:wine.ttl" .
     * :base a ja:MemoryModel ;
     *      ja:externalContent <file:food.ttl> .
     * :model a oa:OntModel ;
     *      oa:baseModel :base ;
     *      oa:documentGraphRepository :repo .
     * }</pre>
     */
    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        checkType(root, OA.OntModel);
        var baseModel = getBase(a, root, mode);
        var specification = getOntModelSpec(a, root);
        var graphRepository = getDocumentGraphRepositoryOrNull(a, root);
        var res = graphRepository == null ?
                OntModelFactory.createModel(baseModel.getGraph(), specification) :
                OntModelFactory.createModel(baseModel.getGraph(), specification, graphRepository);
        root.listProperties(OA.importModels)
                .mapWith(Statement::getResource)
                .mapWith(resource -> {
                    checkType(resource, OA.OntModel);
                    return (OntModel) a.open(resource);
                })
                .forEach(res::addImport);
        return res;
    }

    private Model getBase(Assembler a, Resource root, Mode mode) {
        var base = getUniqueResource(root, OA.baseModel);
        return base == null ? ModelFactory.createDefaultModel() : a.openModel(base, mode);
    }

    private OntSpecification getOntModelSpec(Assembler a, Resource root) {
        var r = getUniqueResource(root, OA.ontModelSpec);
        return r == null ? OntSpecification.OWL2_DL_MEM_BUILTIN_RDFS_INF : (OntSpecification) a.open(r);
    }

    private DocumentGraphRepository getDocumentGraphRepositoryOrNull(Assembler a, Resource root) {
        var r = getUniqueResource(root, OA.documentGraphRepository);
        return r == null ? null : (DocumentGraphRepository) a.open(r);
    }

}
