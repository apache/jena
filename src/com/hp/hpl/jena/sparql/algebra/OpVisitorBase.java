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

import com.hp.hpl.jena.sparql.algebra.op.* ;


public class OpVisitorBase implements OpVisitor
{

    public void visit(OpBGP opBGP)
    {}

    public void visit(OpQuadPattern quadPattern)
    {}
    
    public void visit(OpTriple opTriple)
    {}

    public void visit(OpPath opPath)
    {}

    public void visit(OpProcedure opProc)
    {}
    
    public void visit(OpPropFunc opPropFunc)
    {}
    
    public void visit(OpJoin opJoin)
    {}

    public void visit(OpSequence opSequence)
    {}
    
    public void visit(OpDisjunction opDisjunction)
    {}

    public void visit(OpLeftJoin opLeftJoin)
    {}

    public void visit(OpConditional opCond)
    {}

    public void visit(OpMinus opMinus)
    {}
    
    public void visit(OpDiff opDiff)
    {}
    
    public void visit(OpUnion opUnion)
    {}

    public void visit(OpFilter opFilter)
    {}

    public void visit(OpGraph opGraph)
    {}

    public void visit(OpService opService)
    {}

    public void visit(OpDatasetNames dsNames)
    {}

    public void visit(OpTable opUnit)
    {}

    public void visit(OpExt opExt)
    {}

    public void visit(OpNull opNull)
    {}

    public void visit(OpLabel opLabel)
    {}

    public void visit(OpAssign opAssign)
    {}

    public void visit(OpExtend opExtend)
    {}

    public void visit(OpList opList)
    {}

    public void visit(OpOrder opOrder)
    {}

    public void visit(OpProject opProject)
    {}

    public void visit(OpDistinct opDistinct)
    {}

    public void visit(OpReduced opReduced)
    {}

    public void visit(OpSlice opSlice)
    {}

    public void visit(OpGroup opGroup)
    {}
    
    public void visit(OpTopN opTop)
    {}
}
