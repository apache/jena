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

/** A visitor helper that maps all visits to a few general ones */ 
public abstract class OpVisitorByType implements OpVisitor
{
    protected abstract void visitN(OpN op) ;

    protected abstract void visit2(Op2 op) ;
    
    protected abstract void visit1(Op1 op) ;
    
    protected abstract void visit0(Op0 op) ;    
    
    protected abstract void visitExt(OpExt op) ;    

    protected void visitModifer(OpModifier opMod)
    { visit1(opMod) ; }

    public void visit(OpBGP opBGP)
    { visit0(opBGP) ; }
    
    public void visit(OpQuadPattern quadPattern)
    { visit0(quadPattern) ; }

    public void visit(OpTriple opTriple)
    { visit0(opTriple) ; }
    
    public void visit(OpPath opPath)
    { visit0(opPath) ; }
    
    public void visit(OpProcedure opProcedure)
    { visit1(opProcedure) ; }

    public void visit(OpPropFunc opPropFunc)
    { visit1(opPropFunc) ; }

    public void visit(OpJoin opJoin)
    { visit2(opJoin) ; }

    public void visit(OpSequence opSequence)
    { visitN(opSequence) ; }
    
    public void visit(OpDisjunction opDisjunction)
    { visitN(opDisjunction) ; }
    
    public void visit(OpLeftJoin opLeftJoin)
    { visit2(opLeftJoin) ; }

    public void visit(OpDiff opDiff)
    { visit2(opDiff) ; }

    public void visit(OpMinus opMinus)
    { visit2(opMinus) ; }

    public void visit(OpUnion opUnion)
    { visit2(opUnion) ; }
    
    public void visit(OpConditional opCond)
    { visit2(opCond) ; }

    public void visit(OpFilter opFilter)
    { visit1(opFilter) ; }

    public void visit(OpGraph opGraph)
    { visit1(opGraph) ; }

    public void visit(OpService opService)
    { visit1(opService) ; }

    public void visit(OpDatasetNames dsNames)
    { visit0(dsNames) ; }

    public void visit(OpTable opUnit)
    { visit0(opUnit) ; }

    public void visit(OpExt opExt)
    { visitExt(opExt) ; }

    public void visit(OpNull opNull)
    { visit0(opNull) ; }

    public void visit(OpLabel opLabel)
    { visit1(opLabel) ; }

    public void visit(OpAssign opAssign)
    { visit1(opAssign) ; }

    public void visit(OpExtend opExtend)
    { visit1(opExtend) ; }

    public void visit(OpList opList)
    { visitModifer(opList) ; }

    public void visit(OpOrder opOrder)
    { visitModifer(opOrder) ; }

    public void visit(OpProject opProject)
    { visitModifer(opProject) ; }

    public void visit(OpReduced opReduced)
    { visitModifer(opReduced) ; }

    public void visit(OpDistinct opDistinct)
    { visitModifer(opDistinct) ; }

    public void visit(OpSlice opSlice)
    { visitModifer(opSlice) ; }

    public void visit(OpGroup opGroup)
    { visit1(opGroup) ; }

    public void visit(OpTopN opTop)
    { visit1(opTop) ; }
}
