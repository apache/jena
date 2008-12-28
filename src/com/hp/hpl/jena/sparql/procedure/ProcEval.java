/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.procedure;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.algebra.op.OpProcedure;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.pfunction.*;
import com.hp.hpl.jena.sparql.util.Context;

public class ProcEval
{
    public static Procedure build(OpProcedure opProc, ExecutionContext execCxt)
    {
        return build(opProc.getProcId(), opProc.getArgs(), execCxt) ;
    }
    
    // ----
    
    public static Procedure build(Node procId, ExprList args, ExecutionContext execCxt)
    {
        Context context = execCxt.getContext() ;
        ProcedureRegistry reg = chooseProcedureRegistry(context) ;
        ProcedureFactory f = reg.get(procId.getURI()) ;
        Procedure proc = f.create(procId.getURI()) ;
        args.prepareExprs(context) ;        // Allow args to build as well.
        proc.build(procId, args, execCxt) ;
        return proc ;
    }
    
    private static ProcedureRegistry chooseProcedureRegistry(Context context)
    {
        ProcedureRegistry registry = ProcedureRegistry.get(context) ;
        // Else global
        if ( registry == null )
            registry = ProcedureRegistry.get() ;
        return registry ;
    }
    
    // ----
    
    public static Procedure build(Node procId, PropFuncArg subjArg, PropFuncArg objArg, ExecutionContext execCxt)
    {
        Context context = execCxt.getContext() ;
        PropertyFunctionRegistry reg = choosePropFuncRegistry(context) ;
        PropertyFunctionFactory f = reg.get(procId.getURI()) ;
        PropertyFunction pf = f.create(procId.getURI()) ;
        pf.build(subjArg, procId, objArg, execCxt) ;
        //Make wrapper
        return new ProcedurePF(pf, subjArg, procId, objArg) ;
    }
    
 
    static public PropertyFunctionRegistry choosePropFuncRegistry(Context context)
    {
        PropertyFunctionRegistry registry = PropertyFunctionRegistry.get(context) ; 
        if ( registry == null )
            registry = PropertyFunctionRegistry.get() ;
        return registry ;
    }
    
    // ----

//    /** Evaluate a procedure */
//    public static QueryIterator eval(QueryIterator queryIterator, Procedure proc)
//    {
//        return eval(queryIterator, proc, null) ;
//    }
//
    /** Evaluate a procedure */
    public static QueryIterator eval(QueryIterator queryIterator, Procedure proc, ExecutionContext execCxt)
    {
        return proc.proc(queryIterator, execCxt) ;
    }

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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