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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.engine.main.JoinClassifier ;
import com.hp.hpl.jena.sparql.engine.main.LeftJoinClassifier ;

/** Choose join strategy */ 
public class TransformJoinStrategy extends TransformCopy
{
    // OpSequence - linear join
    // OpCondition - linear left join
    public TransformJoinStrategy()
    {}
    
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)
    { 
        // Look one level in for any filters with out-of-scope variables.
        boolean canDoLinear = JoinClassifier.isLinear(opJoin) ;

        if ( canDoLinear )
        {
            if ( right instanceof OpTable )
            {
                // Safe to swap? Need to reclassify.
                boolean b = JoinClassifier.isLinear(right, left) ;
                if ( b )
                {
                    // Swap left and right so start with a flow of concrete data.
                    Op tmp = left ;
                    left = right ; 
                    right = tmp ;
                }
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
