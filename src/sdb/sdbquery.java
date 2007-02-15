/*
 * (c) Copyright 2006 Hewlett--Packard Development Company, LP
 * [See end of file]
 */

package sdb;


import java.util.Iterator;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;
import arq.cmdline.ModQueryIn;
import arq.cmdline.ModResultsOut;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.engine.QueryEngineFactory;
import com.hp.hpl.jena.query.engine.QueryEngineRegistry;
import com.hp.hpl.jena.query.util.QueryExecUtils;
import com.hp.hpl.jena.query.util.Utils;

import com.hp.hpl.jena.sdb.engine.QueryEngineFactorySDB;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.engine.compiler.QC;
import com.hp.hpl.jena.sdb.util.PrintSDB;

 
 /** Query an SDB model.
  * 
  *  <p>
  *  Usage:<pre>
  *    sdb.sdbquery [db spec] [ query | --query=file ]
  *  </pre>
  *  </p>
  * 
  * @author Andy Seaborne
  * @version $Id: sdbquery.java,v 1.30 2006/04/22 19:51:11 andy_seaborne Exp $
  */ 
 
public class sdbquery extends CmdArgsDB
{
    public static final String usage = "sdbquery --sdb <SPEC> [--direct] [ <query> | --query=file ]" ;
    
    // ModQueryIn, ModResultsOut or maybe extend arq.query itself.
    private static ArgDecl argDeclDirect  = new ArgDecl(false,  "direct") ;
    private static ArgDecl argDeclRepeat   = new ArgDecl(true,  "repeat") ;
    
    boolean printResults = true ;
    int repeatCount = 1 ;
    
    ModQueryIn    modQuery =    new ModQueryIn() ;
    ModResultsOut modResults =  new ModResultsOut() ;

    public static void main (String [] argv)
    {
        new sdbquery(argv).mainAndExit() ;
    }
   
    protected sdbquery(String[] args)
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
        {
            for ( Iterator iter = QueryEngineRegistry.get().factories().iterator() ; iter.hasNext() ; )
            {
                QueryEngineFactory f = (QueryEngineFactory)iter.next();
                if ( f instanceof QueryEngineFactorySDB )
                    iter.remove() ;
            }
        }
        
        if ( isVerbose() )
        {
            QC.PrintSQL = true ;
            modQuery.getQuery().serialize(System.out) ;
            System.out.println(divider) ; 
        }
        
        // Force setup
        getModStore().getStore() ;
        if ( getModTime().timingEnabled() )
        {
            // Setup costs : flush classes into memory and establish connection
            getModTime().startTimer() ;
            getModStore().getStore() ;
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
            for ( int i = 0 ; i < repeatCount ; i++ )
            {
                getModTime().startTimer() ;
                Query query = modQuery.getQuery() ;
                QueryExecution qExec = QueryExecutionFactory.create(query, getModStore().getDataset()) ;
                
                if ( isVerbose() )
                    PrintSDB.print(((QueryEngineSDB)qExec).getOp()) ;
                
                if ( false )
                    System.err.println("Execute query for loop "+(i+1)+" "+memStr()) ;
                QueryExecUtils.executeQuery(query, qExec, modResults.getResultsFormat()) ;
                qExec.close() ;
                long queryTime = getModTime().endTimer() ;
                totalTime += queryTime ;
            }
        } catch (QueryException ex)
        {
            System.out.println("Query exception: "+ex.getMessage()) ;
        }
        finally {
            if ( getModTime().timingEnabled() )
            {
                System.out.println("Execute time:    "+String.format("%.4f", new Double(totalTime/(1000.0*repeatCount)) )) ;
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
 


/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
