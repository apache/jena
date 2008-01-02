/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpSubstitute;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class QueryIterSQL extends QueryIterRepeatApply
{

    private OpSQL opSQL ;
    
    public QueryIterSQL(OpSQL op, 
                        QueryIterator input ,
                        ExecutionContext context)
    { 
        super(input, context) ;
        this.opSQL = op ;
    }
    
    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        OpSQL execSQL = this.opSQL ;

        if ( binding != null && ! isRoot(binding) )
        {
            QueryCompiler qc = opSQL.getRequest().getStore().getQueryCompilerFactory().createQueryCompiler(opSQL.getRequest()) ;
            Op op2 = OpSubstitute.substitute(opSQL.getOriginal(), binding) ;
            Op op = qc.compile(op2) ;
            if ( op instanceof OpSQL )
                execSQL = (OpSQL)op ;
            else
                throw new SDBInternalError("Failed to recompile the OpSQL to an OpSQL") ;
        }

        return execSQL.exec(binding, getExecContext()) ;
    }
    
    private static boolean isRoot(Binding binding)
    {
        return ! binding.vars().hasNext() ; 
    }

    @Override
    public void output(IndentedWriter out)
    {
        opSQL.output(out) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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