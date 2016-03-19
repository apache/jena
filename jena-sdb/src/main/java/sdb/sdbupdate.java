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

import jena.cmd.ArgDecl;
import jena.cmd.CmdException ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sdb.SDB ;
import org.apache.jena.sdb.compiler.SDB_QC ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import sdb.cmd.CmdArgsDB;

 
public class sdbupdate extends CmdArgsDB
{
    static final ArgDecl updateArg = new ArgDecl(ArgDecl.HasValue, "update", "file") ;
    
    List<String> requestFiles = null ;
    
    public static void main(String... argv) {
        SDB.init();
        new sdbupdate(argv).mainRun();
    }
   
    protected sdbupdate(String... args)
    {
        super(args);
        super.add(updateArg, "--update=FILE", "Update commands to execute") ;
    }

    @Override
    protected String getCommandName() { return Lib.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> [ <update> |  --update=file ]"; }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs(); 
    }
    
    static final String divider = "- - - - - - - - - - - - - -" ;
    
    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        if ( isVerbose() )
            SDB_QC.PrintSQL = true ;
        
        getStore() ;
        Dataset dataset = getModStore().getDataset() ;

        List<String> requestFiles = getValues(updateArg) ;
        
        if ( requestFiles.size() == 0 && getPositional().size() == 0 )
            throw new CmdException("Nothing to do") ;

        for ( String filename : requestFiles )
            execOneFile(filename, dataset) ;

        for ( String requestString : super.getPositional() ) {
            requestString = indirect(requestString) ;
            execOne(requestString, dataset) ;
        }
    }

    private void execOneFile(String filename, Dataset store) {
        UpdateRequest req = UpdateFactory.read(filename, Syntax.syntaxARQ) ;
        UpdateExecutionFactory.create(req, store).execute() ;
    }

    private void execOne(String requestString, Dataset store) {
        UpdateRequest req = UpdateFactory.create(requestString, Syntax.syntaxARQ) ;
        UpdateExecutionFactory.create(req, store).execute() ;
    }
}
