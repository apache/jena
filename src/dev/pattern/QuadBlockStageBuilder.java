/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.compiler.*;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.layout1.CodecSimple;
import com.hp.hpl.jena.sdb.layout1.QuadBlockCompiler1;
import com.hp.hpl.jena.sdb.layout1.SlotCompiler1;
import com.hp.hpl.jena.sdb.layout1.StoreSimplePGSQL;
import com.hp.hpl.jena.sdb.store.Store;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.vocabulary.RDF;

// replacement QuadBlockCompilerTriple -- eventually
public class QuadBlockStageBuilder //extends QuadBlockCompilerBase 
    
{
    
    SDBRequest request ;
    SlotCompiler slotCompiler ;
    private PTable pTable ;
    private QuadBlockCompiler compiler ;

    public QuadBlockStageBuilder(SDBRequest request, QuadBlockCompiler compiler, SlotCompiler slotCompiler)
    {
        //super(request, slotCompiler) ;
        this.request = request ;
        this.slotCompiler = slotCompiler ;
        this.compiler = compiler ;
        pTable = new PTable();
        pTable.add(RDF.type.asNode(), "TYPE") ;
    }

    
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
        // What's the abstraction of a Table?
        //  PTable, Triple table,  == SqlStage?
        //Stage == table
        
        SqlNode sqlNode = slotCompiler.start(quads) ;   // ****
        SqlExprList conditions = new SqlExprList() ;
        
        QuadBlock plainQuads = new QuadBlock() ;
        quads = new QuadBlock(quads) ;          // Copy
        
        SqlStageList sList = new SqlStageList() ;
        // Concurrent modification - need to use an explicit index.
        for ( int i = 0 ; i < quads.size() ; )
        {
            System.out.println("Acc:   "+plainQuads.size()) ;
            System.out.println("Quads: "+quads.size()) ;
            System.out.println("SList: "+sList.size()) ;
            
            Quad q = quads.get(i) ;
            if ( pTable.trigger(q) )
            {
                // Do accumulator
                if ( plainQuads.size() != 0 )
                {
                    SqlStage stagePre = new SqlStagePlain(compiler, plainQuads) ;
                    sList.add(stagePre) ;
                    plainQuads.clear() ;
                }
                
                System.out.println("trigger") ;
//                System.out.println("quads: "+quads) ;
                // Removes current quad
                SqlStage stage = pTable.process(i, quads) ;
                sList.add(stage) ;
//                System.out.println("quads: "+quads) ;
//                System.out.println(stage) ;
                continue ;
            }
            // Not a special.
            plainQuads.add(q) ;
            i++ ;
        }

        // Remaining?
        if ( plainQuads.size() != 0 )
        {
            System.out.println("trailer") ;
            SqlStage stagePre = new SqlStagePlain(compiler, new QuadBlock(plainQuads)) ;
            sList.add(stagePre) ;
            plainQuads.clear() ;
        }
        System.out.println(sList) ;
        // Split into stages.
        
        // This splits into 2 parts then block compiles all quads.
        // Instead, work quad by quad until a PTable triggers
        //  No trigger => plain quad stage.
        //  Let the PTable remove quads it handles
        //  Do PTable stage.
        //  Continue.

        sqlNode = QC.innerJoin(request, sqlNode, sList.build(request, slotCompiler)) ;
        sqlNode = slotCompiler.finish(sqlNode, quads) ;
        return sqlNode ;
    }

//    @Override
//    protected SqlNode compile(Quad quad)
//    {   // DUMMY
//        return null ;
//    }

    public static void main(String[] args)
    {
        
        Store store = new StoreSimplePGSQL(null) ;
        SDBRequest request = new SDBRequest(store, new Query());
        SlotCompiler sComp =  new SlotCompiler1(request, new CodecSimple()) ;
        QuadBlockCompiler comp = new QuadBlockCompiler1(request, sComp) ;
        
        QuadBlockStageBuilder builder = new QuadBlockStageBuilder(request, comp, sComp) ;
        QuadBlock quadBlock = new QuadBlock() ;
        Quad q1 = new Quad(Quad.defaultGraph, 
                           Var.alloc("s"), RDF.type.asNode(), Var.alloc("o") ) ;
        Quad q2 = new Quad(Quad.defaultGraph, 
                           Var.alloc("s"), Node.createURI("http://host/p"), Var.alloc("o") ) ;
        
        quadBlock.add(q1);
        quadBlock.add(q2);
        
        SqlNode sqlNode = builder.compileNew(quadBlock) ;
        System.out.println(sqlNode) ;
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