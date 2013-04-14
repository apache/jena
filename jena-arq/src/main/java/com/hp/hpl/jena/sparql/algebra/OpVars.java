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

package com.hp.hpl.jena.sparql.algebra;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.OpWalker.WalkerVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;

/** Get vars for a pattern  */ 

public class OpVars
{
    /** @deprecated use {@linkplain #visibleVars} */
    @Deprecated
    public static Set<Var> patternVars(Op op) { return visibleVars(op) ; } 
    
    public static Set<Var> visibleVars(Op op)
    {
        Set<Var> acc = new HashSet<Var>() ;
        visibleVars(op, acc) ;
        return acc ; 
    }
    
    public static void visibleVars(Op op, Set<Var> acc)
    {
        OpVarsPattern visitor = new OpVarsPattern(acc) ;
        OpWalker.walk(new WalkerVisitorVisible(visitor, acc), op) ;
    }

    // All mentioned variables regardless of scope/visibility.
    public static Collection<Var> mentionedVars(Op op)
    {
        Set<Var> acc = new HashSet<Var>() ;
        mentionedVars(op, acc) ;
        return acc ;
    }

    // All mentioned variables regardless of scope/visibility.
    public static void mentionedVars(Op op, Set<Var> acc)
    {
        OpVarsQuery visitor = new OpVarsQuery(acc) ;
        OpWalker.walk(op, visitor) ;
    }
    
    public static Collection<Var> vars(BasicPattern pattern)
    {
        Set<Var> acc = new HashSet<Var>() ;
        vars(pattern, acc) ;
        return acc ;
    }
    
    public static void vars(BasicPattern pattern, Collection<Var> acc)
    {
        for ( Triple triple : pattern )
            addVarsFromTriple(acc, triple) ;
    }
    

    /** Do project and don't walk into it. MINUS vars aren't visiible either */
    private static class WalkerVisitorVisible extends WalkerVisitor
    {
        private final Collection<Var> acc ;

        public WalkerVisitorVisible(OpVarsPattern visitor, Collection<Var> acc)
        {
            super(visitor) ;
            this.acc = acc ;
        }

        @Override
        public void visit(OpProject op)
        {
            before(op) ;
            // Skip Project subop.
            acc.addAll(op.getVars()) ;
            after(op) ;  
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
    
//    /** Don't accumulate RHS of OpMinus*/
//    private static class WalkerVisitorVisible extends WalkerVisitorProjectDirect
//    {
//        public WalkerVisitorVisible(OpVarsPattern visitor, Collection<Var> acc)
//        {
//            super(visitor, acc) ;
//        }
//        
//        @Override
//        public void visit(OpMinus op)
//        {
//            before(op) ;
//            if ( op.getLeft() != null ) op.getLeft().visit(this) ;
//            // Skip right.
//            //if ( op.getRight() != null ) op.getRight().visit(this) ;
//            if ( visitor != null ) op.visit(visitor) ;      
//            after(op) ;  
//        }
//    }
    
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
            // The walker should have handled this. 
            throw new ARQInternalErrorException() ;
            // Code to do it without walker support ...  
//            // Seems a tad wasteful to do all that work then throw it away.
//            acc.clear() ;
//            acc.addAll(opProject.getVars()) ;
        }
        
        @Override
        public void visit(OpAssign opAssign)
        {
            acc.addAll(opAssign.getVarExprList().getVars()) ;
            //opAssign.getSubOp().visit(this) ;
        }
        
        @Override
        public void visit(OpExtend opExtend)
        {
            acc.addAll(opExtend.getVarExprList().getVars()) ;
            //opAssign.getSubOp().visit(this) ;
        }
        
        @Override
        public void visit(OpPropFunc opPropFunc)
        {
            addvars(opPropFunc.getSubjectArgs()) ;
            addvars(opPropFunc.getObjectArgs()) ;
        }
        
        private void addvars(PropFuncArg pfArg)
        {
            if ( pfArg.isNode() )
            {
                addVar(acc, pfArg.getArg()) ;
                return ;
            }
            for ( Node n : pfArg.getArgList() )
                addVar(acc,n) ;
        }
        
        @Override
        public void visit(OpProcedure opProc)
        {
            opProc.getArgs().varsMentioned(acc) ;
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
