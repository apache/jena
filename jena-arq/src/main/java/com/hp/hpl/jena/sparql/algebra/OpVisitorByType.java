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

/** A visitor helper that maps all visits to a few general ones */ 
public abstract class OpVisitorByType implements OpVisitor
{
    protected abstract void visitN(OpN op) ;

    protected abstract void visit2(Op2 op) ;
    
    protected abstract void visit1(Op1 op) ;
    
    // This may be needed - Filters can have a EXISTS/NOT EXISTS expression in them. 
    //protected abstract void visitF(OpFilter op) ;
    
    protected abstract void visit0(Op0 op) ;    
    
    protected abstract void visitExt(OpExt op) ;    

    protected abstract void visitFilter(OpFilter op) ;
    
    protected abstract void visitLeftJoin(OpLeftJoin op) ;
    
    protected void visitModifer(OpModifier opMod)
    { visit1(opMod) ; }

    @Override
    public void visit(OpBGP opBGP)
    { visit0(opBGP) ; }
    
    @Override
    public void visit(OpQuadPattern quadPattern)
    { visit0(quadPattern) ; }

    @Override
    public void visit(OpQuadBlock quadBlock)
    { visit0(quadBlock) ; }

    @Override
    public void visit(OpTriple opTriple)
    { visit0(opTriple) ; }
    
    @Override
    public void visit(OpQuad opQuad)
    { visit0(opQuad) ; }

    @Override
    public void visit(OpPath opPath)
    { visit0(opPath) ; }
    
    @Override
    public void visit(OpProcedure opProcedure)
    { visit1(opProcedure) ; }

    @Override
    public void visit(OpPropFunc opPropFunc)
    { visit1(opPropFunc) ; }

    @Override
    public void visit(OpJoin opJoin)
    { visit2(opJoin) ; }

    @Override
    public void visit(OpSequence opSequence)
    { visitN(opSequence) ; }
    
    @Override
    public void visit(OpDisjunction opDisjunction)
    { visitN(opDisjunction) ; }
    
    @Override
    public void visit(OpLeftJoin opLeftJoin)
    { visitLeftJoin(opLeftJoin) ; }

    @Override
    public void visit(OpDiff opDiff)
    { visit2(opDiff) ; }

    @Override
    public void visit(OpMinus opMinus)
    { visit2(opMinus) ; }

    @Override
    public void visit(OpUnion opUnion)
    { visit2(opUnion) ; }
    
    @Override
    public void visit(OpConditional opCond)
    { visit2(opCond) ; }

    @Override
    public void visit(OpFilter opFilter)
    { visitFilter(opFilter) ; }

    @Override
    public void visit(OpGraph opGraph)
    { visit1(opGraph) ; }

    @Override
    public void visit(OpService opService)
    { visit1(opService) ; }

    @Override
    public void visit(OpDatasetNames dsNames)
    { visit0(dsNames) ; }

    @Override
    public void visit(OpTable opUnit)
    { visit0(opUnit) ; }

    @Override
    public void visit(OpExt opExt)
    { visitExt(opExt) ; }

    @Override
    public void visit(OpNull opNull)
    { visit0(opNull) ; }

    @Override
    public void visit(OpLabel opLabel)
    { visit1(opLabel) ; }

    @Override
    public void visit(OpAssign opAssign)
    { visit1(opAssign) ; }

    @Override
    public void visit(OpExtend opExtend)
    { visit1(opExtend) ; }

    @Override
    public void visit(OpList opList)
    { visitModifer(opList) ; }

    @Override
    public void visit(OpOrder opOrder)
    { visitModifer(opOrder) ; }

    @Override
    public void visit(OpProject opProject)
    { visitModifer(opProject) ; }

    @Override
    public void visit(OpReduced opReduced)
    { visitModifer(opReduced) ; }

    @Override
    public void visit(OpDistinct opDistinct)
    { visitModifer(opDistinct) ; }

    @Override
    public void visit(OpSlice opSlice)
    { visitModifer(opSlice) ; }

    @Override
    public void visit(OpGroup opGroup)
    { visit1(opGroup) ; }

    @Override
    public void visit(OpTopN opTop)
    { visit1(opTop) ; }
}
