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

package sdb;


import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;
import arq.cmdline.ModQueryIn;
import arq.cmdline.ModResultsOut;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.compiler.SDB_QC;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.util.PrintSDB;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.sparql.util.Utils;

 
 /** Query an SDB model.
  * 
  *  <p>
  *  Usage:<pre>
  *    sdb.sdbquery [db spec] [ query | --query=file ]
  *  </pre>
  *  </p>
  */ 
 
public class sdbquery extends CmdArgsDB
{
    // TODO See if inheriting from arq.query is a good idea.
    public static final String usage = "sdbquery --sdb <SPEC> [--direct] [ <query> | --query=file ]" ;
    
    // ModQueryIn, ModResultsOut or maybe extend arq.query itself.
    private static ArgDecl argDeclDirect  = new ArgDecl(false,  "direct") ;
    private static ArgDecl argDeclRepeat   = new ArgDecl(true,  "repeat") ;
    
    boolean printResults = true ;
    int repeatCount = 1 ;
    
    ModQueryIn    modQuery =    new ModQueryIn(Syntax.syntaxARQ) ;
    ModResultsOut modResults =  new ModResultsOut() ;

    public static void main (String... argv)
    {
        SDB.init();
        new sdbquery(argv).mainRun() ;
    }
   
    protected sdbquery(String... args)
    {
        super(args);
        addModule(modQuery) ;
        addModule(modResults) ;
        getUsage().startCategory("Misc") ;
        add(argDeclRepeat) ;
        getUsage().addUsage("--repeat=N", "Do the query N times (for timing)") ; 
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> [--direct] [ <query> | --query=file ]"; }

    @Override
    protected void processModulesAndArgs()
    {
        if ( contains(argDeclRepeat) )
            repeatCount = Integer.parseInt(getArg(argDeclRepeat).getValue()) ;
    }
    
    static final String divider = "- - - - - - - - - - - - - -" ;
    
    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        if ( contains(argDeclDirect) )
            QueryEngineSDB.unregister() ;
        
        if ( isVerbose() )
        {
            SDB_QC.PrintSQL = true ;
            modQuery.getQuery().serialize(System.out) ;
            System.out.println(divider) ; 
        }
        
        // Force setup
        {
            getStore() ;
            getModStore().getDataset() ;
            Query query = modQuery.getQuery() ;
            QueryExecution qExec = QueryExecutionFactory.create(query, getModStore().getDataset()) ;
            // Don't execute
            qExec.abort();
        }
        
        if ( getModTime().timingEnabled() )
        {
            // Setup costs : flush classes into memory and establish connection
            getModTime().startTimer() ;
            long connectTime =  getModTime().endTimer() ;
            //System.out.println("Connect time:    "+timeStr(connectTime)) ;
            
            getModTime().startTimer() ;
            Query query = modQuery.getQuery() ;
            long javaTime = getModTime().endTimer() ;
            
            if ( isVerbose() )
                System.out.println("Class load time: "+getModTime().timeStr(javaTime)) ;
        }
        
        
        long totalTime = 0 ;
        try {
            getModTime().startTimer() ;
            for ( int i = 0 ; i < repeatCount ; i++ )
            {
//                if ( i == 2 )
//                {
//                    // Reset timer to forget classloading overhead
//                    getModTime().endTimer() ;
//                    getModTime().startTimer() ;
//                }
                    
                Query query = modQuery.getQuery() ;
                try ( QueryExecution qExec = QueryExecutionFactory.create(query, getModStore().getDataset()) ) {
                    if ( isVerbose() )
                        PrintSDB.print(((QueryExecutionBase)qExec).getPlan().getOp()) ;
                    
                    if ( false )
                        System.err.println("Execute query for loop "+(i+1)+" "+memStr()) ;
                    QueryExecUtils.executeQuery(query, qExec, modResults.getResultsFormat()) ;
                }
            }
            long queryTime = getModTime().endTimer() ;
            totalTime = queryTime ;
        } catch (QueryException ex)
        {
            System.out.println("Query exception: "+ex.getMessage()) ;
        }
        finally {
            if ( getModTime().timingEnabled() )
            {
                System.out.println("Execute time:    "+String.format("%.4f", totalTime / ( 1000.0 * repeatCount ) )) ;
            }
        }
    }
    
    static String memStr()
    {
        long mem = Runtime.getRuntime().totalMemory() ;
        long free = Runtime.getRuntime().freeMemory() ;
        return "[T:"+mem+"/F:"+free+"]" ;
    }
     
}
