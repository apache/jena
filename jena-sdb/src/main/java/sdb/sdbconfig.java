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

import arq.cmd.CmdException;

import sdb.cmd.CmdArgsDB;
import sdb.cmd.ModConfig;

import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.Store;

/** Configure an SDB database.  Destroys all existing data permanently. */ 

public class sdbconfig extends CmdArgsDB
{
    ModConfig modConfig = new ModConfig() ; 
    
    public static void main(String ... argv)
    {
        SDB.init();
        new sdbconfig(argv).mainRun() ;
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
        if ( getNumPositional() > 0  )
            throw new CmdException("No position arguments (specify DB in spec file or with --dbName DB") ;
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
        
        if ( isVerbose() || getModTime().timingEnabled() )
            modConfig.enact(store, getModTime()) ;
        else
            modConfig.enact(store) ;
    }
}
