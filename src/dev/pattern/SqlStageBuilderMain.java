/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.engine.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.engine.compiler.QuadBlockCompiler;

// Two phases : 
//    QuadBlock => QuadBlock rewrite
//    QuadBlock => QuadBlock+SQL 

public class SqlStageBuilderMain
{
    QuadBlockCompiler baseCompiler ;
    private SDBRequest request ;
    public SqlStageBuilderMain(SDBRequest request, QuadBlockCompiler compiler)
    {
        this.request = request ;
        this.baseCompiler = compiler ;
    }
    
    QuadBlockRewrite qbr = new QBR_RdfType() ;
    
    public SqlNode compileToSql(QuadBlock quads)
    {
        quads = qbr.rewrite(quads) ;
        SqlStageList sList = compile(quads) ;
        return sList.build(request) ;
    }
    
    public SqlStageList compile(QuadBlock quads)
    {
        SqlStageBuilder bob = new SqlStageBuilderPlain(baseCompiler) ;
        return bob.compile(quads) ;
    }
    
//    public SqlStageList compile(QuadBlock quads)
//    {
//        return splitType(quads, new SqlStageBuilderPlain(baseCompiler)) ;
//    }
    
//    // Just avoid competing matchers for now!
//    
//    static final Node rdfType = RDF.type.asNode() ;
//    // Need a single global var allocator.
//    static VarAlloc varAlloc = new VarAlloc("X") ;
//    
//    private SqlStageList splitType(QuadBlock quads, SqlStageBuilder otherHandler)
//    {
//        int i = -1 ;
//        SqlStageList stageList = new SqlStageList() ;
//        
//        for ( ; i < quads.size() ; ) 
//        {
//            SqlStage stage = null ;
//            int j = quads.findFirst(i, null, null, rdfType, null) ;
//            if ( j == -1 )
//            {
//                QuadBlock qbRest = quads.subBlock(i) ;
//                stage = new SqlStagePlain(baseCompiler, qbRest) ;
//                stageList.add(stage) ;
//                break ;
//            }
//            i = j ;
//            
//            QuadBlock qb1 = quads.subBlock(0, i) ;
//            Quad rdfTypeQuad = quads.get(i) ;
//            stage = new SqlStagePlain(baseCompiler, qb1) ;
//            stageList.add(stage) ;
//            i++ ;
//            
//            QuadBlock qbType = new QuadBlock() ;
//            Var v = varAlloc.allocVar() ;
//            Quad q1 = new Quad(rdfTypeQuad.getGraph(), rdfTypeQuad.getSubject(), rdfType, v) ;
//            Quad q2 = new Quad(rdfTypeQuad.getGraph(), v, RDFS.subClassOf.asNode(), rdfTypeQuad.getObject()) ;
//            qbType.add(q1) ;
//            qbType.add(q2) ;
//            stage = new SqlStagePlain(baseCompiler, qbType) ;
//            stageList.add(stage) ;
//        }
//        return stageList ;
//    }
//    
//    
//    private SqlStageList split(QuadBlock quads)
//    {
//        SqlStageList sList = new SqlStageList() ;
//        sList.add(new SqlStagePlain(baseCompiler, quads)) ;
//        return sList ;
//    }
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