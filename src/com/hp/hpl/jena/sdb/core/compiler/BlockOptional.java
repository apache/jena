/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;

public class BlockOptional extends BlockBase
{
    Block left ;
    Block right ;
    
    public BlockOptional(Block left, Block right)
    {
        this.left = left ;
        this.right = right ;
    }

    public Block getLeft() { return left ; }

    public Block getRight() { return right ; }

    public Set<Var> getDefinedVars()
    {
        // TODO Make this more efficient if it is used much
        Set<Var> s1 = left.getDefinedVars() ;
        Set<Var> s2 = right.getDefinedVars() ;
        Set<Var> x = new LinkedHashSet<Var>() ;
        x.addAll(s1) ;
        x.addAll(s2) ;
        return x ;
    }

    
    
    public SqlNode generateSQL(CompileContext context, QueryCompilerBase queryCompiler)
    {
        return queryCompiler.compile(this, context) ;
    }
    
    public Block substitute(Binding binding)
    {
        return new BlockOptional(left.substitute(binding),
                                 right.substitute(binding)) ;
    }

    public void output(IndentedWriter out)
    {
        out.ensureStartOfLine() ;
        out.println("(Optional") ;
        out.incIndent(INDENT) ;
        left.output(out) ;
        out.println();
        right.output(out) ;
        out.decIndent(INDENT) ;
        out.print(")") ;
    }


}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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