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

import static org.apache.jena.shacl.sys.C.TRUE;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.engine.Parameter;
import org.apache.jena.shacl.engine.constraint.SparqlComponent;
import org.apache.jena.shacl.vocabulary.SHACL;

public class Parameters {

    /** Parse a parameter declaration */
    /*package*/ static List<Parameter> parseParameters(Graph shapesGraph, Node sccNode) {
        // scc is the SPARQL Constraint Component or SPARQL target type node.
        List<Parameter> params =
            G.listSP(shapesGraph, sccNode, SHACL.parameter).stream()
                .map(pn->parseParameter(shapesGraph, sccNode, pn))
                .collect(Collectors.toList());
        return params;
    }

    private static Parameter parseParameter(Graph shapesGraph, Node sccNode, Node parameterNode) {
        Node path = G.getZeroOrOneSP(shapesGraph, parameterNode, SHACL.path);
        if ( ! path.isURI() )
            throw new ShaclParseException("SparqlConstraintComponent: Not a URI for parameter name: "+path);
        String sparqlName = path.getLocalName();
        // Parameter constraints
        boolean isOptional = G.contains(shapesGraph, parameterNode, SHACL.optional, TRUE);
        // "all properties that are applicable to property shapes may also be used for parameters."
        List<Constraint> constraints = null;
        return new Parameter(path, sparqlName, isOptional, constraints);
    }

    /** For a specific nodeShape or propertyShape, extract the parameter-&gt;value* map. */
    public static MultiValuedMap<Parameter, Node> parameterValues(Graph shapesGraph, Node sh, SparqlComponent scc) {
        MultiValuedMap<Parameter, Node> paramValues = MultiMapUtils.newListValuedHashMap();
        scc.getParams().forEach(param->{
            List<Node> values = G.listSP(shapesGraph, sh, param.getParameterPath());
            if ( ! values.isEmpty() ) {
                // Constraints.
                paramValues.putAll(param, values);
            }
        });
        return paramValues;
    }

    public static boolean doesShapeHaveAllParameters(Graph shapesGraph, Node sh, List<Node> required) {
        for(Node p : required) {
            if ( ! G.contains(shapesGraph, sh, p, null ) ) {
                return false;
            }
        }
        return true;
    }
}
