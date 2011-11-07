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
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

/* Improvements to filters that do not change the rest of the tree 
 * (so, for example, not filter replacement or equality/assignment
 *  which both do change the sub op of the filter).  
 * 
 * Filter placment and equality/assignment interact.
 * Maybe need one place for all filter-related stuff, in which case this is becomes a library of code,
 * hence the statics for the real work. 
 */

/** Redo FILTER (A&&B) as FILTER(A) FILTER(B) (as an expr list).
 *    via multiple elements of the exprList of the OpFilter.
 *    This allows them to be placed independently.
 */

public class TransformFilterConjunction extends TransformCopy
{
    public TransformFilterConjunction() {}
    
    @Override
    public Op transform(OpFilter opFilter, Op subOp)
    {
        ExprList exprList = opFilter.getExprs() ;
        exprList = ExprList.splitConjunction(exprList) ;
        // Do not use -- OpFilter.filter(exprList, subOp) -- it compresses (filter (..) (filter ))
        return OpFilter.filterDirect(exprList, subOp) ;
    }

}
