/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.OpWalker.WalkerVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.query.SortCondition;

/** Get vars for a pattern  */ 

public class OpVars
{
    public static Set<Var> patternVars(Op op)
    {
        Set<Var> acc = new HashSet<Var>() ;
        patternVars(op, acc) ;
        return acc ; 
    }
    
    public static void patternVars(Op op, Set<Var> acc)
    {
        //OpWalker.walk(op, new OpVarsPattern(acc)) ;
        OpVisitor visitor = new OpVarsPattern(acc) ;
        OpWalker.walk(new WalkerVisitorSkipMinus(visitor), op, visitor) ;
    }
    
    public static Set<Var> allVars(Op op)
    {
        Set<Var> acc = new HashSet<Var>() ;
        allVars(op, acc) ;
        return acc ;
    }

    public static void allVars(Op op, Set<Var> acc)
    {
        OpWalker.walk(op, new OpVarsQuery(acc)) ;
    }
    
    public static void vars(BasicPattern pattern, Collection<Var> acc)
    {
        for ( Triple triple : pattern )
            addVarsFromTriple(acc, triple) ;
    }
    
    /** Don't accumulate RHS of OpMinus*/
    static class WalkerVisitorSkipMinus extends WalkerVisitor
    {
        public WalkerVisitorSkipMinus(OpVisitor visitor)
        {
            super(visitor) ;
        }
        
        @Override
        public void visit(OpMinus op)
        {
            before(op) ;
            if ( op.getLeft() != null ) op.getLeft().visit(this) ;
            // Skip right.
            //if ( op.getRight() != null ) op.getRight().visit(this) ;
            if ( visitor != null ) op.visit(visitor) ;      
            after(op) ;  
        }
    }
    
    private static class OpVarsPattern extends OpVisitorBase
    {
        // The possibly-set-vars
        protected Set<Var> acc ;

        OpVarsPattern(Set<Var> acc) { this.acc = acc ; }

        @Override
        public void visit(OpBGP opBGP)
        {
            vars(opBGP.getPattern(), acc) ;
        }
        
        @Override
        public void visit(OpPath opPath)
        {
            addVar(acc, opPath.getTriplePath().getSubject()) ;
            addVar(acc, opPath.getTriplePath().getObject()) ;
        }

        @Override
        public void visit(OpQuadPattern quadPattern)
        {
            addVar(acc, quadPattern.getGraphNode()) ;
            vars(quadPattern.getBasicPattern(), acc) ;
            // Pure quading
//            for ( Iterator iter = quadPattern.getQuads().iterator() ; iter.hasNext() ; )
//            {
//                Quad quad = (Quad)iter.next() ;
//                addVarsFromQuad(acc, quad) ;
//            }
        }

        @Override
        public void visit(OpGraph opGraph)
        {
            addVar(acc, opGraph.getNode()) ;
        }

        @Override
        public void visit(OpDatasetNames dsNames)
        {
            addVar(acc, dsNames.getGraphNode()) ;
        }
        
        @Override
        public void visit(OpTable opTable)
        {
            // Only the variables with values in the tables
            // (When building, undefs didn't get into bindings so no variable mentioned) 
            Table t = opTable.getTable() ;
            acc.addAll(t.getVars());
        }
        
        @Override
        public void visit(OpProject opProject) 
        {   
            // Seems a tad wasteful to do all that work then throw it away.
            // But it needs the walker redone.
            // TODO Rethink walker for part walks. 
            // Better: extend a Walking visitor - OpWalker.Walker
            acc.clear() ;
            acc.addAll(opProject.getVars()) ;
        }
        
        @Override
        public void visit(OpAssign opAssign)
        {
            acc.addAll(opAssign.getVarExprList().getVars()) ;
            //opAssign.getSubOp().visit(this) ;
        }
    }
    
    private static class OpVarsQuery extends OpVarsPattern
    {
        OpVarsQuery(Set<Var> acc) { super(acc) ; }

        @Override
        public void visit(OpFilter opFilter)
        {
            opFilter.getExprs().varsMentioned(acc);
        }

        @Override
        public void visit(OpOrder opOrder)
        {
            for ( Iterator<SortCondition> iter = opOrder.getConditions().iterator() ; iter.hasNext(); )
            {
                SortCondition sc = iter.next();
                Set<Var> x = sc.getExpression().getVarsMentioned() ;
                acc.addAll(x) ;
            }
        }
    }

    private static void addVarsFromTriple(Collection<Var> acc, Triple t)
    {
        addVar(acc, t.getSubject()) ;
        addVar(acc, t.getPredicate()) ;
        addVar(acc, t.getObject()) ;
    }
    
    private static void addVarsFromQuad(Collection<Var> acc, Quad q)
    {
        addVar(acc, q.getSubject()) ;
        addVar(acc, q.getPredicate()) ;
        addVar(acc, q.getObject()) ;
        addVar(acc, q.getGraph()) ;
    }
    
    private static void addVar(Collection<Var> acc, Node n)
    {
        if ( n == null )
            return ;
        
        if ( n.isVariable() )
            acc.add(Var.alloc(n)) ;
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