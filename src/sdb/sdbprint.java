/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
 * @author Andy Seaborne
 */

public class sdbprint extends CmdArgsDB
{
    LayoutType layoutDefault = LayoutType.LayoutTripleNodesHash ;

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    
    ModQueryIn modQuery = new ModQueryIn() ;
    ArgDecl argDeclPrintSQL     = new ArgDecl(ArgDecl.NoValue, "sql") ;
    ArgDecl argDeclPrint = new ArgDecl(ArgDecl.HasValue, "print") ;

    boolean printQuery = false ;
    boolean printOp = false ;
    boolean printSqlNode = false ;
    boolean printPlan = false ;
    boolean printSQL = false ;
    public static void main (String... argv)
    {
        new sdbprint(argv).main() ;
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
        if ( storeDesc.getLayout() != LayoutType.LayoutRDB )
        {
            // Only fake the connection if not ModelRDB
            // else we need a live conenction (currently)
            storeDesc.connDesc.setJdbcURL(JDBC.jdbcNone) ;
            //storeDesc.connDesc.setType("none") ;
        }
        if ( storeDesc.getLayout() == null )
            storeDesc.setLayout(layoutDefault) ;
        
        printSQL = contains(argDeclPrintSQL) ;
        @SuppressWarnings("unchecked")
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
        SDB.getContext().set(SDB.annotateGeneratedSQL, true) ;
        
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