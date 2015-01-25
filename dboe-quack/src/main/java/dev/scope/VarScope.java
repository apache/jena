/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package dev.scope;

import static com.hp.hpl.jena.sparql.util.VarUtils.addVar ;
import static com.hp.hpl.jena.sparql.util.VarUtils.addVars ;

import java.util.* ;
import java.util.Map.Entry ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.SetUtils ;

import com.hp.hpl.jena.sparql.algebra.* ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class VarScope {
    
    // Use OpLabel to record
    
    static class Scope {
        Set<Var> fixed = new HashSet<>() ;
        Set<Var> opt = null ;
        Scope(Set<Var> fixed) { this.fixed = fixed ; }
        @Override
        public String toString() {
            String x = "Scope: Fixed:"+fixed ;
            if ( opt != null )
                x += " // opt:"+opt ;
            return x ;
        }
    }
    
    static class OpScope extends OpLabel {
        public static OpScope create(Scope scope, Op op) { return new OpScope(scope, op) ; }
        protected OpScope(Scope scope, Op op) { super(scope, op) ; }
        
        @Override
        public Scope getObject() { return (Scope)super.getObject() ; }
        
        @Override
        public String getName() {
            return "scope" ;
        }
        
        @Override
        public void visit(OpVisitor opVisitor)
        { super.visit(opVisitor) ; }

        @Override
        public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
            if ( !(other instanceof OpScope) )
                return false ;
            OpScope opScope = (OpScope)other ;
            if ( !Lib.equal(getObject(), opScope.getObject()) )
                return false ;

            return Lib.equal(getSubOp(), opScope.getSubOp()) ;
        }

        @Override
        public int hashCode() {
            int x = 0xB9 ;
            x ^= Lib.hashCodeObject(getObject(), 0) ;
            x ^= Lib.hashCodeObject(getSubOp(), 0) ;
            return x ;
        }
    }
    
    static class TransformScope extends TransformCopy {
        
        @Override
        public Op transform(OpBGP opBGP) {
            //Op op2 = super.transform(opBGP) ;
            Set<Var> fixed = fixed(opBGP) ;
            Scope scope = new Scope(fixed) ;
            return OpScope.create(scope , opBGP) ;
        }
        
        @Override
        public Op transform(OpUnion opUnion, Op left, Op right) {
            Scope leftScope = getScope(left) ;
            Scope rightScope = getScope(right) ;
            Op op = super.transform(opUnion, left, right) ;
            Scope scope ;
            if ( leftScope != null && rightScope != null ) {
                Set<Var> x = SetUtils.intersection(leftScope.fixed, rightScope.fixed) ;
                scope = new Scope(x) ;
                return OpScope.create(scope , op) ;
            }
            return op ;
        }

        
        private static Scope getScope(Op op) {
            try { 
                return (Scope)((OpLabel)op).getObject() ;
            } catch (ClassCastException ex) { return null ; }
        }
    }
    
    public static void main(String ... argv) {
        exec("(bgp (?s :p :o) (?s :q ?z) (?z :r 123))") ;
        exec("(union (bgp (?s :p :o)) (bgp  (?s :q ?z)) )") ;
    }
    
    public static void exec(String str) {
        
        System.out.println(str) ;
        Op op = SSE.parseOp(str) ;
        System.out.println(op) ;
        TransformScope scoper = new TransformScope() ;
        Op op2 = Transformer.transform(scoper, op) ;
        System.out.println(op2) ;
        
//        Map<Op, Collection<Var>> scopes = new IdentityHashMap<>() ;
//        System.out.println(str) ;
//        Op op = SSE.parseOp(str) ;
//        System.out.println(op) ;
//        Set<Var> v1 = VarFinder.fixed(op) ;
//        System.out.println(v1) ;
//        Set<Var> v2 = fixed(op) ;
//        System.out.println(v2) ;
//        System.out.println() ;
    }
    
    
    public static Set<Var> fixed(Op op) {
        return VarUsageVisitorFixed.apply(op).defines ;
    }
    
    // Slimmed down VarUsageVisitor - fixed only (not the join classifier requirements) 
    // This class is top-down recursive 
    private static class VarUsageVisitorFixed extends OpVisitorBase //implements OpVisitor
    {
        static VarUsageVisitorFixed apply(Op op)
        {
            VarUsageVisitorFixed v = new VarUsageVisitorFixed() ;
            op.visit(v) ;
            return v ;
        }

        Set<Var> defines = null ;
        Set<Var> assignDefines = null ;
        
        VarUsageVisitorFixed()
        {
            defines = new HashSet<>() ;
            assignDefines = new HashSet<>() ;
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
            accumulate(opJoin.getLeft()) ;
            accumulate(opJoin.getRight()) ;
        }
        
        @Override
        public void visit(OpSequence opSequence)
        {
            for ( Op op : opSequence.getElements() )
                accumulate(op) ;    
        }
        
        private void accumulate(Op op)
        {
            op.visit(this) ;
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
            accumulate(left) ;
        }
        
        @Override
        public void visit(OpUnion opUnion)
        {
            // Must be defined in both sizes.
            Set<Var> leftUsage = fixed(opUnion.getLeft()) ;
            Set<Var> rightUsage = fixed(opUnion.getRight()) ;
            leftUsage.retainAll(rightUsage) ;
            defines.addAll(leftUsage) ;
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
                assignDefines.add(e.getKey()) ;
            }
        }
        
        @Override
        public void visit(OpProject opProject)
        {
            // Intersection of projection and really defined.
            List<Var> vars = opProject.getVars() ;
            Set<Var> subDefined = fixed(opProject.getSubOp()) ;
            vars.retainAll(subDefined) ;
            defines.addAll(vars) ;
        }

        @Override
        public void visit(OpTable opTable)
        { 
            defines.addAll(opTable.getTable().getVars()) ;
        }
    }
}

