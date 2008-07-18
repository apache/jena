/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.opt;

import opt.Rewrite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.util.Context;

public class Optimize implements Rewrite
{
    static private Log log = LogFactory.getLog(Optimize.class) ;
    // Optimize at query execution time.

    public static Op optimize(Op op, ExecutionContext execCxt)
    {
        Context context = execCxt.getContext() ;
        // Look up to find the rewriter
        Rewrite opt = new Optimize(execCxt) ;
        op = opt.rewrite(op) ;
        return op ;
    }

    // The execution-independent optimizations
    public static Op optimize(Op op, Context context)
    {
        Rewrite opt = new Optimize(context) ;
        return opt.rewrite(op) ;
    }

    private final Context context ;
    private Optimize(ExecutionContext execCxt)
    {
        this(execCxt.getContext()) ;
    }
    
    private Optimize(Context context)
    {
        this.context = context ;
    }
    

    public Op rewrite(Op op)
    {
        if ( false )
        {
            op = apply("Simplify", new TransformSimplify(), op) ;
            op = apply("Delabel", new TransformRemoveLabels(), op) ;
        }
        // TODO Improve filter placement to go through assigns that have no effect.
        // Do this before filter placement and other sequence generating transformations.
        // or improve to place in a sequence. 
        op = apply("Filter Equality", new TransformEqualityFilter(), op) ;
        //op = apply("Property Functions", new TransformPropertyFunction(context), op) ;
        op = apply("Filter placement", new TransformFilterPlacement(), op) ;
        
        // Mark
        op = OpLabel.create("Transformed", op) ;
        return op ;
    }

    static Op apply(String label, Transform transform, Op op)
    {
        Op op2 = Transformer.transform(transform, op) ;
        
        final boolean debug = false ;
        
        if ( debug )
        {
            log.info("Transform: "+label) ;
            if ( op == op2 ) 
            {
                log.info("No change (==)") ;
                return op2 ;
            }

            if ( op.equals(op2) ) 
            {
                log.info("No change (equals)") ;
                return op2 ;
            }
            log.info("\n"+op.toString()) ;
            log.info("\n"+op2.toString()) ;
        }
        return op2 ;
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