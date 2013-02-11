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

import java.sql.SQLException;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileManager;

public class sdbsql extends CmdArgsDB
{
    private static ArgDecl argDeclQuery = new ArgDecl(true, "file", "query") ; 
    
    static public final String usage = "sdbsql --sdb <SPEC> SQLSTRING | --file=FILE" ;
    
    public static void main (String... argv)
    {
        SDB.init();
        new sdbsql(argv).mainRun() ;
    }

    public sdbsql(String... argv)
    {
        super(argv) ;
        add(argDeclQuery, "--file=", "SQL command to execute (or positional arguments)") ;
        // default is time off
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" --sdb <SPEC> SQLSTRING | --file=FILE"; }

    @Override
    protected void processModulesAndArgs()
    {
        if ( contains(argDeclQuery) && getNumPositional() > 0 )
            cmdError("Can't have both --query and a positional query string", true) ;
            
        if ( !contains(argDeclQuery) && getNumPositional() == 0 )
            cmdError("Nothing to execute", true) ;
        
        if ( getNumPositional() > 1 )
            cmdError("Too many statements to execute", true) ;
    }
    
    @Override
    protected void execCmd(List<String> positionalArg)
    {
        if ( contains(argDeclQuery) )
        {
            String x = super.getValue(argDeclQuery) ;
            String sqlStmt = FileManager.get().readWholeFileAsUTF8(x) ;
            positionalArg.add(sqlStmt) ;
        }
        
        for ( String x : positionalArg)
            execOneSQL(x) ;
        
        getModStore().getConnection().close() ;
    }
    
    private void execOneSQL(String sqlStmt)
    {
        if ( isVerbose() )
        {
            System.out.print(sqlStmt) ;
            if ( ! sqlStmt.endsWith("\n") )
                System.out.println() ;
        }
         
        if ( sqlStmt.startsWith("@") ) 
            sqlStmt = FileManager.get().readWholeFileAsUTF8(sqlStmt.substring(1)) ;
        
        getModTime().startTimer() ;
        long queryTime = 0 ;
        ResultSetJDBC rs = null ;
        try {
            rs = getModStore().getConnection().exec(sqlStmt) ;
            queryTime = getModTime().readTimer() ;
            
            if ( rs == null )
                System.out.println("Executed with no errors or results") ;
            else
            { 
                if ( isQuiet() )
                    RS.consume(rs.get()) ;
                else
                    RS.printResultSet(rs.get()) ;
            }
        } catch (SQLException ex)
        {
            System.err.println("SQL Exception: "+ex.getMessage()) ;
            throw new TerminationException(9) ;
        }
        finally { RS.close(rs) ; }
        
        long time = getModTime().endTimer() ;
        long fmtTime = time-queryTime ;
        if ( getModTime().timingEnabled() )
        {
            String totalTimeStr = getModTime().timeStr(time) ;
            String queryTimeStr = getModTime().timeStr(queryTime) ;
            String fmtTimeStr = getModTime().timeStr(fmtTime) ;
            System.out.printf("Query: %s (query %s, formatting %s)\n", totalTimeStr, queryTimeStr, fmtTimeStr) ;
        }
    }

}
