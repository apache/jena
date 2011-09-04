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

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.Op1 ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder ;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced ;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice ;
import com.hp.hpl.jena.sparql.algebra.op.OpTopN ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class TransformTopN extends TransformCopy {

	private static final int defaultTopNSortingThreshold = 1000;
	public static final Symbol externalSortBufferSize = ARQConstants.allocSymbol("topNSortingThreshold") ;

    @Override
	public Op transform(OpSlice opSlice, Op subOp) { 

        /* This looks for two cases:
         * (slice X N
         *   (order (cond) PATTERN) )
         * ==> 
         * (slice X _
         *   (top (X+N cond) PATTERN) )
         * 
         * and
         * 
         * (slice X N
         *   (distinct or reduced
         *     (order (cond) PATTERN) ))
         * ==>  
         * (slice X _
         *   (top (X+N cond) (distinct PATTERN))
         *
         * and evaluation of (top) looks for (top N (distinct PATTERN))
         * See OpExecutor.execute(OpTopN)
         * 
         * Note that in TransformDistinctToReduced (distinct (order X)) is turned into (reduced (order X))
         * and that this optimization should be before that one but this is order independent
         * as we process reducded or distinct in the same way. 
         */
        
        int threshold = (Integer)ARQ.getContext().get(externalSortBufferSize, defaultTopNSortingThreshold) ;
        long offset = ( opSlice.getStart() != Query.NOLIMIT ) ? opSlice.getStart() : 0L ;
    	if ( offset + opSlice.getLength() < threshold )
    	{
        	if ( subOp instanceof OpOrder ) 
        	{
        	    // First case.
        	    OpOrder opOrder = (OpOrder)subOp ;
                OpTopN opTopN = new OpTopN( opOrder.getSubOp(), (int)(offset+opSlice.getLength()), opOrder.getConditions() ) ;
        	    if ( offset == 0 ) {
        	        return opTopN ;
        	    } else {
                    return new OpSlice( opTopN, offset, Query.NOLIMIT ) ;        	        
        	    }
        	}
            	
        	if ( subOp instanceof OpDistinct || subOp instanceof OpReduced )
        	{
        	    Op subSubOp = ((Op1)subOp).getSubOp() ;
        	    if ( subSubOp instanceof OpOrder ) {
        	        OpOrder opOrder = (OpOrder)subSubOp ;
        	        Op opDistinct2 = OpDistinct.create(opOrder.getSubOp()) ;
        	        OpTopN opTopN = new OpTopN( opDistinct2, (int)(offset+opSlice.getLength()), opOrder.getConditions() ) ; 
        	        if ( offset == 0 ) {
        	            return opTopN ;
        	        } else {
                        return new OpSlice( opTopN, offset, Query.NOLIMIT ) ;         	            
        	        }
        	    }
        	}
    	}

    	// Pass through.
    	return super.transform(opSlice, subOp) ; 
   	}
	
}
