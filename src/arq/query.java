/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq;

import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import arq.cmdline.*;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.resultset.ResultSetException;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.sparql.util.Utils;

public class query extends CmdARQ
{
    private ArgDecl argRepeat = new ArgDecl(ArgDecl.HasValue, "repeat") ;
    protected int repeatCount = 1 ; 
    
    protected ModTime       modTime =     new ModTime() ;
    protected ModQueryIn    modQuery =    new ModQueryIn() ;
    protected ModDataset    modDataset =  null ;
    protected ModResultsOut modResults =  new ModResultsOut() ;
    protected ModEngine     modEngine =   new ModEngine() ;
    
    public static void main (String... argv)
    {
        new query(argv).mainRun() ;
    }
    
    public query(String[] argv)
    {
        super(argv) ;
        modDataset = setModDataset() ;
        super.addModule(modQuery) ;
        super.addModule(modResults) ;
        super.addModule(modDataset) ;
        super.addModule(modEngine) ;
        super.addModule(modTime) ;
        super.add(argRepeat) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( contains(argRepeat) )
            try {
                repeatCount = Integer.parseInt(getValue(argRepeat)) ;
            } catch (NumberFormatException ex)
            {
                throw new CmdException("Can't parse "+getValue(argRepeat)+" as an integer", ex) ;
            }
    }
    
    protected ModDataset setModDataset()
    {
        return new ModDatasetAssembler() ;
    }
    
    @Override
    protected void exec()
    {
        for ( int i = 0 ; i < repeatCount ; i++ )
            queryExec() ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --data=<file> --query=<query>" ; }
    
    protected Dataset getDataset()  { return modDataset.getDataset() ; }
    
    protected void queryExec()
    {
        try{
            Query query = modQuery.getQuery() ;
            if ( isVerbose() )
            {
                IndentedWriter out = new IndentedWriter(System.out, true) ;
                query.serialize(out) ;
                out.flush() ;
                System.out.println();
            }
            
            Dataset dataset = getDataset() ;
            modTime.startTimer() ;
            QueryExecution qe = QueryExecutionFactory.create(query, dataset) ;
            // Check there is a dataset
            
            if ( dataset == null && ! query.hasDatasetDescription() )
            {
                System.err.println("Dataset not specified in query nor provided on command line.");
                throw new TerminationException(1) ;
            }
            QueryExecUtils.executeQuery(query, qe, modResults.getResultsFormat()) ;
            long time = modTime.endTimer() ;
            if ( modTime.timingEnabled() )
                System.out.println("Time: "+modTime.timeStr(time)+" sec") ;
            qe.close() ;
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
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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