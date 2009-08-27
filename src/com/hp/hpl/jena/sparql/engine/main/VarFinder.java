/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;

class VarFinder
{
    // See also VarUtils and OpVars.
    // This class is specific to the needs of the main query engine and scoping of variables
    
    static Set<Var> optDefined(Op op)
    {
        return VarUsageVisitor.apply(op).optDefines ;
    }
    
    private static Set<Var> fixed(Op op)
    {
        return VarUsageVisitor.apply(op).defines ;
    }
    
    
    static Set<Var> filter(Op op)
    {
        return VarUsageVisitor.apply(op).filterMentions ;
    }

    private static void vars(Set<Var> vars, Triple triple)
    {
        slot(vars, triple.getSubject()) ;
        slot(vars, triple.getPredicate()) ;
        slot(vars, triple.getObject()) ;
    }
    
    private static void vars(Set<Var> acc, BasicPattern pattern)
    {
        for ( Triple triple : pattern )
            vars(acc, triple) ;
    }

    private static void slot(Set<Var> vars, Node node)
    {
        if ( Var.isVar(node) )
            vars.add(Var.alloc(node)) ;
    }

    VarUsageVisitor varUsageVisitor ;
    
    VarFinder(Op op)
    { varUsageVisitor = VarUsageVisitor.apply(op) ; }
    
    public Set<Var> getOpt() { return varUsageVisitor.optDefines ; }
    public Set<Var> getFilter() { return varUsageVisitor.filterMentions ; }
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

        VarUsageVisitor()
        {
            defines = new HashSet<Var>() ;   
            optDefines = new HashSet<Var>() ;
            filterMentions = new HashSet<Var>() ;
        }
        
        VarUsageVisitor(Set<Var> _defines, Set<Var> _optDefines, Set<Var> _filterMentions)
        {
            defines = _defines ;
            optDefines = _optDefines ;
            filterMentions = _filterMentions ;
        }
        
        @Override
        public void visit(OpQuadPattern quadPattern)
        {
            slot(defines, quadPattern.getGraphNode()) ;
            BasicPattern triples = quadPattern.getBasicPattern() ;
            vars(defines, triples) ;
//            List quads = quadPattern.getQuads() ;
//            for ( Iterator iter = quads.iterator() ; iter.hasNext(); )
//            {
//                Quad quad = (Quad)iter.next() ;
//                //slot(quad.getGraph()) ;
//                slot(defines, quad.getSubject()) ;
//                slot(defines, quad.getPredicate()) ;
//                slot(defines, quad.getObject()) ;
//            }
        }

        @Override
        public void visit(OpBGP opBGP)
        {
            BasicPattern triples = opBGP.getPattern() ;
            vars(defines, triples) ;
        }
        
        @Override
        public void visit(OpExt opExt)
        {
            opExt.effectiveOp().visit(this) ;
        }
        
        @Override
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

        @Override
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
            
            // Remove any definites that are in the optionals 
            // as, overall, they are definites 
            optDefines.removeAll(leftUsage.defines) ;

            // And the associated filter.
            if ( opLeftJoin.getExprs() != null )
                opLeftJoin.getExprs().varsMentioned(filterMentions);
        }

        @Override
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

        @Override
        public void visit(OpGraph opGraph)
        {
            slot(defines, opGraph.getNode()) ;
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
            List<Var> vars = opAssign.getVarExprList().getVars() ;
            defines.addAll(vars) ;
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
        }

        @Override
        public void visit(OpTable opTable)
        { }

        @Override
        public void visit(OpNull opNull)
        { }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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