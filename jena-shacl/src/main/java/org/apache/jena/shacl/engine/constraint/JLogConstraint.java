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

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.sys.ShaclSystem;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHJ;

/** A constraint that logs when touched but does not causes a violation */
public class JLogConstraint extends ConstraintTerm {

    private final String message;

    public JLogConstraint(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Node getComponent() {
        return SHJ.LogConstraintComponent;
    }

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Node n) {
        String msg = String.format("%s[%s]", message, ShLib.displayStr(n));
        ShaclSystem.systemShaclLogger.warn(msg);
        return null;
    }

    @Override
    public String toString() {
        return "Log["+message+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        JLogConstraint other = (JLogConstraint)obj;
        return Objects.equals(message, other.message);
    }
}

