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

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder ;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced ;

public class TransformDistinctToReduced extends TransformCopy {

    // Best is this is after TransformTopN but they are order independent
    // TopN of "reduced or distinct of order" is handled. 
    @Override
    public Op transform(OpDistinct opDistinct, Op subOp) { 
        if ( subOp instanceof OpOrder ) {
            return OpReduced.create(subOp) ;
        }
        return super.transform(opDistinct, subOp) ; 
    }

}

