/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import arq.sparql;
import arq.cmd.QueryCmdUtils;
import arq.cmd.ResultsFormat;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.core.DataSourceImpl;
import com.hp.hpl.jena.query.engine.QueryEngineBase;
import com.hp.hpl.jena.query.engine2.QueryEngineRef;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.engine2.op.OpLeftJoin;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

import engine3.LeftJoinClassifier;
import engine3.QueryEngineX;

public class Run
{
    public static void main(String[] argv)
    {
        //classify() ;
        query() ;
    }
        
        
    private static void classify()
    {
        String pattern = "" ;
        classify("{ ?s ?p ?o OPTIONAL { ?s1 ?p2 ?x} }", true)  ;
        classify("{ ?s ?p ?o OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 ?p2 ?x} } }", true)  ;
        classify("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 :p ?o3} } }", true)  ;
        classify("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 :p ?x} } }", false)  ;
        System.exit(0) ;
    }
        
    
    private static void classify(String pattern, boolean expected)
    {
        System.out.println() ;
        System.out.println(pattern) ;
        String qs1 = "PREFIX : <http://example/>\n" ;
        String qs = qs1+"SELECT * "+pattern;
        Query query = QueryFactory.create(qs) ;
        QueryEngineX qe = new QueryEngineX(query) ;
        Op op = ((QueryEngineX)qe).getPatternOp() ;
        
        
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


    public static void query()
    {
        String qs1 = "PREFIX : <http://example/>\n" ;
        String qs = qs1+"SELECT * { }" ;
        Query query = QueryFactory.create(qs) ;
        
        //Query query = QueryFactory.read("Q.rq") ;
        Model data = FileManager.get().loadModel("D.ttl") ;
        DataSource ds = new DataSourceImpl() ;
        ds.setDefaultModel(data) ;
        //ds.addNamedModel("http://example/g", data) ;
        
        if ( false )
        {
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            System.out.println("==== Engine1") ;
            QueryCmdUtils.executeQuery(query, qExec, ResultsFormat.FMT_RS_TEXT) ;
        }

        if ( true )
        {
            QueryEngineRef.register() ;
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            System.out.println("==== EngineRef") ;
            //System.out.print(((QueryEngineBase)qExec).getPlan()) ;
            QueryCmdUtils.executeQuery(query, qExec, ResultsFormat.FMT_RS_TEXT) ;
            QueryEngineRef.unregister() ;
        }

        QueryEngineX.register() ;
        System.out.println("==== EngineX") ;
        QueryEngineX qe = new QueryEngineX(query) ;
        qe.setDataset(ds) ;
        System.out.print(((QueryEngineBase)qe).getPlan()) ;
        QueryCmdUtils.executeQuery(query, qe, ResultsFormat.FMT_RS_TEXT) ;
        System.exit(0) ;
    }
    
    private static void execQuery(String datafile, String queryfile)
    {
        QueryEngineX.register() ;
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