/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import arq.qexpr;
import arq.qparse;
import arq.sparql;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.table.TableWriter;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.util.FileManager;


public class Run
{
    public static void main(String[] argv)
    {
//        String []a = {"--data=D.ttl", "--query=Q.rq"} ;
//        //ARQ.setFalse(StageBasic.altMatcher) ;
//        arq.sparql.main(a) ;

        String []a = {"--query=Q.rq"} ;
//      //ARQ.setFalse(StageBasic.altMatcher) ;
        arq.qparse.main(a) ;
        
        System.exit(0) ;
        
        //runQExpr() ;
        //print() ;
        codeSSE() ;
        //runQParse() ;
        //classifyJ() ;
        //classifyLJ() ;
        execQuery("D.ttl", "Q.rq") ;
        //exec("D.ttl", "SSE/test.sse") ;
    }
        
    private static void codeSSE()
    {
        dev.sse.main(new String[]{"('a'^^<>)"}) ;
        System.exit(0) ;
        
        System.exit(0) ;
        
        // ----
        
        if ( false )
        {
            Item item = SSE.readFile("SSE/graph.sse") ;
            System.out.println(item.toString()) ;
            System.out.println() ;
        }
        
        if ( false )
        {
            Graph graph = SSE.readGraph("SSE/graph.sse") ;
            Model model = ModelFactory.createModelForGraph(graph) ;
            model.write(System.out, "TTL") ;
        }
        //System.exit(0) ;
        // --------
        if ( false )
        {
            Table table = SSE.readTable("SSE/table.sse") ;
            ResultSet rs = table.toResultSet() ;
            ResultSetFormatter.out(rs) ;
            System.out.println(table) ;
            
            Table table2 = SSE.parseTable(TableWriter.asSSE(table));
            ResultSet rs2 = table2.toResultSet() ;
            ResultSetFormatter.out(rs2) ;
            System.exit(0) ;
        }
        
        dev.qexec.main(new String[]{
            //"--engine=ref" ,
            //--data=D.ttl",
            "--query=Q.sse"}) ;
        System.exit(0) ;
        
    }

    private static void codeLARQ()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = buildIndex(model, "D.ttl") ;
        LARQ.setDefaultIndex(index) ;
        
        Query query = QueryFactory.read("Q.rq") ;
        query.serialize(System.out) ;
        System.out.println();
                                          
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        //LARQ.setDefaultIndex(qExec.getContext(), index) ;
        ResultSetFormatter.out(System.out, qExec.execSelect(), query) ;
        qExec.close() ;
    }

    static IndexLARQ buildIndex(Model model, String datafile)
    {
        // ---- Read and index all literal strings.
        IndexBuilderString larqBuilder = new IndexBuilderString() ;
        
        // Index statements as they are added to the model.
        model.register(larqBuilder) ;
        
        // To just build the index, create a model that does not store statements 
        // Model model2 = ModelFactory.createModelForGraph(new GraphSink()) ;
        
        FileManager.get().readModel(model, datafile) ;
        
        // ---- Alternatively build the index after the model has been created. 
        // larqBuilder.indexStatements(model.listStatements()) ;
        
        // ---- Finish indexing
        larqBuilder.closeForWriting() ;
        model.unregister(larqBuilder) ;
        
        // ---- Create the access index  
        IndexLARQ index = larqBuilder.getIndex() ;
        return index ; 
    }

    static void performQuery(Model model, IndexLARQ index, String queryString)
    {  
        // Make globally available
        LARQ.setDefaultIndex(index) ;
        
        Query query = QueryFactory.create(queryString) ;
        query.serialize(System.out) ;
        System.out.println();
                                          
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        //LARQ.setDefaultIndex(qExec.getContext(), index) ;
        ResultSetFormatter.out(System.out, qExec.execSelect(), query) ;
        qExec.close() ;
    }

    
//    private static Model makeModel()
//    {
//        String BASE = "http://example/" ;
//        Model model = ModelFactory.createDefaultModel() ;
//        model.setNsPrefix("", BASE) ;
//        Resource r1 = model.createResource(BASE+"r1") ;
//        Resource r2 = model.createResource(BASE+"r2") ;
//        Property p1 = model.createProperty(BASE+"p") ;
//        Property p2 = model.createProperty(BASE+"p2") ;
//        RDFNode v1 = model.createTypedLiteral("1", XSDDatatype.XSDinteger) ;
//        RDFNode v2 = model.createTypedLiteral("2", XSDDatatype.XSDinteger) ;
//        
//        r1.addProperty(p1, v1).addProperty(p1, v2) ;
//        r1.addProperty(p2, v1).addProperty(p2, v2) ;
//        r2.addProperty(p1, v1).addProperty(p1, v2) ;
//        
//        return model  ;
//    }
//    
    public static void print()
    {
        QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--print=query",
            "--print=op",
            "--print=plan",
            "--query=Q.rq" , 
        } ;
        qparse.main(a) ;
    }

    private static void execQuery(String datafile, String queryfile)
    {
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sparql.main(a) ;
        System.exit(0) ;
        
    }
    
    private static void exec(String datafile, String queryfile)
    {
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data="+datafile,
            "--file="+queryfile ,
            "--print=plan"
        } ;
        
        qexec.main(a) ;
        System.exit(0) ;
        
    }

    
    public static void runQParse()
    {
        qparse.main(new String[]{ //"--in=prefix", 
                                  //"--out=prefix",
                                  "--print=plan",
                                  //"--print=query",
                                  //"--engine=ref",
                                  "--query=Q.rq"
                                  //"--query=testing/ARQ/Serialization/syntax-general-13.rq" ,
                                  }) ;
        System.exit(0) ;
    }

    static public void runQExpr()
    {
        qexpr.main(new String[]{"xsd:double('1.3e0')"}) ;
//        qexpr.main(new String[]{"(1+3)"}) ;
//        qexpr.main(new String[]{"(?x+3)"}) ;
//        System.exit(0) ;
//        qexpr.main(new String[]{"35.59/1.87715"}) ;
//        qexpr.main(new String[]{"0.5"}) ;
//        qexpr.main(new String[]{"1/2"}) ;
//        qexpr.main(new String[]{"1/3"}) ;
//        qexpr.main(new String[]{"100*1.0"}) ;
        System.exit(0) ;
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