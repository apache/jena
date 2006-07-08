/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.BindingMap;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.sdb.condition.SDBConstraint;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.compiler.BlockBGP;
import com.hp.hpl.jena.sdb.core.compiler.QC;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerBase;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.util.Pair;

public class QueryCompiler1 extends QueryCompilerBase
{
    private static Log log = LogFactory.getLog(QueryCompiler1.class) ;
    
    private EncoderDecoder codec ;
    private TripleTableDesc tripleTableDesc ; // ==> Tablefactory??

    public QueryCompiler1(EncoderDecoder codec, TripleTableDesc tripleTableDesc)
    {
        if ( tripleTableDesc == null )
            this.tripleTableDesc = new TripleTableDescSPO() ;
        else
            this.tripleTableDesc = tripleTableDesc ;
        this.codec = codec ;
    }
    
    public QueryCompiler1(EncoderDecoder codec) { this(codec, null) ; }
    
    @Override
    public SqlNode compile(BlockBGP blockBGP, CompileContext context)
    {
        // Common code - hmmm ...
        SqlNode sqlNode = startBasicBlock(context, null, blockBGP) ;

        for ( Triple triple : blockBGP.getTriples() )
        {
            SqlNode sNode = match(context, triple) ;
            if ( sNode != null )
                sqlNode = QC.innerJoin(context, sqlNode, sNode) ;
        }
        sqlNode = finishBasicBlock(context, sqlNode, blockBGP) ;
        return sqlNode ;
    }


    @Override
    protected void startCompile(CompileContext context, Block block)
    { return ; } 


    @Override
    protected SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode)
    {
        List<Pair<Var, SqlColumn>>cols = new ArrayList<Pair<Var, SqlColumn>>() ;
        Set<Var> x = block.getProjectVars() ;

        if ( x == null )
            x = block.getDefinedVars() ;
        for ( Var v : x )
        {
            if ( v.isSystemVar() )
                continue ;
            SqlColumn c = sqlNode.getScope().getColumnForVar(v) ;
            cols.add(new Pair<Var, SqlColumn>(v, c)) ;
        }
        return new SqlProject(sqlNode, cols) ;
    }


    protected SqlNode match(CompileContext context, Triple triple)
    {
        String sCol = tripleTableDesc.getSubjectColName() ;
        String pCol = tripleTableDesc.getPredicateColName() ;
        String oCol = tripleTableDesc.getObjectColName() ;

        String alias = context.allocTableAlias() ;
        TableTriples1 tripleTable = new TableTriples1(tripleTableDesc.getTableName(), alias) ;
        tripleTable.addNote(FmtUtils.stringForTriple(triple, null)) ;
        
        SqlExprList conditions = new SqlExprList() ;
        processSlot(context, tripleTable, triple.getSubject(),   sCol, conditions) ; 
        processSlot(context, tripleTable, triple.getPredicate(), pCol, conditions) ;
        processSlot(context, tripleTable, triple.getObject(),    oCol, conditions) ;

        if ( conditions.size() == 0 )
            return tripleTable ;
        return new SqlRestrict(tripleTable, conditions) ;
    }

    private SqlNode startBasicBlock(CompileContext context, ScopeBase scope, BlockBGP blockBGP)
    { return null ; }

    private SqlNode finishBasicBlock(CompileContext context, SqlNode sqlNode,  BlockBGP blockBGP)
    { 
        if ( blockBGP.getConstraints().size() > 0 )
        {
            String alias = context.allocAlias("R"+SDBConstants.SQLmark) ;
            SqlExprList sqlConditions = new SqlExprList() ;
            for ( SDBConstraint c : blockBGP.getConstraints() )
            {
                SqlExpr sqlExpr = c.asSqlExpr(sqlNode.getScope()) ;
                sqlConditions.add(sqlExpr) ;
            }
            sqlNode = new SqlRestrict(alias, sqlNode, sqlConditions) ;
        }
        return sqlNode ; 
    }
    
    private void processSlot(CompileContext context, TableTriples1 triples, Node n, String col, SqlExprList conditions)
    {
        SqlColumn thisCol = new SqlColumn(triples, col) ;
        
        if ( ! n.isVariable() )
        {
            String str = codec.encode(n) ;
            //str = SQLUtils.quote(str) ;
            SqlExpr c = new S_Equal(thisCol, new SqlConstant(str)) ;
            c.addNote("Const: "+n) ;
            conditions.add(c) ;
            return ;
        }
    
        // Variable
        Var var = new Var(n) ;
        
        if ( triples.getScope().hasColumnForVar(var) )
        {
            // Becomes join condition.
//            SqlColumn otherCol = scope.getColumnForVar(var) ;
//            SqlExpr c = new S_Equal(otherCol, thisCol) ;
//            c.addNote("processVar: "+var) ;
//            conditions.add(c) ;
            return ;
        }
        triples.setColumnForVar(var, thisCol) ;
    }
    
    @Override
    protected QueryIterator assembleResults(java.sql.ResultSet rs, Binding binding,
                                            Set<Var> vars, ExecutionContext execCxt) throws SQLException
    {
        List<Binding> results = new ArrayList<Binding>() ;
        
        while(rs.next())
        {
            Binding b = new BindingMap(binding) ;
            for ( Var v : vars )
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