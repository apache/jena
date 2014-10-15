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

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sparql.util.Utils;

/** Format an SDB database.  Destroys all existing data permanently.
 *  Ignores -dbName argument in favour of the command line positional parameter. 
 */ 

public class sdbtruncate extends CmdArgsDB
{
    private static ArgDecl argDeclConfirm  = new ArgDecl(false,  "confirm", "force") ;
    
    public static void main (String... argv)
    {
        SDB.init();
        new sdbtruncate(argv).mainRun() ;
    }

    private String dbToZap = null ;
    
    protected sdbtruncate(String... args)
    {
        super(args);
        super.add(argDeclConfirm, "--confirm", "Confirm action") ;
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return Utils.className(this)+" --sdb <SPEC> --confirm" ; }
    
    @Override
    protected void processModulesAndArgs()
    {
        if ( getNumPositional() > 0  )
            throw new CmdException("No position arguments (specify DB in spec file or with --dbName DB") ;
        if ( ! super.contains(argDeclConfirm) )
            throw new CmdException("Argument --confirm required") ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        getStore().getTableFormatter().truncate() ;
    }
}
