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

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;

/** sh:minLength */
public class StrMinLengthConstraint extends ConstraintTerm {

    private final int minLength;

    public StrMinLengthConstraint(int minLength) {
        this.minLength = minLength;
    }

    public int getMinLength() {
        return minLength;
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Node n) {
        if ( n.isBlank() ) {
            String msg = toString()+": Blank node: "+ShLib.displayStr(n);
            return new ReportItem(msg, n);
        }
        String str = NodeFunctions.str(n);
        if ( str.length() >= minLength )
            return null;
        String msg = toString()+": String too short: "+str;
        return new ReportItem(msg, n);
    }

    @Override
    public Node getComponent() {
        return SHACL.MinLengthConstraintComponent;
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compact(out, "minLength", minLength);
    }

    @Override
    public String toString() {
        return "MinLengthConstraint["+minLength+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(minLength);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof StrMinLengthConstraint) )
            return false;
        StrMinLengthConstraint other = (StrMinLengthConstraint)obj;
        return minLength == other.minLength;
    }
}
