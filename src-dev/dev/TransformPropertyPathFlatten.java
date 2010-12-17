/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.List ;
import java.util.Stack ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpPath ;
import com.hp.hpl.jena.sparql.core.PathBlock ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.VarAlloc ;
import com.hp.hpl.jena.sparql.path.P_Alt ;
import com.hp.hpl.jena.sparql.path.P_FixedLength ;
import com.hp.hpl.jena.sparql.path.P_Inverse ;
import com.hp.hpl.jena.sparql.path.P_Link ;
import com.hp.hpl.jena.sparql.path.P_Mod ;
import com.hp.hpl.jena.sparql.path.P_NegPropSet ;
import com.hp.hpl.jena.sparql.path.P_OneOrMore ;
import com.hp.hpl.jena.sparql.path.P_Path0 ;
import com.hp.hpl.jena.sparql.path.P_Path1 ;
import com.hp.hpl.jena.sparql.path.P_Path2 ;
import com.hp.hpl.jena.sparql.path.P_ReverseLink ;
import com.hp.hpl.jena.sparql.path.P_Seq ;
import com.hp.hpl.jena.sparql.path.P_ZeroOrMore ;
import com.hp.hpl.jena.sparql.path.P_ZeroOrOne ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.path.PathVisitor ;

/** Rewrite property paths as per the SPARQL spec 
 * except we are doing it (extended) algebra to algebra,
 * not syntax to algebra. 
 */
public class TransformPropertyPathFlatten extends TransformCopy
{

    @Override
    public Op transform(OpPath opPath)
    { 
        if ( opPath.getTriplePath().isTriple() )
            return opPath ;
        
        TriplePath tp = opPath.getTriplePath() ;
        Path p1 = tp.getPath() ;
        Path p2 = transform(p1) ;
        
        if ( p1 == p2 )
            return opPath ;
        
        return new OpPath(new TriplePath(tp.getSubject(), p2, tp.getObject())) ;
    }

    static interface PathEval<T>
    {

        public T eval(P_Link pathNode) ;
        public T eval(P_ReverseLink pathNode) ;
        public T eval(P_NegPropSet pathNotOneOf) ;
        public T eval(P_Inverse inversePath) ;
        public T eval(P_Mod pathMod) ;
        public T eval(P_FixedLength pFixedLength) ;
        public T eval(P_ZeroOrOne path) ;
        public T eval(P_ZeroOrMore path) ;
        public T eval(P_OneOrMore path) ;
        public T eval(P_Alt pathAlt) ;
        public T eval(P_Seq pathSeq) ;
    }
    
    static abstract class PathVisitorByType implements PathVisitor
    {
        public abstract void visitNegPS(P_NegPropSet path) ;
        public abstract void visit0(P_Path0 path) ;
        public abstract void visit1(P_Path1 path) ;
        public abstract void visit2(P_Path2 path) ;

        public void visit(P_Link pathNode)              { visit0(pathNode) ; }

        public void visit(P_ReverseLink pathNode)       { visit0(pathNode) ; }

        public void visit(P_NegPropSet pathNotOneOf)    { visitNegPS(pathNotOneOf) ; }

        public void visit(P_Inverse inversePath)        { visit1(inversePath) ; }

        public void visit(P_Mod pathMod)                { visit1(pathMod) ; }

        public void visit(P_FixedLength pFixedLength)   { visit1(pFixedLength) ; }

        public void visit(P_ZeroOrOne path)             { visit1(path) ; }

        public void visit(P_ZeroOrMore path)            { visit1(path) ; }

        public void visit(P_OneOrMore path)             { visit1(path) ; }

        public void visit(P_Alt pathAlt)                { visit2(pathAlt) ; }

        public void visit(P_Seq pathSeq)                { visit2(pathSeq) ; }
    }
    
    static class PathWalkEval<T> implements PathVisitor
    {
        Stack<T> stack = new Stack<T>() ;
        private PathEval<T> operation ;
        
        public PathWalkEval(PathEval<T> operation) { this.operation = operation ; }
        
        public void visit(P_Link pathNode)
        {
            stack.push(operation.eval(pathNode)) ;
        }

        public void visit(P_ReverseLink pathNode)
        {
            stack.push(operation.eval(pathNode)) ;
        }

        public void visit(P_NegPropSet pathNotOneOf)
        {
            stack.push(operation.eval(pathNotOneOf)) ;
        }

        public void visit(P_Inverse inversePath)
        {
            stack.push(operation.eval(inversePath)) ;
        }

        public void visit(P_Mod pathMod)
        {}

        public void visit(P_FixedLength pFixedLength)
        {}

        public void visit(P_ZeroOrOne path)
        {}

        public void visit(P_ZeroOrMore path)
        {}

        public void visit(P_OneOrMore path)
        {}

        public void visit(P_Alt pathAlt)
        {}

        public void visit(P_Seq pathSeq)
        {}
        
    }
    
    /*
X uri Y                     ==> Triple pattern: X uri Y
X !(:uri1|...|:urin) Y      ==> NegatedPropertySet(X,{:uri1 ... :urin} ,Y)
X !(^:uri1|...|^:urin)Y     ==> ^(X !(:uri1|...|:urin) Y)
X !(:uri1|...|:urii|^:urii+1|...|^:urim) Y  ==>  { X !(:uri1|...|:urii|)Y } UNION { X !(^:urii+1|...|^:urim) Y } 
X ^path Y                   ==>   Y path X
X path1 / path2 Y           ==>   X path1 ?V . ?V path2 Y
X path1 | path2 Y           ==>  { X path1 Y } UNION { X path2 Y}  
X path* Y                   ==>  { X path{0} Y } UNION { X path+ Y}
X path+ Y                   ==>  ArbitraryPath(X, path, Y)
X path? Y                   ==> { X path{0} Y } UNION { X path Y}
X path{0} Y                 ==>  ZeroLengthPath(X, path, Y)
X path{n} Y where n > 0     ==>  X path ?V1 . ?V1 path ?V2 ... ?Vn-1 path Y
X path{n,m} Y               ==>   { X path{n} Y } UNION { X path{n+1} Y } ... UNION { X path{m} Y}
X path{n,} Y                ==>   X path{n} ?V . ?V path* Y
X path{,n} Y                ==>   X path{0,n} Y
*/
    static private Path transform(Path p)
    {
        RewritePath rewrite = new RewritePath() ;
        
        p.visit(rewrite) ;
        //VarAlloc.getVarAllocator().allocVar()
        
        return null ;
    }
    
    static PathBlock rewrite(PathBlock path)
    {
        return null ;
    }
    
    // Path Compiler.
    
    static class RewritePath implements PathVisitor
    {
        // Stack?
        VarAlloc varAlloc = VarAlloc.getVarAllocator() ;
        Stack<Path> stack = new Stack<Path>() ;
        Node subject ;
        Path path ;
        Node object ;
       
        public void visit(P_Link pathNode)
        {
            Triple t = new Triple(subject, pathNode.getNode(), object) ;
        }

        public void visit(P_ReverseLink pathNode)
        {
            Triple t = new Triple(object, pathNode.getNode(), subject) ;
        }

        /*
         * X !(:uri1|...|:urin) Y                       ==>  NegatedPropertySet(X,{:uri1 ... :urin} ,Y)
         * X !(^:uri1|...|^:urin)Y                      ==>  ^(X !(:uri1|...|:urin) Y)
         * X !(:uri1|...|:urii|^:urii+1|...|^:urim) Y   ==>  { X !(:uri1|...|:urii|)Y } UNION { X !(^:urii+1|...|^:urim) Y } 
         */
        public void visit(P_NegPropSet pathNotOneOf)
        {
            List<Node> fwd = pathNotOneOf.getFwdNodes() ;
            
            
            
            List<Node> bwd = pathNotOneOf.getBwdNodes() ;
            
            
            // Union
        }

        public void visit(P_Inverse inversePath)
        {
            path = inversePath ;
        }

        public void visit(P_Mod pathMod)
        {
            // ????
            path = null ;
        }

        // path{N}
        public void visit(P_FixedLength pFixedLength)
        {
            Node s = subject ;
            Path p = pFixedLength.getSubPath() ;
            //p = rewrite(p) ;
            
            PathBlock pattern = new PathBlock() ;
            
            for ( int i = 0 ; i < pFixedLength.getCount() ; i++ )
            {
                Node o = null ;
                pattern.add(new TriplePath(s, p, o)) ;
                
                if ( i == pFixedLength.getCount() )
                    o = object ;
                else
                    o = varAlloc.allocVar() ;
                // Shuffle
                s = o ;
            }
            
            
            
            //????
        }

        public void visit(P_ZeroOrOne path)
        {
            /// ???
            path = null ;
        }

        public void visit(P_ZeroOrMore path)
        {
            /// ???
            path = null ;
        }

        public void visit(P_OneOrMore path)
        {
            /// ???
            path = null ;
        }

        public void visit(P_Alt pathAlt)
        {
            pathAlt.getRight().visit(this) ;
            Path p1 = path ;
            pathAlt.getRight().visit(this) ;
            Path p2 = path ;
            // Make UNION.
        }

        public void visit(P_Seq pathSeq)
        {
            
        }
        
    }
    
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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