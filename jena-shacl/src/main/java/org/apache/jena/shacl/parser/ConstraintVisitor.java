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
    default void visit(ReportConstraint constraint){}
    default void visit(SparqlConstraint constraint){}
    default void visit(ConstraintComponentSPARQL constraint){}
    default void visit(UniqueLangConstraint constraint){}
    default void visit(HasValueConstraint constraint){}
    default void visit(MinCount constraint){}
    default void visit(MaxCount constraint){}
    default void visit(ShXone constraint){}
    default void visit(ShAnd constraint){}
    default void visit(ShOr constraint){}
    default void visit(ShNot constraint){}
    default void visit(ShNode constraint){}
    default void visit(QualifiedValueShape constraint){}
    default void visit(LessThanOrEqualsConstraint constraint){}
    default void visit(DisjointConstraint constraint){}
    default void visit(EqualsConstraint constraint){}
    default void visit(LessThanConstraint constraint){}
    default void visit(ClosedConstraint constraint){}
    default void visit(ClassConstraint constraint){}
    default void visit(StrMaxLengthConstraint constraint){}
    default void visit(StrLanguageIn constraint){}
    default void visit(StrMinLengthConstraint constraint){}
    default void visit(JViolationConstraint constraint){}
    default void visit(DatatypeConstraint constraint){}
    default void visit(JLogConstraint constraint){}
    default void visit(PatternConstraint constraint){}
    default void visit(ValueMinExclusiveConstraint constraint){}
    default void visit(ValueMinInclusiveConstraint constraint){}
    default void visit(ValueMaxInclusiveConstraint constraint){}
    default void visit(ValueMaxExclusiveConstraint constraint){}
    default void visit(InConstraint constraint){}
    default void visit(NodeKindConstraint constraint){}
}
