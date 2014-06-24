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

package com.hp.hpl.jena.sparql.engine.main;

import static com.hp.hpl.jena.sparql.util.VarUtils.* ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpNull ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

public class VarFinder
{
    // See also VarUtils and OpVars.
    // This class is specific to the needs of the main query engine and scoping of variables
    
    public static Set<Var> optDefined(Op op)
    {
        return VarUsageVisitor.apply(op).optDefines ;
    }
    
    public static Set<Var> fixed(Op op)
    {
        return VarUsageVisitor.apply(op).defines ;
    }
    
    public static Set<Var> filter(Op op)
    {
        return VarUsageVisitor.apply(op).filterMentions ;
    }

    VarUsageVisitor varUsageVisitor ;
    
    public VarFinder(Op op)
    { varUsageVisitor = VarUsageVisitor.apply(op) ; }
    
    public Set<Var> getOpt() { return varUsageVisitor.optDefines ; }
    public Set<Var> getFilter() { return varUsageVisitor.filterMentions ; }
    public Set<Var> getAssign() { return varUsageVisitor.assignMentions ; }
    public Set<Var> getFixed() { return varUsageVisitor.defines ; }
    
    private static class VarUsageVisitor extends OpVisitorBase //implements OpVisitor
    {
        static VarUsageVisitor apply(Op op)
        {
            VarUsageVisitor v = new VarUsageVisitor() ;
            op.visit(v) ;
            return v ;
        }

        Set<Var> defines = null ;
        Set<Var> optDefines = null ;
        Set<Var> filterMentions = null ;
        Set<Var> assignMentions = null ;

        VarUsageVisitor()
        {
            defines = new HashSet<>() ;
            optDefines = new HashSet<>() ;
            filterMentions = new HashSet<>() ;
            assignMentions = new HashSet<>() ;
        }
        
        VarUsageVisitor(Set<Var> _defines, Set<Var> _optDefines, Set<Var> _filterMentions, Set<Var> _assignMentions)
        {
            defines = _defines ;
            optDefines = _optDefines ;
            filterMentions = _filterMentions ;
            assignMentions = _assignMentions ;
        }
        
        @Override
        public void visit(OpQuadPattern quadPattern)
        {
            addVar(defines, quadPattern.getGraphNode()) ;
            BasicPattern triples = quadPattern.getBasicPattern() ;
            addVars(defines, triples) ;
        }

        @Override
        public void visit(OpBGP opBGP)
        {
            BasicPattern triples = opBGP.getPattern() ;
            addVars(defines, triples) ;
        }
        
        @Override
        public void visit(OpExt opExt)
        {
            opExt.effectiveOp().visit(this) ;
        }
        
        @Override
        public void visit(OpJoin opJoin)
        {
            joinAcc(opJoin.getLeft()) ;
            joinAcc(opJoin.getRight()) ;
        }
        
        @Override
        public void visit(OpSequence opSequence)
        {
            for ( Op op : opSequence.getElements() )
                joinAcc(op) ;    
        }
        
        private void joinAcc(Op op)
        {
            VarUsageVisitor usage = VarUsageVisitor.apply(op) ;
            
            defines.addAll(usage.defines) ;
            optDefines.addAll(usage.optDefines) ;
            filterMentions.addAll(usage.filterMentions) ;
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin)
        {
            leftJoin(opLeftJoin.getLeft(), opLeftJoin.getRight(), opLeftJoin.getExprs()) ;
        }
        
        @Override
        public void visit(OpConditional opLeftJoin)
        { 
            leftJoin(opLeftJoin.getLeft(), opLeftJoin.getRight(), null) ;
        }

        private void leftJoin(Op left, Op right, ExprList exprs)
        {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(left) ;
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(right) ;
            
            defines.addAll(leftUsage.defines) ;
            optDefines.addAll(leftUsage.optDefines) ;
            filterMentions.addAll(leftUsage.filterMentions) ;
            assignMentions.addAll(leftUsage.assignMentions) ;
            
            optDefines.addAll(rightUsage.defines) ;     // Asymmetric.
            optDefines.addAll(rightUsage.optDefines) ;
            filterMentions.addAll(rightUsage.filterMentions) ;
            assignMentions.addAll(rightUsage.assignMentions) ;
            
            // Remove any definites that are in the optionals 
            // as, overall, they are definites 
            optDefines.removeAll(leftUsage.defines) ;

            // And the associated filter.
            if ( exprs != null )
                exprs.varsMentioned(filterMentions);
        }
        
        @Override
        public void visit(OpUnion opUnion)
        {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(opUnion.getLeft()) ;
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(opUnion.getRight()) ;
            
            // defines = union(left.define, right.define) ??
            // Can be both definite and optional (different sides).
            defines.addAll(leftUsage.defines) ;
            optDefines.addAll(leftUsage.optDefines) ;
            filterMentions.addAll(leftUsage.filterMentions) ;
            assignMentions.addAll(leftUsage.assignMentions) ;
            defines.addAll(rightUsage.defines) ;
            optDefines.addAll(rightUsage.optDefines) ;
            filterMentions.addAll(rightUsage.filterMentions) ;
            assignMentions.addAll(rightUsage.assignMentions) ;
        }

        @Override
        public void visit(OpGraph opGraph)
        {
            addVar(defines, opGraph.getNode()) ;
            opGraph.getSubOp().visit(this) ;
        }
        
        // @Override
        @Override
        public void visit(OpFilter opFilter)
        {
            opFilter.getExprs().varsMentioned(filterMentions);
            opFilter.getSubOp().visit(this) ;
        }
        
        @Override
        public void visit(OpAssign opAssign)
        {
            opAssign.getSubOp().visit(this) ;
            processVarExprList(opAssign.getVarExprList()) ;
        }
        
        @Override
        public void visit(OpExtend opExtend)
        {
            opExtend.getSubOp().visit(this) ;
            processVarExprList(opExtend.getVarExprList()) ;
        }
        
        private void processVarExprList(VarExprList varExprList)
        {
            Map<Var, Expr> map = varExprList.getExprs() ;
            for ( Entry<Var, Expr> e : map.entrySet() )
            {
                defines.add(e.getKey()) ;
                e.getValue().varsMentioned(assignMentions);
            }
        }
        
        @Override
        public void visit(OpProject opProject)
        {
            List<Var> vars = opProject.getVars() ;
            VarUsageVisitor subUsage = VarUsageVisitor.apply(opProject.getSubOp()) ;
            
            subUsage.defines.retainAll(vars) ;
            subUsage.optDefines.retainAll(vars) ;
            subUsage.optDefines.retainAll(vars) ;
            defines.addAll(subUsage.defines) ;
            optDefines.addAll(subUsage.optDefines) ;
            filterMentions.addAll(subUsage.filterMentions) ;
            assignMentions.addAll(subUsage.assignMentions) ;
        }

        @Override
        public void visit(OpTable opTable)
        { 
            defines.addAll(opTable.getTable().getVars()) ;
        }

        @Override
        public void visit(OpNull opNull)
        { }
    }
}
