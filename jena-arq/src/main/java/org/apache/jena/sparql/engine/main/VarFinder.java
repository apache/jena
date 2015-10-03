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

import java.util.HashSet ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;
import java.util.Set ;

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.util.VarUtils ;

public class VarFinder
{
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
    
    public VarFinder(Op op)
    { varUsageVisitor = VarUsageVisitor.apply(op) ; }
    
    public Set<Var> getOpt() { return varUsageVisitor.optDefines ; }
    public Set<Var> getFilter() { return varUsageVisitor.filterMentions ; }
    public Set<Var> getAssign() { return varUsageVisitor.assignMentions ; }
    public Set<Var> getFixed() { return varUsageVisitor.defines ; }
    
    private static class VarUsageVisitor 
        //extends OpVisitorBase
        implements OpVisitor
    {
        static VarUsageVisitor apply(Op op) {
            VarUsageVisitor v = new VarUsageVisitor();
            op.visit(v);
            return v;
        }

        Set<Var> defines        = null;
        Set<Var> optDefines     = null;
        Set<Var> filterMentions = null;
        Set<Var> assignMentions = null;

        VarUsageVisitor() {
            defines = new HashSet<>();
            optDefines = new HashSet<>();
            filterMentions = new HashSet<>();
            assignMentions = new HashSet<>();
        }

        VarUsageVisitor(Set<Var> _defines, Set<Var> _optDefines, Set<Var> _filterMentions, Set<Var> _assignMentions) {
            defines = _defines;
            optDefines = _optDefines;
            filterMentions = _filterMentions;
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
            filterMentions.addAll(usage.defines) ;
            filterMentions.addAll(usage.optDefines) ;
            filterMentions.addAll(usage.filterMentions) ;
            filterMentions.addAll(usage.assignMentions) ;
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
            assignMentions.addAll(leftUsage.assignMentions);

            optDefines.addAll(rightUsage.defines); // Asymmetric.
            optDefines.addAll(rightUsage.optDefines);
            filterMentions.addAll(rightUsage.filterMentions);
            assignMentions.addAll(rightUsage.assignMentions);

            // Remove any definites that are in the optionals
            // as, overall, they are definites
            optDefines.removeAll(leftUsage.defines);

            // And the associated filter.
            if ( exprs != null )
                exprs.varsMentioned(filterMentions);
        }

        @Override
        public void visit(OpUnion opUnion) {
            mergeVars(opUnion.getLeft());
            mergeVars(opUnion.getRight());
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
            opFilter.getExprs().varsMentioned(filterMentions);
            opFilter.getSubOp().visit(this);
        }

        @Override
        public void visit(OpAssign opAssign) {
            opAssign.getSubOp().visit(this);
            processVarExprList(opAssign.getVarExprList());
        }

        @Override
        public void visit(OpExtend opExtend) {
            opExtend.getSubOp().visit(this);
            processVarExprList(opExtend.getVarExprList());
        }

        private void processVarExprList(VarExprList varExprList) {
            Map<Var, Expr> map = varExprList.getExprs();
            for ( Entry<Var, Expr> e : map.entrySet() ) {
                defines.add(e.getKey());
                e.getValue().varsMentioned(assignMentions);
            }
        }

        @Override
        public void visit(OpProject opProject) {
            List<Var> vars = opProject.getVars();
            VarUsageVisitor subUsage = VarUsageVisitor.apply(opProject.getSubOp());
            subUsage.defines.retainAll(vars);
            subUsage.optDefines.retainAll(vars);
            subUsage.filterMentions.retainAll(vars) ;
            subUsage.assignMentions.retainAll(vars) ;
            defines.addAll(subUsage.defines);
            optDefines.addAll(subUsage.optDefines);
            filterMentions.addAll(subUsage.filterMentions);
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
            VarUtils.addVarNodes(defines, opPropFunc.getSubjectArgs().getArgList()) ;
            VarUtils.addVarNodes(defines, opPropFunc.getObjectArgs().getArgList()) ;
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
            // Not subOp.
            VarExprList varExprs = opGroup.getGroupVars() ;
            varExprs.getExprs().forEach((v,expr)->{
                addVar(defines, v) ;
                // Not the expressions.
            }) ;
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
    }
}
