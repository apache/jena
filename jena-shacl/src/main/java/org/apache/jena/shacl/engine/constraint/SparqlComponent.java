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

package org.apache.jena.shacl.engine.constraint;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.shacl.engine.Parameter;
import org.apache.jena.shacl.lib.ShLib;

/** A component used for SPARQL-based constraint-components and SPARQL Target types.
 *  It is a SPARQL query together with its SHACL parameters.
 *  <p>
 *  Constraint components additionally have reporting information.
 */
public class SparqlComponent {

    private final String sparqlString;
    private final List<Parameter> params;
    private final List<Node> requiredParameters;
    private final List<Node> optionalParameters;
    // Use by constraint-components
    private final Node reportNode;
    private final String message;
    private final Query query;

    public static SparqlComponent constraintComponent(Node reportNode, String sparqlString, List<Parameter> params, String message) {
        return new SparqlComponent(sparqlString, params, reportNode, message);
    }

    public static SparqlComponent targetType(String sparqlString, List<Parameter> params) {
        return new SparqlComponent(sparqlString, params, null, null);
    }

    private SparqlComponent(String sparqlString, List<Parameter> params, Node reportNode, String message) {
        this.params = params;
        this.message = message;
        this.requiredParameters = params.stream()
            .filter(param->!param.isOptional())
            .map(param->param.getParameterPath())
            .collect(toList());
        this.optionalParameters = params.stream()
            .filter(param->param.isOptional())
            .map(param->param.getParameterPath())
            .collect(toList());
        this.reportNode = reportNode;
        this.sparqlString = sparqlString;
        this.query = ShLib.parseQueryString(sparqlString);
    }

    public Node getReportComponent() {
        return reportNode;
    }

    public Query getQuery() {
        return query;
    }

//    public String getSparqlString() {
//        return sparqlString;
//    }
//
//    public boolean isSelect() {
//        return query.isSelectType();
//    }

    public List<Parameter> getParams() {
        return params;
    }

    public String getMessage() {
        return message;
    }

    public List<Node> getRequiredParameters() {
        return requiredParameters;
    }

    public List<Node> getOptionalParameters() {
        return optionalParameters;
    }

    @Override
    public String toString() {
        StringBuilder sbuff = new StringBuilder();
        if ( query.isSelectType())
            sbuff.append("SELECT:");
        else if ( query.isAskType() )
            sbuff.append("ASK:");
        else
            sbuff.append("???:");
        sbuff.append(params.toString());
        return sbuff.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, optionalParameters, params, query, reportNode, requiredParameters, sparqlString);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SparqlComponent other = (SparqlComponent)obj;
        return Objects.equals(message, other.message) && Objects.equals(optionalParameters, other.optionalParameters)
               && Objects.equals(params, other.params) && Objects.equals(query, other.query) && Objects.equals(reportNode, other.reportNode)
               && Objects.equals(requiredParameters, other.requiredParameters) && Objects.equals(sparqlString, other.sparqlString);
    }
}
