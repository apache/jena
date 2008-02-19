/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.net.MalformedURLException;

import arq.sparql;
import arq.sse_query;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.IRIRelativize;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.util.DateTimeStruct;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;

public class Run
{
    public static void main(String[] argv) throws Exception
    {
        {
        String []a = { "--file=Q2.rq", "--print=plan" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
        }
        
        remote() ; System.exit(0) ;
        
        runUpdate() ;
        arq.qexpr.main(new String[]{"64*1000*4e9/(3600*24*365*1000)"} );
        System.exit(0) ;
        
        Rewrite.main() ; System.exit(0) ;
        
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "-query=Q.rq" , 
        } ;
        
        sparql.main(a) ;
        System.exit(0) ;
    }
    
    static void remote()
    {
        String url = "http://dbpedia.openlinksw.com:8890/sparql";
        //String url = "http://localhost:2222/sparql";
        String defaultgraph = "http://dbpedia.org";
        String shortQuery = "SELECT ?predicate ?object " +
        "WHERE { " +
        "<http://dbpedia.org/resource/Philolaus> ?predicate ?object. "+
        "FILTER( "+
        "(!isLiteral(?object)) "+
        "&&( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) "+
        "&&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) "+
        "&&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) "+
        "&&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) "+
        "&&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ) "+
        "&&( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) "+
        "&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) "+
        "&&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) "+
        "&&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) "+
        "&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
        "&&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) "+
        "&&( !regex(str(?object), 'http://www.geonames.org') )).} ";

        String longQuery="SELECT ?predicate ?object " +
        "WHERE { " +
        "<http://dbpedia.org/resource/Philolaus> ?predicate ?object. "+
        "FILTER( "+
        "(!isLiteral(?object)) "+
        "&&( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) "+
        "&&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) "+
        "&&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) "+
        "&&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) "+
        "&&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ) "+
        "&&( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) "+
        "&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) "+
        "&&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) "+
        "&&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) "+
        "&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
        "&&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) "+
        "&&( !regex(str(?object), 'http://www.geonames.org') )).} ";


        ResultSet rs;
        String xml;
        QueryEngineHTTP queryExecution;
        
        if ( false )
        {
            HttpQuery.urlLimit = 5 ;
            queryExecution = new QueryEngineHTTP(url, shortQuery);
            queryExecution.addDefaultGraph(defaultgraph);
            rs = queryExecution.execSelect();
            ResultSetFormatter.out(rs) ;
//            xml = ResultSetFormatter.asXMLString(rs);
//            System.out.println("Short Query ResultSet length: "+xml.length());
            return ;
        }
        
        if ( false )
            try{
                queryExecution=new QueryEngineHTTP(url,longQuery);
                queryExecution.addDefaultGraph(defaultgraph);
                rs = queryExecution.execSelect();
                xml = ResultSetFormatter.asXMLString(rs);
                System.out.println("Long Query ResultSet length: "+xml.length()+"\n");
                System.out.println("Long query XML: "+xml);
            }catch (Exception e) {e.printStackTrace();}

        String queryWithIncreasingLength="";
        
        // 0 to 30: breaks at 15
        for (int i = 14; i < 16; i++) {
            queryWithIncreasingLength = makeQueryString ( i);
            
            if ( false )
            {
                Query q = QueryFactory.create(queryWithIncreasingLength) ;
                System.out.println(q) ;
            }
            
            queryExecution=new QueryEngineHTTP(url,queryWithIncreasingLength);
            queryExecution.addDefaultGraph(defaultgraph);
            rs = queryExecution.execSelect();
            System.out.println("Query with length: "+queryWithIncreasingLength.length()
                               +" produces ResultSet length: "+ResultSetFormatter.toList(rs).size());
        }
    }


    public static String makeQueryString ( int charsToAdd){
        String add="";

        for (int i = 0; i <charsToAdd; i++) {
            add+="0123456789"+"0123456789";
        }
        // NOTE THE LAST LINE
        String query = "SELECT ?predicate ?object " +
        "WHERE { " +
        "<http://dbpedia.org/resource/Philolaus> ?predicate ?object. "+
        "FILTER( "+
        "(!isLiteral(?object)) "+
        "&&( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) "+
        "&&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) "+
        "&&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) "+
        "&&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) "+
        "&&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ) "+
        "&&( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) "+
        "&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) "+
        "&&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) "+
        "&&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) "+
        "&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
        "&&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) "+
        "&&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) "+
        "&&( !regex(str(?object), 'http://www.geonames.org"+add+"') )).} ";


        return query;
    }



    static void Service() throws Exception
    {
        
        //if ( true ) System.setProperty("socksProxyHost", "socks-server") ;
        //java.net.PasswordAuthentication
        
//        QueryExecution qExec = QueryExecutionFactory.sparqlService("http://sparql.org/books", "SELECT * { ?s ?p ?o}") ;

        QueryEngineHTTP qExec = QueryExecutionFactory.createServiceRequest("http://sparql.org/books",
                                                         QueryFactory.create("SELECT * { ?s ?p ?o}")) ;
        //qExec.addParam("default-graph-uri", "json") ;
        //qExec.addDefaultGraph("foo") ;
        qExec.setBasicAuthentication("user", "password".toCharArray()) ;
        
        ResultSetFormatter.out(qExec.execSelect()) ;
        System.out.println("Finished") ;
        System.exit(0) ;
        
        
//        AlgebraGenerator.AlgebraStaging = true ;
//        String []a = { "--file=Q.rq", "--print=op" , "--print=plan"} ;
//        arq.qparse.main(a) ;
//        System.exit(0) ;
        
        //socksProxyHost=socks-server
        if ( false ) System.setProperty("socksProxyHost", "socks-server") ;
        if ( false )
        {
            Query query = QueryFactory.read("Q.rq") ;
            Op op = Algebra.compile(query) ;
            System.out.println(op) ;
            Query query2 = OpAsQuery.asQuery(op) ;
            System.out.println(query2) ;
            System.exit(0) ;
        }

        String DIR = "testing/ARQ/Algebra/" ;

        //        ARQ.getContext().set(ARQ.filterPlacement, false) ;
        execQuery(DIR+"opt-filter-1.ttl", DIR+"opt-filter-1.rq") ;
       
//        runQParse() ;
//        execQuery(DIR+"D.ttl", DIR+"Q.arq") ;
        execQuerySSE(DIR+"D.ttl", DIR+"Q.sse") ;
    }
    
    private static void runParseDateTime(String str)
    {
        System.out.println(str) ; 
        DateTimeStruct dt = DateTimeStruct.parseDateTime(str) ;
        System.out.println(str + " ==> " + dt) ;
        if ( ! str.equals(dt.toString())) 
            System.out.println("*** Different") ;
    }
    
    public static void code()
    {
        // see Rewrite.java
        Query q = QueryFactory.create("SELECT * { ?s ?p ?o FILTER(?o = 3) }") ;
        Op op = Algebra.compile(q) ;
        op = Algebra.optimize(op) ;
        OpWalker.walk(op, new F()) ;
        
    }

    static class F extends OpVisitorBase
    {
        public void visit(OpFilter opFilter)
        {
            //WriteropFilter.getExprs()
        }
    }
    
    
    private static void code1(String uri, String base)
    {
        int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;
        IRI baseIRI = IRIFactory.jenaImplementation().construct(base) ;
        IRI rel = baseIRI.relativize(uri, relFlags) ;
        
        String s = null ; 
        try { s = rel.toASCIIString() ; }
        catch (MalformedURLException  ex) { s = rel.toString() ; }
            
        System.out.println("("+uri+" ["+base+"]) ==> <"+s+">") ;
        
        String s2 = baseIRI.create(s).toString() ;
        System.out.println("     "+s2) ;
        
    }

    private static void runQParse()
    {
        String []a = { "--file=Q.rq", "--print=op" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQParseARQ()
    {
        String []a = { "--file=Q.arq", "--out=arq", "--print=op", "--print=query" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runUpdate()
    {
        String a[] = {"--desc=etc/graphstore.ttl", "--update=update.ru", "--dump"} ;
        arq.update.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQTest()
    {
        String DIR = "/home/afs/W3C/DataAccess/tests/data-r2/expr-equals/" ;
        String []a1 = { "--strict", "--data="+DIR+"data-eq.ttl",
            "--query="+DIR+"query-eq2-2.rq",
            "--result="+DIR+"result-eq2-2.ttl"} ;

        String []a2 = { "--strict", "--data="+DIR+"data-eq.ttl",
            "--query="+DIR+"query-eq2-graph-1.rq",
            "--result="+DIR+"result-eq2-graph-1.ttl"} ;

        arq.qtest.main(a1) ;
        System.exit(0 ) ; 
  
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
    
    private static void execQuerySSE(String datafile, String queryfile)
    {
        //com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sse_query.main(a) ;
        System.exit(0) ;
        
    }
    
}

class RunLARQ
{
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
        larqBuilder.closeWriter() ;
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
}



/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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