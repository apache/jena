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

package com.hp.hpl.jena.sparql.algebra;

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.op.* ;

public class TransformBase implements Transform
{
    public Op transform(OpTable opTable)                    { return opTable ; }
    public Op transform(OpBGP opBGP)                        { return opBGP ; }
    public Op transform(OpTriple opTriple)                  { return opTriple ; }
    public Op transform(OpPath opPath)                      { return opPath ; } 

    public Op transform(OpProcedure opProc, Op subOp)       { return opProc ; }
    public Op transform(OpPropFunc opPropFunc, Op subOp)    { return opPropFunc ; }

    public Op transform(OpDatasetNames dsNames)             { return dsNames ; }
    public Op transform(OpQuadPattern quadPattern)          { return quadPattern ; }
    
    public Op transform(OpFilter opFilter, Op subOp)        { return opFilter ; }
    public Op transform(OpGraph opGraph, Op subOp)          { return opGraph ; } 
    public Op transform(OpService opService, Op subOp)      { return opService ; } 

    public Op transform(OpAssign opAssign, Op subOp)        { return opAssign ; }
    public Op transform(OpExtend opExtend, Op subOp)        { return opExtend ; }
    
    public Op transform(OpJoin opJoin, Op left, Op right)           { return opJoin ; }
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)   { return opLeftJoin ; }
    public Op transform(OpDiff opDiff, Op left, Op right)           { return opDiff ; }
    public Op transform(OpMinus opMinus, Op left, Op right)         { return opMinus ; }
    public Op transform(OpUnion opUnion, Op left, Op right)         { return opUnion ; }
    public Op transform(OpConditional opCond, Op left, Op right)    { return opCond ; } 
    
    public Op transform(OpSequence opSequence, List<Op> elts)       { return opSequence ; }
    public Op transform(OpDisjunction opDisjunction, List<Op> elts) { return opDisjunction ; }

    public Op transform(OpExt opExt)                        { return opExt ; }
    public Op transform(OpNull opNull)                      { return opNull ; }
    public Op transform(OpLabel opLabel, Op subOp)          { return opLabel ; }
    
    public Op transform(OpList opList, Op subOp)            { return opList ; }
    public Op transform(OpOrder opOrder, Op subOp)          { return opOrder ; }
    public Op transform(OpTopN opTop, Op subOp)             { return opTop ; }
    public Op transform(OpProject opProject, Op subOp)      { return opProject ; }
    public Op transform(OpDistinct opDistinct, Op subOp)    { return opDistinct ; }
    public Op transform(OpReduced opReduced, Op subOp)      { return opReduced ; }
    public Op transform(OpSlice opSlice, Op subOp)          { return opSlice ; }
    public Op transform(OpGroup opGroup, Op subOp)          { return opGroup ; }
}
