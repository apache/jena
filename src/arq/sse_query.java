/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq;


import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import arq.cmdline.*;

import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DataSourceImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.PlanOp;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.query.Dataset;

public class sse_query extends CmdARQ
{
    // Merging with qparse/sparql
    // 1 - split those two into Query and QueryExecution parts
    // 2 - This is then calls on the QueryExecution parts
    // 3 - Printing plan - uses a verbose prefix setting.  Scan to see what's in use.
    //      WriterOp.reducePrologue(prologue, op) => prologue.
    
    protected final ArgDecl printDecl  = new ArgDecl(ArgDecl.HasValue, "print") ;
    
    ModAlgebra    modAlgebra =  new ModAlgebra() ;
    ModDataset    modDataset =  new ModDatasetAssembler() ;
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
            dataset = new DataSourceImpl();

        modTime.startTimer() ;
        DatasetGraph dsg = new DataSourceGraphImpl(dataset) ;

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
        QueryExecUtils.executeAlgebra(op, dsg, modResults.getResultsFormat()) ;

        long time = modTime.endTimer() ;
        if ( modTime.timingEnabled() )
            System.out.println("Time: "+modTime.timeStr(time)) ;
    }    

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) 2010 Talis Information Ltd.
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