/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sdb.compiler.PatternTable;
import com.hp.hpl.jena.sdb.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.compiler.QuadBlockCompilerMain;
import com.hp.hpl.jena.sdb.compiler.SlotCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.layout2.hash.SlotCompilerHash;
import com.hp.hpl.jena.sdb.layout2.index.SlotCompilerIndex;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexPGSQL;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.PrintSDB;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.vocabulary.RDF;

public class QBuilder
{
    public static void main(String[] args)
    {
        PatternTable patternTable = new PatternTable();
        
        patternTable.add(RDF.type.asNode(), "TYPE") ;
        patternTable.add(RDF.value.asNode(), "VALUE") ;
        QuadBlockCompilerMain.patternTable = patternTable ;
        
        // Make getting a slot compiler easier for testing?
        
//        Store store = new StoreSimplePGSQL(null) ;
//        SDBRequest request = new SDBRequest(store, new Query());
//        SlotCompiler sComp =  new SlotCompiler1(request, new CodecSimple()) ;
//        QuadBlockCompiler comp = new QuadBlockCompiler1(request, sComp) ;
        Store store = new StoreTriplesNodesIndexPGSQL(null) ;
        SDBRequest request = new SDBRequest(store, new Query());
        
        SlotCompiler sComp = null ;
        if ( false )
            sComp =  new SlotCompilerIndex(request) ;
        else
            sComp =  new SlotCompilerHash(request) ;

        if ( false )
        {
            //store.getQueryCompilerFactory().createQueryCompiler(request).
            String queryString = "SELECT * { ?s ?p ?o}" ;
            Query query = QueryFactory.create(queryString) ;
            QueryEngineSDB qe = new QueryEngineSDB(store, query, ARQ.getContext()) ;
            Op op = qe.getOp() ;
            PrintSDB.print(op) ;
            PrintSDB.printSQL(op) ;
        }
            
        QuadBlockCompilerMain builder = new QuadBlockCompilerMain(request, sComp) ;
        QuadBlock quadBlock = new QuadBlock() ;
        
        Quad qX = new Quad(Quad.defaultGraph,
                           SSE.parseTriple("(triple ?s rdf:type 'XYZ')")) ;
        
        
//        Quad qValue = new Quad(Quad.defaultGraph, 
//                               Var.alloc("s"), RDF.value.asNode(), Node.createLiteral("XYZ") ) ;
//        Quad qType  = new Quad(Quad.defaultGraph, 
//                               Var.alloc("s"), RDF.type.asNode(), Node.createLiteral("XYZ") ) ;
        
        Quad qValue = SSE.parseQuad("(quad _ ?s rdf:value ?o)") ;
        Quad qType  = SSE.parseQuad("(quad _ ?s rdf:type  ?o)") ;
        
        Quad q1 = SSE.parseQuad("(quad _ ?s <http://host/p> ?o)") ;
        Quad q2 = SSE.parseQuad("(quad _ ?s <http://host/p> 'XYZ')") ;
        
        quadBlock.add(qType);
        quadBlock.add(q2);
        quadBlock.add(qValue);
//        quadBlock.add(q1);
        
        //System.out.println(quadBlock) ;
        //System.out.println() ;
        SqlNode sqlNode = builder.compile(quadBlock) ;
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