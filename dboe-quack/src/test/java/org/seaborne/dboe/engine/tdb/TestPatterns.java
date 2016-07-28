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
import java.util.Arrays ;
import java.util.Collection ;

import org.apache.jena.query.* ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.algebra.optimize.Optimize ;
import org.apache.jena.sparql.algebra.optimize.Optimize.RewriterFactory ;
import org.apache.jena.sparql.engine.main.OpExecutorFactory ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.junit.* ;
import org.junit.runner.RunWith ;
import org.junit.runners.MethodSorters ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;
import org.seaborne.dboe.engine.Quack2 ;
import org.seaborne.dboe.jenax.Txn ;
import org.seaborne.tdb2.TDB2Factory ;
import org.seaborne.tdb2.solver.OpExecutorTDB1 ;
import org.seaborne.tdb2.sys.SystemTDB ;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPatterns extends Assert
{
    @Parameters(name="{0}")
    public static Collection<Object[]> data()
    { 
        return Arrays.asList(new Object[][] {
            { "OpExecutorTDB", OpExecutorTDB1.OpExecFactoryTDB }
            ,
            { "OpExecutorQuackTDB", OpExecutorQuackTDB.factorySubstitute}
            ,
            { "OpExecutorQuackTDB3", OpExecutorQuackTDB.factoryPredicateObject}
        }) ;                                        
    }

    private RewriterFactory rewriterFactory = null ;
    private ReorderTransformation  reorder = null ;

    @BeforeClass static public void beforeClass() {
        Quack2.init() ;
    }
    
    @AfterClass static public void afterClass() { }
    
    // Unset any optimization.
    @Before
    public void optimizerOff() {
        rewriterFactory = Optimize.getFactory() ;
        Optimize.setFactory(Optimize.noOptimizationFactory) ;
        reorder = SystemTDB.defaultReorderTransform ;
        // Turn off optimization so test queries execute as written.
        SystemTDB.defaultReorderTransform = ReorderLib.identity() ;

    }

    @After
    public void optimizerReset() {
        if ( rewriterFactory != null ) {
            SystemTDB.defaultReorderTransform = reorder ;
            Optimize.setFactory(rewriterFactory) ;
        }
    }

    private static final String DIR ="testing/BasicPatterns" ;
    final static Dataset dataset1 = RDFDataMgr.loadDataset(DIR+"/data-bgp.ttl") ;

    final OpExecutorFactory factory ;

    public TestPatterns(String name/*ignored*/, OpExecutorFactory factory) {
        this.factory = factory ;
        
    }
    // See testing/BasicPatterns/produce.rb
    @Test public void bgp_1_triple_00()    { test("bgp-1-triple-00.rq") ; }
    @Test public void bgp_1_triple_01()    { test("bgp-1-triple-01.rq") ; }
    @Test public void bgp_1_triple_02()    { test("bgp-1-triple-02.rq") ; }
    @Test public void bgp_1_triple_03()    { test("bgp-1-triple-03.rq") ; }
    @Test public void bgp_1_triple_04()    { test("bgp-1-triple-04.rq") ; }
    @Test public void bgp_1_triple_05()    { test("bgp-1-triple-05.rq") ; }
    @Test public void bgp_1_triple_06()    { test("bgp-1-triple-06.rq") ; }
    @Test public void bgp_1_triple_07()    { test("bgp-1-triple-07.rq") ; }
    @Test public void bgp_1_triple_08()    { test("bgp-1-triple-08.rq") ; }
    @Test public void bgp_1_triple_09()    { test("bgp-1-triple-09.rq") ; }
    @Test public void bgp_1_triple_10()    { test("bgp-1-triple-10.rq") ; }
    @Test public void bgp_1_triple_11()    { test("bgp-1-triple-11.rq") ; }
    @Test public void bgp_2_term_00()      { test("bgp-2-term-00.rq") ; }
    @Test public void bgp_2_term_01()      { test("bgp-2-term-01.rq") ; }
    @Test public void bgp_2_term_02()      { test("bgp-2-term-02.rq") ; }
    @Test public void bgp_2_term_03()      { test("bgp-2-term-03.rq") ; }
    @Test public void bgp_2_term_04()      { test("bgp-2-term-04.rq") ; }
    @Test public void bgp_2_term_05()      { test("bgp-2-term-05.rq") ; }
    @Test public void bgp_2_var_00()       { test("bgp-2-var-00.rq") ; }
    @Test public void bgp_2_var_01()       { test("bgp-2-var-01.rq") ; }
    @Test public void bgp_2_var_02()       { test("bgp-2-var-02.rq") ; }
    @Test public void bgp_2_var_03()       { test("bgp-2-var-03.rq") ; }
    @Test public void bgp_2_var_04()       { test("bgp-2-var-04.rq") ; }
    @Test public void bgp_2_var_05()       { test("bgp-2-var-05.rq") ; }
    @Test public void bgp_2_var_06()       { test("bgp-2-var-06.rq") ; }
    @Test public void bgp_3_term_00()      { test("bgp-3-term-00.rq") ; }
    @Test public void bgp_3_term_01()      { test("bgp-3-term-01.rq") ; }
    @Test public void bgp_3_term_02()      { test("bgp-3-term-02.rq") ; }
    @Test public void bgp_3_term_03()      { test("bgp-3-term-03.rq") ; }
    @Test public void bgp_3_term_04()      { test("bgp-3-term-04.rq") ; }
    @Test public void bgp_3_term_05()      { test("bgp-3-term-05.rq") ; }
    @Test public void bgp_3_term_06()      { test("bgp-3-term-06.rq") ; }
    @Test public void bgp_3_var_00()       { test("bgp-3-var-00.rq") ; }
    @Test public void bgp_3_var_01()       { test("bgp-3-var-01.rq") ; }
    @Test public void bgp_3_var_02()       { test("bgp-3-var-02.rq") ; }
    @Test public void bgp_3_var_03()       { test("bgp-3-var-03.rq") ; }
    @Test public void bgp_3_var_04()       { test("bgp-3-var-04.rq") ; }
    @Test public void bgp_3_var_05()       { test("bgp-3-var-05.rq") ; }
    @Test public void bgp_3_var_06()       { test("bgp-3-var-06.rq") ; }
    @Test public void bgp_other_00()       { test("bgp-other-00.rq") ; }
    @Test public void bgp_other_01()       { test("bgp-other-01.rq") ; }
    @Test public void bgp_other_02()       { test("bgp-other-02.rq") ; }
    @Test public void bgp_other_03()       { test("bgp-other-03.rq") ; }
    @Test public void bgp_input_00()       { test("bgp-input-00.rq") ; }
    @Test public void bgp_input_01()       { test("bgp-input-01.rq") ; }
    @Test public void bgp_input_02()       { test("bgp-input-02.rq") ; }
    @Test public void bgp_input_03()       { test("bgp-input-03.rq") ; }
    @Test public void bgp_input_04()       { test("bgp-input-04.rq") ; }
    
    private void test(String fn) {
        Dataset dataset2 = TDB2Factory.createDataset() ;
        Txn.execWrite(dataset2, ()->{
            RDFDataMgr.read(dataset2, DIR+"/data-bgp.ttl") ;
            if ( factory != null )
                QC.setFactory(dataset2.getContext(), factory) ;

            Query query = QueryFactory.read(DIR+"/"+fn) ;
            PrintStream out = System.out ;

            ResultSetRewindable rs1 ;
            ResultSetRewindable rs2 ;
            try {
                rs1 = qexec(query, dataset1) ;
                rs2 = qexec(query, dataset2) ;
            } catch (RuntimeException ex) {
                out.println("*** Failure: "+fn) ;
                out.println(query) ;
                out.flush() ;
                throw ex ;
            }
            boolean b = ResultSetCompare.equalsByTerm(rs1, rs2) ;

            //int count = ResultSetFormatter.consume(rs1) ; 

            if ( !b ) {

                out.println("---- Test") ;
                out.println(query) ;
                RDFDataMgr.write(out, dataset2, Lang.TRIG);
                out.println("-- Expected") ;
                rs1.reset();
                ResultSetFormatter.out(out, rs1, query) ;
                out.println("-- Actual") ;
                rs2.reset();
                ResultSetFormatter.out(out, rs2, query) ;
                out.println("Results not equal") ;
            }
            assertTrue("Result Sets do not compare equals by term" ,b) ;
            //assertNotEquals("Bad test - zero results", 0, count) ;
        }) ;
    }
    
    private static ResultSetRewindable qexec(Query query, Dataset ds) {
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        return ResultSetFactory.makeRewindable(qExec.execSelect()) ;
    }
}
