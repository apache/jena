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

package org.apache.jena.shacl.engine.exec;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.engine.constraint.ConstraintTerm;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;

public class TripleValidator {

    private final ValidationOp validationOp;
    private final ConstraintTerm ct;
    private final Target target;

    //Target and Constraint?
    public TripleValidator(ValidationOp validationOp) {
        this.validationOp = validationOp;
        this.ct = (ConstraintTerm)validationOp.getConstraint();
        this.target = validationOp.getTarget();
   }

    public ReportItem validate(ValidationContext vCtx, Triple triple) {
        Node focusNode = focusNode(triple, validationOp);
        if ( focusNode == null )
            return null;
        ConstraintTerm ct = (ConstraintTerm)validationOp.getConstraint();
        return ct.validate(vCtx, focusNode);
    }

    // XXX Move.
    public static Node focusNode(Triple triple, ValidationOp validationOp) {
        Target target = validationOp.getTarget();
        switch(target.getTargetType()) {
            //case targetClass :
            case targetNode :
                return target.getObject();
            case targetObjectsOf :
                return triple.getObject();
            case targetSubjectsOf : {
                Path path = validationOp.getPath();
                if ( path != null ) {
                    if ( !( path instanceof P_Link ) )
                        throw new ShaclException("Not a predicate path: "+path);
                    return triple.getObject();
                }
                return triple.getSubject();
            }
            default :
        }
        return null;
    }

    public Shape getShape() {
        return validationOp.getShape();
    }

    public Path getPath() {
        return validationOp.getPath();
    }

    public Target getTarget() {
        return validationOp.getTarget();
    }

    public Node getFocusNode(Triple triple) {
        return ShLib.focusNode(triple, target);
    }

    public Constraint getConstraint() {
        return validationOp.getConstraint();
    }
}
