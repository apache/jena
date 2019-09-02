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

import org.apache.jena.graph.Node;
import org.apache.jena.shacl.engine.Parameter;

public class SparqlComponent {

    private final Node reportNode;
    private final String sparqlString;
    private final boolean isSelect;
    private final List<Parameter> params;
    private final List<Node> requiredParameters;
    private final List<Node> optionalParameters;

    public SparqlComponent(Node reportNode, boolean isSelect, String sparqlString, List<Parameter> params) {
        this.reportNode = reportNode;
        this.sparqlString = sparqlString;
        this.isSelect = isSelect;
        this.params = params;
        this.requiredParameters = params.stream()
            .filter(param->!param.isOptional())
            .map(param->param.getParameterPath())
            .collect(toList());
        this.optionalParameters = params.stream()
            .filter(param->param.isOptional())
            .map(param->param.getParameterPath())
            .collect(toList());
    }

    public Node getReportComponent() {
        return reportNode;
    }

    public String getSparqlString() {
        return sparqlString;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public List<Parameter> getParams() {
        return params;
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
        if ( isSelect )
            sbuff.append("SELECT:");
        else
            sbuff.append("ASK:");
        sbuff.append(params.toString());
        return sbuff.toString();
    }
}
