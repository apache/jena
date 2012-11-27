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

package com.hp.hpl.jena.sparql.sse.writers;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.path.* ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/** SSE Writer */
public class WriterPath
{
    private static final int NL = WriterLib.NL ;
    private static final int NoNL = WriterLib.NoNL ;
    private static final int NoSP = WriterLib.NoSP ;
    
    public static void write(Path path, Prologue prologue)
    {
        output(IndentedWriter.stdout, path, new SerializationContext(prologue)) ;
    }
    
    public static void output(IndentedWriter out, Path path, SerializationContext naming)
    {
        WriterPathVisitor w = new WriterPathVisitor(out, naming.getPrologue()) ;
        w.output(path) ;
        w.out.flush();
    }
    
    public static void output(IndentedWriter out, TriplePath tp, SerializationContext naming)
    {
        WriterLib.start(out, Tags.tagTriplePath, NoNL) ;
        outputPlain(out, tp, naming) ;
        WriterLib.finish(out, Tags.tagTriplePath) ;
    }
    
    public static void outputPlain(IndentedWriter out, TriplePath tp, SerializationContext naming)
    {
        boolean oneLiner = oneLiner(tp.getPath()) ;
        if ( oneLiner )
        {
            WriterNode.output(out, tp.getSubject(), naming) ;
            out.print(" ") ;
            WriterPath.output(out, tp.getPath(), naming) ;
            out.print(" ") ;
            WriterNode.output(out, tp.getObject(), naming) ;
        }
        else
        {
            nl(out, false);
            WriterNode.output(out, tp.getSubject(), naming) ;
            nl(out);
            WriterPath.output(out, tp.getPath(), naming) ;
            nl(out);
            WriterNode.output(out, tp.getObject(), naming) ;
        }
    }
    
    private static boolean oneLiner(Path path)
    {
        return (path instanceof P_Link) ;
    }
    
    private static final boolean multiline = false ;
    private static final boolean maxBracket = false ;
    
    private static void nl(IndentedWriter out)
    {
        nl(out, true) ;
    }

    private static void nl(IndentedWriter out, boolean spaceForNL)
    {
        if ( multiline )
            out.println();
        else
            if ( spaceForNL ) out.print(" ") ;
    }
    
    public static String asString(Path path) { return asString(path, null) ; }
    
    public static String asString(Path path, Prologue prologue)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        WriterPathVisitor w = new WriterPathVisitor(buff, prologue) ;
        path.visit(w) ;
        w.out.flush();
        return buff.asString() ;
    }
    
    private static class WriterPathVisitor implements PathVisitor
    {

        private IndentedWriter out ;
        private Prologue prologue ;

        WriterPathVisitor(IndentedWriter indentedWriter, Prologue prologue)
        { 
            this.out = indentedWriter ; 
            this.prologue = prologue ;
        }

        private void output(Path path)
        {
            path.visit(this) ;
        }

        private void output(Node node)
        {
            out.print(FmtUtils.stringForNode(node, prologue)) ;
        }
        
        @Override
        public void visit(P_Link pathNode)
        {
            output(pathNode.getNode()) ;
        }

        @Override
        public void visit(P_ReverseLink pathNode)
        {
            out.print("(") ;
            out.print(Tags.tagPathRev) ;
            out.print(" ") ;
            output(pathNode.getNode()) ;
            out.print(")") ;
        }

        @Override
        public void visit(P_NegPropSet pathNotOneOf)
        {
            out.print("(") ;
            out.print(Tags.tagPathNotOneOf) ;

            for ( P_Path0 p : pathNotOneOf.getNodes() )
            {
                out.print(" ") ;
                output(p) ;
            }
            out.print(")") ;
        }

        @Override
        public void visit(P_Alt pathAlt)
        {
            visit2(pathAlt, Tags.tagPathAlt) ;
        }

        @Override
        public void visit(P_Seq pathSeq)
        {
            visit2(pathSeq, Tags.tagPathSeq) ;
        }

        private void visit2(P_Path2 path2, String nodeName)
        {
            out.print("(") ;
            out.print(nodeName) ;
            nl(out) ; 
            out.incIndent() ;
            output(path2.getLeft()) ;
            nl(out) ; 
            output(path2.getRight()) ;
            out.decIndent() ;
            out.print(")") ;
        }

        @Override
        public void visit(P_Mod pathMod)
        {
            out.print("(") ;
            out.print(Tags.tagPathMod) ;
            out.print(" ") ;
            out.print(modInt(pathMod.getMin())) ;
            out.print(" ") ;
            out.print(modInt(pathMod.getMax())) ;
            writeOneLiner(pathMod.getSubPath()) ;
            out.print(")") ;
        }

        private static String modInt(long value)
        {
            if ( value == P_Mod.INF ) return "*" ;
            if ( value == P_Mod.UNSET ) return "_" ;
            return Long.toString(value) ;
        }

        @Override
        public void visit(P_FixedLength path)
        {
            out.print("(") ;
            out.print(Tags.tagPathFixedLength) ;
            out.print(" ") ;
            
            out.print(modInt(path.getCount())) ;
            writeOneLiner(path.getSubPath()) ;
            out.print(")") ;
        }
        
        @Override
        public void visit(P_Distinct pathDistinct)
        {
            writePath(Tags.tagPathDistinct, pathDistinct.getSubPath()) ;
        }

        @Override
        public void visit(P_Multi pathMulti)
        {
            writePath(Tags.tagPathMulti, pathMulti.getSubPath()) ;
        }

        @Override
        public void visit(P_Shortest path)
        {
            writePath(Tags.tagPathShortest, path.getSubPath()) ;
        }

        @Override
        public void visit(P_ZeroOrOne path)
        { 
            writePath(Tags.tagPathZeroOrOne, path.getSubPath()) ;
        }

        @Override
        public void visit(P_ZeroOrMore1 path)
        { 
            writePath(Tags.tagPathZeroOrMore1, path.getSubPath()) ;
        }

        @Override
        public void visit(P_ZeroOrMoreN path)
        { 
            writePath(Tags.tagPathZeroOrMoreN, path.getSubPath()) ;
        }

        @Override
        public void visit(P_OneOrMore1 path)
        { 
            writePath(Tags.tagPathOneOrMore1, path.getSubPath()) ;
        }
        
        @Override
        public void visit(P_OneOrMoreN path)
        { 
            writePath(Tags.tagPathOneOrMoreN, path.getSubPath()) ;
        }
        
        private void writeOneLiner(Path path)
        {
            if ( oneLiner(path) )
                out.print(" ") ;
            else
                nl(out) ;
            out.incIndent() ;
            output(path) ;
            out.decIndent() ;
        }
        
        private void writePath(String tag, Path subPath)
        {
            out.print("(") ;
            out.print(tag) ;
            writeOneLiner(subPath) ;
            out.print(")") ;
        }

        @Override
        public void visit(P_Inverse reversePath)
        {
            out.print("(") ;
            out.print(Tags.tagPathReverse) ;
            nl(out) ; 
            
            out.incIndent() ;
            Path p = reversePath.getSubPath() ;
            output(p) ;
            out.decIndent() ;
            nl(out, false) ;
            
            out.print(")") ;
        }

    }
}
