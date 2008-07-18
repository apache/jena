/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package opt;

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.algebra.opt.TransformPropertyFunction;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.util.Context;

public class Optimize implements Rewrite
{
    // Optimize at query execution time.

    public static Op optimize(Op op, ExecutionContext execCxt)
    {
        Context context = execCxt.getContext() ;
        // Look up to find the rewriter
        Rewrite opt = new Optimize(execCxt) ;
        op = opt.rewrite(op) ;
        op = new OpLabel("Optimized", op) ; // Debug
        return op ;
    }

    // The execution-independent optimizations
    public static Op optimize(Op op, Context context)
    {
        //Rewrite opt = new Optimize() ;
        //return opt.rewrite(op) ;
        throw new ARQNotImplemented("optimize(op, context)") ;
        //return null ;
    }

    private final Context context ;
    private Optimize(ExecutionContext execCxt)
    {
        this.context = execCxt.getContext() ;
    }

    public Op rewrite(Op op)
    {
        //op = apply("Simplify", new TransformSimplify(), op) ;
        //op = apply("Delabel", new TransformRemoveLabels(), op) ;
        
        op = apply("Property Functions", new TransformPropertyFunction(context), op) ;
        op = apply("Filter placement", new TransformFilterPlacement(), op) ;
        
        return op ;
    }

    static Op apply(String label, Transform transform, Op op)
    {
        Op op2 = Transformer.transform(transform, op) ;
//        if ( op == op2 ) 
//        {
//            System.out.println("No change (==)") ;
//            System.out.println() ;
//            return op2 ;
//        }
//        
//        if ( op.equals(op2) ) 
//        {
//            System.out.println("No change (equals)") ;
//            System.out.println() ;
//            return op2 ;
//        }
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