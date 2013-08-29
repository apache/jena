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

/** One step in the transformation process.
 *  Used with Transformer, performs a bottom-up rewrite.
 */
public class TransformCopy implements Transform
{
    public static final boolean COPY_ALWAYS         = true ;
    public static final boolean COPY_ONLY_ON_CHANGE = false ;
    private boolean alwaysCopy = false ;
    
    public TransformCopy()                                          { this(COPY_ONLY_ON_CHANGE) ; }
    public TransformCopy(boolean alwaysDuplicate)                   { this.alwaysCopy = alwaysDuplicate ; }

    @Override
    public Op transform(OpTable opTable)                            { return xform(opTable) ; }
    @Override
    public Op transform(OpBGP opBGP)                                { return xform(opBGP) ; }
    @Override
    public Op transform(OpQuadPattern opQuadPattern)                { return xform(opQuadPattern) ; }
    @Override
    public Op transform(OpQuadBlock opQuadBlock)                    { return xform(opQuadBlock) ; }
    @Override
    public Op transform(OpTriple opTriple)                          { return xform(opTriple) ; }
    @Override
    public Op transform(OpQuad opQuad)                              { return xform(opQuad) ; }
    @Override
    public Op transform(OpPath opPath)                              { return xform(opPath) ; }

    @Override
    public Op transform(OpProcedure opProc, Op subOp)               { return xform(opProc, subOp) ; }
    @Override
    public Op transform(OpPropFunc opPropFunc, Op subOp)            { return xform(opPropFunc, subOp) ; }
    @Override
    public Op transform(OpDatasetNames opDatasetNames)              { return xform(opDatasetNames) ; }

    @Override
    public Op transform(OpFilter opFilter, Op subOp)                { return xform(opFilter, subOp) ; }
    @Override
    public Op transform(OpGraph opGraph, Op subOp)                  { return xform(opGraph, subOp) ; }
    @Override
    public Op transform(OpService opService, Op subOp)              { return xform(opService, subOp) ; }

    @Override
    public Op transform(OpAssign opAssign, Op subOp)                { return xform(opAssign, subOp) ; }
    @Override
    public Op transform(OpExtend opExtend, Op subOp)                { return xform(opExtend, subOp) ; }
    
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)           { return xform(opJoin, left, right) ; }
    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)   { return xform(opLeftJoin, left, right) ; }
    @Override
    public Op transform(OpDiff opDiff, Op left, Op right)           { return xform(opDiff, left, right) ; }
    @Override
    public Op transform(OpMinus opMinus, Op left, Op right)         { return xform(opMinus, left, right) ; }
    @Override
    public Op transform(OpUnion opUnion, Op left, Op right)         { return xform(opUnion, left, right) ; }
    @Override
    public Op transform(OpConditional opCond, Op left, Op right)    { return xform(opCond, left, right) ; }

    @Override
    public Op transform(OpSequence opSequence, List<Op> elts)           { return xform(opSequence, elts) ; }
    @Override
    public Op transform(OpDisjunction opDisjunction, List<Op> elts)     { return xform(opDisjunction, elts) ; }
    
    @Override
    public Op transform(OpExt opExt)                                { return xform(opExt) ; }
    
    @Override
    public Op transform(OpNull opNull)                              { return opNull.copy() ; }
    @Override
    public Op transform(OpLabel opLabel, Op subOp)                  { return xform(opLabel, subOp) ; }
    
    @Override
    public Op transform(OpList opList, Op subOp)                    { return xform(opList, subOp) ; }
    @Override
    public Op transform(OpOrder opOrder, Op subOp)                  { return xform(opOrder, subOp) ; }
    @Override
    public Op transform(OpTopN opTop, Op subOp)                     { return xform(opTop, subOp) ; }
    @Override
    public Op transform(OpProject opProject, Op subOp)              { return xform(opProject, subOp) ; }
    @Override
    public Op transform(OpDistinct opDistinct, Op subOp)            { return xform(opDistinct, subOp) ; }
    @Override
    public Op transform(OpReduced opReduced, Op subOp)              { return xform(opReduced, subOp) ; }
    @Override
    public Op transform(OpSlice opSlice, Op subOp)                  { return xform(opSlice, subOp) ; }
    @Override
    public Op transform(OpGroup opGroup, Op subOp)                  { return xform(opGroup, subOp) ; }

    private Op xform(Op0 op)
    { 
        if ( ! alwaysCopy )
            return op ;
        return op.copy() ;
    }

    private Op xform(Op1 op, Op subOp)
    { 
        if ( ! alwaysCopy && op.getSubOp() == subOp )
            return op ;
        return op.copy(subOp) ;
    }
    
    private Op xform(Op2 op, Op left, Op right)
    {
        if ( ! alwaysCopy && op.getLeft() == left && op.getRight() == right )
            return op ;
        return op.copy(left, right) ;
    }
    
    private Op xform(OpN op, List<Op> elts)
    {
        // Need to do one-deep equality checking.
        if ( ! alwaysCopy && equals1(elts, op.getElements()) )
            return op ;
        return op.copy(elts) ;
    }
    
    private Op xform(OpExt op)
    {
        try {
            return op.apply(this);
        } catch (Exception e) {
            // May happen if the OpExt doesn't implement apply()
            return op;
        }
    }
    
    private boolean equals1(List<Op> list1, List<Op> list2)
    {
        if ( list1.size() != list2.size() )
            return false ;
        for ( int i = 0 ; i < list1.size() ; i++ )
        {
            if ( list1.get(i) != list2.get(i) )
                return false ;
        }
        return true ;
    }
}
