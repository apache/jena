/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.BindingRoot;
import com.hp.hpl.jena.query.engine1.*;
import com.hp.hpl.jena.query.engine1.plan.PlanElementExternal;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.engine.PlanSDB;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.shared.PrefixMapping;


/** Print utilities */

public class PrintSDB
{
    public static void printPlan(Store store, Query query, QueryEngine queryEngine)
    {
        if ( queryEngine == null )
            queryEngine = new QueryEngineSDB(store, query) ;
        PlanElement plan = queryEngine.getPlan() ;
        PlanFormatter.out(System.out, plan) ;
    }
    
    /** Print plan with SQL parts */
    public static void printBlocks(Store store, Query query, QueryEngineSDB queryEngine)
    {
        if ( queryEngine == null )
            queryEngine = new QueryEngineSDB(store, query) ;
        IndentedWriter w = new IndentedWriter(System.out) ;
        PlanVisitor fmt = new PrintSDBBlocks(w, store, query) ;
        PlanWalker.walk(queryEngine.getPlan(), fmt) ;
        w.flush();
    }

    /** Print just the SQL parts */
    public static void printSQL(Store store, Query query, QueryEngineSDB queryEngine)
    {
        if ( queryEngine == null )
            queryEngine = new QueryEngineSDB(store, query) ;
        IndentedWriter w = new IndentedWriter(System.out) ;
        PlanFormatterVisitor fmt = new PrintPlanSQL(w, store, query) ;
        fmt.startVisit() ;
        queryEngine.getPlan().visit(fmt) ;
        fmt.finishVisit() ;
    }

    
    // Print SQL bits
    static class PrintPlanSQL extends PlanFormatterVisitor
    {
        private Store store ;
        private Query query ;
        
        public PrintPlanSQL(IndentedWriter w, Store store, Query query)
        { super(w, (PrefixMapping)null) ; this.store = store ; this.query = query ; }

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
            SqlNode sqlNode = store.getQueryCompiler().compileQuery(store, query, block) ;

            String sqlStmt = store.getSQLGenerator().generateSQL(sqlNode) ; 
            out.println("[SQL --------") ;
            out.incIndent() ;
            out.print(sqlStmt) ;
            out.decIndent() ;
            out.println();
            out.print("-------- ]") ;
        }
    }
    
    // Find SQL-ish blocks.
    static class PrintSDBBlocks extends PlanVisitorBase
    {
        private Store store ;
        private Query query ;
        private IndentedWriter out ;
        private String separator = null ;

        PrintSDBBlocks(IndentedWriter w, Store store, Query query)
        { this.out = w ; this.store = store ; this.query = query ; }
        
        @Override
        public void visit(PlanElementExternal planElt)
        {
            if ( planElt instanceof PlanSDB )
            {
                // Mimic what the QueryIterSDB/QueryCompilerBasicPattern does.
                PlanSDB planSDB = (PlanSDB)planElt ;
                Block block = planSDB.getBlock() ;
                block = block.substitute(new BindingRoot());
                SqlNode sqlNode = store.getQueryCompiler().compileQuery(store, query, block) ;
                String sqlStmt = store.getSQLGenerator().generateSQL(sqlNode) ; 
                out.println(sqlStmt) ;
            }
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