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

package com.hp.hpl.jena.sparql.algebra;

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.op.* ;

public interface Transform
{
    // Op0
    public Op transform(OpTable opUnit) ;
    public Op transform(OpBGP opBGP) ;
    public Op transform(OpTriple opTriple) ;
    public Op transform(OpQuad opQuad) ;
    public Op transform(OpPath opPath) ;
    public Op transform(OpDatasetNames dsNames) ;
    public Op transform(OpQuadPattern quadPattern) ;
    public Op transform(OpQuadBlock quadBlock) ;
    public Op transform(OpNull opNull) ;
    
    // Op1
    public Op transform(OpFilter opFilter, Op subOp) ;
    public Op transform(OpGraph opGraph, Op subOp) ;
    public Op transform(OpService opService, Op subOp) ;
    public Op transform(OpProcedure opProcedure, Op subOp) ;
    public Op transform(OpPropFunc opPropFunc, Op subOp) ;
    public Op transform(OpLabel opLabel, Op subOp) ;
    public Op transform(OpAssign opAssign, Op subOp) ;
    public Op transform(OpExtend opExtend, Op subOp) ;

    // Op2
    public Op transform(OpJoin opJoin, Op left, Op right) ;
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) ;
    public Op transform(OpDiff opDiff, Op left, Op right) ;
    public Op transform(OpMinus opMinus, Op left, Op right) ;
    public Op transform(OpUnion opUnion, Op left, Op right) ;
    public Op transform(OpConditional opCondition, Op left, Op right) ;
    
    // OpN
    public Op transform(OpSequence opSequence, List<Op> elts) ;
    public Op transform(OpDisjunction opDisjunction, List<Op> elts) ;

    // Extensions
    public Op transform(OpExt opExt) ;
    
    // OpModifier
    public Op transform(OpList opList, Op subOp) ;
    public Op transform(OpOrder opOrder, Op subOp) ;
    public Op transform(OpTopN opTop, Op subOp) ;
    public Op transform(OpProject opProject, Op subOp) ;
    public Op transform(OpDistinct opDistinct, Op subOp) ;
    public Op transform(OpReduced opReduced, Op subOp) ;
    public Op transform(OpSlice opSlice, Op subOp) ;
    public Op transform(OpGroup opGroup, Op subOp) ;
}
