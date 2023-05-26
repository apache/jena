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

import java.io.PrintStream;

import arq.cmdline.* ;
import org.apache.commons.io.output.NullPrintStream;
import org.apache.jena.Jena;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.query.* ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.RiotNotFoundException ;
import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.core.Transactional ;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.sparql.mgt.Explain ;
import org.apache.jena.sparql.resultset.ResultSetException ;
import org.apache.jena.sparql.resultset.ResultsFormat ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.apache.jena.system.Txn ;

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
        modVersion.addClass(null, Jena.class);

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
            else if ( x.length == 2 ) {
                try { warmupCount = Integer.parseInt(x[0]) ; }
                catch (NumberFormatException ex)
                { throw new CmdException("Can't parse "+x[0]+" in arg "+getValue(argRepeat)+" as an integer") ; }
                try { repeatCount = Integer.parseInt(x[1]) ; }
                catch (NumberFormatException ex)
                { throw new CmdException("Can't parse "+x[1]+" in arg "+getValue(argRepeat)+" as an integer") ; }
            } else
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
            // Include the results format so that is warmed up as well.
            queryExec(false, modResults.getResultsFormat(), NullPrintStream.NULL_PRINT_STREAM) ;

        for ( int i = 0 ; i < repeatCount ; i++ )
            queryExec(modTime.timingEnabled(),  modResults.getResultsFormat(), System.out) ;

        if ( modTime.timingEnabled() && repeatCount > 1 ) {
            long avg = totalTime/repeatCount ;
            String avgStr = modTime.timeStr(avg) ;
            System.err.println("Total time: "+modTime.timeStr(totalTime)+" sec for repeat count of "+repeatCount+ " : average: "+avgStr) ;
        }
    }

    @Override
    protected String getCommandName() { return Lib.className(this) ; }

    @Override
    protected String getSummary() { return getCommandName()+" --data=<file> --query=<query>" ; }

    /** Choose the dataset.
     * <li> use the data as described on the command line
     * <li> else use FROM/FROM NAMED if present (pass null to ARQ)
     * <li> else provided an empty dataset and hope the query has VALUES/BIND
     */
    protected Dataset getDataset(Query query)  {
        try {
            Dataset ds = modDataset.getDataset();
            if ( ds == null )
                ds = dealWithNoDataset(query);
            return ds;
        }
        catch (RiotNotFoundException ex) {
            System.err.println("Failed to load data: " + ex.getMessage());
            throw new TerminationException(1);
        }
        catch (RiotException ex) {
            System.err.println("Failed to load data");
            throw new TerminationException(1);
        }
    }

    protected Query getQuery() {
        try {
            return modQuery.getQuery() ;
        } catch (NotFoundException ex) {
            System.err.println("Failed to load query: "+ex.getMessage());
            throw new TerminationException(1);
        }
    }

    // Policy for no command line dataset. null means "whatever" (use FROM)
    protected Dataset dealWithNoDataset(Query query)  {
        if ( query.hasDatasetDescription() )
            return null;
        return DatasetFactory.createTxnMem();
        //throw new CmdException("No dataset provided") ;
    }

    protected long totalTime = 0 ;
    protected void queryExec(boolean timed, ResultsFormat fmt, PrintStream resultsDest)
    {
        try {
            Query query = getQuery() ;
            if ( isVerbose() ) {
                IndentedWriter out = new IndentedWriter(resultsDest, true);
                query.serialize(out);
                out.setLineNumbers(false);
                out.println();
                out.flush();
            }

            if ( isQuiet() )
                LogCtl.setError(SysRIOT.riotLoggerName) ;
            Dataset dataset = getDataset(query) ;
            // Check there is a dataset. See dealWithNoDataset(query).
            // The default policy is to create an empty one - convenience for VALUES and BIND providing the data.
            if ( dataset == null && !query.hasDatasetDescription() ) {
                System.err.println("Dataset not specified in query nor provided on command line.");
                throw new TerminationException(1);
            }
            Transactional transactional = (dataset != null && dataset.supportsTransactions()) ? dataset : new TransactionalNull() ;
            Txn.executeRead(transactional, ()->{
                modTime.startTimer() ;
                try ( QueryExecution qe = QueryExecutionFactory.create(query, dataset) ) {
                    try { QueryExecUtils.executeQuery(query, qe, fmt, resultsDest); }
                    catch (QueryCancelledException ex) {
                        IO.flush(resultsDest);
                        System.err.println("Query timed out");
                    }
                    long time = modTime.endTimer();
                    if ( timed ) {
                        totalTime += time;
                        System.err.println("Time: " + modTime.timeStr(time) + " sec");
                    }
                }
                catch (ResultSetException ex) {
                    System.err.println(ex.getMessage());
                    ex.printStackTrace(System.err);
                }
                catch (QueryException qEx) {
                    // System.err.println(qEx.getMessage()) ;
                    throw new CmdException("Query Exeception", qEx);
                }
            });
        }
        catch (ARQInternalErrorException intEx) {
            System.err.println(intEx.getMessage()) ;
            if ( intEx.getCause() != null )
            {
                System.err.println("Cause:") ;
                intEx.getCause().printStackTrace(System.err) ;
                System.err.println() ;
            }
            intEx.printStackTrace(System.err) ;
        }
        catch (JenaException | CmdException ex) { throw ex ; }
        catch (Exception ex) {
            throw new CmdException("Exception", ex) ;
        }
    }
}
