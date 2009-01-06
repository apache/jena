/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.procedure ;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.IterLib;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase;

public abstract class ProcedureBase extends PrintSerializableBase implements Procedure
{
    private Node procId ;
    private ExprList args ;

    public void build(Node procId, ExprList args, ExecutionContext execCxt)
    {
        this.procId = procId ;
        this.args = args ;
    }
 
    public final QueryIterator proc(QueryIterator input, ExecutionContext execCxt)
    {
        return new RepeatApplyIterator(input, execCxt) ;
    }
    
    public abstract QueryIterator exec(Binding binding, Node name, ExprList args, ExecutionContext execCxt) ;
    
    
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.print("Procedure ["+FmtUtils.stringForNode(procId, sCxt)+"]") ;
        out.print("[") ;
        out.print(args.toString()) ;
        out.print("]") ;
        out.println() ;
    }
    
    class RepeatApplyIterator extends QueryIterRepeatApply
    {
        private ExecutionContext execCxt ;
        private Node name ;
        
       public RepeatApplyIterator(QueryIterator input, ExecutionContext execCxt)
       { 
           super(input, execCxt) ;
       }

        @Override
        protected QueryIterator nextStage(Binding binding)
        {
            QueryIterator iter = exec(binding, name, args, super.getExecContext()) ;
            if ( iter == null ) 
                iter = IterLib.noResults(execCxt) ;
            return iter ;
        }
        
        @Override
        protected void details(IndentedWriter out, SerializationContext sCxt)
        {
            out.print("Procedure ["+FmtUtils.stringForNode(name, sCxt)+"]") ;
            WriterExpr.output(out, args, sCxt) ;
            out.println() ;
        }
    }

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */