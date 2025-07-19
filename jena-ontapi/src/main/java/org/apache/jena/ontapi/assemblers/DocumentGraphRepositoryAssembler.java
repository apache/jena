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
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class DocumentGraphRepositoryAssembler extends AssemblerBase {

    /**
     * example:
     * <pre>{@code
     * @prefix : <http://ex.com#> .
     * @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
     * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
     *
     * :repo a oa:DocumentGraphRepository ;
     *      oa:graph :g1, :g2 .
     *
     * :g1 a oa:Graph ;
     *      oa:graphIRI "vocab1" ;
     *      oa:graphLocation "file:ontologies/vocab1.ttl" .
     *
     * :g2 a oa:Graph ;
     *      oa:graphIRI "vocab2" ;
     *      oa:graphLocation "file:ontologies/vocab2.ttl" .
     * }</pre>
     */
    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        checkType(root, OA.DocumentGraphRepository);
        DocumentGraphRepository repo = new DocumentGraphRepository();

        Model model = root.getModel();

        model.listStatements(root, OA.graph, (RDFNode) null)
                .mapWith(stmt -> stmt.getObject().asResource())
                .forEach(graph -> {
                    String id = graph.getProperty(OA.graphIRI).getString();
                    String location = graph.getProperty(OA.graphLocation).getString();
                    repo.addMapping(id, location);
                });

        return repo;
    }
}
