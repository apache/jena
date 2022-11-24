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

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.sys.ShexLib;

public class TripleExprRef extends TripleExpression {

    private Node ref;

    public TripleExprRef(Node node) {
        super(null);
        this.ref = node;
    }

    public Node ref() {
        return ref;
    }

    @Override
    public void visit(TripleExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        TripleExprRef other = (TripleExprRef)obj;
        return Objects.equals(ref, other.ref);
    }

    @Override
    public void print(IndentedWriter iOut, NodeFormatter nFmt) {
        iOut.print("tripleExprRef: ");
        nFmt.format(iOut, ref);
        iOut.println();
    }

    @Override
    public String toString() {
        return "TripleExpressionRef["+ShexLib.displayStr(ref)+"]";
    }
}
