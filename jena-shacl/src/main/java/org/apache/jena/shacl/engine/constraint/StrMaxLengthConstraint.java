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
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;

/** sh:maxLength */
public class StrMaxLengthConstraint extends ConstraintTerm {

    private final int maxLength;

    public StrMaxLengthConstraint(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Node n) {
        if ( n.isBlank() ) {
            String msg = toString()+": Blank node: "+ShLib.displayStr(n);
            return new ReportItem(msg, n);
        }
        String str = NodeFunctions.str(n);
        if ( str.length() <= maxLength )
            return null;
        String msg = toString()+": String too long: "+str ;
        return new ReportItem(msg,n);
    }

    @Override
    public Node getComponent() {
        return SHACL.MaxLengthConstraintComponent;
    }

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compact(out, "maxLength", maxLength);
    }

    @Override
    public String toString() {
        return "MaxLengthConstraint["+maxLength+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxLength);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof StrMaxLengthConstraint other) )
            return false;
        return maxLength == other.maxLength;
    }
}
