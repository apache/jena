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

package dev ;

import java.util.List ;

import org.apache.jena.atlas.logging.LogCtl ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.PlanOp ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;

public class RefactorQueryExecution
{
    static { LogCtl.setCmdLogging(); }

    public static void main(String... argv) throws Exception {
    }

    /* Rework query execution.
     * Only execute from op.
     *   Standard QueryEngineFactory 
     *   QueryEngineFactory is not per implementation.
     * op-> Plan.
     * Replace QueryEngineRegistry.findFactory
     *   by standard process of:
     * 
     * Wrappign in a 
     */
    
    static void execute(DatasetGraph dsg, Op op, Context context) {
        /*
        Algebra.exec(op, dsg)
        ==>
        QueryEngineFactory f = QueryEngineRegistry.findFactory(op, ds, null) ;
        Plan plan = f.create(op, ds, BindingRoot.create(), null) ;
        return plan.iterator() ;
        */
        
        context = Context.setupContext(context, dsg) ;
        OpExecutorFactory factory = QC.getFactory(context) ;
        if ( factory == null )
            factory = OpExecutor.stdFactory ;
        
        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg, factory) ;
        QueryIterator qIterRoot = OpExecutor.createRootQueryIterator(execCxt) ; 
        QueryIterator qIterPlan = QC.execute(op, qIterRoot, execCxt) ;
        Plan plan = new PlanOp(op, null, qIterPlan) ;
        runPlan(plan) ;
        
        /*
        Query query = null ;
        Dataset dataset = null ;
        QueryEngineFactory qefactory = null ;
        @SuppressWarnings("resource")
        QueryExecutionBase queryExecutionBase = new QueryExecutionBase(query, dataset, context, qefactory)  ;
        */
    }
    
    private static void runPlan(Plan plan) {
        //QueryExecUtils.execute(op, dsg)
        List<String> vars = null ;
        ResultsFormat outputFormat = ResultsFormat.FMT_TEXT ;
        
        Op op = plan.getOp();
        if ( op instanceof OpProject )
            vars = Var.varNames(((OpProject)op).getVars()) ;
        else
            vars = Var.varNames(OpVars.visibleVars(op)) ;

        ResultSet results = ResultSetFactory.create(plan.iterator(), vars) ;
        QueryExecUtils.outputResultSet(results, null, outputFormat) ;
    }
}
