/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package arq;


import org.apache.jena.atlas.io.IndentedWriter ;
import arq.cmd.CmdException ;
import arq.cmd.TerminationException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModAlgebra ;
import arq.cmdline.ModDataset ;
import arq.cmdline.ModDatasetGeneralAssembler ;
import arq.cmdline.ModEngine ;
import arq.cmdline.ModResultsOut ;
import arq.cmdline.ModTime ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.PlanOp ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class sse_query extends CmdARQ
{
    // Merging with qparse/sparql
    // 1 - split those two into Query and QueryExecution parts
    // 2 - This is then calls on the QueryExecution parts
    // 3 - Printing plan - uses a verbose prefix setting.  Scan to see what's in use.
    //      WriterOp.reducePrologue(prologue, op) => prologue.
    
    protected final ArgDecl printDecl  = new ArgDecl(ArgDecl.HasValue, "print") ;
    
    ModAlgebra    modAlgebra =  new ModAlgebra() ;
    ModDataset    modDataset =  new ModDatasetGeneralAssembler() ;
    ModResultsOut modResults =  new ModResultsOut() ;
    ModTime       modTime =     new ModTime() ;
    ModEngine     modEngine =   new ModEngine() ;

    boolean printOp      = false ;
    boolean printPlan    = false ;
    
    public static void main (String... argv)
    {
        new sse_query(argv).mainRun() ;
    }
    
    public sse_query(String[] argv)
    {
        super(argv) ;
        super.add(printDecl, "--print=op/plan",  "Print details") ;
        super.addModule(modAlgebra) ;
        super.addModule(modResults) ;
        super.addModule(modDataset) ;
        super.addModule(modTime) ;
        super.addModule(modEngine) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;

        for (String arg : getValues(printDecl))
        {
            if ( arg.equalsIgnoreCase("op") ||
                      arg.equalsIgnoreCase("alg") || 
                      arg.equalsIgnoreCase("algebra") ) { printOp = true ; }
            else if ( arg.equalsIgnoreCase("plan"))     { printPlan = true ; }
            else
                throw new CmdException("Not a recognized print form: "+arg+" : Choices are: query, op, quad") ;
        }
        
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --data=<file> --query=<query>" ; }

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    //static final String divider = "" ;
    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    @Override
    protected void exec()
    {
        Op op = modAlgebra.getOp() ;

        if ( op == null )
        {
            System.err.println("No query expression to execute") ;
            throw new TerminationException(9) ;
        }

        Dataset dataset = modDataset.getDataset() ;
        // Check there is a dataset.
        if ( dataset == null )
            dataset = DatasetFactory.createMem() ;

        modTime.startTimer() ;
        DatasetGraph dsg = dataset.asDatasetGraph() ;

        if ( printOp || printPlan )
        {
            if ( printOp )
            {
                divider() ;
                IndentedWriter out = new IndentedWriter(System.out, true) ;
                op.output(out) ;
                out.flush();
            }

            if ( printPlan )
            {
                QueryIterator qIter = Algebra.exec(op, dsg) ;
                Plan plan = new PlanOp(op, null, qIter) ;
                divider() ;
                IndentedWriter out = new IndentedWriter(System.out, false) ;
                plan.output(out) ;
                out.flush();
            }
            //return ;
        }

        // Do not optimize.  Execute as-is.
        QueryExecUtils.execute(op, dsg, modResults.getResultsFormat()) ;

        long time = modTime.endTimer() ;
        if ( modTime.timingEnabled() )
            System.out.println("Time: "+modTime.timeStr(time)) ;
    }    

}
