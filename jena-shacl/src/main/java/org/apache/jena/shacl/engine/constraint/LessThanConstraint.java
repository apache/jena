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

import static org.apache.jena.shacl.compact.writer.CompactOut.compact;
import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.Objects;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.path.Path;

/** sh:lessThan */
public class LessThanConstraint extends ConstraintPairwise {

    public LessThanConstraint(Node value) {
        super(value, SHACL.LessThanConstraintComponent);
    }

    @Override
    public void validate(ValidationContext vCxt, Shape shape, Node focusNode, Path path,
                         Set<Node> pathNodes, Set<Node> compareNodes) {
        for ( Node vn : pathNodes ) {
            for ( Node v : compareNodes ) {
                int r = super.compare(vn, v) ;
                if ( r != Expr.CMP_LESS ) {
                    String msg = toString()+": value node "+displayStr(vn)+" is not less than "+displayStr(v);
                    vCxt.reportEntry(msg, shape, focusNode, path, vn, this);
                }
            }
        }
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compact(out, nodeFmt, "lessThan", value);
    }

    @Override
    public String toString() {
        return "LessThan["+displayStr(value)+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, 3);
    }
}
