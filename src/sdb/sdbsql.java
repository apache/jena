/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.util.FileManager;

public class sdbsql extends CmdArgsDB
{
    private static ArgDecl argDeclQuery = new ArgDecl(true, "file", "query") ; 
    
    static public final String usage = "sdbsql --sdb <SPEC> SQLSTRING | --file=FILE" ;
    
    public static void main (String [] argv)
    {
        new sdbsql(argv).mainAndExit() ;
    }

    private sdbsql(String[] argv)
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
        long x = 0 ;
        try {
            ResultSet rs = getModStore().getConnection().exec(sqlStmt) ;
            x = getModTime().readTimer() ;
            
            if ( rs == null )
                System.out.println("Executed with no errors or results") ;
            else
            { 
                if ( isQuiet() )
                    RS.consume(rs) ;
                else
                    RS.printResultSet(rs) ;
            }
        } catch (SQLException ex)
        {
            System.err.println("SQL Exception: "+ex.getMessage()) ;
            throw new TerminationException(9) ;
        }
        long time = getModTime().endTimer() ;
        if ( getModTime().timingEnabled() )
            System.out.println("Query: "+getModTime().timeStr(time)+"("+getModTime().timeStr(x)+"/"+getModTime().timeStr(time-x)+")") ; 
    }

}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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