/*
 * (c) Copyright 2006 Hewlett--Packard Development Company, LP
 * [See end of file]
 */

package sdb;


import java.util.Iterator;

import sdb.cmd.CmdArgsDB;

import arq.cmd.QExec;
import arq.cmd.ResultsFormat;
import arq.cmdline.ArgDecl;
import arq.cmdline.ModTime;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.engine.QueryEngineFactory;
import com.hp.hpl.jena.query.engine.QueryEngineRegistry;
import com.hp.hpl.jena.query.util.Utils;

import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerBasicPattern;
import com.hp.hpl.jena.sdb.engine.QueryEngineFactorySDB;
import com.hp.hpl.jena.util.FileManager;
 
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
    public static final String usage = "sdbquery <SPEC> [--direct] [ <query> | --query=file ]" ;
    
    private static ArgDecl argDeclQuery   = new ArgDecl(true,   "query") ;
    private static ArgDecl argDeclDirect  = new ArgDecl(false,  "direct") ;
    private static ArgDecl argDeclRepeat   = new ArgDecl(true,  "repeat") ;
    
    ModTime modTime = new ModTime() ;
    
    boolean printResults = true ;
    int repeatCount = 1 ;

    public static void main (String [] argv)
    {
        new sdbquery(argv).mainAndExit() ;
    }
   
    protected sdbquery(String[] args)
    {
        super(args);
        add(argDeclQuery) ;
        add(argDeclDirect) ;
        add(argDeclRepeat) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> [--direct] [ <query> | --query=file ]"; }

    @Override
    protected void checkCommandLine()
    {
        
        if ( contains(argDeclQuery) && getNumPositional() > 0 )
            cmdError("Can't have both --query and a positional query string", true) ;
            
        if ( !contains(argDeclQuery) && getNumPositional() == 0 )
            cmdError("No query to execute", true) ;
        
        if ( contains(argDeclRepeat) )
            repeatCount = Integer.parseInt(getArg(argDeclRepeat).getValue()) ;
        
    }
    
    @Override
    protected void exec0()
    {
        String queryFile = getValue(argDeclQuery) ;
        String queryStr = FileManager.get().readWholeFileAsUTF8(queryFile) ;
        exec1(queryStr) ;
    }

    static final String divider = "- - - - - - - - - - - - - -" ;
    
    @Override
    protected boolean exec1(String queryStr)
    {
        if ( queryStr == null )
        {
            System.err.println("No query string") ;
            return false ;
        }
        if ( queryStr.startsWith("@") )
            queryStr = FileManager.get().readWholeFileAsUTF8(queryStr.substring(1)) ;
        
        if ( contains(argDeclDirect) )
        {
            for ( Iterator iter = QueryEngineRegistry.get().factories().iterator() ; iter.hasNext() ; )
            {
                QueryEngineFactory f = (QueryEngineFactory)iter.next();
                if ( f instanceof QueryEngineFactorySDB )
                    iter.remove() ;
            }
        }
        
        if ( verbose )
        {
            QueryCompilerBasicPattern.printBlock = true ;
            QueryCompilerBasicPattern.printAbstractSQL = true ;
            QueryCompilerBasicPattern.printSQL = true ;
            QueryCompilerBasicPattern.printDivider = divider ;
        }
        
        execQuery(queryStr) ;
        return false ;
    }
    
    protected void execQuery(String queryStr)
    {
        // Force setup
        getModStore().getStore() ;
        if ( modTime.timingEnabled() )
        {
            // Setup costs : fluish classes into memory and establish connection
            modTime.startTimer() ;
            getModStore().getStore() ;
            long connectTime =  modTime.endTimer() ;
            //System.out.println("Connect time:    "+timeStr(connectTime)) ;
            
            modTime.startTimer() ;
            Query query = QueryFactory.create(queryStr) ;
//            QueryExecution qExec = QueryExecutionFactory.create(query, getSDBModel()) ;
//            qExec.close() ;
            long javaTime = modTime.endTimer() ;
            
            if ( verbose )
                System.out.println("Class load time: "+modTime.timeStr(javaTime)) ;
        }
        
        
        if ( verbose )
        {
            QueryCompilerBasicPattern.printSQL = true ;
            Query query = QueryFactory.create(queryStr) ;
            query.serialize(System.out) ;
            System.out.println(divider) ; 
        }
        
        ResultsFormat fmt = ResultsFormat.FMT_TEXT ;
        if ( !printResults )
            fmt = ResultsFormat.FMT_NONE ;

        long totalTime = 0 ;
        try {
            for ( int i = 0 ; i < repeatCount ; i++ )
            {
                modTime.startTimer() ;
                Query query = QueryFactory.create(queryStr) ;
                QueryExecution qExec = QueryExecutionFactory.create(query, getModStore().getDataset()) ;
                if ( false )
                    System.err.println("Execute query for loop "+(i+1)+" "+memStr()) ;
                QExec.executeQuery(query, qExec, fmt) ;
                qExec.close() ;
                long queryTime = modTime.endTimer() ;
                totalTime += queryTime ;
            }
        } catch (QueryException ex)
        {
            System.out.println("Query exception: "+ex.getMessage()) ;
        }
        finally {
            if ( modTime.timingEnabled() )
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
