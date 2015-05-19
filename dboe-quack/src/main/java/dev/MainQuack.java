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

package dev;

import java.io.PrintStream ;

import org.apache.jena.atlas.logging.LogCtl ;
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
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.seaborne.dboe.engine.Quack ;
import org.seaborne.dboe.engine.general.OpExecutorRowsMain ;
import org.seaborne.dboe.engine.general.OpExecutorStageMain ;
import org.seaborne.dboe.engine.general.QueryEngineMain2 ;
import org.seaborne.dboe.engine.tdb.OpExecutorQuackTDB ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.sys.SystemTDB ;

public class MainQuack {
    static { LogCtl.setLog4j() ; }
    static String TDIR1 = "testing/Engines" ;
    static String TDIR2 = "testing/BasicPatterns" ;

    public static void main(String... args) {
//        System.out.printf("%s\n", IndexRef.parse("DB[SPO]")) ;
//        System.out.printf("%s\n", IndexRef.parse("[SPO]")) ;
//        System.exit(0) ;
//        
//        
//        Quack.init() ;
//        QuackPredObj.init() ;
//        Quack.setOpExecutorFactory(OpExecutorQuackTDB.factorySubstitute) ;
//        
//        
//        DatasetGraphTDB dsgtdb = build(Location.mem()) ;
//        AccessorTDB accessor = AccessorTDB.create(new StorageTDB(dsgtdb)) ; 
//        Planner planner = new PlannerPredObjList(accessor) ;
//        BasicPattern bgp = SSE.parseBGP("(bgp (?s :q ?v) (?s :p 123))") ;
//        
//        Node p = SSE.parseNode(":p") ;
//        Node q = SSE.parseNode(":q") ;
//        
//        accessor.getNodeTable().getAllocateNodeId(p) ;
//        accessor.getNodeTable().getAllocateNodeId(q) ;
//        
//        List<Tuple<Slot<NodeId>>> tuples = ELibTDB.convertTriples(bgp.getList(), accessor.getNodeTable()) ;
//        PhysicalPlan<NodeId> plan = planner.generatePlan(tuples) ;
//        RowList<NodeId> input = RowLib.identityRowList()  ;
//        plan.execute(input) ;
//        
//        System.out.println(plan) ;
//        System.exit(0) ;
    }
    
    public static void mainNodeId(String datafile, String queryFile) {
        Quack.init() ;
        
        Query query = QueryFactory.read(queryFile) ;
        Dataset dsMem = TDBFactory.createDataset() ;
        RDFDataMgr.read(dsMem, datafile) ;
        
//        // TDB current execution.
//        Quack.setOpExecutorFactory(dsMem, OpExecutorQuackTDB.factoryTDB1) ;
//        doOne("TDB", dsMem, query) ;

        //ARQ.setExecutionLogging(InfoLevel.ALL) ;
        Quack.explain(true) ;
        Quack.setOpExecutorFactory(dsMem, OpExecutorQuackTDB.factoryPredicateObject) ;
        doOne("Quack/PredObj", dsMem, query) ;
        System.out.flush() ;

//        Quack.setOpExecutorFactory(dsMem, OpExecutorQuackTDB.factorySubstitute) ;
//        doOne("Quack/Plain", dsMem, query) ;
//        System.out.flush() ;
        
//        try {
//            StepPredicateObjectList.UseNaiveExecution = true ;
//            doOne("QuackPredObj[simple]", dsMem, query) ;
//        } finally { StepPredicateObjectList.UseNaiveExecution = false ; }
    }

    public static void mainNode(String datafile, String queryFile) {
            // New node-space executor.
            System.out.println("**** OpExecutorMain2 ??????") ;
    
            Query query = QueryFactory.read(queryFile) ;
            Dataset dsMem = DatasetFactory.createMem() ;
            RDFDataMgr.read(dsMem, datafile) ;
    
    //        RDFDataMgr.write(System.out, dsMem, Lang.TRIG) ;
    //        System.out.println(query);
    
            QueryEngineMain2.register();
            try {
                // Test convert to Quad form and execute
                doOne("ARQ", dsMem, query) ;
    
                QC.setFactory(dsMem.getContext(), OpExecutorStageMain.factoryMain) ;
                doOne("StageMain", dsMem, query) ;
    
                QC.setFactory(dsMem.getContext(), OpExecutorRowsMain.factoryRowsMain) ;
                doOne("ARQ/2", dsMem, query) ;
            } finally { 
                QueryEngineMain2.register();
            }
        }

    private static void doOne(String label, Dataset ds, Query query) {
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        System.out.println("** "+label) ; System.out.flush() ;
        QueryExecUtils.executeQuery(query, qExec);
    }

//    private static DatasetGraphTDB build(Location loc) {
//        String[] indexes = { "SOP", "POS", "PSO", "OSP" } ;
//        return build(loc, indexes) ;
//    }
//    
//    private static DatasetGraphTDB build(Location loc, String[] indexes) {
//        StoreParams params = StoreParams.builder()
//            .tripleIndexes(indexes)
//            .node2NodeIdCacheSize(100)
//            .nodeId2NodeCacheSize(100)
//            .nodeMissCacheSize(100)
//            .build() ;
//            
//        DatasetBuilder builder = DatasetBuilderStd.stdBuilder() ;
//        DatasetGraphTDB dsg = builder.build(loc, StoreParams.getDftStoreParams()) ;
//        
//        QC.setFactory(dsg.getContext(), OpExecutorQuackTDB.factoryPredicateObject) ;
//        return dsg ; 
//    }

    // Unset any optimization.
    private static RewriterFactory rewriterFactory = null ;
    private static ReorderTransformation  reorder = null ;
    
    public static void optimizerOff() {
        rewriterFactory = Optimize.getFactory() ;
        Optimize.setFactory(Optimize.noOptimizationFactory) ;
        reorder = SystemTDB.defaultReorderTransform ;
        // Turn off optimization so test queries execute as written.
        SystemTDB.defaultReorderTransform = ReorderLib.identity() ;
    }

    public static void optimizerReset() {
        if ( rewriterFactory != null ) {
            SystemTDB.defaultReorderTransform = reorder ;
            Optimize.setFactory(rewriterFactory) ;
        }
    }
    
    // Run from the test suite
    private static void test(String queryFile, String dataFile) {
        runTest(TDIR1+"/"+queryFile, TDIR1+"/"+dataFile, OpExecutorQuackTDB.factoryPredicateObject) ;
    }

    private static void runTest(String queryFile, String datafile, OpExecutorFactory factory) {
        
        //AccessOps.DEBUG = true ;
        Query query = QueryFactory.read(queryFile) ;
        Dataset ds = DatasetFactory.createMem() ; // TDBFactory.createDataset() ;
        RDFDataMgr.read(ds, datafile) ;
        // Default. Generated expected results.
        QueryExecution qExec1 = QueryExecutionFactory.create(query, ds) ;
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec1.execSelect()) ;
        
        if ( true ) {
            System.out.println(query) ;
            RDFDataMgr.write(System.out, ds, Lang.TRIG);
        }

        // Test 
        optimizerOff() ;
        ds = TDBFactory.createDataset() ;
        RDFDataMgr.read(ds, datafile) ;
        // Test
        if ( factory != null )
            QC.setFactory(ds.getContext(), factory) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSetRewindable rs = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
        optimizerReset();
        
        boolean b = ResultSetCompare.equalsByTerm(rs1, rs) ;
        PrintStream out = System.out ;
        if ( ! b ) {
            out.println("---- Test") ;
            System.out.println(query) ;
            RDFDataMgr.write(System.out, ds, Lang.TRIG);
            out.println("-- Expected") ;
            rs1.reset();
            ResultSetFormatter.out(out, rs1, query) ;
            out.println("-- Actual") ;
            rs.reset();
            ResultSetFormatter.out(out, rs, query) ;
            out.println("Results not equal") ;
        }
        else
            out.println("Test OK") ;
    }
}
