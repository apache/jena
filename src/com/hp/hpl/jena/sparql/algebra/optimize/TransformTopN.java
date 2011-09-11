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
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.Op1 ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced ;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice ;
import com.hp.hpl.jena.sparql.algebra.op.OpTopN ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class TransformTopN extends TransformCopy {

	private static final int defaultTopNSortingThreshold = 1000;
	public static final Symbol externalSortBufferSize = ARQConstants.allocSymbol("topNSortingThreshold") ;

    @Override
	public Op transform(OpSlice opSlice, Op subOp) { 
        /* 
         * This looks for all the follwoing cases of slice with optionally 
         * distinct and/or project follow by order
         * 
         *  + slice order
         *  + slice distinct|reduced order
         *  + slice project order
         *  but not:
         *  + slice distinct project order
         *
         * In detail:
         * 
         * Case 1:
         *  (slice X N
         *   (order (cond) PATTERN) )
         * ==> 
         * (slice X _
         *   (top (X+N cond) PATTERN) )
         * 
         * Case 2:
         * (slice X N
         *   (distinct or reduced
         *     (order (cond) PATTERN) ))
         * ==>  
         * (slice X _
         *   (top (X+N cond) (distinct PATTERN))
         *   
         * Case 3: 
         * (slice X N
         *   (project (vars)
         *     (order (cond) 
         *         PATTERN ))))
         * ==>
         * (slice X _
         *   (project (vars) 
         *     (top (X+N cond) (distinct PATTERN)))
         * The project can also be over the slice.    
         *
         * The case of (slice (distinct (project (vars) (order ...))))
         * does not work because distinct-project menas we do not know how
         * but to make topN buffer.
         * 
         * When there is no project, we can push the distinct under the topN,
         * but because the sort variables may include one projected away, it's not possible
         * to do this with project.  The key is that distinct-project can change the number
         * of rows in ways that mean we can not predict the topN slice.
         *
         * A partial optimization is to see if "cond" only uses projected variables could be considered.
         * We could add project understanding to topN.
         *     
         * Note that in TransformDistinctToReduced (distinct (order X)) is turned into (reduced (order X))
         * and that this optimization should be before that one but this is order independent
         * as we process reduced or distinct in the same way. 
         */
        
        if ( opSlice.getLength() == Query.NOLIMIT )
            return super.transform(opSlice, subOp) ;
        
        int threshold = (Integer)ARQ.getContext().get(externalSortBufferSize, defaultTopNSortingThreshold) ;
        long offset = ( opSlice.getStart() != Query.NOLIMIT ) ? opSlice.getStart() : 0L ;

        if ( offset + opSlice.getLength() >= threshold )
            return super.transform(opSlice, subOp) ;
            
        if ( subOp instanceof OpOrder ) 
        {
            // First case: slice-order
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
            // Second case: slice-distinct-order or slice-reduced-order 
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

        if ( subOp instanceof OpProject )
        {
            // Third case: slice-project-order 
            Op subSubOp = ((Op1)subOp).getSubOp() ;
            if ( subSubOp instanceof OpOrder ) 
            {
                OpProject opProject = (OpProject)subOp ;
                OpOrder opOrder = (OpOrder)subSubOp ;
                // NB leave project over topN, unlike the distinct case where distinct goes under topN.
                OpTopN opTopN = new OpTopN( opOrder.getSubOp(), (int)(offset+opSlice.getLength()), opOrder.getConditions() ) ;
                Op proj = new OpProject(opTopN, opProject.getVars()) ;
                if ( offset == 0 ) {
                    return proj ;
                } else {
                    return new OpSlice( proj, offset, Query.NOLIMIT ) ;                       
                }
            }
        }

    	// Pass through.
    	return super.transform(opSlice, subOp) ; 
   	}
	
}
