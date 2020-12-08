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

public abstract class ConstraintVisitorBase implements ConstraintVisitor {
    @Override
    public void visit(ClassConstraint constraint) {}

    @Override
    public void visit(DatatypeConstraint constraint) {}

    @Override
    public void visit(NodeKindConstraint constraint) {}

    @Override
    public void visit(MinCount constraint) {}

    @Override
    public void visit(MaxCount constraint) {}

    @Override
    public void visit(ValueMinExclusiveConstraint constraint) {}

    @Override
    public void visit(ValueMinInclusiveConstraint constraint) {}

    @Override
    public void visit(ValueMaxInclusiveConstraint constraint) {}

    @Override
    public void visit(ValueMaxExclusiveConstraint constraint) {}

    @Override
    public void visit(StrMinLengthConstraint constraint) {}

    @Override
    public void visit(StrMaxLengthConstraint constraint) {}

    @Override
    public void visit(PatternConstraint constraint) {}

    @Override
    public void visit(StrLanguageIn constraint) {}

    @Override
    public void visit(UniqueLangConstraint constraint) {}

    @Override
    public void visit(EqualsConstraint constraint) {}

    @Override
    public void visit(DisjointConstraint constraint) {}

    @Override
    public void visit(LessThanConstraint constraint) {}

    @Override
    public void visit(LessThanOrEqualsConstraint constraint) {}

    @Override
    public void visit(ShNot constraint) {}

    @Override
    public void visit(ShAnd constraint) {}

    @Override
    public void visit(ShOr constraint) {}

    @Override
    public void visit(ShXone constraint) {}

    @Override
    public void visit(ShNode constraint) {}

    @Override
    public void visit(QualifiedValueShape constraint) {}

    @Override
    public void visit(ClosedConstraint constraint) {}

    @Override
    public void visit(HasValueConstraint constraint) {}

    @Override
    public void visit(InConstraint constraint) {}

    @Override
    public void visit(ConstraintComponentSPARQL constraint) {}

    @Override
    public void visit(SparqlConstraint constraint) {}

    @Override
    public void visit(JViolationConstraint constraint) {}

    @Override
    public void visit(JLogConstraint constraint) {}
}
