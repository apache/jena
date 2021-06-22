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

package org.apache.jena.shacl.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.engine.Parameter;
import org.apache.jena.shacl.engine.constraint.SparqlComponent;
import org.apache.jena.shacl.parser.Parameters;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.ModelUtils;

public class EvalSparql {
    // Maybe Merge with SparqlValidation
    // It is similar but different (simpler).
    // And written at a different time so don't disturb SparqlValidation unnecessarily.

    private static boolean USE_QueryTransformOps = true;

    public static Collection<Node> evalSparqlComponent(Graph data, Node node, SparqlComponent sparqlComponent) {
        checkForRequiredParams(data, node, sparqlComponent);

        Query query = sparqlComponent.getQuery();
        if ( ! query.isSelectType() )
            throw new ShaclException("Not a SELECT query");
        //Parameters
        if ( USE_QueryTransformOps ) {
            // Done with QueryTransformOps.transform
            DatasetGraph dsg = DatasetGraphFactory.wrap(data);
            Map<Var, Node> substitutions = parametersToSyntaxSubstitutions(data, node, sparqlComponent.getParams());
            Query query2 = QueryTransformOps.transform(query, substitutions);
            try ( QueryExecution qExec = QueryExecutionFactory.create(query2, dsg)) {
                return evalSparqlOneVar(qExec);
            }
        } else {
            // Done with pre-binding.
            Model model = ModelFactory.createModelForGraph(data);
            QuerySolutionMap qsm = parametersToPreBinding(model, node, sparqlComponent.getParams());
            try ( QueryExecution qExec = QueryExecutionFactory.create(query, model, qsm)) {
                return evalSparqlOneVar(qExec);
            }
        }
    }

    public static Collection<Node> evalSparqlOneVar(QueryExecution qExec) {
        List<Var> vars = qExec.getQuery().getProjectVars();
        if ( vars.size() != 1 )
            throw new ShaclException("Except SELECT query with one output variable.");
        Var var = vars.get(0);
        return Iter.iter(qExec.execSelect())
                              .map(row->row.get(var.getVarName()).asNode())
                              .toSet();
    }

    private static Map<Var, Node> parametersToSyntaxSubstitutions(Graph data, Node node, List<Parameter> params) {
        Map<Var, Node> substitions = new HashMap<>();
        params.forEach(param->{
            Node path = param.getParameterPath();
            // Full path not supported (needs string-syntax rewrite)
            Node v = G.getOneSP(data, node, path);
            substitions.put(Var.alloc(param.getSparqlName()), v);
        });
        return substitions;
    }

    private static QuerySolutionMap parametersToPreBinding(Model data, Node node, List<Parameter> params) {
        QuerySolutionMap qsm = new QuerySolutionMap();
        params.forEach(param->{
            Node path = param.getParameterPath();
            // Full path not supported (needs string-syntax rewrite)
            Node v = G.getOneSP(data.getGraph(), node, path);
            qsm.add(param.getSparqlName(), ModelUtils.convertGraphNodeToRDFNode(v, data));
        });
        return qsm;
    }

    private static void checkForRequiredParams(Graph data, Node node, SparqlComponent sparqlComponent) {
        if ( ! Parameters.doesShapeHaveAllParameters(data, node, sparqlComponent.getRequiredParameters()) ) {
            throw new ShaclException("Missing required parameter: "+node);
        }
    }
}
