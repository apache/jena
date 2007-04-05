/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.io.IOException;
import java.util.Iterator;

import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import arq.cmdline.*;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DataSourceImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.*;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef;
import com.hp.hpl.jena.sparql.resultset.ResultSetException;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileUtils;

public class qexec extends CmdARQ
{
    // Merging with qparse/sparql
    // 1 -  split those two into Query and QueryExecution parts
    // 2 - This is then calls on the QueryExecution parts
    // 
    
    protected final ArgDecl queryFileDecl = new ArgDecl(ArgDecl.HasValue, "query", "file") ;
    protected final ArgDecl printDecl  = new ArgDecl(ArgDecl.HasValue, "print") ;
    ModDataset    modDataset =  new ModAssembler() ;    // extends ModDataset
    ModResultsOut modResults =  new ModResultsOut() ;
    ModTime       modTime =     new ModTime() ;
    ModEngine     modEngine =   new ModEngine() ;

    
    String queryFilename = null ;
    String queryString   = null ;
    boolean printOp      = false ;
    boolean printPlan    = false ;
    
    public static void main (String [] argv)
    {
        new qexec(argv).main() ;
    }
    
    public qexec(String[] argv)
    {
        super(argv) ;
        super.add(queryFileDecl, "--query=FILE", "Algebra file to execute") ;
        super.add(printDecl, "--print=op/plan",  "Print details") ;
        super.addModule(modResults) ;
        super.addModule(modDataset) ;
        super.addModule(modTime) ;
        super.addModule(modEngine) ;
    }

    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( contains(queryFileDecl) )
            queryFilename = super.getValue(queryFileDecl) ;
        
        for ( Iterator iter = getValues(printDecl).iterator() ; iter.hasNext() ; )
        {
            String arg = (String)iter.next() ;
            if ( arg.equalsIgnoreCase("op") ||
                      arg.equalsIgnoreCase("alg") || 
                      arg.equalsIgnoreCase("algebra") ) { printOp = true ; }
            else if ( arg.equalsIgnoreCase("plan"))     { printPlan = true ; }
            else
                throw new CmdException("Not a recognized print form: "+arg+" : Choices are: query, op, quad") ;
        }
        
    }
    
    protected String getCommandName() { return Utils.className(this) ; }
    
    protected String getSummary() { return getCommandName()+" --data=<file> --query=<query>" ; }

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    //static final String divider = "" ;
    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    protected void exec()
    {
        
        // This coudl all be neatened up and integrate with query/qparse.
        // But I need the tool now!
    try {
        // ModAlgebra?
        
        Op op = null ;
        
        if ( queryFilename != null )
        {
            if ( queryFilename.equals("-") )
            {
                try {
                    // Stderr?
                    queryString  = FileUtils.readWholeFileAsUTF8(System.in) ;
                    // And drop into next if
                } catch (IOException ex)
                { throw new CmdException("Error reading stdin", ex) ; }
            }
            else
                op = Algebra.read(queryFilename) ;
        }

        if ( queryString != null )
            op = Algebra.parse(queryString) ;
        
        if ( op == null )
        {
            System.err.println("No query expression to execute") ;
            throw new TerminationException(9) ;
        }
        
        Dataset dataset = modDataset.getDataset() ;
        // Check there is a dataset
        if ( dataset == null )
        {
            dataset = new DataSourceImpl();
//            System.err.println("No dataset") ;
//            throw new TerminationException(1) ;
        }
        
        modTime.startTimer() ;
        DatasetGraph dsg = new DataSourceGraphImpl(dataset) ;
        
        QueryExecutionGraph qe = QueryExecutionGraphFactory.create(new Query(), dsg) ;
        
//        if ( ! ( qe instanceof QueryExecutionOp ) )
//        {
//            System.err.println("Didn't find a query engine capable of dealing with an algebra expression directly") ;
//            throw new TerminationException(1) ;
//        }
//        QueryExecutionOp qexec = (QueryExecutionOp)qe ;
//        QueryIterator qIter = qexec.eval(op, dsg) ;
        
        QueryIterator qIter = null ;
        // quick hack.
        if ( qe instanceof QueryEngineMain )
            qIter = QueryEngineMain.eval(op, dsg) ;
        else if ( qe instanceof QueryEngineRef )
            qIter = QueryEngineRef.eval(op, dsg) ;
        else
        {
            System.err.println("Didn't find a query engine capable of dealing with an algebra expression directly") ;
            throw new TerminationException(1) ;
        }
        
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
                divider() ;
                IndentedWriter out = new IndentedWriter(System.out, false) ;
                Plan plan = new PlanOp(op, qIter) ;
                plan.output(out) ;
                out.flush();
            }
            return ;
        }
        
        QueryExecUtils.executeAlgebra(op, qIter, modResults.getResultsFormat()) ;
        
        long time = modTime.endTimer() ;
        if ( modTime.timingEnabled() )
            System.out.println("Time: "+modTime.timeStr(time)) ;
        
    }
    catch (ARQInternalErrorException intEx)
    {
        System.err.println(intEx.getMessage()) ;
        if ( intEx.getCause() != null )
        {
            System.err.println("Cause:") ;
            intEx.getCause().printStackTrace(System.err) ;
            System.err.println() ;
        }
        intEx.printStackTrace(System.err) ;
    }
    catch (ResultSetException ex)
    {
        System.err.println(ex.getMessage()) ;
        ex.printStackTrace(System.err) ;
    }
    catch (QueryException qEx)
    {
        //System.err.println(qEx.getMessage()) ;
        throw new CmdException("Query Exeception", qEx) ;
    }
    catch (JenaException ex) { throw ex ; } 
    catch (CmdException ex) { throw ex ; } 
    catch (Exception ex)
    {
        throw new CmdException("Exception", ex) ;
    }
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