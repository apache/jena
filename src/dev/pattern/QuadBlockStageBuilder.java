/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import com.hp.hpl.jena.sdb.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.compiler.QuadBlockCompilerBase;
import com.hp.hpl.jena.sdb.compiler.SlotCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sparql.core.Quad;

public class QuadBlockStageBuilder extends QuadBlockCompilerBase 
{
    public QuadBlockStageBuilder(SDBRequest request, SlotCompiler slotCompiler)
    {
        super(request, slotCompiler) ;
    }

    PTable pTable = new PTable() ; 
    
    // New version of QuadBlockCompilerBase
    // build SqlStageList which is one table access each.
    // Each triple is a table stage.

    // SlotComplier.start(quads) ;
    // for each stage
    //   build(slotCompiler, request, SqrExprList)
    // SlotCompiler.finish(sqlNode, quads) 
    // QuadCompilerTriple becomes the SqlStage for a single triple.
    //   SqlStage.compile(slotCompiler?) ;
    
    // SlotCompilers for layout1, layout2/index, layout2/hash 
    

    //@Override
    public SqlNode compileNew(QuadBlock quads)
    {
        //Stage == table
        
        SqlNode sqlNode = slotCompiler.start(quads) ; 
        SqlExprList conditions = new SqlExprList() ;
        
        // Split into stages.
        
        // This splits into 2 parts then block compiles all quads.
        // Instead, work quad by quad until a PTable triggers
        //  No trigger => plain quad stage.
        //  Let the PTable remove quads it handles
        //  Do PTable stage.
        //  Continue.
        
        SqlStageList sList = pTable.modBlock(this, quads) ;
        // SlotCompiler argument?
        //SqlStageList sList = new SqlStageList() ;
        sList.build(request, slotCompiler) ;
        
        slotCompiler.finish(sqlNode, quads) ;
        
        return null ;
    }

    @Override
    protected SqlNode compile(Quad quad)
    {   // DUMMY
        return null ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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