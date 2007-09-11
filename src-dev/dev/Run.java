/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import arq.sparql;

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
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprNotComparableException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.util.FileManager;


public class Run
{
    public static void main(String[] argv)
    {
        
        if ( true )
        {
            runParseDateTime("2007-08-31T12:34:56Z") ;
            runParseDateTime("2007-08-31T12:34:56") ;
            runParseDateTime("2007-08-31T12:34:56.003") ;
            runParseDateTime("2007-08-31T12:34:56.003+05:00") ;
            runParseDateTime("-2007-08-31T12:34:56.003-05:00") ;
            System.exit(0) ;
        }
        
        String []a = { "--file=Q.arq", "--out=arq", "--print=op", "--print=query"} ;
        arq.qparse.main(a) ;
        System.exit(0) ;
        System.out.println() ;
        String DIR = "" ;
        execQuery(DIR+"D.ttl", DIR+"Q.arq") ;
    }
    
    private static void runParseDateTime(String str)
    {
        System.out.println(str) ; 
        try {
            DT dt = parseDateTime(str) ;
            System.out.println(str + " ==> " + dt) ;
            if ( ! str.equals(dt.toString())) 
                System.out.println("*** Different") ;
        } catch (DateTimeParseException ex)
        {
            ex.printStackTrace(System.err) ;
        }
    }

    public static void expr(String expr)
    {
        String []a = new String[]{expr} ;
        System.out.println("Eval: "+expr) ;
        arq.qexpr.execAndReturn(a) ;
    }
    
    public static void code()
    {
        Node n1 = SSE.parseNode("'2007-08-31'^^xsd:date") ;
        Node n2 = SSE.parseNode("'2007-08-31Z'^^xsd:date") ;

        NodeValue nv1 = NodeValue.makeNode(n1) ;
        NodeValue nv2 = NodeValue.makeNode(n2) ;

        try {
            System.out.println(NodeValue.compare(nv1, nv2)) ;
        } catch (ExprNotComparableException ex)
        { System.out.println("Can't compare") ; }

        System.out.println() ;

        Binding b = new BindingMap() ;
        b.add(Var.alloc("x"), n1) ;
        b.add(Var.alloc("y"), n2) ;

        expr("( < ?x ?y)", b) ;
        expr("( = ?x ?y)", b) ;
        expr("( = ?x ?x)", b) ;
        expr("( = ?y ?y)", b) ;
    }
    
        
    private static void expr(String string, Binding b)
    {
        Expr expr = SSE.parseExpr(string) ;
        boolean rc = expr.isSatisfied(b, null) ;
        System.out.print(string) ;
        System.out.print(" ==> ") ;
        System.out.println(rc) ;
    }
    
    static class DT
    {
        String neg = null ;         // Null if none. 
        String year = null ;
        String month = null ;
        String day = null ;
        String hour = null ;
        String minute = null ;
        String seconds = null ;     // Inc. fractional parts
        String timezone = null ;    // Null if none.
        
        public String toString()
        { 
            String ySep = "-" ;
            String tSep = ":" ;
            String x = year+ySep+month+ySep+day+"T"+hour+tSep+minute+tSep+seconds ;
            if ( neg != null )
                x = neg+x ;
            if ( timezone != null )
                x = x+timezone ;
            return x ; 
        }
    }
    
    static class DateTimeParseException extends RuntimeException
    {}
    
    private static DT parseDateTime(String str)
    {
        // -? YYYY-MM-DD T hh:mm:ss.ss TZ
        DT dt = new DT() ;
        int idx = 0 ;
        
        if ( str.startsWith("-") )
        {
            dt.neg = "-" ;
            idx = 1 ;
        }
        
        // ---- Year-Month-Day
        dt.year = getDigits(4, str, idx) ;
        idx += 4 ;
        check(str, idx, '-') ;
        idx += 1 ;
        
        dt.month = getDigits(2, str, idx) ;
        idx += 2 ;
        check(str, idx, '-') ;
        idx += 1 ;
        
        dt.day = getDigits(2, str, idx) ;
        idx += 2 ;
        // ---- 
        check(str, idx, 'T') ;
        idx += 1 ;
        
        // ---- 
        // Hour-minute-seconds
        dt.hour = getDigits(2, str, idx) ;
        idx += 2 ;
        check(str, idx, ':') ;
        idx += 1 ;
        
        dt.minute = getDigits(2, str, idx) ;
        idx += 2 ;
        check(str, idx, ':') ;
        idx += 1 ;
        
        // seconds
        dt.seconds = getDigits(2, str, idx) ;
        idx += 2 ;
        if ( idx < str.length() && str.charAt(idx) == '.' )
        {
            idx += 1 ;
            int idx2 = idx ;
            for ( ; idx2 < str.length() ; idx2++ )
            {
                char ch = str.charAt(idx) ;
                if ( ! Character.isDigit(ch) )
                    break ;
            }
            if ( idx == idx2 )
                throw new DateTimeParseException() ;
            dt.seconds = dt.seconds+'.'+str.substring(idx, idx2) ;
            idx = idx2 ;
        }

        // timezone. Z or +/- 00:00
        
        if ( idx < str.length() )
        {
            if ( str.charAt(idx) == 'Z' )
            {
                dt.timezone = "Z" ;
                idx += 1 ;
            }
            else
            {
                boolean signPlus = false ;
                if ( str.charAt(idx) == '+' )
                    signPlus = true ;
                else if ( str.charAt(idx) == '-' )
                    signPlus = false ;
                else
                    throw new DateTimeParseException() ;
                dt.timezone = getDigits(2, str, idx) ;
                check(str, idx, ':') ;
                dt.timezone = dt.timezone+':'+getDigits(2, str, idx) ;
                idx += 5 ;
                 
            }
        }
        
        if ( idx != str.length() )
            throw new DateTimeParseException() ;
        return dt ;
    }
    
    
    private static String getDigits(int num, String string, int start)
    {
        for ( int i = start ; i < (start+num) ; i++ )
        {
            char ch = string.charAt(i) ;
            if ( ! Character.isDigit(ch) )
                throw new DateTimeParseException() ;
            continue ;
        }
        return string.substring(start, start+num) ;
    }
    
    private static void check(String string, int start, char x)
    {
        if ( string.charAt(start) != x ) 
            throw new DateTimeParseException() ;
    }
//
//    // Unused, untested
//    private static boolean hasTZ(String lex)
//    {
//        int idx = "CCYY-MM-DDThh:mm:ss".length() ;
//        
//        if ( lex.charAt(idx) == '-' )
//            idx ++ ;
//        
//        if ( lex.charAt(idx) == '.' )
//        {
//            // skip fractional seconds.
//            int i = idx+1 ;
//            for ( ; i<lex.length() ; i++ )
//            {
//                char ch = lex.charAt(i) ;
//                if ( ! Character.isDigit(ch) )
//                    break ;
//            }
//            idx = i ; 
//        }
//
//        // Anything left is the timezone.
//        return idx < lex.length() ;
//    }

    private static void runQParse()
    {
        String []a = { "--file=Q.rq", "--print=op", "--print=query" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQParseARQ()
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