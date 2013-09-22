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
import arq.cmdline.ModQueryIn;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.util.PrintSDB;


/**
 * Compile and print the SQL for a SPARQL query.
 */

public class sdbprint extends CmdArgsDB
{
    LayoutType layoutDefault = LayoutType.LayoutTripleNodesHash ;

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    
    ModQueryIn modQuery = new ModQueryIn(Syntax.syntaxARQ) ;
    ArgDecl argDeclPrintSQL     = new ArgDecl(ArgDecl.NoValue, "sql") ;
    ArgDecl argDeclPrint = new ArgDecl(ArgDecl.HasValue, "print") ;

    boolean printQuery = false ;
    boolean printOp = false ;
    boolean printSqlNode = false ;
    boolean printPlan = false ;
    boolean printSQL = false ;
    public static void main (String... argv)
    {
        SDB.init();
        new sdbprint(argv).mainRun() ;
    }
    
    public sdbprint(String... args)
    {
        super(args);
        super.addModule(modQuery) ;
        super.getUsage().startCategory("SQL") ;
        super.add(argDeclPrintSQL, "--sql", "Print SQL") ;
        super.add(argDeclPrint, "--print=", "Print any of 'query', 'op', 'sqlnode', 'SQL', 'plan'") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        // Force the connection to be a null one.
        // Known to be called after arg module initialization.
        StoreDesc storeDesc = getModStore().getStoreDesc() ;
        storeDesc.connDesc.setJdbcURL(JDBC.jdbcNone) ;

        if ( storeDesc.getLayout() == null )
            storeDesc.setLayout(layoutDefault) ;
        
        printSQL = contains(argDeclPrintSQL) ;
        List<String> strList = getValues(argDeclPrint) ;
        for ( String arg : strList )
        {
            if ( arg.equalsIgnoreCase("query"))         { printQuery = true ; }
            else if ( arg.equalsIgnoreCase("Op"))       { printOp = true ; }
            else if ( arg.equalsIgnoreCase("SqlNode"))  { printSqlNode = true ; }
            else if ( arg.equalsIgnoreCase("sql"))      { printSQL = true ; }
            else if ( arg.equalsIgnoreCase("plan"))     { printPlan = true ; }
            else
                throw new CmdException("Not a recognized print form: "+arg+" : Choices are: query, prefix, op, sqlNode, sql") ;
        }
    }

    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        Query query = modQuery.getQuery() ;
        compilePrint(getStore(), query) ;
    }

    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    private void compilePrint(Store store, Query query)
    {
        SDB.getContext().setIfUndef(SDB.annotateGeneratedSQL, true) ;
        
        if ( !printQuery && ! printOp && ! printSqlNode && ! printSQL && ! printPlan )
            printSQL = true ;
        
        if ( isVerbose() )
        {
            //printQuery = true ;
            printOp = true ;
            //printSqlNode = true ;
            printSQL = true ;
        }
        
        if ( printQuery )
        {
            divider() ;
            query.serialize(System.out, Syntax.syntaxARQ) ;
        }
        
        QueryEngineSDB qe = new QueryEngineSDB(store, query) ;
        Op op = qe.getOp() ;

        if ( printOp )
        {
            divider() ;
            PrintSDB.print(op) ;
            // No newline.
        }

        if ( printSqlNode )
        {
            divider() ;
            PrintSDB.printSqlNodes(op) ;
        }
        
        if ( printSQL )
        {
            divider() ;
            PrintSDB.printSQL(op) ;
        }
        
        if ( printPlan )
        {
            divider() ;
            System.out.print(qe.getPlan()) ;
        }
    }

    
    
    @Override
    protected String getSummary()
    {
        return "Usage: [--layout schemaName] [--query URL | string ] " ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
}
