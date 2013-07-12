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

import com.hp.hpl.jena.sparql.algebra.op.* ;


public class OpVisitorBase implements OpVisitor
{

    @Override public void visit(OpBGP opBGP)                    {}

    @Override public void visit(OpQuadPattern quadPattern)      {}
 
    @Override public void visit(OpQuadBlock quadBlock)          {}
    
    @Override public void visit(OpTriple opTriple)              {}
 
    @Override public void visit(OpQuad opQuad)                  {}

    @Override public void visit(OpPath opPath)                  {}

    @Override public void visit(OpProcedure opProc)             {}
    
    @Override public void visit(OpPropFunc opPropFunc)          {}
    
    @Override public void visit(OpJoin opJoin)                  {}

    @Override public void visit(OpSequence opSequence)          {}
    
    @Override public void visit(OpDisjunction opDisjunction)    {}

    @Override public void visit(OpLeftJoin opLeftJoin)          {}

    @Override public void visit(OpConditional opCond)           {}

    @Override public void visit(OpMinus opMinus)                {}
    
    @Override public void visit(OpDiff opDiff)                  {}
    
    @Override public void visit(OpUnion opUnion)                {}

    @Override public void visit(OpFilter opFilter)              {}

    @Override public void visit(OpGraph opGraph)                {}

    @Override public void visit(OpService opService)            {}

    @Override public void visit(OpDatasetNames dsNames)         {}

    @Override public void visit(OpTable opTable)                {}

    @Override public void visit(OpExt opExt)                    {}

    @Override public void visit(OpNull opNull)                  {}

    @Override public void visit(OpLabel opLabel)                {}

    @Override public void visit(OpAssign opAssign)              {}

    @Override public void visit(OpExtend opExtend)              {}

    @Override public void visit(OpList opList)                  {}

    @Override public void visit(OpOrder opOrder)                {}

    @Override public void visit(OpProject opProject)            {}

    @Override public void visit(OpDistinct opDistinct)          {}

    @Override public void visit(OpReduced opReduced)            {}

    @Override public void visit(OpSlice opSlice)                {}

    @Override public void visit(OpGroup opGroup)                {}
    
    @Override public void visit(OpTopN opTop)                   {}
}
