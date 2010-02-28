/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer ;
import com.hp.hpl.jena.sparql.util.IndentedWriter ;

public class PathWriter
{

    public static void write(Path path, Prologue prologue)
    {
        write(IndentedWriter.stdout, path, prologue) ;
    }
    
    public static void write(IndentedWriter out, Path path, Prologue prologue)
    {
        PathWriterWorker w = new PathWriterWorker(out, prologue) ;
        path.visit(w) ;
        out.flush();
    }
    
    public static String asString(Path path) { return asString(path, null) ; }
    
    public static String asString(Path path, Prologue prologue)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        PathWriterWorker w = new PathWriterWorker(buff, prologue) ;
        path.visit(w) ;
        w.out.flush();
        return buff.asString() ;
    }

    static class PathWriterWorker implements PathVisitor
    {

        private IndentedWriter out ;
        private Prologue prologue ;
        private static boolean alwaysInnerParens = true ;
        private boolean needParens = false ;

        PathWriterWorker(IndentedWriter indentedWriter, Prologue prologue)
        { 
            this.out = indentedWriter ; 
            this.prologue = prologue ;
        }

        private void visitPath(Path path)
        { visitPath(path, true) ; }
        
        private void visitPath(Path path, boolean needParensThisTime)
        {
            if ( alwaysInnerParens )
                needParensThisTime = true ;
            boolean b = needParens ;
            needParens = needParensThisTime ;
            path.visit(this) ;
            needParens = b ;
        }
        
        //@Override
        private void output(Node node)
        {
            out.print(FmtUtils.stringForNode(node, prologue)) ;
        }
        
      //@Override
        private void output(P_Path0 path0)
        {
            if ( ! path0.isForward() )
                out.print("^") ;
            out.print(FmtUtils.stringForNode(path0.getNode(), prologue)) ;
        }
        
        //@Override
        public void visit(P_Link pathNode)
        {
            output(pathNode.getNode()) ;
        }
        
        public void visit(P_ReverseLink pathNode)
        {
            out.println("^") ;
            output(pathNode.getNode()) ;
        }

        //@Override
        public void visit(P_NegPropClass pathNotOneOf)
        {
            List<Node> props = pathNotOneOf.getExcludedNodes() ;
            if ( props.size() == 0 )
                throw new ARQException("Bad path element: NotOneOf found with no elements") ;
            out.print("!") ;
            if ( props.size() == 1 )
                output(props.get(0)) ;
            else
            {
                out.print("(") ;
                boolean first = true ;
                for (Node p : props)
                {
                    if (!first) out.print("|") ;
                    first = false ;
                    output(p) ;
                }
                out.print(")") ;
            }

//            List<P_Path0> props = pathNotOneOf.getNodes()  ;
//            if ( props.size() == 0 )
//                throw new ARQException("Bad path element: NotOneOf found with no elements") ;
//            out.print("!") ;
//            if ( props.size() == 1 )
//                output(props.get(0)) ;
//            else
//            {
//                out.print("(") ;
//                boolean first = true ;
//                for (P_Path0 p : props)
//                {
//                    if (!first) out.print("|") ;
//                    first = false ;
//                    output(p) ;
//                }
//                out.print(")") ;
//            }
        }

        
        
        //@Override
        public void visit(P_Alt pathAlt)
        {
            visit2(pathAlt, "|", true) ;
        }

        //@Override
        public void visit(P_Seq pathSeq)
        {
            visit2(pathSeq, "/", false) ;
        }

        // Should pass around precedence numbers.
        private void visit2(P_Path2 path2, String sep, boolean isSeq)
        {
            if ( needParens ) out.print("(") ;
            visitPath(path2.getLeft()) ;
            out.print(sep) ;
            // Don't need parens if same as before.
            if ( isSeq )
            {
                // Make / and ^ chains look nice
                if ( path2.getRight() instanceof P_Seq )
                    visitPath(path2.getRight(), needParens) ;
                else
                    visitPath(path2.getRight(), true) ;
            }
            else
                visitPath(path2.getRight(), true) ;
            
            if ( needParens ) out.print(")") ;
        }

        //@Override
        public void visit(P_Mod pathMod)
        {
            if ( needParens )
                out.print("(") ;
            if ( alwaysInnerParens )
                out.print("(") ;
            pathMod.getSubPath().visit(this) ;
            if ( alwaysInnerParens )
                out.print(")") ;
            
            
            if ( pathMod.isZeroOrMore() )
                out.print("*") ;
            else if ( pathMod.isOneOrMore() )
                out.print("+") ;
            else if ( pathMod.isZeroOrOne() )
                out.print("?") ;
            else
            {
                out.print("{") ;
                out.print(Long.toString(pathMod.getMin())) ;
                out.print(",") ;
                out.print(Long.toString(pathMod.getMax())) ;
                out.print("}") ;
            }
            if ( needParens )
                out.print(")") ;
        }

        // Need to consider binary ^
        public void visit(P_Inverse inversePath)
        {
            out.print("^") ;
            Path p = inversePath.getSubPath() ;
            boolean parens = true ; 
            if ( p instanceof P_Link )
                parens = false ;
            visitPath(p, parens) ;
        }
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