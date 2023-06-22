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

package org.apache.jena.shacl.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.engine.Parameter;
import org.apache.jena.shacl.engine.constraint.SparqlComponent;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.vocabulary.SHACL;

public class TargetExtensions {
    /*package*/ MultiValuedMap<Node, SparqlComponent> paramPathToComponents = MultiMapUtils.newListValuedHashMap();
    /*package*/ Set<Parameter> parameters = new HashSet<>();

    // SPARQL-based target types.
    public static TargetExtensions parseSPARQLTargetType(Graph shapesGraph) {
        TargetExtensions x = new TargetExtensions();
        G.allNodesOfTypeRDFS(shapesGraph, SHACL.SPARQLTargetType).forEach(sttNode->{
            SparqlComponent c = sparqlTargetType(shapesGraph, sttNode);
            if ( c != null ) {
                for ( Parameter p : c.getParams() ) {
                    x.paramPathToComponents.put(p.getParameterPath(), c);
                    x.parameters.add(p);
                }
            }
        });
        return x;
    }

    /*
    ex:BornInCountryTarget
        a sh:SPARQLTargetType ;
        sh:labelTemplate "All persons born in {$country}" ;
        sh:parameter [
            sh:path ex:country ;
            sh:description "The country that the focus nodes are 'born' in." ;
            sh:class ex:Country ;
            sh:nodeKind sh:IRI ;
        ] ;
        sh:prefixes ex: ;
        sh:select """
            SELECT ?this
            WHERE {
                ?this a ex:Person .
                ?this ex:bornIn $country .
            }
            """ .
    */
    public static SparqlComponent sparqlTargetType(Graph shapesGraph, Node sparqlTargetTypeNode) {
        List<Parameter> params = Parameters.parseParameters(shapesGraph, sparqlTargetTypeNode);
        String sparqlString = ShLib.extractSPARQLQueryString(shapesGraph, sparqlTargetTypeNode);
        // sh:labelTemplates
        return SparqlComponent.targetType(sparqlString, params);
    }

    public boolean hasParameters() {
        return ! parameters.isEmpty();
    }
}
