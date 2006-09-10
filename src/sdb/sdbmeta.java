/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import sdb.cmd.ModStore;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.StoreConfig;
import com.hp.hpl.jena.sdb.util.HSQLUtils;

public class sdbmeta extends CmdArgsDB
{
    static ArgDecl argDeclFormat = new ArgDecl(true, "format","fmt") ;
    static ArgDecl argDeclName = new ArgDecl(true, "name") ;
    String name = StoreConfig.defaultName ;
    
    ModStore modStore = new ModStore();
    
    // subcommands via first positional argument
    
    String format = "N3" ;
    
    public static void main (String [] argv)
    {
        new sdbmeta(argv).mainAndExit() ;
    }
    
    protected sdbmeta(String[] argv)
    {
        super(argv) ;
        addModule(modStore) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        if ( contains(argDeclFormat))
            format = getValue(argDeclFormat) ;
        if ( contains(argDeclName) )
            name = getValue(argDeclName) ;
        if (getNumPositional() == 0)
            cmdError("Subcommand required (get,names,put,remove,reset)", true) ;
    }

    @Override
    protected String getSummary()
    { return Utils.className(this)+" --sdb <SPEC> [--name=NAME] [get|names|put|remove|reset]" ; }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        String subCmd = positionalArgs.remove(0) ;
        
        // Avoid needing fully built store.
        StoreConfig conf = new StoreConfig(modStore.getConnection()) ;
        
        try {
            if ( subCmd.equalsIgnoreCase("get") )
                execGet(conf, name) ;
            else if ( subCmd.equalsIgnoreCase("names") )
                execNames(conf) ;
            else if ( subCmd.equalsIgnoreCase("put") )
                execPut(conf, name, positionalArgs) ;
            else if ( subCmd.equalsIgnoreCase("remove") )
                execRemove(conf, name) ;
            else if ( subCmd.equalsIgnoreCase("reset") )
                execReset(conf, name) ;
            else 
                cmdError("Subcommand not recognized: "+subCmd, true) ;
        } finally 
        {
            // TODO SDBConnection.close which does the HSQLDB thing
            // and store.close calls SDBConnection.close
            // SDBConnections may be shared so not this. 
            if ( modStore.getStoreDesc().dbType == DatabaseType.HSQLDB )
                HSQLUtils.shutdown(modStore.getConnection()) ;
            try { modStore.getConnection().getSqlConnection().close() ; }
            catch (SQLException ex) {}
        }
    }

    private void execGet(StoreConfig conf, String name)
    {
        Model m = conf.getModel(name) ;
        if ( m == null )
        {
            System.out.println("No configuration model") ;
            throw new TerminationException(1) ;
        }
            
        m.write(System.out, format) ;
        
    }
    
    private void execPut(StoreConfig conf, String name, List<String> positionalArgs)
    {}
    
    private void execNames(StoreConfig conf)
    {
        List<String> names = conf.getNames() ;
        if ( names.size() == 0 )
            System.out.println("No names") ;
        else
            for ( String name : names )
                System.out.println("Name: "+name) ;
    }

    private void execRemove(StoreConfig conf, String name)
    {}

    private void execReset(StoreConfig conf, String name)
    {
        if ( ! confirm("Confirm reset") )
            throw new TerminationException(0) ;
        //conf.reset() ;
    }

    private boolean confirm(String prompt)
    {
        System.out.print(prompt+" [Y/n]: ") ;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in)) ;
            String line = r.readLine() ;
            if ( line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes") )
                return true ;  
            return false ;
        } catch (IOException ex)
        {
            ex.printStackTrace(System.err) ;
            return false ;
        }
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