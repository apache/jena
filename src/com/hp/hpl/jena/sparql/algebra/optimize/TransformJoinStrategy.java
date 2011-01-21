/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.engine.main.JoinClassifier ;
import com.hp.hpl.jena.sparql.engine.main.LeftJoinClassifier ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Choose join strategy */ 
public class TransformJoinStrategy extends TransformCopy
{
    private final Context context ;

    // OpSequence - linear join
    // OpCondition - linear left join
    public TransformJoinStrategy(Context context)
    {
        this.context = context ;
    }
    
    
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)
    { 
        // Look one level in for any filters with out-of-scope variables.
        boolean canDoLinear = JoinClassifier.isLinear(opJoin) ;

        if ( canDoLinear )
        {
            if ( right instanceof OpTable )
            {
                // Swap left and right so start with a flow of concrete data.
                Op tmp = left ;
                left = right ; 
                right = tmp ;
            }
            
            // Streamed evaluation
            return OpSequence.create(left, right) ;
        }
        // Can't do better.
        return super.transform(opJoin, left,right) ;
    }
    
    //public Op transform(OpSequence opSequence, List<Op> elts)
    
    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op opLeft, Op opRight)
    { 
      // Test whether we can do an indexed substitute into the right if possible.
      boolean canDoLinear = LeftJoinClassifier.isLinear(opLeftJoin) ;
      
      if ( canDoLinear )
      {
          // Pass left into right for substitution before right side evaluation.
          // In an indexed left join, the LHS bindings are visible to the
          // RHS execution so the expression is evaluated by moving it to be 
          // a filter over the RHS pattern. 
          
          if (opLeftJoin.getExprs() != null )
              opRight = OpFilter.filter(opLeftJoin.getExprs(), opRight) ;
          return new OpConditional(opLeft, opRight) ;
      }

      // Not index-able. 
      return super.transform(opLeftJoin, opLeft, opRight) ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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