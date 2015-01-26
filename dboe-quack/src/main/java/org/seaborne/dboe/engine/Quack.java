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

package org.seaborne.dboe.engine;

import static com.hp.hpl.jena.tdb.sys.Names.primaryIndexQuads ;
import static com.hp.hpl.jena.tdb.sys.Names.primaryIndexTriples ;

import java.util.Arrays ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.seaborne.dboe.engine.explain.Explain2 ;
import org.seaborne.dboe.engine.explain.ExplainCategory ;
import org.seaborne.dboe.engine.tdb.OpExecutorQuackTDB ;
import org.seaborne.dboe.engine.tdb.PlannerSubstitution ;
import org.seaborne.dboe.engine.tdb.QueryEngineQuackTDB ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.mgt.Explain.InfoLevel ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.MappingRegistry ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.setup.StoreParams ;
import com.hp.hpl.jena.tdb.solver.QueryEngineTDB ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.FileRef ;
import com.hp.hpl.jena.tdb.sys.Names ;

public class Quack {
    /**
     * Context symbol for the choice of OpExecutorFactory.
     * Used by QueryEngineQuackFactory
     */
    public static Symbol opExecutorFactory = Symbol.create("quack:opExecutorFactory") ;
    
    public static final ExplainCategory quackExec = ExplainCategory.create("quack-exec") ;
    public static final ExplainCategory quackPlan = ExplainCategory.create("quack-plan") ;

    public final static Logger log = LoggerFactory.getLogger(Quack.class) ;
    public final static Logger debugLog = LoggerFactory.getLogger(Quack.class) ;
    public final static Logger joinStatsLog = LoggerFactory.getLogger(ARQ.logExecName) ;
    public static boolean JOIN_EXPLAIN = false ;

    private static boolean initialized = false ;
    
    public static void setVerbose(boolean verbose) { 
        if ( verbose ) {
            // Force statistics output 
            LogCtl.enable(ARQ.logExecName) ;
            JOIN_EXPLAIN = true ;
            ARQ.setExecutionLogging(InfoLevel.ALL) ;
            Explain2.setActive(Quack.quackExec);
            Explain2.setActive(Quack.quackPlan);
        } else {
            ARQ.setExecutionLogging(InfoLevel.NONE) ;
            Explain2.remove(Quack.quackExec); 
            Explain2.remove(Quack.quackPlan);
            JOIN_EXPLAIN = false ;
        }
    }
    
    static { init() ; }
    
    // Called by the assembler
    
    public static void init() { 
        if ( initialized )
            return ;
        Quack.log.info("Quack");
        initialized = true ;
        TDB.init();
        MappingRegistry.addPrefixMapping("quack", "quack:");
        
        QueryEngineTDB.unregister(); 
        QueryEngineQuackTDB.register() ;
        Quack.setOpExecutorFactory(OpExecutorQuackTDB.factorySubstitute) ;
        PlannerSubstitution.DOMERGE = false ;

//        rewire() ;

//        if (PlannerTDB.DOMERGE ) {
//            log.info("Quack initialization [ Merge ]");
//            hardRewire();
//        } else
//            log.info("Quack initialization [ No Merge ]");
        // printIndexes() ;
        // ARQ.setExecutionLogging(InfoLevel.ALL);
    }
    
    private static void printIndexes() {
        log.info("Triple primary: "+Names.primaryIndexTriples) ;
        log.info("Triple indexes: "+Arrays.asList(Names.tripleIndexes)) ;
        log.info("Quad primary:   "+Names.primaryIndexQuads) ;
        log.info("Quad indexes:   "+Arrays.asList(Names.quadIndexes)) ;
    }

    public static void reset() {
        QueryEngineQuackTDB.unregister() ;
        QueryEngineTDB.register() ;
    }
    
    public static void rewire() {
        // Reset dataset building global defaults!
        FileRef.register("PSO.idn") ;
        FileRef.register("PSO.dat") ;
    }
    
    public static void explain(boolean state) {
        if ( state )
            Explain2.setActive(quackExec) ;
        else
            Explain2.remove(quackExec);
    }
    
    
    /** Set the OpExecutorFactory to be used by QueryEngineQuackFactory */
    public static void setOpExecutorFactory(OpExecutorFactory factory) {
        setOpExecutorFactory(ARQ.getContext(), factory) ;
    }
    
    /** Set the OpExecutorFactory to be used by QueryEngineQuackFactory */
    public static void setOpExecutorFactory(Context context, OpExecutorFactory factory) {
        if ( factory == null )
            context.remove(opExecutorFactory);
        else
            context.set(opExecutorFactory, factory);
    }

    /** Set the OpExecutorFactory to be used by QueryEngineQuackFactory */
    public static void setOpExecutorFactory(Dataset dataset, OpExecutorFactory factory) {
        setOpExecutorFactory(dataset.getContext(), factory);
    }

    /** Set the OpExecutorFactory to be used by QueryEngineQuackFactory */
    public static void setOpExecutorFactory(DatasetGraph dataset, OpExecutorFactory factory) {
        setOpExecutorFactory(dataset.getContext(), factory);
    }

    /* Return the current global setting for OpExecutorFactory for QueryEngineQuack.*/ 
    public static OpExecutorFactory getOpExecutorFactory(Context context) {
        return (OpExecutorFactory)context.get(Quack.opExecutorFactory, null) ;
    }
    
    public static void hardRewire() {
        // TODO Need to set the constants 
//        SystemParams sParams = SystemParams.getStdSystemParams() ;
//        sParams.tripleIndexes = new String[] { primaryIndexTriples, "POS", "PSO", "OSP" } ;
//        sParams.quadIndexes = new String[] { primaryIndexQuads, "GPOS", "GPSO", "GOSP", 
//            "POSG", "PSOG", "OSPG", "SPOG"} ;
        //SystemParams.setDefault(...) ;
        
        // Final :-(
//        Names.tripleIndexes = new String[] { primaryIndexTriples, "POS", "PSO", "OSP" } ;
//        Names.quadIndexes = new String[] { primaryIndexQuads, "GPOS", "GPSO", "GOSP", 
//                        "POSG", "PSOG", "OSPG", "SPOG"} ;
        FileRef.register("PSO.idn") ;
        FileRef.register("PSO.dat") ;
        
        // Replace OSP - until a global default SystemParams is ready. 
        Names.tripleIndexes[2] = "PSO" ;
    }
    
    public static Dataset createDataset(OpExecutorFactory executorFactory) {
        return createDataset(Location.mem(), executorFactory) ;
    }
    
    public static DatasetGraph createDatasetGraph(OpExecutorFactory executorFactory) {
        return createDatasetGraph(Location.mem(), executorFactory) ;
    }
    
    public static Dataset createDataset(Location location, OpExecutorFactory executorFactory) {
        return DatasetFactory.create(createDatasetGraph(location, executorFactory)) ;
    }
    
    public static DatasetGraph createDatasetGraph(Location location, OpExecutorFactory executorFactory) {
        LogCtl.setError(FileRef.class) ;
        StoreParams sParams = StoreParams.builder()
            // Test to see if POS exists but PSO does not.
            .tripleIndexes(new String[] { primaryIndexTriples, "POS", "PSO", "OSP" })
            //{ primaryIndexQuads, "GPOS", "GOSP", "POSG", "OSPG", "SPOG"} ;
            .quadIndexes(new String[] { primaryIndexQuads, "GPOS", "GPSO", "GOSP", 
            "POSG", "PSOG", "OSPG", "SPOG"})
            .build() ;
        DatasetGraphTDB dsg = DatasetBuilderStd.stdBuilder().build(location, sParams) ;
        QC.setFactory(dsg.getContext(), executorFactory) ;
        return dsg ;
    }
}
