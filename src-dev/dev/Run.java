/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.ArrayList;
import java.util.List;

import arq.qexpr;
import arq.qparse;
import arq.sparql;
import arq.cmd.ResultsFormat;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.algebra.AlgebraGenerator;
import com.hp.hpl.jena.query.algebra.Op;
import com.hp.hpl.jena.query.algebra.op.OpBGP;
import com.hp.hpl.jena.query.algebra.op.OpFilter;
import com.hp.hpl.jena.query.algebra.op.OpJoin;
import com.hp.hpl.jena.query.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.query.core.BasicPattern;
import com.hp.hpl.jena.query.core.DataSourceImpl;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.ResultSetStream;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.main.JoinClassifier;
import com.hp.hpl.jena.query.engine.main.LeftJoinClassifier;
import com.hp.hpl.jena.query.engine.main.QueryEngineMain;
import com.hp.hpl.jena.query.engine.ref.QueryEngineRef;
import com.hp.hpl.jena.query.expr.E_LessThan;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.expr.NodeValue;
import com.hp.hpl.jena.query.expr.NodeVar;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.query.util.QueryExecUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;


public class Run
{
    public static void main(String[] argv)
    {
        //runQExpr() ;
        print() ;
        //code() ;
        //classifyJ() ;
        //classifyLJ() ;
        execQuery("D.ttl", "Q.rq") ;
        query() ;
    }
        
    
    private static void code()
    {
        // Experimental low level access to query execution. 
        String BASE = "http://example/" ; 
        BasicPattern bp = new BasicPattern() ;
        Var var_x = Var.alloc("x") ;
        Var var_z = Var.alloc("z") ;
        
        bp.add(new Triple(var_x, Node.createURI(BASE+"p"), var_z)) ;
        Op op = new OpBGP(bp) ;
        //Expr expr = ExprUtils.parse("?z < 2 ") ;
        Expr expr = new E_LessThan(new NodeVar(var_z), NodeValue.makeNodeInteger(2)) ;
        op = OpFilter.filter(expr, op) ;
        
        Model m = makeModel() ;
        m.write(System.out, "TTL") ;
        System.out.println("--------------") ;
        System.out.print(op) ;
        System.out.println("--------------") ;
        QueryIterator qIter = QueryEngineMain.eval(op, m.getGraph()) ;
        
        
        // Either read the query itertaor driectly ...
        if ( false )
        {
            for ( ; qIter.hasNext() ; )
            {
                Binding b = qIter.nextBinding() ;
                Node n = b.get(var_x) ;
                System.out.println(FmtUtils.stringForNode(n)) ;
                System.out.println(b) ; 
            }
            qIter.close() ;
        }
        else
        {
            // Or make ResultSet from it (but not both)
            List varNames = new ArrayList() ;
            varNames.add("x") ;
            varNames.add("z") ;
            ResultSet rs = new ResultSetStream(varNames, m, qIter);
            ResultSetFormatter.out(rs) ;
            qIter.close() ;
        }
        System.exit(0) ;
    }

    private static Model makeModel()
    {
        String BASE = "http://example/" ;
        Model model = ModelFactory.createDefaultModel() ;
        model.setNsPrefix("", BASE) ;
        Resource r1 = model.createResource(BASE+"r1") ;
        Resource r2 = model.createResource(BASE+"r2") ;
        Property p1 = model.createProperty(BASE+"p") ;
        Property p2 = model.createProperty(BASE+"p2") ;
        RDFNode v1 = model.createTypedLiteral("1", XSDDatatype.XSDinteger) ;
        RDFNode v2 = model.createTypedLiteral("2", XSDDatatype.XSDinteger) ;
        
        r1.addProperty(p1, v1).addProperty(p1, v2) ;
        r1.addProperty(p2, v1).addProperty(p2, v2) ;
        r2.addProperty(p1, v1).addProperty(p1, v2) ;
        
        return model  ;
    }
    
    private static void classifyJ()
    {
        classifyJ("{?s :p :o . { ?s :p :o FILTER(true) } }", true) ;
        classifyJ("{?s :p :o . { ?s :p :o FILTER(?s) } }", true) ;
        classifyJ("{?s :p :o . { ?s :p ?o FILTER(?o) } }", true) ;
        classifyJ("{?s :p :o . { ?s :p :o FILTER(?o) } }", true) ;
        classifyJ("{?s :p :o . { ?x :p :o FILTER(?s) } }", false) ;

        classifyJ("{ { ?s :p :o FILTER(true) } ?s :p :o }", true) ;
        classifyJ("{ { ?s :p :o FILTER(?s) }   ?s :p :o }", true) ;
        classifyJ("{ { ?s :p ?o FILTER(?o) }   ?s :p :o }", true) ;
        classifyJ("{ { ?s :p :o FILTER(?o) }   ?s :p :o }", true) ;
        classifyJ("{ { ?x :p :o FILTER(?s) }   ?s :p :o }", false) ;

        classifyJ("{?s :p :o . { OPTIONAL { ?s :p :o FILTER(true) } } }", true) ;
        classifyJ("{?s :p :o . { OPTIONAL { ?s :p :o FILTER(?s) } } }", true) ;
        classifyJ("{?s :p :o . { ?x :p :o OPTIONAL { ?s :p :o FILTER(?x) } } }", true) ;
        classifyJ("{?s :p :o . { OPTIONAL { ?s :p ?o FILTER(?o) } } }", true) ;
        classifyJ("{?s :p :o . { OPTIONAL { ?s :p :o FILTER(?o) } } }", true) ;
        classifyJ("{?s :p :o . { OPTIONAL { ?x :p :o FILTER(?s) } } }", false) ;

        classifyJ("{?s :p :o . { OPTIONAL { ?s :p :o } } }", true) ;
        
        System.exit(0) ;
    }
    
        
    private static void classifyJ(String pattern, boolean expected)
    {
        System.out.println("--------------------------------") ;
        System.out.println(pattern) ;
        String qs1 = "PREFIX : <http://example/>\n" ;
        String qs = qs1+"SELECT * "+pattern;
        Query query = QueryFactory.create(qs) ;
        Op op = AlgebraGenerator.compile(query.getQueryPattern()) ;
        
        if ( op instanceof OpJoin )
        {
            boolean nonLinear = JoinClassifier.isLinear((OpJoin)op) ;
            System.out.println("Linear: "+nonLinear) ;
            if ( nonLinear != expected )
            {
                System.out.print(op) ;
                System.out.println("**** Mismatch with expectation") ;
            }
        }
        else
            System.out.println("Not a join") ;

    }

    private static void classifyLJ()
    {
        String pattern = "" ;
        classifyLJ("{ ?s ?p ?o OPTIONAL { ?s1 ?p2 ?x} }", true)  ;
        classifyLJ("{ ?s ?p ?o OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 ?p2 ?x} } }", true)  ;
        classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 :p ?o3} } }", true)  ;
        classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 :p ?x} } }", false)  ;
        System.exit(0) ;
    }
        
    
    private static void classifyLJ(String pattern, boolean expected)
    {
        System.out.println() ;
        System.out.println(pattern) ;
        String qs1 = "PREFIX : <http://example/>\n" ;
        String qs = qs1+"SELECT * "+pattern;
        Query query = QueryFactory.create(qs) ;
        Op op = AlgebraGenerator.compile(query.getQueryPattern()) ;
        
        if ( op instanceof OpLeftJoin )
        {
            boolean nonLinear = LeftJoinClassifier.isLinear((OpLeftJoin)op) ;
            System.out.println("Linear: "+nonLinear) ;
            if ( nonLinear != expected )
                System.out.println("**** Mismatch with expectation") ;
        }
        else
            System.out.println("Not a left join") ;
        
    }

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

    public static void query()
    {
//        String qs1 = "PREFIX : <http://example/>\n" ;
//        String qs = qs1+"SELECT * { }" ;
//        Query query = QueryFactory.create(qs) ;
        
        Query query = QueryFactory.read("Q.rq") ;
        Model data = FileManager.get().loadModel("D.ttl") ;
        DataSource ds = new DataSourceImpl() ;
        ds.setDefaultModel(data) ;
        //ds.addNamedModel("http://example/g", data) ;
        
        if ( false )
        {
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            System.out.println("==== Engine1") ;
            QueryExecUtils.executeQuery(query, qExec, ResultsFormat.FMT_RS_TEXT) ;
        }

        if ( false )
        {
            QueryEngineRef.register() ;
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            System.out.println("==== EngineRef") ;
            //System.out.print(((QueryEngineBase)qExec).getPlan()) ;
            QueryExecUtils.executeQuery(query, qExec, ResultsFormat.FMT_RS_TEXT) ;
            QueryEngineRef.unregister() ;
        }

        QueryEngineMain.register() ;
        System.out.println("==== EngineX") ;
        QueryEngineMain qe = new QueryEngineMain(query) ;
        qe.setDataset(ds) ;
        QueryIterator qIter = qe.getPlan().iterator() ; 
        System.out.println(qIter) ;
        System.out.print(qe.getPlan()) ;
        QueryExecUtils.executeQuery(query, qe, ResultsFormat.FMT_RS_TEXT) ;
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
    public static void runQParse()
    {
        qparse.main(new String[]{ //"--in=prefix", 
                                  //"--out=prefix",
                                  "--print=op",
                                  "--print=query",
                                  "--engine=ref",
                                  "--query=Q.rq"
                                  //"--query=testing/ARQ/Serialization/syntax-general-13.rq" ,
                                  }) ;
        System.exit(0) ;
    }

    static public void runQExpr()
    {
        qexpr.main(new String[]{"--print=prefix", "--", "-3"}) ;
        qexpr.main(new String[]{"--print=prefix", "--", "+3"}) ;
        qexpr.main(new String[]{"--print=prefix", "--", "+ 3"}) ;
        qexpr.main(new String[]{"--print=prefix", "--", "- 3"}) ;
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