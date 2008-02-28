/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

public class VarFinder
{
    public static Set optDefined(Op op)
    {
        return VarUsageVisitor.apply(op).optDefines ;
    }
    
    public static Set fixed(Op op)
    {
        return VarUsageVisitor.apply(op).defines ;
    }
    
    
    public static Set filter(Op op)
    {
        return VarUsageVisitor.apply(op).filterMentions ;
    }

    VarUsageVisitor varUsageVisitor ;
    
    public VarFinder(Op op)
    { varUsageVisitor = VarUsageVisitor.apply(op) ; }
    
    public Set getOpt() { return varUsageVisitor.optDefines ; }
    public Set getFilter() { return varUsageVisitor.filterMentions ; }
    public Set getFixed() { return varUsageVisitor.defines ; }
    
    private static class VarUsageVisitor extends OpVisitorBase //implements OpVisitor
    {
        static VarUsageVisitor apply(Op op)
        {
            VarUsageVisitor v = new VarUsageVisitor() ;
            op.visit(v) ;
            return v ;
        }

        Set defines = null ;
        Set optDefines = null ;
        Set filterMentions = null ;

        VarUsageVisitor()
        {
            defines = new HashSet() ;   
            optDefines = new HashSet() ;
            filterMentions = new HashSet() ;
        }
        
        VarUsageVisitor(Set _defines, Set _optDefines, Set _filterMentions)
        {
            defines = _defines ;
            optDefines = _optDefines ;
            filterMentions = _filterMentions ;
        }
        
        //@Override
        public void visit(OpQuadPattern quadPattern)
        {
            slot(quadPattern.getGraphNode()) ;
            List quads = quadPattern.getQuads() ;
            for ( Iterator iter = quads.iterator() ; iter.hasNext(); )
            {
                Quad quad = (Quad)iter.next() ;
                //slot(quad.getGraph()) ;
                slot(quad.getSubject()) ;
                slot(quad.getPredicate()) ;
                slot(quad.getObject()) ;
            }
        }

        //@Override
        public void visit(OpBGP opBGP)
        {
            BasicPattern triples = opBGP.getPattern() ;
            for ( Iterator iter = triples.iterator() ; iter.hasNext(); )
            {
                Triple triple = (Triple)iter.next() ;
                slot(triple.getSubject()) ;
                slot(triple.getPredicate()) ;
                slot(triple.getObject()) ;
            }
        }
        
        private void slot(Node node)
        {
            if ( Var.isVar(node) )
                defines.add(Var.alloc(node)) ;
        }
        
        //@Override
        public void visit(OpExt opExt)
        {
            opExt.effectiveOp().visit(this) ;
        }
        
        //@Override
        public void visit(OpJoin opJoin)
        {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(opJoin.getLeft()) ;
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(opJoin.getRight()) ;

            defines.addAll(leftUsage.defines) ;
            optDefines.addAll(leftUsage.optDefines) ;
            filterMentions.addAll(leftUsage.filterMentions) ;
            
            defines.addAll(rightUsage.defines) ;
            optDefines.addAll(rightUsage.optDefines) ;
            filterMentions.addAll(rightUsage.filterMentions) ;
        }

        //@Override
        public void visit(OpLeftJoin opLeftJoin)
        {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(opLeftJoin.getLeft()) ;
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(opLeftJoin.getRight()) ;
            
            defines.addAll(leftUsage.defines) ;
            optDefines.addAll(leftUsage.optDefines) ;
            filterMentions.addAll(leftUsage.filterMentions) ;
            
            optDefines.addAll(rightUsage.defines) ;     // Asymmetric.
            optDefines.addAll(rightUsage.optDefines) ;
            filterMentions.addAll(rightUsage.filterMentions) ;
            
//            // Remove any definites that are in the optionals 
//            // as, overall, they are definites 
//            // Don't do this?
//            optDefines.removeAll(leftUsage.defines) ;

            // And the associated filter.
            if ( opLeftJoin.getExprs() != null )
                opLeftJoin.getExprs().varsMentioned(filterMentions);
        }

        //@Override
        public void visit(OpUnion opUnion)
        {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(opUnion.getLeft()) ;
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(opUnion.getRight()) ;
            
            // Can be both definite and optional (different sides).
            defines.addAll(leftUsage.defines) ;
            optDefines.addAll(leftUsage.optDefines) ;
            filterMentions.addAll(leftUsage.filterMentions) ;
            defines.addAll(rightUsage.defines) ;
            optDefines.addAll(rightUsage.optDefines) ;
            filterMentions.addAll(rightUsage.filterMentions) ;
        }

        //@Override
        public void visit(OpGraph opGraph)
        {
            slot(opGraph.getNode()) ;
        }
        
        // @Override
        public void visit(OpFilter opFilter)
        {
            opFilter.getExprs().varsMentioned(filterMentions);
            opFilter.getSubOp().visit(this) ;
        }
        
        public void visit(OpAssign opAssign)
        {
            opAssign.getSubOp().visit(this) ;
            List vars = opAssign.getVarExprList().getVars() ;
            defines.addAll(vars) ;
        }
        
        public void visit(OpProject opProject)
        {
            List vars = opProject.getVars() ;
            VarUsageVisitor subUsage = VarUsageVisitor.apply(opProject.getSubOp()) ;
            
            subUsage.defines.retainAll(vars) ;
            subUsage.optDefines.retainAll(vars) ;
            subUsage.optDefines.retainAll(vars) ;
            defines.addAll(subUsage.defines) ;
            optDefines.addAll(subUsage.optDefines) ;
            filterMentions.addAll(subUsage.filterMentions) ;
        }

        public void visit(OpTable opTable)
        { }

        public void visit(OpNull opNull)
        { }

//        public void visit(OpProcedure opProc)
//        {
//            throw new ARQNotImplemented("VarFinded/OpProcedure") ;
//        }
//
//        public void visit(OpPropFunc opPropFunc)
//        {
//            throw new ARQNotImplemented("VarFinded/OpPropFunc") ;
//        }
//
//        public void visit(OpService opService)
//        {
//            opService.getSubOp().visit(this) ;
//            slot(opService.getService()) ;
//        }
//
//        public void visit(OpDatasetNames dsNames)
//        { }
//
//        public void visit(OpStage opStage)
//        {
//            throw new ARQNotImplemented("VarFinded/OpStage") ;
//        }
//
//        public void visit(OpDiff opDiff)
//        {
//            throw new ARQNotImplemented("VarFinded/OpDiff") ;
//        }
//
//        public void visit(OpList opList)
//        {
//            opList.getSubOp().visit(this) ;
//        }
//
//        public void visit(OpOrder opOrder)
//        {
//            opOrder.getSubOp().visit(this) ;
//        }
//
//        public void visit(OpReduced opReduced)
//        {
//            opReduced.getSubOp().visit(this) ;
//        }
//
//        public void visit(OpDistinct opDistinct)
//        {
//            opDistinct.getSubOp().visit(this);
//        }
//
//        public void visit(OpSlice opSlice)
//        {
//            opSlice.getSubOp().visit(this);
//        }
//
//        public void visit(OpGroupAgg opGroupAgg)
//        {
//            // XXX Group and variable definedness : check this
//            defines.addAll(opGroupAgg.getGroupVars().getVars()) ;
//        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */