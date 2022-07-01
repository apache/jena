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

import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.parser.Shape;

/** A constraint that combines N other constraints */
public abstract class ConstraintOpN extends ConstraintOp {

    protected  final List<Shape> others;

    protected ConstraintOpN(List<Shape> subShapes) {
        others = subShapes;
    }

    public List<Shape> getOthers() {
        return Collections.unmodifiableList(others);
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nodeFmt) {
        out.print(toString());
        out.ensureStartOfLine();
        out.incIndent();
        for ( Shape sub: others ) {
            sub.print(out, nodeFmt);
            out.ensureStartOfLine();
        }
        out.decIndent();
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        super.printCompact(out, nodeFmt);
    }
}
