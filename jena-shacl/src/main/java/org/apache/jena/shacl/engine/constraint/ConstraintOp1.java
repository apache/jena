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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.parser.Shape;

/** A constraint that operates on one other constraints */
public abstract class ConstraintOp1 extends ConstraintOp {

    protected  final Shape other;

    protected ConstraintOp1(Shape subShape) {
        other = subShape;
    }

    public Shape getOther() {
        return other;
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nodeFmt) {
        out.print(toString());
        out.ensureStartOfLine();
        out.incIndent();
        other.print(out, nodeFmt);
        out.decIndent();
        out.ensureStartOfLine();
    }
}
