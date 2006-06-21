/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.BindingMap;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.query.util.NodeUtils;
import com.hp.hpl.jena.sdb.condition.SDBConstraint;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.engine.QueryCompilerBase;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;
import com.hp.hpl.jena.sdb.util.Pair;


public class QueryCompiler1
    extends QueryCompilerBase
    //implements QueryCompiler
{
    private static Log log = LogFactory.getLog(QueryCompiler1.class) ;
    private EncoderDecoder codec ;
    private TripleTableDesc tripleTableDesc ;

    public QueryCompiler1(EncoderDecoder codec, TripleTableDesc tripleTableDesc)
    {
        if ( tripleTableDesc == null )
            this.tripleTableDesc = new TripleTableDescSPO() ;
        else
            this.tripleTableDesc = tripleTableDesc ;
        this.codec = codec ;
    }
    
    public QueryCompiler1(EncoderDecoder codec) { this(codec, null) ; }
    
    // This layout does not optimize conditions. 
    public ConditionCompiler getConditionCompiler()
    {
        return null ;
    }
    
    @Override
    protected SqlNode startQueryBlock(CompileContext context, Block block, SqlNode sqlNode)
    { return sqlNode ; }

    @Override
    protected SqlNode finishQueryBlock(CompileContext context, Block block, SqlNode sqlNode)
    {
        List<Pair<Node, SqlColumn>>cols = new ArrayList<Pair<Node, SqlColumn>>() ;
        for ( Node v : block.getAllVars() )
        {
            if ( ! v.isVariable() )
            {
                log.warn("finishQueryBlock: Not a variable: "+v) ;
            }
            if ( ! NodeUtils.isApplicationVar(v) )
                continue ;
            cols.add(new Pair<Node, SqlColumn>(v, context.getAlias(v))) ;
        }
        return new SqlProject(sqlNode, cols) ;
    }
    
    @Override
    protected SqlNode startBasicBlock(CompileContext context, BasicPattern basicPattern, SqlNode sqlNode)
    { return sqlNode ; }
    
    @Override
    protected SqlNode finishBasicBlock(CompileContext context, BasicPattern basicPattern, List<SDBConstraint> constraints, SqlNode sqlNode, SqlExprList delayedConditions)
    { 
        if ( constraints.size() > 0 )
        {
            String alias = context.allocAlias("R$") ;
            // Convert to SqlExprList
            SqlExprList sqlConditions = new SqlExprList() ;
            for ( SDBConstraint c : constraints )
            {
                SqlExpr sqlExpr = c.asSqlExpr(context) ;
                sqlConditions.add(sqlExpr) ;
            }
            sqlNode = new SqlRestrict(alias, sqlNode, sqlConditions) ;
        }

        return sqlNode ;
    }
    
    @Override
    protected SqlNode match(CompileContext context, Triple triple)
    {
        String sCol = tripleTableDesc.getSubjectColName() ;
        String pCol = tripleTableDesc.getPredicateColName() ;
        String oCol = tripleTableDesc.getObjectColName() ;
        
        String alias = context.allocTableAlias() ;
        TableTriples1 tripleTable = new TableTriples1(tripleTableDesc.getTableName(), alias) ;
        SqlExprList conditions = new SqlExprList() ;
        
        // Turn triple pattern into conditions
        processVar(context, tripleTable, triple.getSubject(),   sCol, conditions) ; 
        processVar(context, tripleTable, triple.getPredicate(), pCol, conditions) ;
        processVar(context, tripleTable, triple.getObject(),    oCol, conditions) ;
        
        if ( conditions.size() == 0 )
            return tripleTable ;
        return new SqlRestrict(tripleTable, conditions) ;
    }
    
    private void processVar(CompileContext context, TableTriples1 triples, Node n, String col, SqlExprList conditions)
    {
        SqlColumn thisCol = new SqlColumn(triples, col) ;
        
        if ( ! n.isVariable() )
        {
            String str = codec.encode(n) ;
            //str = SQLUtils.quote(str) ;
            SqlExpr c = new S_Equal(thisCol, new SqlConstant(str)) ;
            conditions.add(c) ;
            return ;
        }
    
        // Variable
        if ( context.hasAlias(n) )
        {
            SqlColumn otherCol = context.getAlias(n) ;
            SqlExpr c = new S_Equal(otherCol, thisCol) ;
            conditions.add(c) ;
            return ;
        }
    
        // New variable mentioned
        context.setAlias(n, thisCol) ;
    }
    
    @Override
    protected QueryIterator assembleResults(java.sql.ResultSet rs, Binding binding,
                                            List<Node> vars, ExecutionContext execCxt) throws SQLException
    {
        List<Binding> results = new ArrayList<Binding>() ;
        
        while(rs.next())
        {
            Binding b = new BindingMap(binding) ;
            for ( Node v : vars )
            {
                try {
                    String s = rs.getString(v.getName()) ;
                    // Same as rs.wasNull()
                    if ( s == null )
                        continue ;
                    Node n = codec.decode(s) ;
                    b.add(v.getName(), n) ;
                    // Ignore any access error (variable requested not in results)
                } catch (SQLException ex) {}
            }
            results.add(b) ;
        }
        // Crude - copying.
        return new QueryIterPlainWrapper(results.iterator(), execCxt) ;
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