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

package org.apache.jena.shex.expressions;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.sys.ValidationContext;

import java.util.List;
import java.util.Set;

public abstract class ShapeExpression implements ShapeElement {

    private List<SemAct> semActs;

    public ShapeExpression(List<SemAct> semActs) {
        this.semActs = semActs;
    }
    protected ShapeExpression() { this(null); }

    public List<SemAct> getSemActs() {
        return semActs;
    }

    @Override
    public abstract boolean satisfies(ValidationContext vCxt, Node data);

    public boolean testShapeExprSemanticActions(ValidationContext v, Node focus) {
        if (this.semActs == null)
            return true;
        return v.dispatchShapeExprSemanticAction(this, focus);
    }

    @Override
    public abstract void print(IndentedWriter out, NodeFormatter nFmt);

    public abstract void visit(ShapeExprVisitor visitor);

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
