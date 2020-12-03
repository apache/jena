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
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.compact.writer.CompactOut;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHJ;

/** A constraint that causes a violation if it's object is "true" */
public class JViolationConstraint extends ConstraintTerm {

    private final boolean generateViolation;

    public JViolationConstraint(boolean generateViolation) {
        this.generateViolation = generateViolation;
    }

    public boolean generatesViolation() {
        return generateViolation;
    }

    @Override
    public Node getComponent() {
        return SHJ.ViolationConstraintComponent;
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Node n) {
        if ( ! generateViolation )
            return null;
        return new ReportItem("Violation");
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        CompactOut.compactUnquotedString(out, "violation", Boolean.toString(generateViolation));
    }

    @Override
    public String toString() {
        return "Violation["+generateViolation+"]";
    }

    @Override
    public int hashCode() {
        return 158+(generateViolation?1:2);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        JViolationConstraint other = (JViolationConstraint)obj;
        return generateViolation == other.generateViolation;
    }
}

