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

package com.hp.hpl.jena.sparql.path;

import static com.hp.hpl.jena.sparql.path.P_Mod.UNSET ;

import java.util.List ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

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
        
        private void output(Node node)
        {
            out.print(FmtUtils.stringForNode(node, prologue)) ;
        }
        
        private void output(P_Path0 path0)
        {
            if ( ! path0.isForward() )
                out.print("^") ;
            out.print(FmtUtils.stringForNode(path0.getNode(), prologue)) ;
        }
        
        @Override
        public void visit(P_Link pathNode)
        {
            output(pathNode.getNode()) ;
        }
        
        @Override
        public void visit(P_ReverseLink pathNode)
        {
            out.println("^") ;
            output(pathNode.getNode()) ;
        }

        @Override
        public void visit(P_NegPropSet pathNotOneOf)
        {
            List<P_Path0> props = pathNotOneOf.getNodes() ;
            if ( props.size() == 0 )
                throw new ARQException("Bad path element: NotOneOf found with no elements") ;
            out.print("!") ;
            if ( props.size() == 1 )
                output(props.get(0)) ;
            else
            {
                out.print("(") ;
                boolean first = true ;
                for (P_Path0 p : props)
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

        
        
        @Override
        public void visit(P_Alt pathAlt)
        {
            visit2(pathAlt, "|", true) ;
        }

        @Override
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

        @Override
        public void visit(P_Mod pathMod)
        {
            if ( needParens )
                out.print("(") ;
            if ( alwaysInnerParens )
                out.print("(") ;
            pathMod.getSubPath().visit(this) ;
            if ( alwaysInnerParens )
                out.print(")") ;

            out.print("{") ;
            if ( pathMod.getMin() != UNSET )
                out.print(Long.toString(pathMod.getMin())) ;
            out.print(",") ;
            if ( pathMod.getMax() != UNSET )
                out.print(Long.toString(pathMod.getMax())) ;
            out.print("}") ;
            
//            if ( pathMod.isZeroOrMore() )
//                out.print("*") ;
//            else if ( pathMod.isOneOrMore() )
//                out.print("+") ;
//            else if ( pathMod.isZeroOrOne() )
//                out.print("?") ;
//            else
//            {
//                out.print("{") ;
//                if ( pathMod.getMin() != UNSET )
//                    out.print(Long.toString(pathMod.getMin())) ;
//                out.print(",") ;
//                if ( pathMod.getMax() != UNSET )
//                    out.print(Long.toString(pathMod.getMax())) ;
//                out.print("}") ;
//            }
            if ( needParens )
                out.print(")") ;
        }

        @Override
        public void visit(P_FixedLength pFixedLength)
        {
            if ( needParens )
                out.print("(") ;
            if ( alwaysInnerParens )
                out.print("(") ;
            pFixedLength.getSubPath().visit(this) ;
            if ( alwaysInnerParens )
                out.print(")") ;

            out.print("{") ;
            out.print(Long.toString(pFixedLength.getCount())) ;
            out.print("}") ;
            if ( needParens )
                out.print(")") ;
        }

        @Override
        public void visit(P_Distinct pathDistinct)
        {
            out.print("DISTINCT(") ;
            pathDistinct.getSubPath().visit(this) ;
            out.print(")") ;
        }

        @Override
        public void visit(P_Multi pathMulti)
        {
            out.print("MULTI(") ;
            pathMulti.getSubPath().visit(this) ;
            out.print(")") ;
        }

        @Override
        public void visit(P_Shortest path)
        {
            out.print("SHORTEST(") ;
            path.getSubPath().visit(this) ;
            out.print(")") ;
        }

        @Override
        public void visit(P_ZeroOrOne path)
        { printPathMod("?", path.getSubPath()) ; }

        @Override
        public void visit(P_ZeroOrMore1 path)
        { printPathMod("*", path.getSubPath()) ; }

        @Override
        public void visit(P_ZeroOrMoreN path)
        { printPathMod("{*}", path.getSubPath()) ; }

        @Override
        public void visit(P_OneOrMore1 path)
        { printPathMod("+", path.getSubPath()) ; }

        @Override
        public void visit(P_OneOrMoreN path)
        { printPathMod("{+}", path.getSubPath()) ; }

        private void printPathMod(String mod, Path path)
        {
            boolean doParens = ( needParens || alwaysInnerParens ) ;
            if ( doParens )
                out.print("(") ;
            path.visit(this) ;
            if ( doParens )
                out.print(")") ;
            out.print(mod) ;
        }
        
        // Need to consider binary ^
        @Override
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
