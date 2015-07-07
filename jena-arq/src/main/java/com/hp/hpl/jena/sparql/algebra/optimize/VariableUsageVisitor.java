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

package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.ArrayList;
import java.util.Collection;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpPath;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpPropFunc;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadBlock;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.algebra.op.OpTopN;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.Vars;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVars;

/**
 * A visitor which tracks variable usage
 * 
 */
public abstract class VariableUsageVisitor extends OpVisitorBase {

    protected VariableUsageTracker tracker;

    public VariableUsageVisitor(VariableUsageTracker tracker) {
        this.tracker = tracker;
    }

    protected abstract void action(Collection<Var> vars);

    protected abstract void action(Var var);

    protected abstract void action(String var);

    @Override
    public void visit(OpBGP opBGP) {
        Collection<Var> vars = new ArrayList<>();
        for (Triple t : opBGP.getPattern().getList()) {
            Vars.addVarsFromTriple(vars, t);
        }
        action(vars);
    }

    @Override
    public void visit(OpQuadPattern quadPattern) {
        Collection<Var> vars = new ArrayList<>();
        for (Quad q : quadPattern.getPattern().getList()) {
            Vars.addVarsFromQuad(vars, q);
        }
        action(vars);
    }

    @Override
    public void visit(OpQuadBlock quadBlock) {
        Collection<Var> vars = new ArrayList<>();
        for (Quad q : quadBlock.getPattern().getList()) {
            Vars.addVarsFromQuad(vars, q);
        }
        action(vars);
    }

    @Override
    public void visit(OpPath opPath) {
        if (opPath.getTriplePath().getSubject().isVariable())
            action(opPath.getTriplePath().getSubject().getName());
        if (opPath.getTriplePath().getObject().isVariable())
            action(opPath.getTriplePath().getObject().getName());
    }

    @Override
    public void visit(OpPropFunc opPropFunc) {
        for (Node subjArg : opPropFunc.getSubjectArgs().getArgList()) {
            if (subjArg.isVariable())
                action(subjArg.getName());
        }
        for (Node objArg : opPropFunc.getObjectArgs().getArgList()) {
            if (objArg.isVariable())
                action(objArg.getName());
        }
    }

    @Override
    public void visit(OpLeftJoin opLeftJoin) {
        Collection<Var> vars = new ArrayList<>();
        if (opLeftJoin.getExprs() != null) {
            for (Expr expr : opLeftJoin.getExprs().getList()) {
                ExprVars.varsMentioned(vars, expr);
            }
        }
        action(vars);
    }

    @Override
    public void visit(OpFilter opFilter) {
        Collection<Var> vars = new ArrayList<>();
        for (Expr expr : opFilter.getExprs().getList()) {
            ExprVars.varsMentioned(vars, expr);
        }
        action(vars);
    }

    @Override
    public void visit(OpGraph opGraph) {
        if (opGraph.getNode().isVariable())
            action(opGraph.getNode().getName());
    }

    @Override
    public void visit(OpDatasetNames dsNames) {
        if (dsNames.getGraphNode().isVariable())
            action(dsNames.getGraphNode().getName());
    }

    @Override
    public void visit(OpTable opTable) {
        action(opTable.getTable().getVars());
    }

    @Override
    public void visit(OpAssign opAssign) {
        Collection<Var> vars = new ArrayList<>();
        for (Var var : opAssign.getVarExprList().getVars()) {
            vars.add(var);
            ExprVars.varsMentioned(vars, opAssign.getVarExprList().getExpr(var));
        }
        action(vars);
    }

    @Override
    public void visit(OpExtend opExtend) {
        Collection<Var> vars = new ArrayList<>();
        for (Var var : opExtend.getVarExprList().getVars()) {
            vars.add(var);
            ExprVars.varsMentioned(vars, opExtend.getVarExprList().getExpr(var));
        }
        action(vars);
    }

    @Override
    public void visit(OpOrder opOrder) {
        Collection<Var> vars = new ArrayList<>();
        for (SortCondition condition : opOrder.getConditions()) {
            ExprVars.varsMentioned(vars, condition);
        }
        action(vars);
    }

    @Override
    public void visit(OpProject opProject) {
        for (Var var : opProject.getVars()) {
            action(var);
        }
    }

    @Override
    public void visit(OpGroup opGroup) {
        Collection<Var> vars = new ArrayList<>();
        for (Var var : opGroup.getGroupVars().getVars()) {
            vars.add(var);
            ExprVars.varsMentioned(vars, opGroup.getGroupVars().getExpr(var));
        }
    }

    @Override
    public void visit(OpTopN opTop) {
        Collection<Var> vars = new ArrayList<>();
        for (SortCondition condition : opTop.getConditions()) {
            ExprVars.varsMentioned(vars, condition);
        }
        action(vars);
    }

}