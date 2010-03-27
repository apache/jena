/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.List;

import com.hp.hpl.jena.sparql.algebra.op.*;

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
    public Op transform(OpProject opProject, Op subOp)      { return opProject ; }
    public Op transform(OpDistinct opDistinct, Op subOp)    { return opDistinct ; }
    public Op transform(OpReduced opReduced, Op subOp)      { return opReduced ; }
    public Op transform(OpSlice opSlice, Op subOp)          { return opSlice ; }
    public Op transform(OpAssign opAssign, Op subOp)        { return opAssign ; }
    public Op transform(OpGroupAgg opGroupAgg, Op subOp)    { return opGroupAgg ; }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */