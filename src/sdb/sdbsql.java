/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.sql.ResultSet;
import java.sql.SQLException;

import sdb.cmd.CmdArgsDB;

import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.util.FileManager;

public class sdbsql extends CmdArgsDB
{
    private static ArgDecl argDeclQuery = new ArgDecl(true, "file", "query") ; 
    
    static public final String usage = "sdbsql --sdb <SPEC> SQLSTRING | --file=FILE" ;
    
    boolean quietMode = false ;
    
    public static void main (String [] argv)
    {
        new sdbsql(argv).mainAndExit() ;
    }

    private sdbsql(String[] argv)
    {
        super(argv) ;
        add(argDeclQuery) ;
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    @Override
    protected void checkCommandLine()
    {
        if ( contains(argDeclQuery) && getNumPositional() > 0 )
            cmdError("Can't have both --query and a positional query string", true) ;
            
        if ( !contains(argDeclQuery) && getNumPositional() == 0 )
            cmdError("Nothing to execute", true) ;
        
        if ( getNumPositional() > 1 )
            cmdError("Too many statements to execute", true) ;
        
        if ( quiet )
            quietMode = true ;
    }
    
    @Override
    protected void exec0()
    {
        String x = super.getValue(argDeclQuery) ;
        String sqlStmt = FileManager.get().readWholeFileAsUTF8(x) ;
        exec1(sqlStmt) ;
    }

    @Override
    protected boolean exec1(String sqlStmt)
    {
        if ( verbose )
        {
            System.out.print(sqlStmt) ;
            if ( ! sqlStmt.endsWith("\n") )
                System.out.println() ;
        }
         
        if ( sqlStmt.startsWith("@") ) 
            sqlStmt = FileManager.get().readWholeFileAsUTF8(sqlStmt.substring(1)) ;
        
        startTimer() ;
        long x = 0 ;
        try {
            ResultSet rs = getConnection().execQuery(sqlStmt) ;
            x = readTimer() ;
            if ( quietMode )
                RS.consume(rs) ;
            else
                RS.printResultSet(rs) ;
        } catch (SQLException ex)
        {
            System.err.println("SQL Exception: "+ex.getMessage()) ;
            return false ;
        }
        long time = endTimer() ;
        if ( timeCommand )
            System.out.println("Query: "+timeStr(time)+"("+timeStr(x)+"/"+timeStr(time-x)+")") ; 
        
        return false ;
    }

}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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