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

import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.util.IndentedLineBuffer;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.QueryCompiler;
import com.hp.hpl.jena.sdb.store.ResultsBuilder;
import com.hp.hpl.jena.sdb.store.Store;

/**
 * Compile a query (in the form of Blocks).  This is the general part of the
 * algorithm where optionals are turned into left joins.  It is parameterized
 * by encoding of the basic patterns.  The different layouts provide that part
 * of the translation to SQL.
 *  
 * @author Andy Seaborne
 * @version $Id: QueryCompilerBase.java,v 1.1 2006/04/22 13:45:58 andy_seaborne Exp $
 */

public abstract class QueryCompilerBasicPattern implements QueryCompiler
{
    private static Log log = LogFactory.getLog(QueryCompilerBasicPattern.class) ;
    
    public static String  printDivider      = null ;
    public static boolean printBlock        = false ;
    public static boolean printAbstractSQL  = false ;
    public static boolean printSQL          = false ;
    
    public final QueryIterator execSQL(Store store,
                                       Block block,
                                       Binding binding,
                                       ExecutionContext execCxt)
    {
        String sqlStmt = asSQL(block) ;
        
        try {
            java.sql.ResultSet jdbcResultSet = store.getConnection().execQuery(sqlStmt) ;
            Set<Var> x = block.getProjectVars() ;
            if ( x == null )
            {
                // This happens when the query isn't a single SDB block. 
                log.info("Null for projection variables - not a single block?") ;
                x = block.getDefinedVars() ;
            }
            if ( x == null )
                log.warn("Null for defined variables") ;
            try {
                return getResultBuilder().assembleResults(jdbcResultSet, binding, x, execCxt) ;
            } finally { jdbcResultSet.close() ; }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing a basic block", ex) ;
        }
    }

    //protected abstract ConditionCompiler getConditionCompiler() ;
    protected abstract ResultsBuilder getResultBuilder() ;

    public String asSQL(Block block)
    {
        verbose ( printBlock, block ) ; 
        CompileContext context = new CompileContext() ;

        // A chance for subclasses to change the block structure (including insert their own block types)
        block = modify(block) ;

        if ( block == null )
            throw new SDBException("asSQL: Block to compile is null") ;
        
        // ... to SqlNode structure
        
        startCompile(context, block) ;
        
        SqlNode sqlNode = block.generateSQL(context, this) ; 

        sqlNode = finishCompile(context, block, sqlNode) ;
        
        verbose ( printAbstractSQL, sqlNode ) ;

        // ... SqlNode to SQL string
        String sqlStmt = generateSQL(sqlNode) ; 
        verbose ( printSQL, sqlStmt ) ; 

        return sqlStmt ;

    }

    /** A chance for subclasses to analyse and alter the block to be compiled into SQL */
    protected Block modify(Block block) { return block ; }

    protected String generateSQL(SqlNode sqlNode)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        GenerateSQL v = new GenerateSQL(buff.getIndentedWriter()) ;
        
        // Top must be a project to cause the SELECT to be written
        if ( ! sqlNode.isProject() )
            sqlNode = SqlProject.project(sqlNode) ;
        
        sqlNode.toString();
        sqlNode.visit(v) ;
        return buff.asString() ;
    }

    protected abstract void startCompile(CompileContext context, Block block) ;
    protected abstract SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode) ;
    
    protected abstract SqlNode compile(BlockBGP blockBGP, CompileContext context) ;
    
    
    
    
    public SqlNode compile(BlockOptional blockOpt, CompileContext context)
    {
        SqlNode fixedNode = blockOpt.getLeft().generateSQL(context, this) ;
        SqlNode optNode = blockOpt.getRight().generateSQL(context, this) ;
        
        if ( optNode.isProject() )
        {
            log.info("Projection from an optional{} block") ;
            optNode = optNode.getProject().getSubNode() ;
        }
        SqlNode sqlNode = QC.leftJoin(context, fixedNode, optNode) ;
        return sqlNode ;
    }
    
    static private void verbose(boolean flag, Object thing)
    {
        if ( flag )
        {
            System.out.println(thing) ;
            if ( printDivider != null ) 
                System.out.println(printDivider) ;
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