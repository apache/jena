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

import java.util.List ;
import java.util.Set ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.ExprVars ;
import com.hp.hpl.jena.sparql.util.Symbol ;


/**
 * Optimization that changes queries that uses <tt>OFFSET/LIMIT</tt> and <tt>ORDER BY</tt>
 * to execute using <tt>Top N</tt>: i.e. while executing, keep only the top N items seen.  
 * This avoids full sort of the whole results, saving space and time.   
 */
public class TransformTopN extends TransformCopy {

	private static final int defaultTopNSortingThreshold = 1000;
	public static final Symbol externalSortBufferSize = ARQConstants.allocSymbol("topNSortingThreshold") ;

	/* For reference: from the algebra generation of a query, the order of operations is: 
	 *  Limit/Offset
	 *   Distinct/reduce
	 *     Project
	 *       OrderBy
	 *         Values
	 *           Having
	 *             Select Expressions
	 *               Group
	 * but note that a subquery can be used to create other orders.                 
	 */

    @Override
	public Op transform(final OpSlice opSlice, final Op inSubOp) { 
        /* 
         * This looks for all the following cases of slice with optionally 
         * distinct and/or project follow by order. It is quicker to execute
         * by avoiding the full sort, just track the top items. 
         * 
         *  + slice-order                   => topN
         *  + slice-distinct|reduced-order  => top-distinct
         *  + slice-project-order           => project-top 
         *  + slice distinct project order  => topN distinct project  (only some cases)  
         *
         * If the slice has an offset, a (slice X _) is added. (slice 0 _) is a no-op and it not added.   
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
         *     (top (X+N cond) PATTERN) ))
         *
         * Case 4:
         * (slice X N
         *   (distinct 
         *     (project (vars) 
         *       (order (cond) PATTERN) )))
         * ==> 
         * If project-order can be swapped, 
         * (slice X N
         *   (top (X+N) (cond)
         *    (distinct
         *      (project (vars)   
         *         PATTERN) )))
         * 
         * Care needed: because of the need to keep distinct, we can't
         * process like case 3. Reversing (order) and (project) can only
         * be done if the projection variables include all variables used
         * by the sort conditions.
         * 
         * When there is no project, we can push the distinct under the topN,
         * but, when there is, the sort variables may include one projected away,
         * it's not possible to do this with project.  The key is that 
         * distinct-project can change the number of rows in ways that mean
         * we can not predict the topN slice.
         */
        
        /* Algorthm:
         *    Test to see if the slice is small enough.
         *    Extract distinct/reduce, and projection details. 
         *    Is it an (order)? If no - not applicable. 
         * 
         * If slice-project-order
         *   Treat as project-slice-order
         *   Output project-top
         * If slice-distinct-project-order
         *   Test to see if project and order can swap
         *   If they can, output top-distinct-project
         *   else no action.
         *  
         * Add a slice if there was an OFFSET. 
         * Distinct and reduce are treated as distinct.
         */
        
        Op subOp = inSubOp ;
        
        if ( opSlice.getLength() == Query.NOLIMIT )
            return doNothing(opSlice, inSubOp) ;
        long limit = opSlice.getLength() ;
        long offset = ( opSlice.getStart() != Query.NOLIMIT ) ? opSlice.getStart() : 0L ;
        long N = limit+offset ;
        
        int threshold = (Integer)ARQ.getContext().get(externalSortBufferSize, defaultTopNSortingThreshold) ;

        if ( N >= threshold )
            return doNothing(opSlice, inSubOp) ;
        
        boolean distinct = false ;
        boolean reduce   = false ;
        // Extract any distinct/reduce.
        if ( subOp instanceof OpDistinct ) {
            distinct = true ;
            subOp = ((Op1)subOp).getSubOp() ;
        } else if ( subOp instanceof OpReduced ) {
            distinct = true ;
            subOp = ((Op1)subOp).getSubOp() ;
        }
        
        // Extract any projection.
        List<Var> projection = null ;
        if ( subOp instanceof OpProject ) {
            OpProject opProject = (OpProject)subOp ;
            projection = opProject.getVars() ;
            subOp = opProject.getSubOp() ;
        }
        
        if ( ! ( subOp instanceof OpOrder ) )
            return doNothing(opSlice, inSubOp) ;
        // We have found an (order)
        OpOrder opOrder = (OpOrder)subOp ;
        subOp = opOrder.getSubOp() ;
        
        // Check safety for the distinct/reduce case  
        
        if ( (reduce || distinct) && projection != null ) {
            List<SortCondition> sortConditions = opOrder.getConditions() ;            
            Set<Var> orderVars = ExprVars.getVarsMentioned(sortConditions) ;
            
            // Slice and distinct interact.
            
            // Is the ordering stable with respect to the project? 
            //  i.e. can we swap them and put a (top) for slice-order  
            // All project vars must be in the order so we can have:
            // slice-distinct-project-order => top-distinct-project 
            
            // If the projection is narrower than the order it is not safe.

            if ( ! projection.containsAll(orderVars) )
                return doNothing(opSlice, inSubOp) ;
        }
        
        Op newOp = subOp ;
        
        if ( ( reduce || distinct ) && projection != null )
            newOp = new OpProject(newOp, projection) ;
        
        if ( distinct )
            newOp = OpDistinct.create(newOp) ;
        if ( reduce )
            newOp = OpReduced.create(newOp) ;

        newOp = new OpTopN( newOp, (int)N, opOrder.getConditions() ) ;

        if ( ! reduce &&  ! distinct && projection != null )
            newOp = new OpProject(newOp, projection) ;
        
        if ( opSlice.getStart() > 0 )
            newOp = new OpSlice(newOp, opSlice.getStart(), Query.NOLIMIT) ;

        return newOp ;
    }

    /** Marker to indicate that this transform is not applied at this point */
    private Op doNothing(OpSlice opSlice, Op subOp) {
        return super.transform(opSlice, subOp) ;
    }
}
