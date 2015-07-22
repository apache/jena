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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpExtendAssign;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;

/**
 * A transform capable of removing assignments from the algebra tree
 * 
 */
public class TransformRemoveAssignment extends TransformCopy {

    private Var var;
    private Expr expr;
    private boolean topmostOnly = true;
    private boolean aboveExtend = false;

    public TransformRemoveAssignment(Var var, Expr expr, boolean topmostOnly) {
        this.var = var;
        this.expr = expr;
        this.topmostOnly = topmostOnly;
    }

    public TransformRemoveAssignment(Var var, Expr expr) {
        this(var, expr, true);
    }

    @Override
    public Op transform(OpAssign opAssign, Op subOp) {
        VarExprList assignments = processAssignments(opAssign);
        if (assignments == null)
            return super.transform(opAssign, subOp);

        // Rewrite appropriately
        if (this.topmostOnly) {
            // If topmost only ignore any transformations lower down the tree
            // hence call getSubOp() rather than using the provided subOp
            if (assignments.size() > 0) {
                return OpAssign.assign(opAssign.getSubOp(), assignments);
            } else {
                return opAssign.getSubOp();
            }
        } else {
            // Otherwise preserve any transformations from lower down the tree
            if (assignments.size() > 0) {
                return OpAssign.assign(subOp, assignments);
            } else {
                return subOp;
            }
        }
    }

    private VarExprList processAssignments(OpExtendAssign opAssign) {
        VarExprList orig = opAssign.getVarExprList();
        if (!orig.contains(this.var))
            return null;
        if (!orig.getExpr(this.var).equals(this.expr))
            return null;

        VarExprList modified = new VarExprList();
        for (Var v : orig.getVars()) {
            if (!v.equals(this.var)) {
                modified.add(v, orig.getExpr(v));
            }
        }
        if (modified.size() > 0 && modified.size() == orig.size())
            return null;
        
        return modified;
    }

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        VarExprList assignments = processAssignments(opExtend);
        if (assignments == null)
            return super.transform(opExtend, subOp);
        
        this.aboveExtend = true;

        // Rewrite appropriately
        if (this.topmostOnly) {
            // If topmost only ignore any transformations lower down the tree
            // hence call getSubOp() rather than using the provided subOp
            if (assignments.size() > 0) {
                return OpExtend.create(opExtend.getSubOp(), assignments);
            } else {
                return opExtend.getSubOp();
            }
        } else {
            // Otherwise preserve any transformations from lower down the tree
            if (assignments.size() > 0) {
                return OpExtend.create(subOp, assignments);
            } else {
                return subOp;
            }
        }
    }

    @Override
    public Op transform(OpProject opProject, Op subOp) {
        if (!opProject.getVars().contains(this.var))
            return super.transform(opProject, subOp);
        
        List<Var> newVars = new ArrayList<Var>(opProject.getVars());
        newVars.remove(this.var);
        if (this.topmostOnly) {
            if (this.aboveExtend) {
                return new OpProject(subOp, newVars);
            } else {
                return opProject;
            }
        } else {
            return new OpProject(subOp, newVars);
        }
    }
}
