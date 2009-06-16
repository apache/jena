/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.util.Context;

/** Environment passed to functions -- see also {@link com.hp.hpl.jena.sparql.engine.ExecutionContext} */

public class FunctionEnvBase implements FunctionEnv
{
    private Context context ;
    private Graph activeGraph ;
    private DatasetGraph dataset ;
    private ExecutionContext execContext = null ;

    /** Create an execution environment suitable for testing fucntions and expressions */ 
    public static FunctionEnv createTest()
    {
        return new FunctionEnvBase(ARQ.getContext()) ;
    }
    
    public FunctionEnvBase() { this(null, null, null) ; }
    
    public FunctionEnvBase(Context context) { this ( context, null, null) ; }
    
    public FunctionEnvBase(ExecutionContext execCxt)
    { 
        this(execCxt.getContext(), execCxt.getActiveGraph(), execCxt.getDataset()) ;
        execContext = execCxt ;
    }

    public FunctionEnvBase(Context context, Graph activeGraph, DatasetGraph dataset)
    {
        this.context = context ;
        this.activeGraph = activeGraph ;
    }

    public Graph getActiveGraph()
    {
        return activeGraph ;
    }

    public Context getContext()
    {
        return context ;
    }

//    public ExecutionContext getExecutionContext()
//    {
//        if ( execContext == null )
//            execContext = new ExecutionContext(context, activeGraph, dataset, QC.getFactory(context)) ;
//        return execContext ;
//    }

    public DatasetGraph getDataset()
    {
        return dataset ;
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