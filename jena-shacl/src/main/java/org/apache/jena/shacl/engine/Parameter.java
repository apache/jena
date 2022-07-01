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

package org.apache.jena.shacl.engine;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.shacl.parser.Constraint;

public class Parameter {

    private final Node             parameterPath;
    private final String           sparqlName;
    private final boolean          isOptional;
    private final List<Constraint> constraints;

    public Parameter(Node parameterPath, String sparqlName, boolean isOptional, List<Constraint> constraints) {
        this.parameterPath = parameterPath;
        this.sparqlName = sparqlName;
        this.isOptional = isOptional;
        this.constraints = constraints;
    }

    public Node getParameterPath() {
        return parameterPath;
    }

    public String getSparqlName() {
        return sparqlName;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    @Override
    public String toString() {
        return "Param["+sparqlName+"]";
    }
}
