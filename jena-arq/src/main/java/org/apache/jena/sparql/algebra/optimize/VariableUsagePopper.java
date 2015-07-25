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

package org.apache.jena.sparql.algebra.optimize;

import java.util.Collection;

import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;

/**
 * An after visitor for tracking variable usage
 * 
 */
public class VariableUsagePopper extends VariableUsageVisitor {

    public VariableUsagePopper(VariableUsageTracker tracker) {
        super(tracker);
    }

    @Override
    protected void action(Collection<Var> vars) {
        this.tracker.decrement(vars);
    }

    @Override
    protected void action(Var var) {
        this.tracker.decrement(var);
    }

    @Override
    protected void action(String var) {
        this.tracker.decrement(var);
    }

    @Override
    public void visit(OpProject opProject) {
        super.visit(opProject);
        this.tracker.pop();
        super.visit(opProject);
    }
}