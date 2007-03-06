/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.util.List;

import sdb.cmd.CmdArgsDB;
import sdb.cmd.ModConfig;

import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sdb.store.Store;

/** Configure an SDB database.  Destroys all existing data permanently. */ 

public class sdbconfig extends CmdArgsDB
{
    ModConfig modConfig = new ModConfig() ; 
    
    public static void main(String ... argv)
    {
        new sdbconfig(argv).main() ;
    }
    
    protected sdbconfig(String... args)
    {
        super(args);
        addModule(modConfig) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return Utils.className(this)+" --sdb <SPEC> <NAME>" ; }

    @Override
    protected void processModulesAndArgs()
    {
        if ( getNumPositional() == 1 )
        {
            String dbToZap = getPositionalArg(0) ;
            getModStore().setDbName(dbToZap) ;
        }
//        else
//            cmdError("Must give the database name explicitly") ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        Store store = getModStore().getStore() ;
        if ( ! modConfig.format() && 
             ! modConfig.createStore() &&
             ! modConfig.dropIndexes() && 
             ! modConfig.addIndexes() )
        {
            System.err.println("Nothing to do : --format | --create | --drop | -indexes") ;
            return ;
        }
        
        if ( modConfig.format() && modConfig.createStore() )
        {
            System.err.println("Both --format and --create (--create formats and adds indexing)") ;
            return ;
        }
        
        if ( ! isQuiet() )
            modConfig.enact(store, getModTime()) ;
        else
            modConfig.enact(store) ;
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