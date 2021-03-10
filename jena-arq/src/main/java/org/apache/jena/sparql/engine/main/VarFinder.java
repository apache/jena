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

package org.apache.jena.sparql.engine.main;

import static org.apache.jena.sparql.util.VarUtils.addVar ;
import static org.apache.jena.sparql.util.VarUtils.addVars ;
import static org.apache.jena.sparql.util.VarUtils.addVarsFromQuad ;
import static org.apache.jena.sparql.util.VarUtils.addVarsFromTriple ;
import static org.apache.jena.sparql.util.VarUtils.addVarsFromTriplePath ;

import java.io.PrintStream ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.lib.SetUtils ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.util.VarUtils ;

public class VarFinder
{
    public static VarFinder process(Op op) {
        return new VarFinder(op) ;
    }

    // See also VarUtils and OpVars.
    // This class is specific to the needs of the main query engine and scoping of variables

    public static Set<Var> optDefined(Op op) {
        return VarUsageVisitor.apply(op).optDefines;
    }

    public static Set<Var> fixed(Op op) {
        return VarUsageVisitor.apply(op).defines;
    }

    public static Set<Var> filter(Op op) {
        return VarUsageVisitor.apply(op).filterMentions;
    }

    public static Set<Var> assignMention(Op op) {
        return VarUsageVisitor.apply(op).assignMentions;
    }

    VarUsageVisitor varUsageVisitor ;

    private VarFinder(Op op)
    { varUsageVisitor = VarUsageVisitor.apply(op) ; }

    public Set<Var> getOpt()        { return varUsageVisitor.optDefines ; }
    public Set<Var> getFilter()     { return varUsageVisitor.filterMentions ; }
    public Set<Var> getFilterOnly() { return varUsageVisitor.filterMentionsOnly ; }
    public Set<Var> getAssign()     { return varUsageVisitor.assignMentions ; }
    public Set<Var> getFixed()      { return varUsageVisitor.defines ; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append("Fixed:").append(getFixed()) ;
        sb.append(", Filter:").append(getFilter()) ;
        sb.append(", Filter2:").append(getFilterOnly()) ;
        sb.append(", Opt:").append(getOpt()) ;
        sb.append(", Assign:").append(getAssign()) ;
        return sb.toString() ;
    }

    public void print(PrintStream out) {
        out.printf("  Filter:       %s\n", getFilter()) ;
        out.printf("  Filter only:  %s\n", getFilterOnly()) ;
        out.printf("  Fixed :       %s\n", getFixed()) ;
        out.printf("  Opt:          %s\n", getOpt()) ;
        out.printf("  Assign:       %s\n", getAssign()) ;
    }

    private static class VarUsageVisitor
        //extends OpVisitorBase
        implements OpVisitor
    {
        static VarUsageVisitor apply(Op op) {
            VarUsageVisitor v = new VarUsageVisitor();
            op.visit(v);
            return v;
        }

        // Fixed by pattern
        Set<Var> defines            = null ;
        // Fixed in optional
        Set<Var> optDefines         = null ;
        // Used in a filter
        Set<Var> filterMentions     = null ;
        // Used in a filter, before defined
        Set<Var> filterMentionsOnly = null ;
        // Used in assign or extend expression
        Set<Var> assignMentions     = null ;

        VarUsageVisitor() {
            defines = new HashSet<>();
            optDefines = new HashSet<>();
            filterMentions = new HashSet<>();
            filterMentionsOnly = new HashSet<>();
            assignMentions = new HashSet<>();
        }

        VarUsageVisitor(Set<Var> _defines, Set<Var> _optDefines, Set<Var> _filterMentions, Set<Var> _filterMentions2, Set<Var> _assignMentions) {
            defines = _defines;
            optDefines = _optDefines;
            filterMentions = _filterMentions;
            filterMentionsOnly = _filterMentions2 ;
            assignMentions = _assignMentions;
        }

        @Override
        public void visit(OpQuadPattern quadPattern) {
            addVars(defines, quadPattern.getGraphNode(), quadPattern.getBasicPattern());
        }

        @Override
        public void visit(OpBGP opBGP) {
            BasicPattern triples = opBGP.getPattern();
            addVars(defines, triples);
        }

        @Override
        public void visit(OpQuadBlock quadBlock) {
            addVars(defines, quadBlock.getPattern()) ;
        }

        @Override
        public void visit(OpTriple opTriple) {
            addVarsFromTriple(defines, opTriple.getTriple()) ;
        }

        @Override
        public void visit(OpQuad opQuad) {
            addVarsFromQuad(defines, opQuad.getQuad()) ;
        }

        @Override
        public void visit(OpPath opPath) {
            addVarsFromTriplePath(defines, opPath.getTriplePath());
        }

        @Override
        public void visit(OpExt opExt) {
            opExt.effectiveOp().visit(this);
        }

        @Override
        public void visit(OpJoin opJoin) {
            mergeVars(opJoin.getLeft());
            mergeVars(opJoin.getRight());
        }

        @Override
        public void visit(OpSequence opSequence) {
            for ( Op op : opSequence.getElements() )
                mergeVars(op);
        }

        private void mergeVars(Op op) {
            VarUsageVisitor usage = VarUsageVisitor.apply(op);
            defines.addAll(usage.defines);
            optDefines.addAll(usage.optDefines);
            filterMentions.addAll(usage.filterMentions);
            filterMentionsOnly.addAll(usage.filterMentionsOnly);
            assignMentions.addAll(usage.assignMentions);
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin) {
            leftJoin(opLeftJoin.getLeft(), opLeftJoin.getRight(), opLeftJoin.getExprs());
        }

        @Override
        public void visit(OpMinus opMinus) {
            mergeMinusDiff(opMinus.getLeft(), opMinus.getRight()) ;
        }

        @Override
        public void visit(OpDiff opDiff) {
            mergeMinusDiff(opDiff.getLeft(), opDiff.getRight()) ;
        }

        private void mergeMinusDiff(Op left, Op right) {
            mergeVars(left) ;
            VarUsageVisitor usage = VarUsageVisitor.apply(right);
            // Everything in the right side is really a filter.
            combinefilterMentions(this, usage.filterMentionsOnly) ;

            filterMentions.addAll(usage.defines) ;
            filterMentions.addAll(usage.optDefines) ;
            filterMentions.addAll(usage.filterMentions) ;
            filterMentions.addAll(usage.assignMentions) ;
        }

        private static void combinefilterMentions(VarUsageVisitor usage, Set<Var> mentions) {
            for ( Var v : mentions ) {
                if ( ! usage.defines.contains(v) )
                    usage.filterMentionsOnly.add(v) ;
            }
        }

        @Override
        public void visit(OpConditional opLeftJoin) {
            leftJoin(opLeftJoin.getLeft(), opLeftJoin.getRight(), null);
        }

        private void leftJoin(Op left, Op right, ExprList exprs) {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(left);
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(right);

            defines.addAll(leftUsage.defines);
            optDefines.addAll(leftUsage.optDefines);
            filterMentions.addAll(leftUsage.filterMentions);
            filterMentionsOnly.addAll(leftUsage.filterMentionsOnly);
            assignMentions.addAll(leftUsage.assignMentions);

            optDefines.addAll(rightUsage.defines); // Asymmetric.
            optDefines.addAll(rightUsage.optDefines);
            filterMentions.addAll(rightUsage.filterMentions);
            filterMentionsOnly.addAll(rightUsage.filterMentionsOnly);
            assignMentions.addAll(rightUsage.assignMentions);

            // Remove any definites that are in the optionals
            // as, overall, they are definites
            optDefines.removeAll(leftUsage.defines);

            // And the associated filter.
            if ( exprs != null ) {
                processExpr(exprs, rightUsage.defines) ;
                ExprVars.varsMentioned(filterMentions, exprs);
            }
        }

        // additionalDefines - set of variables which are defined is the filter is executed.
        private void processExpr(ExprList exprs, Set<Var> additionalDefines) {
            Set<Var> vars = ExprVars.getVarsMentioned(exprs);
            filterMentions.addAll(vars) ;
            for ( Var v : vars ) {
                if ( ! defines.contains(v) && (additionalDefines == null || ! additionalDefines.contains(v) ) )
                    filterMentionsOnly.add(v) ;
            }
        }

        @Override
        public void visit(OpUnion opUnion) {
            VarUsageVisitor usage1 = VarUsageVisitor.apply(opUnion.getLeft());
            VarUsageVisitor usage2 = VarUsageVisitor.apply(opUnion.getRight());

            // Fixed both sides.
            Set<Var> fixed = SetUtils.intersection(usage1.defines, usage2.defines) ;
            defines.addAll(fixed) ;

            // Fixed one side or the other, not both.
            Set<Var> notFixed = SetUtils.symmetricDifference(usage1.defines, usage2.defines) ;
            optDefines.addAll(notFixed) ;

            optDefines.addAll(usage1.optDefines);
            optDefines.addAll(usage2.optDefines);

            filterMentions.addAll(usage1.filterMentions);
            filterMentions.addAll(usage2.filterMentions);

            filterMentionsOnly.addAll(usage1.filterMentionsOnly);
            filterMentionsOnly.addAll(usage2.filterMentionsOnly);

            assignMentions.addAll(usage1.assignMentions);
            assignMentions.addAll(usage2.assignMentions);
        }

        @Override
        public void visit(OpDisjunction opDisjunction) {
            opDisjunction.getElements().forEach(op->mergeVars(op));
        }

        @Override
        public void visit(OpGraph opGraph) {
            addVar(defines, opGraph.getNode());
            opGraph.getSubOp().visit(this);
        }

        @Override
        public void visit(OpFilter opFilter) {
            opFilter.getSubOp().visit(this);
            processExpr(opFilter.getExprs(), null) ;
        }

        @Override
        public void visit(OpAssign opAssign) {
            opAssign.getSubOp().visit(this);
            processAssignVarExprList(opAssign.getVarExprList());
        }

        @Override
        public void visit(OpExtend opExtend) {
            opExtend.getSubOp().visit(this);
            processAssignVarExprList(opExtend.getVarExprList());
        }

        private void processAssignVarExprList(VarExprList varExprList) {
            varExprList.forEachVarExpr((v,e)-> {
                defines.add(v) ; // Expression may eval to error -> unset?
                if ( e != null )
                    ExprVars.nonOpVarsMentioned(assignMentions, e);
            }) ;
        }

        @Override
        public void visit(OpProject opProject) {
            List<Var> vars = opProject.getVars();
            VarUsageVisitor subUsage = VarUsageVisitor.apply(opProject.getSubOp());
            subUsage.defines.retainAll(vars);
            subUsage.optDefines.retainAll(vars);
            subUsage.filterMentions.retainAll(vars) ;
            subUsage.filterMentionsOnly.retainAll(vars) ;
            subUsage.assignMentions.retainAll(vars) ;
            defines.addAll(subUsage.defines);
            optDefines.addAll(subUsage.optDefines);
            filterMentions.addAll(subUsage.filterMentions);
            filterMentionsOnly.addAll(subUsage.filterMentionsOnly);
            assignMentions.addAll(subUsage.assignMentions);
        }

        @Override
        public void visit(OpTable opTable) {
            defines.addAll(opTable.getTable().getVars());
        }

        @Override
        public void visit(OpNull opNull) {}

        @Override
        public void visit(OpPropFunc opPropFunc) {
            VarUtils.addVars(defines, opPropFunc.getSubjectArgs()) ;
            VarUtils.addVars(defines, opPropFunc.getObjectArgs()) ;

            mergeVars(opPropFunc.getSubOp());

            // If definite (from the property function), remove from optDefines.
            optDefines.removeAll(this.defines);
        }

        // Ops that add nothing to variable scoping.
        // Some can't appear without being inside a project anyway
        // but we process generally where possible.

        @Override
        public void visit(OpReduced opReduced)      { mergeVars(opReduced.getSubOp()) ; }

        @Override
        public void visit(OpDistinct opDistinct)    { mergeVars(opDistinct.getSubOp()) ; }

        @Override
        public void visit(OpSlice opSlice)          { mergeVars(opSlice.getSubOp()) ; }

        @Override
        public void visit(OpLabel opLabel)          { mergeVars(opLabel.getSubOp()) ; }

        @Override
        public void visit(OpList opList)            { mergeVars(opList.getSubOp()) ; }

        @Override
        public void visit(OpService opService)      { mergeVars(opService.getSubOp()) ; }

        @Override
        public void visit(OpTopN opTop)             { mergeVars(opTop.getSubOp()) ; }

        @Override
        public void visit(OpOrder opOrder) {
            mergeVars(opOrder.getSubOp()) ;
            opOrder.getConditions().forEach(sc-> {
                sc.getExpression()    ;
            });
        }

        @Override
        public void visit(OpGroup opGroup) {
            // Only the group variables are visible.
            // So not the subOp, and not expressions.
            VarExprList varExprs = opGroup.getGroupVars() ;
            varExprs.forEachVar((v)->addVar(defines, v)) ;
        }

        @Override
        public void visit(OpDatasetNames dsNames) {
            addVar(defines, dsNames.getGraphNode()) ;
        }

        @Override
        public void visit(OpProcedure opProc) {
            for ( Expr expr :  opProc.getArgs() ) {
                Set<Var> vars = expr.getVarsMentioned() ;
                defines.addAll(vars) ;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder() ;
            sb.append("Fixed:").append(defines) ;
            sb.append(", Filter:").append(filterMentions) ;
            sb.append(", Filter2:").append(filterMentionsOnly) ;
            sb.append(", Opt:").append(optDefines) ;
            sb.append(", Assign:").append(assignMentions) ;
            return sb.toString() ;
        }
    }
}
