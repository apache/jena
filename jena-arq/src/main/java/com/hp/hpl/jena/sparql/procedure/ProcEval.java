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

package com.hp.hpl.jena.sparql.procedure;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.op.OpProcedure ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.pfunction.ProcedurePF ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry ;
import com.hp.hpl.jena.sparql.util.Context ;

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
        PropertyFunctionRegistry reg = PropertyFunctionRegistry.chooseRegistry(context) ;
        PropertyFunctionFactory f = reg.get(procId.getURI()) ;
        PropertyFunction pf = f.create(procId.getURI()) ;
        pf.build(subjArg, procId, objArg, execCxt) ;
        //Make wrapper
        return new ProcedurePF(pf, subjArg, procId, objArg) ;
    }
    
 
    /** Evaluate a procedure */
    public static QueryIterator eval(QueryIterator queryIterator, Procedure proc, ExecutionContext execCxt)
    {
        return proc.proc(queryIterator, execCxt) ;
    }

}
