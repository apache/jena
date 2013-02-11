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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.StoreConfig;

public class sdbmeta extends CmdArgsDB
{
    static ArgDecl argDeclSyntax = new ArgDecl(true, "out") ;
    static ArgDecl argDeclTag = new ArgDecl(true, "tag", "name") ;
    String tag = StoreConfig.defaultTag ;
    
    // subcommands via first positional argument
    
    String format = "TTL" ;
    
    public static void main (String... argv)
    {
        SDB.init();
        new sdbmeta(argv).mainRun() ;
    }
    
    protected sdbmeta(String... argv)
    {
        super(argv) ;
        super.add(argDeclSyntax) ;
        super.add(argDeclTag) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        if ( contains(argDeclSyntax))
            format = getValue(argDeclSyntax) ;
        if ( contains(argDeclTag) )
            tag = getValue(argDeclTag) ;
        if (getNumPositional() == 0)
            cmdError("Subcommand required (get,tags,put,remove,reset)", true) ;
        if ( isVerbose() )
        {
            SDBConnection.logSQLStatements = true ;
            SDBConnection.logSQLExceptions = true ;
        }
    }

    @Override
    protected String getSummary()
    { return Utils.className(this)+" --sdb <SPEC> [--tag=TAG] [get|tags|put|remove|reset]" ; }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        String subCmd = positionalArgs.remove(0) ;
        
        // Avoid needing fully built store.
        StoreConfig conf = new StoreConfig(getModStore().getConnection()) ;
        
        if ( subCmd.equalsIgnoreCase("get") )
            execGet(conf, tag) ;
        else if ( subCmd.equalsIgnoreCase("tags") )
            execTags(conf) ;
        else if ( subCmd.equalsIgnoreCase("put") )
            execPut(conf, tag, positionalArgs) ;
        else if ( subCmd.equalsIgnoreCase("remove") )
            execRemove(conf, tag) ;
        else if ( subCmd.equalsIgnoreCase("reset") )
            execReset(conf, tag) ;
        else 
            cmdError("Subcommand not recognized: "+subCmd, true) ;
    }

    private void execGet(StoreConfig conf, String tag)
    {
        Model m = conf.getModel(tag) ;
        if ( m == null )
        {
            System.out.println("No configuration model") ;
            throw new TerminationException(1) ;
        }
            
        m.write(System.out, format) ;
        
    }
    
    private void execPut(StoreConfig conf, String tag, List<String> positionalArgs)
    {
        if ( positionalArgs.size() == 0 )
            throw new CmdException("No file to load") ;
        
        Model model = conf.getModel(tag) ;
        if ( model == null )
            model = ModelFactory.createDefaultModel() ;
        for ( String filename : positionalArgs)
            FileManager.get().readModel(model, filename) ;
        conf.setModel(tag, model) ;
    }
    
    private void execTags(StoreConfig conf)
    {
        List<String> tags = conf.getTags() ;
        if ( tags.size() == 0 )
            System.out.println("No tags") ;
        else
            for ( String tag : tags )
                System.out.println("Tag: "+tag) ;
    }

    private void execRemove(StoreConfig conf, String name)
    {
        if ( ! confirm("Confirm the removal of '"+name+"'") )
            throw new TerminationException(0) ;
        conf.removeModel(name) ;
    }

    private void execReset(StoreConfig conf, String name)
    {
        if ( ! confirm("Confirm reset") )
            throw new TerminationException(0) ;
        conf.reset() ;
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
