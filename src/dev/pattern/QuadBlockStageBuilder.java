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
    private PatternTable patternTable ;
    private QuadBlockCompiler compiler ;

    public QuadBlockStageBuilder(SDBRequest request, QuadBlockCompiler compiler, SlotCompiler slotCompiler)
    {
        //super(request, slotCompiler) ;
        this.request = request ;
        this.slotCompiler = slotCompiler ;
        this.compiler = compiler ;
        patternTable = new PatternTable();
        patternTable.add(RDF.type.asNode(), "TYPE") ;
        patternTable.add(RDF.value.asNode(), "VALUE") ;
    }

    
    //@Override
    public SqlNode compileNew(QuadBlock quads)
    {
        // What's the abstraction of a Table?
        //  PTable, Triple table,  == SqlStage?
        //Stage == table
        
        SqlNode sqlNode = slotCompiler.start(quads) ;   // ****
        SqlExprList conditions = new SqlExprList() ;
        
        QuadBlock plainQuads = new QuadBlock() ;
        quads = new QuadBlock(quads) ;          // Copy it because it's modified.
        
        SqlStageList sList = new SqlStageList() ;
        // Concurrent modification - need to use an explicit index.
        for ( int i = 0 ; i < quads.size() ; )
        {
            Quad q = quads.get(i) ;
            if ( patternTable.trigger(q) )
            {
                // Removes current quad
                SqlStage stage = patternTable.process(i, quads) ;
                if ( stage != null )
                {
                    sList.add(stage) ;
                    continue ;
                }
            }
            sList.add(new SqlStageTripleTable(q)) ; 
            i++ ;
        }

        System.out.println(sList) ;
        sqlNode = QC.innerJoin(request, sqlNode, sList.build(request, slotCompiler)) ;
        sqlNode = slotCompiler.finish(sqlNode, quads) ;
        return sqlNode ;
    }

    public static void main(String[] args)
    {
        
        Store store = new StoreSimplePGSQL(null) ;
        SDBRequest request = new SDBRequest(store, new Query());
        SlotCompiler sComp =  new SlotCompiler1(request, new CodecSimple()) ;
        QuadBlockCompiler comp = new QuadBlockCompiler1(request, sComp) ;
        
        QuadBlockStageBuilder builder = new QuadBlockStageBuilder(request, comp, sComp) ;
        QuadBlock quadBlock = new QuadBlock() ;
        
        Quad qValue = new Quad(Quad.defaultGraph, 
                               Var.alloc("s"), RDF.value.asNode(), Node.createLiteral("XYZ") ) ;
        Quad qType = new Quad(Quad.defaultGraph, 
                           Var.alloc("s"), RDF.type.asNode(), Var.alloc("o") ) ;
        Quad q2 = new Quad(Quad.defaultGraph, 
                           Var.alloc("s"), Node.createURI("http://host/p"), Var.alloc("o") ) ;
        
        quadBlock.add(qType);
        quadBlock.add(q2);
        quadBlock.add(qValue);
        //quadBlock.add(q2);
        
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