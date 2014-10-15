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

import java.util.ArrayList;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModBase;

import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sdb.store.TupleTable;

import com.hp.hpl.jena.sparql.util.Utils;


public class sdbtuple extends CmdArgsDB
{
    static class ModTuple extends ModBase
    {
        
        @Override
        public void registerWith(CmdGeneral cmdLine)
        {}

        @Override
        public void processArgs(CmdArgModule cmdLine)
        {}
        
    }
    
    private static ModTuple modTuple = new ModTuple() ;
    
    // Commands
    private static ArgDecl  argDeclCmdPrint    = new ArgDecl(false, "print") ;
    private static ArgDecl  argDeclCmdLoad     = new ArgDecl(true,  "load") ;
    private static ArgDecl  argDeclCmdCreate   = new ArgDecl(false, "create") ;
    private static ArgDecl  argDeclCmdDrop     = new ArgDecl(false, "drop") ;
    private static ArgDecl  argDeclCmdTruncate = new ArgDecl(false, "truncate") ;

    // Indexes?
    
    private static ArgDecl argDeclCmdTable= new ArgDecl(true, "table") ;
    
    private boolean         cmdPrint           = false ;
    private boolean         cmdLoad            = false ;
    private boolean         cmdCreate          = false ;
    private boolean         cmdDrop            = false ;
    private boolean         cmdTruncate        = false ;
    
    String loadFile = null ;
    
    public static void main(String... args)
    {
        SDB.init() ;
        new sdbtuple(args).mainRun() ;
    }
    
    public List<String> tables = new ArrayList<String>() ;
    public sdbtuple(String... argv)
    {
        super(argv) ;
        getUsage().startCategory("Tuple") ;
        add(argDeclCmdTable, "--table=TableName", "Tuple table to operate on (incldues positional arguments as well)") ;
        add(argDeclCmdPrint, "--print", "Print a tuple table") ;
        add(argDeclCmdLoad, "--load", "Load a tuple table") ;
        add(argDeclCmdCreate, "--create", "Create a tuple table") ;
        add(argDeclCmdDrop, "--drop", "Drop a tuple table") ;
        add(argDeclCmdTruncate, "--truncate", "Truncate a tuple table") ;
    }
    
    private int countBool(boolean...bools)
    {
        int count = 0 ;
        for ( boolean bool : bools )
        {
            if ( bool )
            {
                count++;
            }
        }
        return count ;
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        if ( !contains(argDeclCmdTable) && getNumPositional() == 0 )
            cmdError("No tables specified", true) ;
        
        List<String>x = getPositional() ;
        tables.addAll(x) ;
        
        List<String>y = getValues(argDeclCmdTable) ;
        tables.addAll(y) ;
        
        cmdPrint = contains(argDeclCmdPrint) ;

        cmdLoad = contains(argDeclCmdLoad) ;
        if ( cmdLoad )
            loadFile = getValue(argDeclCmdLoad) ;
        
        cmdCreate = contains(argDeclCmdCreate) ;
        cmdDrop = contains(argDeclCmdDrop) ;
        cmdTruncate = contains(argDeclCmdTruncate) ;
    }

    @Override
    protected String getSummary()
    { return getCommandName()+" --sdb <SPEC> [--print|--load|--create|--drop] [--table TableName] TableName..." ; }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    static final String divider = "- - - - - - - - - - - - - -" ;
    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    // ---- Execution
    
    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        int count = countBool(cmdPrint, cmdLoad, cmdCreate, cmdDrop, cmdTruncate) ;
    
        if ( count == 0 )
            //cmdError("No command : nothing to do!", true) ;
            cmdPrint = true ;
        if ( count > 1 )
            cmdError("Too many commands : too much to do!", true) ;
        
        if ( tables.size() != 1 )
            // May relax this ...
            cmdError("Can only operate on one table", true) ;
        
        for ( String tableName : tables )
            execOne(tableName) ;
    }

    private void execOne(String tableName)
    {
        if ( cmdPrint )     execPrint(tableName) ;
        if ( cmdLoad )      execLoad(tableName) ;
        if ( cmdCreate )    cmdError("Tuple create - not implemented (yet)", true) ;
        if ( cmdDrop )      cmdError("Tuple drop - not implemented (yet)", true) ;
        if ( cmdTruncate )  cmdError("Tuple truncate - not implemented (yet)", true) ;
    }

    private void execPrint(String tableName)
    {
        Store store = getStore() ;
        TupleTable table = null ;
        if ( tableName.equalsIgnoreCase(store.getNodeTableDesc().getTableName()))
        {
            // A hack.
            // Need to chage SQLBridge to work on Node tables directly (no special build of value getters)
            TableDesc desc = new TableDesc(tableName, store.getNodeTableDesc().getNodeRefColName()) ; 
            table = new TupleTable(store, desc) ;
        }
        else
            table = new TupleTable(store, tableName) ;
        divider() ;
        table.dump() ;
    }

    private void execLoad(String tableName)
    {
        cmdError("Tuple load - not implemented (yet)", true) ;
        Store store = getStore() ;

        TupleTable table = null ;
        if ( tableName.equalsIgnoreCase(store.getNodeTableDesc().getTableName()))
            cmdError("Can't load the node table as a tupole table" );
        else
            table = new TupleTable(store, tableName) ;
    }

}
