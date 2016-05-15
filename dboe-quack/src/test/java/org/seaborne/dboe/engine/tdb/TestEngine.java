/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.tdb;

import java.io.PrintStream ;
import java.util.List ;

import org.apache.jena.query.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVars ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.ResultSetStream ;
import org.apache.jena.sparql.engine.main.OpExecutorFactory ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.engine.Quack ;
import org.seaborne.dboe.sys.Names ;
import org.seaborne.tdb2.TDB2Factory ;
import org.seaborne.tdb2.sys.SystemTDB ;

/** Basic testing of OpExecutorQuackTDB, mainly to make sure its called. 
 *  More complete testing happens when the full TDB test suite is run with this engine.
 */

public class TestEngine extends Assert {
    @BeforeClass public static void beforeClass() { Quack.init() ; } 
    
    private static final String DIR = "testing/EngineQ" ;
    private static Location loc = Location.mem() ;
    
    private static Dataset create(String datafile) {
        String x = Names.tripleIndexes[2] ;
        Names.tripleIndexes[2] = "PSO" ;
        try { 
            Dataset ds = TDB2Factory.connectDataset(loc) ;
            SystemTDB.setNonTransactional(ds) ;
            
            if ( datafile != null )
                RDFDataMgr.read(ds, DIR+"/"+datafile) ;
            QC.setFactory(ds.getContext(), OpExecutorQuackTDB.factoryPredicateObject) ;
            return ds ;
        } finally {
            // Force executor for this dataset
            Names.tripleIndexes[2] = x ;
        }
    }
    
    @Test public void exec1() {
        test("SELECT * { }",
             "data1.ttl") ; 
    }

    @Test public void exec2() {
        test("SELECT * { ?x :k ?k }",
             "data1.ttl") ; 
    }

    @Test public void exec3() {
        test("SELECT * { ?x :k ?k . ?x :p ?v . }",
             "data1.ttl") ; 
    }

    @Test public void exec4() {
        test("SELECT * { ?x :k ?k . ?x :p ?v . :s1 :p ?z }",
             "data1.ttl") ; 
    }
    
    @Test public void exec5() {
        // Avoid equality optimziation.
        test("SELECT * { ?s :p :o . ?s :p ?v . FILTER( ?v > 122 && ?v < 124 ) }",
             "data1.ttl") ; 
    }

    @Test public void exec6() {
        test("SELECT * { ?s :p ?v . ?s :p :o . FILTER( ?v > 122 && ?v < 124 ) }",
             "data1.ttl") ; 
    }
    
    // Op tests only?
    
    // Test with OpSeq to test the reorder execution.
    
    @Test public void exec10() {
        testOp("(sequence (bgp (?s :p ?v))  (bgp (?s :p :o)) )",
               "data1.ttl") ;
    }

    @Test public void exec11() {
        testOp("(sequence (bgp (?s :p ?v) (?s :p 123)) (bgp (?s :p ?v))  )",
               "data1.ttl") ;
    }

    @Test public void exec12() {
        testOp("(filter (= ?v 123) (bgp (?s :p ?o) (?s :p ?v)) )",
               "data1.ttl") ;
    }

    @Test public void exec13() {
        testOp("(filter (= ?v 123) (bgp (?s :p ?v) (?s :p ?o)) )",
               "data1.ttl") ;
    }

    @Test public void exec14() {
        testOp("(sequence (assign ((?v 123)) (table unit)) (bgp (?s :p ?v) (?s :p ?o)) )",
               "data1.ttl") ;
    }

    @Test public void exec15() {
        testOp("(sequence (assign ((?v 'Not in the data')) (table unit)) (bgp (?s :p ?v) (?s :p ?o)) )",
               "data1.ttl") ;
    }

    @Test public void exec16() {
        testOp("(sequence (assign ((?A 'Not in the data')) (table unit)) (bgp (?s :p ?v) (?s :p ?o)) )",
               "data1.ttl") ;
    }

    private static final boolean PRINT = false ;
    // Execute three ways: (1) Lizard (2) Plain TDB/modified indexes (3) In-memory
    private void test(String queryString, String datafile) {
        Dataset ds = create(datafile) ;
        OpExecutorFactory origFactory = QC.getFactory(ds.getContext()) ;
        
        Query q = QueryFactory.create("PREFIX : <http://example/> \n"+queryString) ;
        Dataset dsMem = RDFDataMgr.loadDataset(DIR+"/"+datafile) ;
        ResultSetRewindable rsMem = exec(dsMem, q) ;

        // Execute Quack
        ResultSetRewindable rsTDB = exec(ds, q) ;
    
        QC.setFactory(ds.getContext(), OpExecutorQuackTDB.factorySubstitute) ; 
        ResultSetRewindable rsQu = exec(ds, q) ;
        QC.setFactory(ds.getContext(), origFactory) ;
        
        if ( PRINT ) {
            System.out.println(q) ;
            ResultSetFormatter.out(rsQu) ;
        }
        
        boolean b = ResultSetCompare.equalsByTerm(rsTDB, rsQu) ;
        if ( ! b ) {
            PrintStream out = System.out ;
            out.println("---- Different (TDB, Lizard)") ;
            rsTDB.reset();
            ResultSetFormatter.out(out, rsTDB, q) ;
            rsQu.reset();
            ResultSetFormatter.out(out, rsQu, q) ;
            out.println("----") ;
            fail("Results not equal (TDB, Lizard)") ;
        }
        
        b = ResultSetCompare.equalsByTerm(rsMem, rsQu) ;
        if ( ! b ) {
            PrintStream out = System.out ;
            out.println("---- Different (Memory, Lizard)") ;
            rsTDB.reset();
            ResultSetFormatter.out(out, rsMem, q) ;
            rsQu.reset();
            ResultSetFormatter.out(out, rsQu, q) ;
            out.println("----") ;
            fail("Results not equal (Memory, Lizard)") ;
        }
    }
    
    private void testOp(String opString, String datafile) {
        Op op = SSE.parseOp(opString) ;
        
        DatasetGraph dsMem = RDFDataMgr.loadDatasetGraph(DIR+"/"+datafile) ;
        ResultSetRewindable rsMem = exec(dsMem, op) ;
        
        DatasetGraph dsg = create(datafile).asDatasetGraph() ;
        ResultSetRewindable rsQu = exec(dsg, op) ;

        boolean b = ResultSetCompare.equalsByTerm(rsMem, rsQu) ;
        if ( ! b ) {
            PrintStream out = System.out ;
            out.println("---- Different (Memory, Quack)") ;
            rsMem.reset();
            ResultSetFormatter.out(out, rsMem) ;
            rsQu.reset();
            ResultSetFormatter.out(out, rsQu) ;
            out.println("----") ;
            fail("Results not equal (Memory, Quack)") ;
        }
    }

    private ResultSetRewindable exec(Dataset ds, Query q) {
        try(QueryExecution qExec = QueryExecutionFactory.create(q, ds)) {
            ResultSetRewindable rs = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
            return rs ;
        }
    }
    
    private ResultSetRewindable exec(DatasetGraph ds, Op op) {
        QueryIterator qIter = Algebra.exec(op, ds) ;
        List<String> vars = Var.varNames(OpVars.visibleVars(op)) ;
        ResultSetRewindable rs = ResultSetFactory.makeRewindable(new ResultSetStream(vars, null, qIter)) ;
        return rs ; 
    }

}
