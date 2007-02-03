/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.BasicPattern;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine2.op.*;

public class VarFinder
{
    // TODO Consider doing this once and caching the results in this VarFinder.
    // Op ==> results map.
    // Need filter stuff as well.
    
    public static Set optDefined(Op op)
    {
        return VarUsageVisitor.apply(op).optDefines ;
    }
    
    public static Set fixed(Op op)
    {
        return VarUsageVisitor.apply(op).defines ;
    }
    
   
    VarUsageVisitor varUsageVisitor ;
    
    public VarFinder(Op op) { varUsageVisitor = VarUsageVisitor.apply(op) ; }
    
    public Set getOpt() { return varUsageVisitor.optDefines ; }
    public Set getFixed() { return varUsageVisitor.defines ; }
    
    private static class VarUsageVisitor extends OpVisitorBase
    {
        static VarUsageVisitor apply(Op op)
        {
            VarUsageVisitor v = new VarUsageVisitor() ;
            op.visit(v) ;
            return v ;
        }

        Set defines = new HashSet() ;
        Set optDefines = new HashSet() ;
        
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
        public void visit(OpJoin opJoin)
        {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(opJoin.getLeft()) ;
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(opJoin.getRight()) ;
            defines.addAll(leftUsage.defines) ;
            optDefines.addAll(leftUsage.optDefines) ;
            defines.addAll(rightUsage.defines) ;
            optDefines.addAll(rightUsage.optDefines) ;
        }

        //@Override
        public void visit(OpLeftJoin opLeftJoin)
        {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(opLeftJoin.getLeft()) ;
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(opLeftJoin.getRight()) ;
            
            defines.addAll(leftUsage.defines) ;
            optDefines.addAll(leftUsage.optDefines) ;
            optDefines.addAll(rightUsage.defines) ;     // Asymmetric.
            optDefines.addAll(rightUsage.optDefines) ;
            
            // Remove any defintites that are in the optionals 
            // as, overall, they are defintites 
            optDefines.removeAll(leftUsage.defines) ;
        }

        //@Override
        public void visit(OpUnion opUnion)
        {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(opUnion.getLeft()) ;
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(opUnion.getRight()) ;
            
            // Can be both definite and optional (different sides).
            defines.addAll(leftUsage.defines) ;
            optDefines.addAll(leftUsage.optDefines) ;
            defines.addAll(rightUsage.defines) ;
            optDefines.addAll(rightUsage.optDefines) ;
        }

        //@Override
        public void visit(OpGraph opGraph)
        {
            slot(opGraph.getNode()) ;
        }
    }
    
//    private static class Defines extends OpVisitorBase
//    {
//        private Var var ;
//        boolean result = false ;
//        
//        Defines(Var var) { this.var = var ; }
//        
//        //@Override
//        public void visit(OpQuadPattern quadPattern)
//        {
//            List quads = quadPattern.getQuads() ;
//            for ( Iterator iter = quads.iterator() ; iter.hasNext(); )
//            {
//                Quad quad = (Quad)iter.next() ;
//                if ( quad.getGraph().equals(var) )      { result = true ; return ; }
//                if ( quad.getSubject().equals(var) )    { result = true ; return ; }
//                if ( quad.getPredicate().equals(var) )  { result = true ; return ; }
//                if ( quad.getObject().equals(var) )     { result = true ; return ; }
//            }
//        }
//
//        //@Override
//        public void visit(OpBGP opBGP)
//        {
//            List triples = opBGP.getPattern() ;
//            for ( Iterator iter = triples.iterator() ; iter.hasNext(); )
//            {
//                Triple triple = (Triple)iter.next() ;
//                if ( triple.getSubject().equals(var) )    { result = true ; return ; }
//                if ( triple.getPredicate().equals(var) )  { result = true ; return ; }
//                if ( triple.getObject().equals(var) )     { result = true ; return ; }
//            }
//        }
//    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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