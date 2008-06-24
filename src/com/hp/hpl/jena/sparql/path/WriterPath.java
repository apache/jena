/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** SSE Writer */
public class WriterPath implements PathVisitor
{

    public static void write(Path path, Prologue prologue)
    {
        WriterPath w = new WriterPath(IndentedWriter.stdout, prologue) ;
        path.visit(w) ;
        w.out.flush();
    }
    
    public static String asString(Path path) { return asString(path, null) ; }
    
    public static String asString(Path path, Prologue prologue)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        WriterPath w = new WriterPath(buff.getIndentedWriter(), prologue) ;
        path.visit(w) ;
        w.out.flush();
        return buff.asString() ;
    }
    
    private IndentedWriter out ;
    private Prologue prologue ;

    WriterPath(IndentedWriter indentedWriter, Prologue prologue) { this.out = indentedWriter ; this.prologue = prologue ;}
    
    //@Override
    public void visit(P_Link pathNode)
    {
        out.print(FmtUtils.stringForNode(pathNode.getNode(), prologue)) ;
    }

    //@Override
    public void visit(P_Alt pathAlt)
    {
        visit2(pathAlt, PathBase.tagAlt) ;
    }

    //@Override
    public void visit(P_Seq pathSeq)
    {
        visit2(pathSeq, PathBase.tagSeq) ;
    }

    private void visit2(P_Path2 path2, String nodeName)
    {
        out.print("(") ;
        out.print(nodeName) ;
        out.println() ;
        out.incIndent() ;
        path2.getLeft().visit(this) ;
        out.println() ;
        path2.getRight().visit(this) ;
        out.decIndent() ;
        out.print(" )") ;
    }
    
    //@Override
    public void visit(P_Mod pathMod)
    {
        out.print("(") ;
        out.print(PathBase.tagMod) ;
        out.print(" ") ;
        out.print(Long.toString(pathMod.getMin())) ;
        out.print(" ") ;
        out.print(Long.toString(pathMod.getMax())) ;
        out.println() ;
        out.incIndent() ;
        pathMod.getSubPath().visit(this) ;
        out.decIndent() ;
        out.print(" )") ;
    }

    public void visit(P_Reverse reversePath)
    {
        out.print("(") ;
        out.print(PathBase.tagReverse) ;
        out.println() ;
        out.incIndent() ;
        reversePath.getSubPath().visit(this) ;
        out.decIndent() ;
        out.print(" )") ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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