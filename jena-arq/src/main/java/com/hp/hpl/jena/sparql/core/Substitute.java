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

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;

public class Substitute
{
    public static Op substitute(Op op, Binding binding)
    {
        // Want to avoid cost if the binding is empty 
        // but the empty test is not zero-cost on non-empty things.
     
        if ( isNotNeeded(binding) ) return op ;
        return Transformer.transform(new OpSubstituteWorker(binding), op) ;
    }
    
    public static Op substitute(Op op, Var var, Node node)
    {
        Binding b = BindingFactory.binding(var, node) ;
        return substitute(op, b) ;
    }
    
    public static BasicPattern substitute(BasicPattern bgp, Binding binding)
    {
        if ( isNotNeeded(binding) ) return bgp ;
        
        BasicPattern bgp2 = new BasicPattern() ;
        for ( Triple triple : bgp )
        {
            Triple t = substitute(triple, binding) ;
            bgp2.add(t) ;
        }
        return bgp2 ;
    }
    
    public static Triple substitute(Triple triple, Binding binding)
    {
        if ( isNotNeeded(binding) ) return triple ;
        
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        Node s1 = substitute(s, binding) ;
        Node p1 = substitute(p, binding) ;
        Node o1 = substitute(o, binding) ;

        Triple t = triple ;
        if ( s1 != s || p1 != p || o1 != o )
            t = new Triple(s1, p1, o1) ;
        return t ;
    }

    public static TriplePath substitute(TriplePath triplePath, Binding binding)
    {
        if ( triplePath.isTriple() )
            return new TriplePath(Substitute.substitute(triplePath.asTriple(), binding)) ;
  
        Node s = triplePath.getSubject() ;
        Node o = triplePath.getObject() ;
        Node s1 = substitute(s, binding) ;
        Node o1 = substitute(o, binding) ;
        
        TriplePath tp = triplePath ;
        if ( s1 != s || o1 != o )
            tp = new TriplePath(s1, triplePath.getPath(), o1) ;
        return tp ;
    }
    
    public static Quad substitute(Quad quad, Binding binding)
    {
        if ( isNotNeeded(binding) ) return quad ;
        
        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        
        Node g1 = substitute(g, binding) ;
        Node s1 = substitute(s, binding) ;
        Node p1 = substitute(p, binding) ;
        Node o1 = substitute(o, binding) ;

        Quad q = quad ;
        if ( s1 != s || p1 != p || o1 != o || g1 != g )
            q = new Quad(g1, s1, p1, o1) ;
        return q ;
    }


    public static Node substitute(Node n, Binding b)
    {
        return Var.lookup(b, n) ;
    }
    
    public static PropFuncArg substitute(PropFuncArg propFuncArg, Binding binding)
    {
        if ( isNotNeeded(binding) ) return propFuncArg ;
        
        if ( propFuncArg.isNode() ) {
            Node n = propFuncArg.getArg() ;
            if ( ! Var.isVar(n) )
                // Not a Var, no substitute needed. 
                return propFuncArg ;
            return new PropFuncArg(substitute(propFuncArg.getArg(), binding)) ;
        }
        
        List<Node> newArgList = new ArrayList<>() ;
        for ( Node n : propFuncArg.getArgList() )
            newArgList.add(substitute(n, binding)) ;
        return new PropFuncArg(newArgList) ;
    }
    
    public static Expr substitute(Expr expr, Binding binding)
    {
        if ( isNotNeeded(binding) ) return expr ;
        return expr.copySubstitute(binding) ;  
    }
    
    public static ExprList substitute(ExprList exprList, Binding binding)
    {
        if ( isNotNeeded(binding) ) return exprList ;
        return exprList.copySubstitute(binding) ;  
    }
    
    private static boolean isNotNeeded(Binding b)
    {
        return b == null || b.isEmpty() ; 
    }
    
    // ----
    private static class OpSubstituteWorker extends TransformCopy
    {
        private Binding binding ;

        public OpSubstituteWorker(Binding binding) 
        {
            super(TransformCopy.COPY_ALWAYS) ;
            this.binding = binding ;
        }

        @Override
        public Op transform(OpBGP opBGP)
        {
            BasicPattern bgp = opBGP.getPattern() ;
            bgp = substitute(bgp, binding) ;
            return new OpBGP(bgp) ;
        }

        @Override
        public Op transform(OpQuadPattern quadPattern)
        {
            Node gNode = quadPattern.getGraphNode() ;
            Node g = substitute(gNode, binding) ;

            BasicPattern triples = new BasicPattern() ;
            for ( Triple triple : quadPattern.getBasicPattern() )
            {
                Node s = substitute(triple.getSubject(), binding) ;
                Node p = substitute(triple.getPredicate(), binding) ;
                Node o = substitute(triple.getObject(), binding) ;
                Triple t = new Triple(s, p, o) ;
                triples.add(t) ;
            }
            
            // Pure quading.
//            for ( Iterator iter = quadPattern.getQuads().iterator() ; iter.hasNext() ; )
//            {
//                Quad quad = (Quad)iter.next() ;
//                if ( ! quad.getGraph().equals(gNode) )
//                    throw new ARQInternalErrorException("Internal error: quads block is not uniform over the graph node") ;
//                Node s = substitute(quad.getSubject(), binding) ;
//                Node p = substitute(quad.getPredicate(), binding) ;
//                Node o = substitute(quad.getObject(), binding) ;
//                Triple t = new Triple(s, p, o) ;
//                triples.add(t) ;
//            }

            return new OpQuadPattern(g, triples) ;
        }

        @Override
        public Op transform(OpPath opPath)
        {
            return new OpPath(substitute(opPath.getTriplePath(), binding)) ;
        }

        @Override
        public Op transform(OpPropFunc opPropFunc, Op subOp)
        {
            PropFuncArg sArgs = opPropFunc.getSubjectArgs() ;
            PropFuncArg oArgs = opPropFunc.getObjectArgs() ;
            
            PropFuncArg sArgs2 = substitute(sArgs, binding) ;
            PropFuncArg oArgs2 = substitute(oArgs, binding) ;
            
            if ( sArgs2 == sArgs && oArgs2 == oArgs && opPropFunc.getSubOp() == subOp)
                return super.transform(opPropFunc, subOp) ;
            return new OpPropFunc(opPropFunc.getProperty(), sArgs2, oArgs2, subOp) ; 
        }
        
        @Override
        public Op transform(OpFilter filter, Op op)
        {
            ExprList exprs = filter.getExprs().copySubstitute(binding) ;
            if ( exprs == filter.getExprs() )
                return filter ;
            return OpFilter.filter(exprs, op) ; 
        }

        @Override
        public Op transform(OpAssign opAssign, Op subOp)
        { 
            VarExprList varExprList2 = transformVarExprList(opAssign.getVarExprList()) ;
            if ( varExprList2.isEmpty() )
                return subOp ;
            return OpAssign.assign(subOp, varExprList2) ;
        }
        
        @Override
        public Op transform(OpExtend opExtend, Op subOp)
        { 
            VarExprList varExprList2 = transformVarExprList(opExtend.getVarExprList()) ;
            if ( varExprList2.isEmpty() )
                return subOp ;
            
            return OpExtend.create(subOp, varExprList2) ;
        }
        
        private  VarExprList transformVarExprList(VarExprList varExprList)
        {
            VarExprList varExprList2 = new VarExprList() ;
            for ( Var v : varExprList.getVars() )
            {
//                if ( binding.contains(v))
//                    // Already bound. No need to do anything because the 
//                    // logical assignment will test value.  
//                    continue ;
                Expr expr = varExprList.getExpr(v) ;
                expr = expr.copySubstitute(binding) ;
                varExprList2.add(v, expr) ;
            }
            return varExprList2 ;
        }
        

        // The expression?
        //public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)   { return xform(opLeftJoin, left, right) ; }
        
        @Override
        public Op transform(OpGraph op, Op sub)
        {
            Node n = substitute(op.getNode(), binding) ;
            return new OpGraph(n, sub) ;
        }

        @Override
        public Op transform(OpService op, Op sub)
        {
            Node n = substitute(op.getService(), binding) ;
            return new OpService(n, sub, op.getSilent()) ;
        }
    }
}
