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

package org.apache.jena.sparql.algebra;

import static org.apache.jena.sparql.core.Vars.addVar;

import java.util.*;

import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.OpWalker.OpWalkerVisitor;
//import org.apache.jena.sparql.algebra.walker.WalkerVisitor;
import org.apache.jena.sparql.algebra.op.*;
//import org.apache.jena.sparql.algebra.walker.WalkerVisitorVisible;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.util.VarUtils;

/** Get vars for a pattern */

public class OpVars
{
    // Choose the default collector - LinkedHashSet is predictable and
    // keeps the "found" order
    private static Set<Var> collector() {
        return new LinkedHashSet<>();
    }

    public static Set<Var> visibleVars(Op op) {
        Set<Var> acc = collector();
        visibleVars(op, acc);
        return acc;
    }

    public static void visibleVars(Op op, Set<Var> acc) {
        OpVarsPattern visitor = new OpVarsPattern(acc, true);
        // Does not work yet for new walker.
        OpWalker.walk(new OpWalkerVisitorVisible(visitor, acc), op);
    }

    /** The set of variables that will be in every solution of this Op */
    public static Set<Var> fixedVars(Op op) {
        Set<Var> acc = collector();
        fixedVars(op, acc);
        return acc;
    }

    public static void fixedVars(Op op, Set<Var> acc) {
        OpVarsPattern visitor = new OpVarsPattern(acc, true);
        OpWalker.walk(new OpWalkerVisitorFixed(visitor, acc), op);
    }

    public static Tuple<Set<Var>> mentionedVarsByPosition(Op op) {
        Set<Var> graphAcc = collector();
        Set<Var> subjAcc = collector();
        Set<Var> predAcc = collector();
        Set<Var> objAcc = collector();
        Set<Var> unknownAcc = collector();
        OpVarsPatternWithPositions visitor = new OpVarsPatternWithPositions(graphAcc, subjAcc, predAcc, objAcc, unknownAcc, false);
        OpWalker.walk(op, visitor);
        return TupleFactory.tuple(graphAcc, subjAcc, predAcc, objAcc, unknownAcc);
    }

    public static Tuple<Set<Var>> mentionedVarsByPosition(Op... ops) {
        Set<Var> graphAcc = collector();
        Set<Var> subjAcc = collector();
        Set<Var> predAcc = collector();
        Set<Var> objAcc = collector();
        Set<Var> unknownAcc = collector();
        OpVarsPatternWithPositions visitor = new OpVarsPatternWithPositions(graphAcc, subjAcc, predAcc, objAcc, unknownAcc, false);
        for (Op op : ops)
            OpWalker.walk(op, visitor);
        return TupleFactory.tuple(graphAcc, subjAcc, predAcc, objAcc, unknownAcc);
    }

    // All mentioned variables regardless of scope/visibility.
    public static Collection<Var> mentionedVars(Op op) {
        Set<Var> acc = collector();
        mentionedVars(op, acc);
        return acc;
    }

    // All mentioned variables regardless of scope/visibility.
    public static void mentionedVars(Op op, Set<Var> acc) {
        OpVarsMentioned visitor = new OpVarsMentioned(acc);
        OpWalker.walk(op, visitor);
    }

    private static void accVarsFromGroup(Collection<Var> acc, OpGroup opGroup) {
        VarExprList groupByVars  = opGroup.getGroupVars();
        groupByVars.forEachVarExpr((var, expr)->acc.add(var));

        List<ExprAggregator> aggs = opGroup.getAggregators();
        aggs.forEach(exprAgg -> {
            Var v = exprAgg.getVar();
            acc.add(v);
        });
    }

    /**
     * Walk the visible part of an algebra expression
     * (the parts that set variables).
     * Sub-ops of project, and group, and the RHS of MINUS aren't visible
     */
    private static class OpWalkerVisitorVisible extends OpWalkerVisitor
    {
        private final Collection<Var> acc;

        public OpWalkerVisitorVisible(OpVarsPattern visitor, Collection<Var> acc) {
            super(visitor);
            this.acc = acc;
        }

        @Override
        public void visit(OpProject op) {
            before(op);
            // Skip Project subop.
            acc.addAll(op.getVars());
            after(op);
        }

        @Override
        public void visit(OpMinus op) {
            before(op);
            if (op.getLeft() != null)
                op.getLeft().visit(this);
            // Skip right.
            // if ( op.getRight() != null ) op.getRight().visit(this);
            if (visitor != null)
                op.visit(visitor);
            after(op);
        }

        @Override public void visit(OpGroup op) {
            // Do not walk inside the group
            before(op);
            if (visitor != null)
                op.visit(visitor);
            after(op);
        }
    }

    // Only consider variables that are visible and definitely defined.
    // OPTIONAL (2 forms) and UNION are the interesting cases.
    private static class OpWalkerVisitorFixed extends OpWalkerVisitor
    {
        private final Collection<Var> acc;

        public OpWalkerVisitorFixed(OpVarsPattern visitor, Collection<Var> acc) {
            //super(visitor, null, null, null);
            super(visitor);
            this.acc = acc;
        }

        @Override
        public void visit(OpLeftJoin x) {
            x.getLeft().visit(this);
        }

        @Override
        public void visit(OpConditional x) {
            x.getLeft().visit(this);
        }

        @Override
        public void visit(OpUnion x) {
            Set<Var> left = fixedVars(x.getLeft());
            Set<Var> right = fixedVars(x.getRight());
            Set<Var> r = SetUtils.intersection(left,  right);
            acc.addAll(r);
        }

        @Override
        public void visit(OpProject op) {
            before(op);
            // Skip Project subop.
            acc.addAll(op.getVars());
            after(op);
        }

        @Override
        public void visit(OpMinus op) {
            before(op);
            if (op.getLeft() != null)
                op.getLeft().visit(this);
            // Skip right.
            // if ( op.getRight() != null ) op.getRight().visit(this);
//            if (opVisitor != null)
//                op.visit(opVisitor);
            if (visitor != null)
                op.visit(visitor);
            after(op);
        }

        @Override public void visit(OpGroup op) {
            // Do not walk inside the group
            before(op);
            if (visitor != null)
                op.visit(visitor);
            after(op);
        }
    }

    /**
     * Collect variables. What to collect from is controlled by the walker e.g.
     * OpWalkerVisitorFixed, OpWalkerVisitorVisible and for OpVarsMentioned, the full
     * geenral walker.
     */
    private static class OpVarsPattern extends OpVisitorBase {
        // The possibly-set-vars
        protected Set<Var> acc;
        final boolean visibleOnly;

        OpVarsPattern(Set<Var> acc, boolean visibleOnly) {
            this.acc = acc;
            this.visibleOnly = visibleOnly;
        }

        @Override
        public void visit(OpBGP opBGP) {
            VarUtils.addVars(acc, opBGP.getPattern());
        }

        @Override
        public void visit(OpPath opPath) {
            addVar(acc, opPath.getTriplePath().getSubject());
            addVar(acc, opPath.getTriplePath().getObject());
        }

        @Override
        public void visit(OpQuadPattern quadPattern) {
            addVar(acc, quadPattern.getGraphNode());
            VarUtils.addVars(acc, quadPattern.getBasicPattern());
        }

        @Override
        public void visit(OpQuadBlock quadBlock) {
            VarUtils.addVars(acc, quadBlock.getPattern());
        }

        @Override
        public void visit(OpTriple opTriple) {
            VarUtils.addVarsFromTriple(acc, opTriple.getTriple());
        }

        @Override
        public void visit(OpQuad opQuad) {
            VarUtils.addVarsFromQuad(acc, opQuad.getQuad());
        }

        @Override
        public void visit(OpGraph opGraph) {
            addVar(acc, opGraph.getNode());
        }

        @Override
        public void visit(OpDatasetNames dsNames) {
            addVar(acc, dsNames.getGraphNode());
        }

        @Override
        public void visit(OpTable opTable) {
            // Only the variables with values in the tables (When building,
            // undefs didn't get into bindings so no variable mentioned)
            Table t = opTable.getTable();
            acc.addAll(t.getVars());
        }

        @Override
        public void visit(OpProject opProject) {
            // The walker (WalkerVisitorVisible) handles this
            // for visible variables, not mentioned variable collecting.
            // The visibleOnly/clear is simply to be as general as possible.
            // visible: OpWalkerVisitorFixed, OpWalkerVisitorVisible,
            // all (visibleOnly==false) for OpVarsMentioned
            if ( visibleOnly )
                acc.clear();
            acc.addAll(opProject.getVars());
        }

        @Override
        public void visit(OpAssign opAssign) {
            acc.addAll(opAssign.getVarExprList().getVars());
        }

        @Override
        public void visit(OpExtend opExtend) {
            acc.addAll(opExtend.getVarExprList().getVars());
        }

        @Override
        public void visit(OpPropFunc opPropFunc) {
            PropFuncArg.addVars(acc, opPropFunc.getSubjectArgs());
            PropFuncArg.addVars(acc, opPropFunc.getObjectArgs());
        }

        @Override
        public void visit(OpProcedure opProc) {
            ExprVars.varsMentioned(acc, opProc.getArgs());
        }

        @Override
        public void visit(OpExt opExt) {
            // OpWalkerVisitor is taking care of calling
            // opExt.effectiveOp().visit(this)
        }

        @Override
        public void visit(OpGroup opGroup) {
            accVarsFromGroup(acc, opGroup);

        }

    }

    private static class OpVarsPatternWithPositions extends OpVisitorBase {
        // The possibly-set-vars
        protected Set<Var> graphAcc, subjAcc, predAcc, objAcc, unknownAcc;
        final boolean visibleOnly;

        OpVarsPatternWithPositions(Set<Var> graphAcc, Set<Var> subjAcc, Set<Var> predAcc, Set<Var> objAcc, Set<Var> unknownAcc,
                                   boolean visibleOnly) {
            this.graphAcc = graphAcc;
            this.subjAcc = subjAcc;
            this.predAcc = predAcc;
            this.objAcc = objAcc;
            this.unknownAcc = unknownAcc;
            this.visibleOnly = visibleOnly;
        }

        @Override
        public void visit(OpBGP opBGP) {
            vars(opBGP.getPattern());
        }

        @Override
        public void visit(OpPath opPath) {
            addVar(subjAcc, opPath.getTriplePath().getSubject());
            addVar(objAcc, opPath.getTriplePath().getObject());
        }

        @Override
        public void visit(OpQuadPattern quadPattern) {
            addVar(graphAcc, quadPattern.getGraphNode());
            vars(quadPattern.getBasicPattern());
        }

        @Override
        public void visit(OpGraph opGraph) {
            addVar(graphAcc, opGraph.getNode());
        }

        @Override
        public void visit(OpDatasetNames dsNames) {
            addVar(graphAcc, dsNames.getGraphNode());
        }

        @Override
        public void visit(OpTable opTable) {
            // Only the variables with values in the tables
            // (When building, undefs didn't get into bindings so no variable
            // mentioned)
            Table t = opTable.getTable();
            // Treat as unknown position
            unknownAcc.addAll(t.getVars());
        }

        @Override
        public void visit(OpProject opProject) {
            // The walker (WalkerVisitorVisible) handles this
            // for visible variables, not mentioned variable collecting.
            // The visibleOnly/clear is simply to be as general as possible.
            List<Var> vs = opProject.getVars();
            merge(vs);
        }

        @Override
        public void visit(OpGroup opGroup) {
            List<Var> vs = new ArrayList<>();
            accVarsFromGroup(vs, opGroup);
            merge(vs);
        }

        private void merge(List<Var> vs) {
            if ( visibleOnly ) {
                clear(graphAcc, vs);
                clear(subjAcc, vs);
                clear(predAcc, vs);
                clear(objAcc, vs);
            }
            for ( Var v : vs ) {
                if ( !graphAcc.contains(v) && !subjAcc.contains(v) && !predAcc.contains(v) && !objAcc.contains(v) ) {
                    addVar(unknownAcc, v);
                }
            }
        }

        @Override
        public void visit(OpAssign opAssign) {
            // Unknown position
            unknownAcc.addAll(opAssign.getVarExprList().getVars());
        }

        @Override
        public void visit(OpExtend opExtend) {
            // Unknown position
            unknownAcc.addAll(opExtend.getVarExprList().getVars());
        }

        @Override
        public void visit(OpPropFunc opPropFunc) {
            addvars(subjAcc, opPropFunc.getSubjectArgs());
            addvars(objAcc, opPropFunc.getObjectArgs());
        }

        private void addvars(Set<Var> acc, PropFuncArg pfArg) {
            if ( pfArg.isNode() ) {
                addVar(acc, pfArg.getArg());
                return;
            }
            for ( Node n : pfArg.getArgList() )
                addVar(acc, n);
        }

        @Override
        public void visit(OpProcedure opProc) {
            unknownAcc.addAll(OpVars.mentionedVars(opProc));
        }

        private void vars(BasicPattern bp) {
            for ( Triple t : bp.getList() ) {
                addVar(subjAcc, t.getSubject());
                addVar(predAcc, t.getPredicate());
                addVar(objAcc, t.getObject());
            }
        }

        private void clear(Set<Var> acc, List<Var> visible) {
            List<Var> toRemove = new ArrayList<>();
            for ( Var found : acc ) {
                if ( !visible.contains(found) ) {
                    toRemove.add(found);
                }
            }
            for ( Var v : toRemove ) {
                acc.remove(v);
            }
        }
    }

    private static class OpVarsMentioned extends OpVarsPattern {
        OpVarsMentioned(Set<Var> acc) {
            super(acc, false);
        }

        @Override
        public void visit(OpFilter opFilter) {
            ExprVars.varsMentioned(acc, opFilter.getExprs());
        }

        @Override
        public void visit(OpOrder opOrder) {
            ExprVars.varsMentioned(acc, opOrder.getConditions());
        }

        @Override
        public void visit(OpExtend opExtend) {
            visitExtendAssign(opExtend);
        }

        @Override
        public void visit(OpAssign opAssign) {
            visitExtendAssign(opAssign);
        }

        // The expression side of (extend) and (assign)
        private void visitExtendAssign(OpExtendAssign op) {
            op.getVarExprList().forEachExpr((v, e) -> ExprVars.varsMentioned(acc, e));
        }
    }
}
