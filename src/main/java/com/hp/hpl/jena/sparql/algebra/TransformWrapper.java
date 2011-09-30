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

/** Wrap another tranform and pass on the transform operation */
public class TransformWrapper implements Transform
{
    protected final Transform transform ;
    
    public TransformWrapper(Transform transform)
    {
        this.transform = transform ;
    }
    
    public Op transform(OpTable opTable)                    { return transform.transform(opTable) ; }
    public Op transform(OpBGP opBGP)                        { return transform.transform(opBGP) ; }
    public Op transform(OpTriple opTriple)                  { return transform.transform(opTriple) ; }
    public Op transform(OpPath opPath)                      { return transform.transform(opPath) ; } 

    public Op transform(OpProcedure opProc, Op subOp)       { return transform.transform(opProc, subOp) ; }
    public Op transform(OpPropFunc opPropFunc, Op subOp)    { return transform.transform(opPropFunc, subOp) ; }

    public Op transform(OpDatasetNames dsNames)             { return transform.transform(dsNames) ; }
    public Op transform(OpQuadPattern quadPattern)          { return transform.transform(quadPattern) ; }
    
    public Op transform(OpFilter opFilter, Op subOp)        { return transform.transform(opFilter, subOp) ; }
    public Op transform(OpGraph opGraph, Op subOp)          { return transform.transform(opGraph, subOp) ; } 
    public Op transform(OpService opService, Op subOp)      { return transform.transform(opService, subOp) ; } 

    public Op transform(OpAssign opAssign, Op subOp)        { return transform.transform(opAssign, subOp) ; }
    public Op transform(OpExtend opExtend, Op subOp)        { return transform.transform(opExtend, subOp) ; }
    
    public Op transform(OpJoin opJoin, Op left, Op right)           { return transform.transform(opJoin, left, right) ; }
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)   { return transform.transform(opLeftJoin, left, right) ; }
    public Op transform(OpDiff opDiff, Op left, Op right)           { return transform.transform(opDiff, left, right) ; }
    public Op transform(OpMinus opMinus, Op left, Op right)         { return transform.transform(opMinus, left, right) ; }
    public Op transform(OpUnion opUnion, Op left, Op right)         { return transform.transform(opUnion, left, right) ; }
    public Op transform(OpConditional opCond, Op left, Op right)    { return transform.transform(opCond, left, right) ; } 
    
    public Op transform(OpSequence opSequence, List<Op> elts)       { return transform.transform(opSequence, elts) ; }
    public Op transform(OpDisjunction opDisjunction, List<Op> elts) { return transform.transform(opDisjunction, elts) ; }

    public Op transform(OpExt opExt)                        { return transform.transform(opExt) ; }
    public Op transform(OpNull opNull)                      { return transform.transform(opNull) ; }
    public Op transform(OpLabel opLabel, Op subOp)          { return transform.transform(opLabel, subOp) ; }
    
    public Op transform(OpList opList, Op subOp)            { return transform.transform(opList, subOp) ; }
    public Op transform(OpOrder opOrder, Op subOp)          { return transform.transform(opOrder, subOp) ; }
    public Op transform(OpTopN opTop, Op subOp)             { return transform.transform(opTop, subOp) ; }
    public Op transform(OpProject opProject, Op subOp)      { return transform.transform(opProject, subOp) ; }
    public Op transform(OpDistinct opDistinct, Op subOp)    { return transform.transform(opDistinct, subOp) ; }
    public Op transform(OpReduced opReduced, Op subOp)      { return transform.transform(opReduced, subOp) ; }
    public Op transform(OpSlice opSlice, Op subOp)          { return transform.transform(opSlice, subOp) ; }
    public Op transform(OpGroup opGroup, Op subOp)          { return transform.transform(opGroup, subOp) ; }
}
