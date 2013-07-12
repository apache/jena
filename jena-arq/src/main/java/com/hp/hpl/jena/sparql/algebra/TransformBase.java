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

public class TransformBase implements Transform
{
    @Override
    public Op transform(OpTable opTable)                    { return opTable ; }
    @Override
    public Op transform(OpBGP opBGP)                        { return opBGP ; }
    @Override
    public Op transform(OpTriple opTriple)                  { return opTriple ; }
    @Override
    public Op transform(OpQuad opQuad)                      { return opQuad ; }
    @Override
    public Op transform(OpPath opPath)                      { return opPath ; } 

    @Override
    public Op transform(OpProcedure opProc, Op subOp)       { return opProc ; }
    @Override
    public Op transform(OpPropFunc opPropFunc, Op subOp)    { return opPropFunc ; }

    @Override
    public Op transform(OpDatasetNames dsNames)             { return dsNames ; }
    @Override
    public Op transform(OpQuadPattern quadPattern)          { return quadPattern ; }
    @Override
    public Op transform(OpQuadBlock quadBlock)              { return quadBlock ; }
    
    @Override
    public Op transform(OpFilter opFilter, Op subOp)        { return opFilter ; }
    @Override
    public Op transform(OpGraph opGraph, Op subOp)          { return opGraph ; } 
    @Override
    public Op transform(OpService opService, Op subOp)      { return opService ; } 

    @Override
    public Op transform(OpAssign opAssign, Op subOp)        { return opAssign ; }
    @Override
    public Op transform(OpExtend opExtend, Op subOp)        { return opExtend ; }
    
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)           { return opJoin ; }
    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)   { return opLeftJoin ; }
    @Override
    public Op transform(OpDiff opDiff, Op left, Op right)           { return opDiff ; }
    @Override
    public Op transform(OpMinus opMinus, Op left, Op right)         { return opMinus ; }
    @Override
    public Op transform(OpUnion opUnion, Op left, Op right)         { return opUnion ; }
    @Override
    public Op transform(OpConditional opCond, Op left, Op right)    { return opCond ; } 
    
    @Override
    public Op transform(OpSequence opSequence, List<Op> elts)       { return opSequence ; }
    @Override
    public Op transform(OpDisjunction opDisjunction, List<Op> elts) { return opDisjunction ; }

    @Override
    public Op transform(OpExt opExt)                        { return opExt ; }
    @Override
    public Op transform(OpNull opNull)                      { return opNull ; }
    @Override
    public Op transform(OpLabel opLabel, Op subOp)          { return opLabel ; }
    
    @Override
    public Op transform(OpList opList, Op subOp)            { return opList ; }
    @Override
    public Op transform(OpOrder opOrder, Op subOp)          { return opOrder ; }
    @Override
    public Op transform(OpTopN opTop, Op subOp)             { return opTop ; }
    @Override
    public Op transform(OpProject opProject, Op subOp)      { return opProject ; }
    @Override
    public Op transform(OpDistinct opDistinct, Op subOp)    { return opDistinct ; }
    @Override
    public Op transform(OpReduced opReduced, Op subOp)      { return opReduced ; }
    @Override
    public Op transform(OpSlice opSlice, Op subOp)          { return opSlice ; }
    @Override
    public Op transform(OpGroup opGroup, Op subOp)          { return opGroup ; }
}
