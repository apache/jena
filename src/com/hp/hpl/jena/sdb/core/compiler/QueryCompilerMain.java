/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.QueryCompiler;
import com.hp.hpl.jena.sdb.store.ResultsBuilder;
import com.hp.hpl.jena.sdb.store.Store;

/**
 * Compile a query (in the form of Blocks).
 *  
 * @author Andy Seaborne
 * @version $Id: QueryCompilerBase.java,v 1.1 2006/04/22 13:45:58 andy_seaborne Exp $
 */

public abstract class QueryCompilerMain implements QueryCompiler
{
    private static Log log = LogFactory.getLog(QueryCompilerMain.class) ;
    
    public final QueryIterator exec(Store store,
                                       Block block,
                                       Binding binding,
                                       ExecutionContext execCxt)
    {
        String sqlStmt = asSQL(store, execCxt.getQuery(), block) ;
        try {
            // Odd : exitVariables and a project?
            java.sql.ResultSet jdbcResultSet = store.getConnection().execQuery(sqlStmt) ;
            Set<Var> x = QC.exitVariables(block) ;
            try {
                return getResultsBuilder().assembleResults(jdbcResultSet, binding, x, execCxt) ;
            } finally { jdbcResultSet.close() ; }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing a basic block", ex) ;
        }
    }

    protected abstract ResultsBuilder getResultsBuilder() ;
    protected abstract BlockCompiler  getBlockCompiler() ;
    
    public SqlNode compileQuery(Store store, Query query, Block block)
    {
        verbose ( QC.printBlock, block ) ; 
        CompileContext context = new CompileContext(store, query) ;

        // A chance for subclasses to change the block structure (including insert their own block types)
        // Remove?  Now we have customizers?
        
        //store.getCustomizer().modify(??) ;
        block = modify(block) ;

        if ( block == null )
            throw new SDBException("asSQL: Block to compile is null") ;
        
        // ... to SqlNode structure
        
        startCompile(context, block) ;
        
        SqlNode sqlNode = block.compile(getBlockCompiler() , context) ; 

        Set<Var> projectVars = QC.exitVariables(block) ;
        
        sqlNode = finishCompile(context, block, sqlNode, projectVars) ;
        
        verbose ( QC.printAbstractSQL, sqlNode ) ;
        
        return sqlNode ;
    }


    public String asSQL(Store store, Query query, Block block)
    {
        SqlNode sqlNode = compileQuery(store, query, block) ;
        // ... SqlNode to SQL string
        String sqlStmt = store.getSQLGenerator().generateSQL(sqlNode) ; 
        verbose ( QC.printSQL, sqlStmt ) ; 

        return sqlStmt ;
    }
    
    /** A chance for subclasses to analyse and alter the block to be compiled into SQL */
    protected Block modify(Block block) { return block ; }

    protected abstract void startCompile(CompileContext context, Block block) ;
    protected abstract SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode, Set<Var> projectVars) ;
//    
//    
//    public SqlNode compile(BlockOptional blockOpt, CompileContext context)
//    {
//        SqlNode fixedNode = blockOpt.getLeft().generateSQL(context, this) ;
//        SqlNode optNode = blockOpt.getRight().generateSQL(context, this) ;
//        
//        if ( optNode.isProject() )
//        {
//            log.info("Projection from an optional{} block") ;
//            optNode = optNode.getProject().getSubNode() ;
//        }
//        SqlNode sqlNode = QC.leftJoin(context, fixedNode, optNode) ;
//        return sqlNode ;
//    }
    
    static private void verbose(boolean flag, Object thing)
    {
        if ( flag )
        {
            System.out.println(thing) ;
            if ( QC.printDivider != null ) 
                System.out.println(QC.printDivider) ;
            System.out.flush() ;
        }
    }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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