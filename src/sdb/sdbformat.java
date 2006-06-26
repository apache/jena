/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.sql.SQLException;

import sdb.cmd.CmdArgsDB;

import arq.cmd.TerminationException;

import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreFormatter;

/** Format an SDB database.  Destroys all existing data permanently. */ 

public class sdbformat extends CmdArgsDB
{
    public static final String usage = "sdbformat --sdb <SPEC> --dbName <NAME>" ;
           
    public static void main(String [] argv)
    {
        new sdbformat(argv).mainAndExit() ;
    }
    
    protected sdbformat(String[] args)
    {
        super(args);
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected void checkCommandLine()
    {
        if ( getNumPositional() > 0 )
            cmdError("No positional arguments allowed", true) ;
        if ( !contains(super.argDeclDbName) )
            cmdError("Must give the name of the database", true) ;
    }
    
    @Override
    protected void exec0()
    {
        Store store = getStore() ;
        StoreFormatter f = store.getTableFormatter() ; 
        f.format() ;
        
        //getStore().getTableFormatter().format() ;
        // For hsql -- shutdown when finished
        SDBConnection conn = getConnection();
        try
		{
			if (conn.getSqlConnection().getMetaData().getDatabaseProductName().contains("HSQL")) {
				conn.execAny("SHUTDOWN COMPACT;");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
    }
    

    @Override
    protected boolean exec1(String arg)
    {
        System.err.println("Unexpected positional argument") ;
        throw new TerminationException(99) ;
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