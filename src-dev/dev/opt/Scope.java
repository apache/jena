/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.opt;

//import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import java.util.*;

import org.openjena.atlas.lib.SetUtils ;



import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorByType;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

public class Scope
{
    
    public static Map<Op, Set<Var>> scopeMap(Op op)
    {
        ScopeVisitor v = new ScopeVisitor() ;
        OpWalker.walk(op, v) ;
        return v.defined ;
    }
    
    private static class ScopeVisitor extends OpVisitorByType
    {

        // More nodes
        // Optionals as well. 
        
        Map<Op, Set<Var>> defined = new HashMap<Op, Set<Var>>() ;

        // ---- Default actions
        @Override
        protected void visit0(Op0 op)
        {
            Set<Var> x =  Collections.emptySet() ;
            defined.put(op,x) ;
        }

        @Override
        protected void visit1(Op1 op)
        {
            defined.put(op, defined.get(op.getSubOp())) ;
        }

        @Override
        protected void visit2(Op2 op)
        {}

        @Override
        protected void visitExt(OpExt op)
        {}

        @Override
        protected void visitN(OpN op)
        {

        }

        // ---- Compositions
        // When used with the Walker, the sub-ops wil have been done before
        // these composite nodes are visited here.

        @Override
        public void visit(OpSequence opSeq)
        {
            // Need to modify each sub op becase a sequence puts earlier ops in scope of later ones.
            Iterator<Op> seq = opSeq.iterator() ;
            for ( ; seq.hasNext() ; )
            {
                Op op = seq.next();
                Set<Var> vars = defined.get(op) ;
            }
        }

        @Override
        public void visit(OpJoin opJoin)
        {
            Set<Var> vars = new HashSet<Var>() ;
            vars.addAll(defined.get(opJoin.getLeft())) ;
            vars.addAll(defined.get(opJoin.getRight())) ;
            defined.put(opJoin, vars) ;
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin)
        {
            Set<Var> vars = new HashSet<Var>() ;
            vars.addAll(defined.get(opLeftJoin.getLeft())) ;
            //vars.addAll(defined.get(opLeftJoin.getRight())) ; // NOT these
            defined.put(opLeftJoin, vars) ;
        }


        @Override
        public void visit(OpUnion opUnion)
        {
            // rewriting the union to disjunctive normal form is an option.
            Set<Var> left = defined.get(opUnion.getLeft()) ;
            Set<Var> right = defined.get(opUnion.getRight()) ;
            Set<Var> x = SetUtils.intersection(left, right) ;
            defined.put(opUnion, x) ;
        }

        @Override
        public void visit(OpGraph opGraph)
        {
            if ( ! Var.isVar(opGraph.getNode()) )
            {
                visit1(opGraph) ;
                return ;
            }
            Set<Var> x = new HashSet<Var>(defined.get(opGraph.getNode()));
            x.add(Var.alloc(opGraph.getNode())) ;
            defined.put(opGraph, x) ;
        }

        // ---- Other

        @Override
        public void visit(OpFilter opFilter) { visit1(opFilter) ; }

        @Override
        public void visit(OpAssign opAssign) {}

        @Override
        public void visit(OpProject opProject)
        {
            // Restrict to in project AND actually mentioned.
            //Set<Var> x = defined.get(opProject.getSubOp()) ;
            
            List<Var> vars = opProject.getVars() ;
            Set<Var> z = new HashSet<Var>(vars) ;      // The new set
            z.retainAll(vars) ;
            defined.put(opProject, z) ;
        }

        // ---- Base elements

        @Override
        public void visit(OpBGP opBGP)
        {
            Set<Var> acc = new HashSet<Var>() ;
            //for ( Triple t : opBGP.getPattern() )
            for ( Triple t : opBGP.getPattern().getList()  )
                addVarsFromTriple(acc, t) ;
            defined.put(opBGP, acc) ;
        }

        @Override
        public void visit(OpQuadPattern opQuad)
        {
            Set<Var> acc = new HashSet<Var>() ;
            for ( Quad q : opQuad.getPattern()  )
                addVarsFromQuad(acc, q) ;
            defined.put(opQuad, acc) ;
        }

        // ---- Workers

        private static void addVarsFromTriple(Set<Var> acc, Triple t)
        {
            addVar(acc, t.getSubject()) ;
            addVar(acc, t.getPredicate()) ;
            addVar(acc, t.getObject()) ;
        }

        private static void addVarsFromQuad(Set<Var> acc, Quad q)
        {
            addVar(acc, q.getSubject()) ;
            addVar(acc, q.getPredicate()) ;
            addVar(acc, q.getObject()) ;
            addVar(acc, q.getGraph()) ;
        }

        private static void addVar(Set<Var> acc, Node n)
        {
            if ( n == null )
                return ;

            if ( Var.isVar(n) )
                acc.add(Var.alloc(n)) ;
        }

        //
        //    @Override
        //    public void visit(OpQuadPattern quadPattern)
        //    {}
        //
        //    @Override
        //    public void visit(OpTriple opTriple)
        //    {}
        //
        //    @Override
        //    public void visit(OpPath opPath)
        //    {}
        //
        //    @Override
        //    public void visit(OpTable opTable)
        //    {}
        //
        //    @Override
        //    public void visit(OpNull opNull)
        //    {}
        //
        //    @Override
        //    public void visit(OpProcedure opProc)
        //    {}
        //
        //    @Override
        //    public void visit(OpPropFunc opPropFunc)
        //    {}
        //
        //    @Override
        //    public void visit(OpFilter opFilter)
        //    {}
        //
        //    @Override
        //    public void visit(OpGraph opGraph)
        //    {}
        //
        //    @Override
        //    public void visit(OpService opService)
        //    {}
        //
        //    @Override
        //    public void visit(OpDatasetNames dsNames)
        //    {}
        //
        //    @Override
        //    public void visit(OpLabel opLabel)
        //    {}
        //
        //    @Override
        //    public void visit(OpJoin opJoin)
        //    {}
        //
        //    @Override
        //    public void visit(OpSequence opSequence)
        //    {}
        //
        //    @Override
        //    public void visit(OpLeftJoin opLeftJoin)
        //    {}
        //
        //    @Override
        //    public void visit(OpDiff opDiff)
        //    {}
        //
        //    @Override
        //    public void visit(OpUnion opUnion)
        //    {}
        //
        //    @Override
        //    public void visit(OpExt opExt)
        //    {}
        //
        //    @Override
        //    public void visit(OpList opList)
        //    {}
        //
        //    @Override
        //    public void visit(OpOrder opOrder)
        //    {}
        //
        //    @Override
        //    public void visit(OpProject opProject)
        //    {}
        //
        //    @Override
        //    public void visit(OpReduced opReduced)
        //    {}
        //
        //    @Override
        //    public void visit(OpDistinct opDistinct)
        //    {}
        //
        //    @Override
        //    public void visit(OpSlice opSlice)
        //    {}
        //
        //    @Override
        //    public void visit(OpAssign opAssign)
        //    {}
        //
        //    @Override
        //    public void visit(OpGroupAgg opGroupAgg)
        //    {}
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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