/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.Constraint;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.util.IndentedLineBuffer;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.QueryCompiler;
import com.hp.hpl.jena.sdb.store.Store;

import static com.hp.hpl.jena.sdb.core.JoinType.* ;

/**
 * Compile a query (in the form of Blocks) for the triple table only layout.
 * Parameterized by encoding of the columns. 
 *  
 * @author Andy Seaborne
 * @version $Id: QueryCompilerBase.java,v 1.1 2006/04/22 13:45:58 andy_seaborne Exp $
 */

public abstract class QueryCompilerBase implements QueryCompiler
{
    private static Log log = LogFactory.getLog(QueryCompilerBase.class) ;
    
    // TODO Generalize with SDBConnection logging.
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
            java.sql.ResultSet rs = store.getConnection().execQuery(sqlStmt) ;
            List<Node> x = block.getProjectVars() ;
            if ( x == null )
                x = block.getAllVars() ;
            try {
                return assembleResults(rs, binding, x, execCxt) ;
            } finally { rs.close() ; }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing a basic block", ex) ;
        }
        
    }

    protected abstract QueryIterator assembleResults(java.sql.ResultSet rs,
                                                     Binding binding,
                                                     List<Node> vars,
                                                     ExecutionContext execCxt)
                                                    throws SQLException ;
    
    protected String generateSQL(CompileContext context, SqlNode sqlNode)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        GenerateSQL v = new GenerateSQL(context, buff.getIndentedWriter()) ;
        sqlNode.visit(v) ;
        return buff.asString() ;
    }

    public final String asSQL(Block block)
    {
        verbose ( printBlock, block ) ; 
        CompileContext context = new CompileContext() ;

        SqlNode sqlNode = blockToTable(context, block) ;
        verbose ( printAbstractSQL, sqlNode ) ;

        String sqlStmt = generateSQL ( context,  sqlNode ) ; 
        verbose ( printSQL, sqlStmt ) ; 

        return sqlStmt ;
    }

    // ----------------
    // Triples to a relation algebra structure
    
    // Hooks for the block-to-SQL process around matchers

    // Returns input sqlNode or a new SQL node.  Don't return null.
    protected abstract SqlNode  startQueryBlock(CompileContext context, Block block, SqlNode sqlNode) ;
    protected abstract SqlNode  finishQueryBlock(CompileContext context, Block block, SqlNode sqlNode) ;
    
    protected abstract SqlNode  startBasicBlock(CompileContext context, BasicPattern basicPattern, SqlNode sqlNode) ;
    protected abstract SqlNode  finishBasicBlock(CompileContext context, BasicPattern basicPattern, List<Constraint> constraints, SqlNode sqlNode, ConditionList delayedConditions) ;

    protected SqlNode  match(CompileContext context, BasicPattern triples) { return null ; }
    protected abstract SqlNode  match(CompileContext context, Triple triple) ;
    
    protected SqlNode blockToTable(CompileContext context, Block block)
    {
        SqlNode sqlNode = startQueryBlock(context, block, null) ;
        
        ConditionList delayedConditions = new ConditionList() ;
        
        sqlNode = blockToTable(context, block, sqlNode, delayedConditions, 0) ;
        
        if ( delayedConditions.size() != 0 )
        {
            int count = 0 ;
            for ( Condition c : delayedConditions )
                log.warn("Unhandled condition ("+count+"): "+c) ;
        }
        
        sqlNode = finishQueryBlock(context, block, sqlNode) ;
        return sqlNode ;
    }
    
    private SqlNode blockToTable(CompileContext context, Block block, SqlNode sqlNode, ConditionList delayedConditions, int levelCount)
    {
        sqlNode = basicPatternJoin(context, block, sqlNode, delayedConditions) ;
        
        List<SqlNode> opts = new ArrayList<SqlNode>() ;
        for ( Block optBlk : block.getOptionals() )
        {
            SqlNode optNode = blockToTable(context, optBlk, null, delayedConditions, levelCount+1) ;
            opts.add(optNode) ;
        }
        
        SqlNode sNode2 = leftJoin(context, sqlNode, opts, delayedConditions) ;
        return sNode2 ;
    }
    
    private SqlNode basicPatternJoin(CompileContext context, Block block, SqlNode sqlNode, ConditionList delayedConditions)
    {
        if ( block.getBasicPattern().size() == 0 )
        {
            log.warn("Zero-length basic pattern") ;
            throw new SDBException("Zero-length basic pattern") ;
        }

        // Give the real schema a chance to grab the whole basic pattern. 
        SqlNode sn = match(context, block.getBasicPattern()) ;
        if ( sn != null )
            return sn ;

        sqlNode = startBasicBlock(context, block.getBasicPattern(), sqlNode) ;
        
        // Two cases: for the basicPattern
        // A single table becomes a SqlRestrict
        // More than one table becomes a join
        // This happens automatically because each table is converted into
        // an SqlRestrict(SqlTable) which join() rips apart. 
        
        for ( Triple triple : block.getBasicPattern() )
        {
            SqlNode sNode = match(context, triple) ;
            if ( sNode != null )
                sqlNode = innerJoin(context, sqlNode, sNode, delayedConditions) ;
        }

        sqlNode = finishBasicBlock(context, block.getBasicPattern(), block.getBlockConstraints(), sqlNode, delayedConditions) ;

        // Conditions
        
        return sqlNode ;
    }
    
    protected SqlNode innerJoin(CompileContext context, SqlNode left, SqlNode right, ConditionList delayedConditions)
    {
        // Uses the fact that inner joins never contain outer joins (block = BP*, opt(BP)*) 
        return join(context, left, right, INNER, delayedConditions) ; 
    }
    
    protected SqlNode leftJoin(CompileContext context, SqlNode base, List<SqlNode> opts, ConditionList delayedConditions)
    {
        if ( opts.size() == 0 )
            return base ;
        
        for ( SqlNode opt : opts )
        {
            if ( opt.isProject() )
                opt = opt.getProject().getSubNode() ;
            base = join(context, base, opt, LEFT, delayedConditions) ;
        }
        
        return base ;
    }
    
    // Put somewhere useful
    private String sqlNodeName(SqlNode sNode)
    {
        if ( sNode == null ) return "<null>" ;
        if ( sNode instanceof SqlProject )       return "Project" ;
        if ( sNode instanceof SqlRestrict )      return "Restrict/"+sqlNodeName(sNode.getRestrict().getSubNode()) ;
        if ( sNode instanceof SqlTable )         return "Table" ;
        if ( sNode instanceof SqlJoinInner )     return "JoinInner" ;
        if ( sNode instanceof SqlJoinLeftOuter ) return "Joinleft" ;
        return "<unknown>" ;
    }
    
    private SqlNode join(CompileContext context, SqlNode left, SqlNode right,
                         JoinType joinType, ConditionList delayedConditions)
    {
        if ( false )
        {
            String c = delayedConditions.toString() ;
            log.info("join: "+joinType+"("+sqlNodeName(left)+" & "+sqlNodeName(right)+") "+c) ;
        }
        
        List<String> annotations = null ;
        // left - the SQL tree built so far
        // right - the access to one triple to add (usually) 
        
        if ( left == null )
            // Case : first table in "join"
            return right ;
        
        ConditionList conditions = new ConditionList() ;
        
        // Flatten some cases
        if ( left.isRestrict() )
        {
            // We can yank up the conditions into the inner join.
            if ( joinType == INNER )
            {
                conditions.addAll(left.getRestrict().getConditions()) ;
                // Loose the restriction.
                left = left.getRestrict().getSubNode() ;
            }
        }
        
        if ( right.isRestrict() )
        {
            if ( right.getRestrict().getSubNode().isTable() )
            {
                // Restrict-Table : happens when we are building joins from triples.
                // Maybe a special node type for this?
                annotations = right.getRestrict().getSubNode().getAnnotations() ;
                if ( joinType == INNER )
                {
                    // If the RHS is a restriction of a single table and it's an inner join,
                    // then all are candidates to move into the join ON clause
                    // and we can collapse the restriction node itself.
                    conditions.addAll(right.getRestrict().getConditions()) ;
                    right = right.getRestrict().getSubNode() ;
                }
                else
                {
                    // But if a Left Join, find spanning conditions only
                    ConditionList c1 = new ConditionList() ;    // Spanning - move to LJ
                    ConditionList c2 = new ConditionList() ;    // Non-spanning - leave alone
                    // There should be no conditions not involving the left or right (the restricted table)
                    // (Not sure about this - may be a variable in the table that is not in the left (i.e. not spanning)  
                    assignConditions(left, right, right.getRestrict().getConditions(), c2, c2, c1, null) ;
                    conditions.addAll(c1) ;
                    right.getRestrict().getConditions().retainAll(c2) ;
                    // Remove if no conditions left
                    if ( right.getRestrict().getConditions().size() == 0 )
                        right = right.getRestrict().getSubNode() ;
                }
            }
        }
        
        // Put any delayed conditions into the connditions to be considered.
        if ( delayedConditions != null )
        {
            conditions.addAll(delayedConditions) ;
            delayedConditions.clear() ;
        }
        
        SqlJoin join = SqlJoin.create(joinType, left, right, null) ;
        assignConditions(left, right, conditions, join.getConditions(), join.getConditions(), join.getConditions(), delayedConditions) ;
        if ( annotations != null )
            join.getAnnotations().addAll(annotations) ;
        return join ;
    }
    
    // Take a list conditions and assign each to a bucket: involves columns from the left only,
    // involves columns from the right only, involves columns of left and right (it's a join
    // join condition) or "other" which means it uses a column not in the left or right and
    // so will be done elsewhere.
    private void assignConditions(SqlNode left, SqlNode right, ConditionList conditions,
                                  ConditionList leftConditions,
                                  ConditionList rightConditions, // Not used different : rename to "localConditions"
                                  ConditionList spanningConditions,
                                  ConditionList nonSpanningConditions)
    {
        for ( Condition c : conditions )
        {
            // Set<Column> s = c.getColumnsNeeded() 
            
            Column colLeft  = null ;
            if ( c.getLeft() != null )
                colLeft = c.getLeft().asColumn() ;
            
            Column colRight = null ;
            if ( c.getRight() != null )
                colRight = c.getRight().asColumn() ;
            
            if ( colLeft == null && colRight == null )
            {
                log.warn("Condition does not involve left or right: "+c) ;
                nonSpanningConditions.add(c) ;
                continue ;
            }
             
            if ( colLeft == null )
            {
                rightConditions.add(c) ;
                continue ;
            }

            if ( colRight == null )
            {
                leftConditions.add(c) ;
                continue ;
            }
            
            boolean colLeftScope =  ( left.usesColumn(colLeft)  || right.usesColumn(colLeft) ) ; 
            boolean colRightScope = ( left.usesColumn(colRight) || right.usesColumn(colRight) ) ;

            if ( colLeftScope && colRightScope )
            {
                if ( ! ( c instanceof ConditionEqual ) )
                    log.warn("Non-equality condition in spanning conditions") ;
                spanningConditions.add(c) ;
            }
            else
            {
                if( nonSpanningConditions == null )
                {
                    log.warn("Condition involves something else unexpectedly: "+c) ;
                    continue ;
                }
                nonSpanningConditions.add(c) ;
            }
        }
    }
    
    private void verbose(boolean flag, Object thing)
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