/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import arq.sparql;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.util.FileManager;


public class Run
{
    public static void main(String[] argv)
    {
        if ( true )
        {
            Node n = SSE.parseNode("'2007-08-30T01:20:30'^^xsd:dateTime") ;
            
            parseXSDDateTime("2007-08-30T01:20:30") ;
            System.exit(0) ;
            
            XSDDatatype.XSDdateTime.isValid("2007-08-30T01:20:30") ;
            
            XSDDateTime xsdDT = (XSDDateTime)n.getLiteralValue() ;
            System.out.println(xsdDT) ;
            
            NodeValue nv = NodeValue.makeNode(n) ;
            System.out.println(nv) ;
            System.exit(0) ;
        }
        runQParse() ;
        
        String []a = { "--strict", "file:///c:/home/afs/W3C/DataAccess/tests/data-r2/expr-builtin/manifest.ttl" } ;
        arq.qtest.main(a) ;
        System.exit(0) ;
        //execQuery(DIR+"data-3.ttl", DIR+"splitIRI-1.rq") ;
    }
    
    private static void parseXSDDateTime(String lex)
    {
        // The dateTime format is quiote fixed.
        // '-'? YYYY - MM - DD T hh : mm : ss.sss +TZ
        // There is no year zero.  No white space.
        
        int idx = 0 ;
        if ( lex.charAt(0) == '-' )
            idx ++ ;
        
        int idx2 = skipDigits(lex, idx) ;
        if ( idx == idx2 )
            throw new ARQException("No digits") ;
        int year = Integer.parseInt(lex.substring(idx, idx2)) ; 
        check(lex, idx2, '-') ;
        idx = idx2+1 ;
        
        idx2 = skipDigits(lex, idx) ;
        if ( idx == idx2 )
            throw new ARQException("No digits") ;
        int month = Integer.parseInt(lex.substring(idx, idx2)) ;
        check(lex, idx2, '-') ;
        idx = idx2+1 ;
        
        idx2 = skipDigits(lex, idx) ;
        if ( idx == idx2 )
            throw new ARQException("No digits") ;
        int day = Integer.parseInt(lex.substring(idx, idx2)) ;
        check(lex, idx2, 'T') ;
        idx = idx2+1 ;
        
        idx2 = skipDigits(lex, idx) ;
        if ( idx == idx2 )
            throw new ARQException("No digits") ;
        int hour = Integer.parseInt(lex.substring(idx, idx2)) ;
        check(lex, idx2, ':') ;
        idx = idx2+1 ;
        
        idx2 = skipDigits(lex, idx) ;
        if ( idx == idx2 )
            throw new ARQException("No digits") ;
        int min = Integer.parseInt(lex.substring(idx, idx2)) ;
        check(lex, idx2, ':') ;
        idx = idx2+1 ;

        // Seconds
        
        // Timezone

        System.out.println(year) ;
        System.out.println(month) ;
        System.out.println(day) ;
        System.out.println(hour) ;
        System.out.println(min) ;
//        System.out.println(sec) ;
//        System.out.println(tz) ;
        
    }
    
    private static int moveOn(String lex, int start, int[] parse, int parseIdx)
    {
        return -1 ;
    }
    
    
    private static void check(String lex, int idx, char c)
    {
        if ( lex.charAt(idx) != c )
            throw new ARQException("dateTime parse error: expected '"+c+"', got '"+lex.charAt(idx)+"' at "+idx+": "+lex) ;
    }

    private static int skipDigits(String lex, int idx)
    {
        for ( int i = idx ; i < lex.length() ; i++ )
        {
            char ch =  lex.charAt(i) ;
            if ( ! Character.isDigit(ch) )
                return i ;
        }
        
        return lex.length() ;
    }

    private static void runQParse()
    {
        String []a = { "--file=Q.arq", "--out=arq", "--print=op", "--print=query" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
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