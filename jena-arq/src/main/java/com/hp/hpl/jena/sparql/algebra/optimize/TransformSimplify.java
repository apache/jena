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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;

public class TransformSimplify extends TransformCopy
{
    @Override
    public Op transform(OpSequence opSequence, List<Op> elts)
    {
        List<Op> x = new ArrayList<>(elts) ;
        for ( Iterator<Op> iter = x.iterator() ; iter.hasNext() ; )
        {
            Op sub = iter.next() ;
            if ( OpJoin.isJoinIdentify(sub) )
                iter.remove();
        }
        return super.transform(opSequence, x) ;
    }
    
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)
    {
        if ( OpJoin.isJoinIdentify(left) )
            return right ;
        if ( OpJoin.isJoinIdentify(right) )
            return left ;
        // Merge adjacent BGPs
        // Also works on nested subqueries that turned out to be simple BGPs.
        
//        if ( OpBGP.isBGP(left) && OpBGP.isBGP(right) )
//        {
//            BasicPattern pattern = new BasicPattern() ;
//            pattern.addAll( ((OpBGP)left).getPattern() ) ;
//            pattern.addAll( ((OpBGP)right).getPattern() ) ;
//            return new OpBGP(pattern) ;
//        }
        
        return super.transform(opJoin, left, right) ;
    }
}
