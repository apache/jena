/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package opt;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.sse.SSE;

public class RunT
{
    public static void main(String[] argv) throws Exception
    {
        rewrite() ;
        System.exit(0) ;
    }
    
    /*
     * Move PF to Transforms
     * 
     * TransformFilterPlacement  [DONE] - not enabled, yet.
     * TransformPropertyFunction [DONE] - not enabled, yet.
     * Simplify [DONE] 
     *    - algebra.opt.TransformSimplify 
     *    -- called by AlgebraGenerator because of SimplifyEarly
     * Equality filter [DONE]
     *    - algebra.opt.TransformEqualityFilter
     *    -- called via Algebra.compile(,optimize)
     *    
     * TransformRemoveLabels
     * TransformReoderBGP
     */
    
    public static void rewrite()
    {
        // Stage 0 - always
        //    Simplify
        // Stage 1 - general algebra rewrites
        //    ? Filter placement
        //    ? Equality filter
        // Stage 2 - per execution -- context and dataset available.
        //    ? Property function
        //    ? BGP rewrites
        
        Op op = SSE.readOp("Q.sse") ;
        // Always in algebra
        //op = apply("Simplify", new TransformSimplify(), op) ;
        //op = apply("Delabel", new TransformRemoveLabels(), op) ;
        
        op = apply("Property Functions", new TransformPropertyFunction(ARQ.getContext()), op) ;
        // 
//        op = apply("Filter placement 1", new TransformFilterPlacement(), op) ;
//        op = apply("Filter placement 2", new TransformFilterPlacement(), op) ;  // No-op
    }
    
    static Op apply(String label, Transform transform, Op op)
    {
        System.out.println("**** "+label) ;
        Op op2 = Transformer.transform(transform, op) ;
        if ( op == op2 ) 
        {
            System.out.println("No change (==)") ;
            System.out.println() ;
            return op2 ;
        }
        
        if ( op.equals(op2) ) 
        {
            System.out.println("No change (equals)") ;
            System.out.println() ;
            return op2 ;
        }
        
        System.out.println(op) ;
        System.out.println(op2) ;
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