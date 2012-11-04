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
import com.hp.hpl.jena.sparql.algebra.OpWalker.WalkerVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;

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
        OpWalker.walk(new WalkerVisitorSkipMinus(visitor), op) ;
    }
    
    public static Collection<Var> allVars(Op op)
    {
        Set<Var> acc = new HashSet<Var>() ;
        allVars(op, acc) ;
        return acc ;
    }

    public static void allVars(Op op, Set<Var> acc)
    {
        OpWalker.walk(op, new OpVarsQuery(acc)) ;
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
