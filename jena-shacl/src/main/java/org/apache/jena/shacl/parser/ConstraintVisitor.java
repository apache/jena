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

package org.apache.jena.shacl.parser;

import org.apache.jena.shacl.engine.constraint.*;

public interface ConstraintVisitor {
    void visit(SparqlConstraint constraint);
    void visit(ConstraintComponentSPARQL constraint);
    void visit(UniqueLangConstraint constraint);
    void visit(HasValueConstraint constraint);
    void visit(MinCount constraint);
    void visit(MaxCount constraint);
    void visit(ShXone constraint);
    void visit(ShAnd constraint);
    void visit(ShOr constraint);
    void visit(ShNot constraint);
    void visit(ShNode constraint);
    void visit(QualifiedValueShape constraint);
    void visit(LessThanOrEqualsConstraint constraint);
    void visit(DisjointConstraint constraint);
    void visit(EqualsConstraint constraint);
    void visit(LessThanConstraint constraint);
    void visit(ClosedConstraint constraint);
    void visit(ClassConstraint constraint);
    void visit(StrMaxLengthConstraint constraint);
    void visit(StrLanguageIn constraint);
    void visit(StrMinLengthConstraint constraint);
    void visit(JViolationConstraint constraint);
    void visit(DatatypeConstraint constraint);
    void visit(JLogConstraint constraint);
    void visit(PatternConstraint constraint);
    void visit(ValueMinExclusiveConstraint constraint);
    void visit(ValueMinInclusiveConstraint constraint);
    void visit(ValueMaxInclusiveConstraint constraint);
    void visit(ValueMaxExclusiveConstraint constraint);
    void visit(InConstraint constraint);
    void visit(NodeKindConstraint constraint);
}
