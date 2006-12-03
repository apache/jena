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
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.QueryCompiler;
import com.hp.hpl.jena.sdb.store.SQLBridge;
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
        // A results builder (renamed?) should do mapping between SQL ids and SPARQL variables.
        // Generator class : use widely.
        
        Set<Var> projectVars = QC.exitVariables(block) ;
        SQLBridge bridge = createSQLBridge() ;
        
        SqlNode sqlNode = compileQuery(store, execCxt.getQuery(), block, bridge) ;
        //bridge.buildProject(sqlNode, projectVars) ;
        
        verbose ( QC.printAbstractSQL, sqlNode ) ;
        
        String sqlStmt = store.getSQLGenerator().generateSQL(sqlNode) ;
        
        verbose( QC.printSQL, sqlStmt ) ;
        
        // finishCompile pairs with resultBuilder so combine 
        // ResultsBuilder.makeProject(SqlNode, Vars) => SqlNode
        //Scope scope = sqlNode.getIdScope() ;
        
        try {
            // Odd : exitVariables and a project?
            java.sql.ResultSet jdbcResultSet = store.getConnection().execQuery(sqlStmt) ;
            try {
                return bridge.assembleResults(jdbcResultSet, binding, execCxt) ;
            } finally { jdbcResultSet.close() ; }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing a basic block", ex) ;
        }
    }

    protected abstract SQLBridge      createSQLBridge() ;
    protected abstract BlockCompiler  createBlockCompiler() ;
    
    public SqlNode compileQuery(Store store, Query query, Block block)
    {
        SQLBridge bridge = createSQLBridge() ;
        return compileQuery(store, query, block, bridge) ;
    }
    
    protected SqlNode compileQuery(Store store, Query query, Block block, SQLBridge bridge)
    {
        verbose ( QC.printBlock, block ) ; 
        CompileContext context = new CompileContext(store, query.getPrefixMapping()) ;

        // A chance for subclasses to change the block structure
        //  -- including insert their own block types)
        //  -- ?? 
        // Remove?  Now we have customizers?
        // No - make customizers a subclass of this engine. 
        
        block = modify(block) ;

        if ( block == null )
            throw new SDBException("asSQL: Block to compile is null") ;
        
        // ... to SqlNode structure
        
        startCompile(context, block) ;
        
        SqlNode sqlNode = block.compile(createBlockCompiler() , context) ; 

        Set<Var> projectVars = QC.exitVariables(block) ;
        
        sqlNode = finishCompile(context, block, sqlNode, projectVars) ;
        bridge.init(sqlNode, QC.exitVariables(block)) ;
        sqlNode = bridge.buildProject() ;
        
        return sqlNode ;
    }

//    public String asSQL(Store store, Query query, Block block)
//    {
//        SqlNode sqlNode = compileQuery(store, query, block) ;
//        String sqlStmt = store.getSQLGenerator().generateSQL(sqlNode) ; 
//        return sqlStmt ;
//    }
//    
    /** A chance for subclasses to analyse and alter the block to be compiled into SQL */
    protected Block modify(Block block) { return block ; }

    protected abstract void startCompile(CompileContext context, Block block) ;
    protected abstract SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode, Set<Var> projectVars) ;

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