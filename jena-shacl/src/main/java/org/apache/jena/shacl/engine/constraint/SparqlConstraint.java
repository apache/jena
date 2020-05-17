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

import java.util.Set;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.Path;

/** SPARQL Constraint (ASK or SELECT) */
public class SparqlConstraint implements Constraint {

    private final Query query;
    private String message;
    static Var varValue = Var.alloc("value");
    // Output
    static Var varPath = Var.alloc("path");
    // Input substitution.
    static Var varPATH = Var.alloc("PATH");

    public SparqlConstraint(Query query, String message) {
        this.query = query;
        this.message = message; 
    }

    @Override
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        SparqlValidation.validate(vCxt, data, shape, focusNode, null, focusNode, query, null, message, this);
    }

    @Override
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape,
                                      Node focusNode, Path path, Set<Node> valueNodes) {
        valueNodes.forEach(vn->SparqlValidation.validate(vCxt, data, shape, focusNode, path, vn, query, null, message, this));
    }

    @Override
    public Node getComponent() {
        return SHACL.SPARQLConstraintComponent;
    }

    @Override
    public String toString() {
        IndentedLineBuffer out = new IndentedLineBuffer();
        out.setFlatMode(true);
        query.serialize(out);
        String x = out.asString();
        return "SPARQL["+x+"]";
    }
}
