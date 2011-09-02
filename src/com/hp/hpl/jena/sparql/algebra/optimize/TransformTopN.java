/**
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

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.Op1 ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder ;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced ;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice ;
import com.hp.hpl.jena.sparql.algebra.op.OpTopN ;

public class TransformTopN extends TransformCopy {

	public static final int TOPN_LIMIT_THRESHOLD = 100 ;
	
    @Override
	public Op transform(OpSlice opSlice, Op subOp) { 

        /* This looks for two cases:
         * (slice _  N
         *   (order (cond) PATTERN) )
         * ==> (top (N cond) PATTERN)
         * and
         * 
         * (slice _  N
         *   (distinct or reduced
         *     (order (cond) PATTERN) ))
         * ==>  (top (N cond) (distinct PATTERN))
         *
         * and evaluation of (top) looks for (top N (distinct PATTERN))
         * See OpExecutor.execute(OpTopN)
         * 
         * Note that in TransformDistinctToReduced (distinct (order X)) is turned into (reduced (order X))
         * and that this optimization should be before that one but this is order independent
         * as we process reducded or distinct in the same way. 
         */
        
        boolean acceptableStart = ( ( opSlice.getStart() == 0 ) || ( opSlice.getStart() == Query.NOLIMIT ) ) ;
        boolean acceptableFinish =  (opSlice.getLength() < TOPN_LIMIT_THRESHOLD ) ;  
        
    	if ( acceptableStart && acceptableFinish )
    	{
        	if ( subOp instanceof OpOrder ) 
        	{
        	    // First case.
        	    OpOrder opOrder = (OpOrder)subOp ;
        	    return new OpTopN( opOrder.getSubOp(), (int)opSlice.getLength(), opOrder.getConditions() ) ;
        	}
            	
        	if ( subOp instanceof OpDistinct || subOp instanceof OpReduced )
        	{
        	    Op subSubOp = ((Op1)subOp).getSubOp() ;
        	    if ( subSubOp instanceof OpOrder ) {
        	        OpOrder opOrder = (OpOrder)subSubOp ;
        	        Op opDistinct2 = OpDistinct.create(opOrder.getSubOp()) ;
        	        return new OpTopN( opDistinct2, (int)opSlice.getLength(), opOrder.getConditions() ) ;
        	    }
        	}
    	}
    	
    	// Pass through.
    	return super.transform(opSlice, subOp) ; 
   	}
	
}
