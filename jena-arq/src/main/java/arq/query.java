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
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.SysRIOT ;
import arq.cmd.CmdException ;
import arq.cmd.TerminationException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModDataset ;
import arq.cmdline.ModDatasetGeneralAssembler ;
import arq.cmdline.ModEngine ;
import arq.cmdline.ModQueryIn ;
import arq.cmdline.ModResultsOut ;
import arq.cmdline.ModTime ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.resultset.ResultSetException ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class query extends CmdARQ
{
    private ArgDecl argRepeat   = new ArgDecl(ArgDecl.HasValue, "repeat") ;
    private ArgDecl argExplain  = new ArgDecl(ArgDecl.NoValue, "explain") ;
    private ArgDecl argOptimize = new ArgDecl(ArgDecl.HasValue, "opt", "optimize") ;

    protected int repeatCount = 1 ; 
    protected int warmupCount = 0 ;
    protected boolean queryOptimization = true ;
    
    protected ModTime       modTime =     new ModTime() ;
    protected ModQueryIn    modQuery =    null;
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
        modQuery = new ModQueryIn(getDefaultSyntax()) ; 
        modDataset = setModDataset() ;
       
        super.addModule(modQuery) ;
        super.addModule(modResults) ;
        super.addModule(modDataset) ;
        super.addModule(modEngine) ;
        super.addModule(modTime) ;

        super.getUsage().startCategory("Control") ;
        super.add(argExplain,  "--explain", "Explain and log query execution") ;
        super.add(argRepeat,   "--repeat=N or N,M", "Do N times or N warmup and then M times (use for timing to overcome start up costs of Java)");
        super.add(argOptimize, "--optimize=", "Turn the query optimizer on or off (default: on)") ;
    }

    /** Default syntax used when the syntax can not be determined from the command name or file extension
     *  The order of determination is:
     *  <ul>
     *  <li>Explicitly given --syntax</li>
     *  <li>File extension</li>
     *  <li>Command default</li>
     *  <li>System default</li>
     *  </ul>
     *  
     */
    protected Syntax getDefaultSyntax()     { return Syntax.defaultQuerySyntax ; }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( contains(argRepeat) )
        {
            String[] x = getValue(argRepeat).split(",") ;
            if ( x.length == 1 )
            {
                try { repeatCount = Integer.parseInt(x[0]) ; }
                catch (NumberFormatException ex)
                { throw new CmdException("Can't parse "+x[0]+" in arg "+getValue(argRepeat)+" as an integer") ; }
                
            }
            else if ( x.length == 2 )
            {
                try { warmupCount = Integer.parseInt(x[0]) ; }
                catch (NumberFormatException ex)
                { throw new CmdException("Can't parse "+x[0]+" in arg "+getValue(argRepeat)+" as an integer") ; }
                try { repeatCount = Integer.parseInt(x[1]) ; }
                catch (NumberFormatException ex)
                { throw new CmdException("Can't parse "+x[1]+" in arg "+getValue(argRepeat)+" as an integer") ; }
            }
            else
                throw new CmdException("Wrong format for repeat count: "+getValue(argRepeat)) ;
        }
        if ( isVerbose() )
            ARQ.getContext().setTrue(ARQ.symLogExec) ;
        
        if ( hasArg(argExplain) )
            ARQ.setExecutionLogging(Explain.InfoLevel.ALL) ;
        
        if ( hasArg(argOptimize) )
        {
            String x1 = getValue(argOptimize) ;
            if ( hasValueOfTrue(argOptimize) || x1.equalsIgnoreCase("on") || x1.equalsIgnoreCase("yes") ) 
                queryOptimization = true ;
            else if ( hasValueOfFalse(argOptimize) || x1.equalsIgnoreCase("off") || x1.equalsIgnoreCase("no") )
                queryOptimization = false ;
            else throw new CmdException("Optimization flag must be true/false/on/off/yes/no. Found: "+getValue(argOptimize)) ;
        }
    }
    
    protected ModDataset setModDataset()
    {
        return new ModDatasetGeneralAssembler() ;
    }
    
    @Override
    protected void exec()
    {
        if ( ! queryOptimization )
            ARQ.getContext().setFalse(ARQ.optimization) ;
        if ( cmdStrictMode )
            ARQ.getContext().setFalse(ARQ.optimization) ;
        
        // Warm up.
        for ( int i = 0 ; i < warmupCount ; i++ )
        {
            queryExec(false, ResultsFormat.FMT_NONE) ;
        }
        
        for ( int i = 0 ; i < repeatCount ; i++ )
            queryExec(modTime.timingEnabled(),  modResults.getResultsFormat()) ;
        
        if ( modTime.timingEnabled() && repeatCount > 1 )
        {
            long avg = totalTime/repeatCount ;
            String avgStr = modTime.timeStr(avg) ;
            System.err.println("Total time: "+modTime.timeStr(totalTime)+" sec for repeat count of "+repeatCount+ " : average: "+avgStr) ;
        }
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --data=<file> --query=<query>" ; }
    
    protected Dataset getDataset()  { 
        try { return modDataset.getDataset() ; }
        catch ( RiotException ex ) { 
            System.err.println("Failed to load data") ;
            throw new TerminationException(1) ;
        }
    }
    
    protected long totalTime = 0 ;
    protected void queryExec(boolean timed, ResultsFormat fmt)
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
            
            if ( isQuiet() )
                LogCtl.setError(SysRIOT.riotLoggerName) ;
            Dataset dataset = getDataset() ;
            modTime.startTimer() ;
            QueryExecution qe = QueryExecutionFactory.create(query, dataset) ;
            // Check there is a dataset
            
            if ( dataset == null && ! query.hasDatasetDescription() )
            {
                System.err.println("Dataset not specified in query nor provided on command line.");
                throw new TerminationException(1) ;
            }
            try { QueryExecUtils.executeQuery(query, qe, fmt) ; } 
            catch (QueryCancelledException ex) { 
                System.out.flush() ;
                System.err.println("Query timed out") ;
            }
            
            long time = modTime.endTimer() ;
            if ( timed )
            {
                totalTime += time ;
                System.err.println("Time: "+modTime.timeStr(time)+" sec") ;
            }
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
        catch (JenaException | CmdException ex) { throw ex ; }
        catch (Exception ex)
        {
            throw new CmdException("Exception", ex) ;
        }
    }    
}
