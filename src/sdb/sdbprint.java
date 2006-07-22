/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import sdb.cmd.CmdArgsDB;
import arq.cmdline.ModQueryIn;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.core.BindingRoot;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.PlanFormatter;
import com.hp.hpl.jena.query.engine1.PlanFormatterVisitor;
import com.hp.hpl.jena.query.engine1.PlanVisitorBase;
import com.hp.hpl.jena.query.engine1.plan.PlanElementExternal;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerBasicPattern;
import com.hp.hpl.jena.sdb.engine.PlanSDB;
import com.hp.hpl.jena.sdb.engine.PlanTranslatorGeneral;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.store.QueryCompiler;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreBase;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Compile and print the SQL for a SPARQL query.
 * @author Andy Seaborne
 * @version $Id: sdbprint.java,v 1.12 2006/04/24 17:31:26 andy_seaborne Exp $
 */

public class sdbprint extends CmdArgsDB
{
    String layoutNameDefault = "layout2" ;

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    
    ModQueryIn modQuery = new ModQueryIn() ;

    public static void main (String [] argv)
    {
        new sdbprint(argv).mainAndExit() ;
    }
    
    protected sdbprint(String[] args)
    {
        super(args);
        super.addModule(modQuery) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        // Force the connection to be a null one.
        // Known to be called after arg module initialization.
        StoreDesc storeDesc = getModStore().getStoreDesc() ;
        storeDesc.connDesc.jdbcURL = JDBC.jdbcNone ;
        storeDesc.connDesc.type = "none" ;
        if ( storeDesc.layoutName == null )
            storeDesc.layoutName = layoutNameDefault ;
        
    }

    public static void compilePrint(String queryString, String layoutName)
    {
        System.err.println("BROKEN - FIX ME") ;
    }
    
    @Override
    protected void exec()
    {
        Query query = modQuery.getQuery() ;
        Store store = getModStore().getStore() ; 

        compilePrint(query, store.getQueryCompiler()) ;
  }

    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    private void compilePrint(Query query, QueryCompiler compiler)
    {
        
        if ( ! quiet )
        {
            divider() ;
            query.serialize(System.out, Syntax.syntaxARQ) ;
        }
        
        if ( verbose )
        {
            divider() ;
            query.serialize(System.out, Syntax.syntaxPrefix) ;
        }

        Store store = new StoreBase(null, new PlanTranslatorGeneral(true, true), null, null, compiler) ;
        QueryEngineSDB qe = new QueryEngineSDB(store, query) ;

        if ( verbose )
        {
            
            PlanElement plan = qe.getPlan() ;
            divider() ;
            PlanFormatter.out(System.out, plan) ;
        }

        // Print all SDB things in the plan
        divider() ;
        //PlanWalker.walk(qe.getPlan(), new PrintSDBBlocks(store)) ;
        
        // Print the new plan with SQL.
        IndentedWriter w = new IndentedWriter(System.out) ;
        PlanFormatterVisitor fmt = new PrintPlanSQL(w, store) ;
        fmt.startVisit() ;
        qe.getPlan().visit(fmt) ;
        fmt.finishVisit() ;
    }

    class PrintPlanSQL extends PlanFormatterVisitor
    {
        private Store store ;
        public PrintPlanSQL(IndentedWriter w, Store store) { super(w, (PrefixMapping)null) ; this.store = store ; }

        @Override
        public void visit(PlanElementExternal planElt)
        {
            if ( ! ( planElt instanceof PlanSDB ) )
            {
                super.visit(planElt) ; 
                return ;
            }
            PlanSDB planSDB = (PlanSDB)planElt ;
            Block block = planSDB.getBlock() ;
            block = block.substitute(new BindingRoot());
            String sqlStmt = store.getQueryCompiler().asSQL(block) ;
            out.println("[SQL --------") ;
            out.incIndent() ;
            out.print(sqlStmt) ;
            out.decIndent() ;
            out.println();
            out.print("-------- ]") ;
        }
    }
    
    // Find SQL-ish blocks.
    class PrintSDBBlocks extends PlanVisitorBase
    {
        private Store store ;
        String separator = null ;

        PrintSDBBlocks(Store store) { this.store = store ; }
        
        @Override
        public void visit(PlanElementExternal planElt)
        {
            if ( planElt instanceof PlanSDB )
            {
                if ( separator != null )
                    System.out.println(separator) ;
                separator = divider ;
                PlanSDB planSDB = (PlanSDB)planElt ;
                if ( verbose )
                {
                    QueryCompilerBasicPattern.printBlock = false ;  // Done earlier.
                    QueryCompilerBasicPattern.printAbstractSQL = true ;
                    QueryCompilerBasicPattern.printDivider = divider ;
                }
                // Mimic what the QueryIterSDB/QueryCompilerBasicPattern does.
                Block block = planSDB.getBlock() ;
                block = block.substitute(new BindingRoot());
                
                String sqlStmt = store.getQueryCompiler().asSQL(block) ;
                System.out.println(sqlStmt) ;
            }
        }
    }
    
    @Override
    protected String getSummary()
    {
        return "Usage: [--layout schemaName] [--query URL | string ] " ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    @Override
    protected void exec0()
    {}

    @Override
    protected boolean exec1(String arg)
    {
        return false ;
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