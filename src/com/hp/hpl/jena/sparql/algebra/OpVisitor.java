/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.sparql.algebra.op.* ;

public interface OpVisitor
{
    // Op0
    public void visit(OpBGP opBGP) ;
    public void visit(OpQuadPattern quadPattern) ;
    public void visit(OpTriple opTriple) ;
    public void visit(OpPath opPath) ;
    public void visit(OpTable opTable) ;
    public void visit(OpNull opNull) ;
    
    //Op1
    public void visit(OpProcedure opProc) ;
    public void visit(OpPropFunc opPropFunc) ;
    public void visit(OpFilter opFilter) ;
    public void visit(OpGraph opGraph) ;
    public void visit(OpService opService) ;
    public void visit(OpDatasetNames dsNames) ;
    public void visit(OpLabel opLabel) ;
    public void visit(OpAssign opAssign) ;
    public void visit(OpExtend opExtend) ;
    
    // Op2
    public void visit(OpJoin opJoin) ;
    public void visit(OpLeftJoin opLeftJoin) ;
    public void visit(OpUnion opUnion) ;
    public void visit(OpDiff opDiff) ;
    public void visit(OpMinus opMinus) ;
    
    public void visit(OpConditional opCondition) ;
    
    // OpN
    public void visit(OpSequence opSequence) ;
    public void visit(OpDisjunction opDisjunction) ;

    public void visit(OpExt opExt) ;
    
    // OpModifier
    public void visit(OpList opList) ;
    public void visit(OpOrder opOrder) ;
    public void visit(OpProject opProject) ;
    public void visit(OpReduced opReduced) ;
    public void visit(OpDistinct opDistinct) ;
    public void visit(OpSlice opSlice) ;

    public void visit(OpGroup opGroup) ;
    public void visit(OpTopN opTop) ;
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
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