/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.compiler.BlockBGP;
import com.hp.hpl.jena.sdb.core.compiler.ConditionCompilerNone;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerTriplePatternSlot;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.engine.SDBConstraint;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;
import com.hp.hpl.jena.sdb.store.ResultsBuilder;
import com.hp.hpl.jena.sdb.util.Pair;

public class QueryCompiler1 extends QueryCompilerTriplePatternSlot
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
    
//    @Override
//    protected SqlNode match(CompileContext context, Triple triple)
//    {
//        String alias = context.allocTableAlias() ;
//        // For a triple, add the table triple table 
//        String sCol = tripleTableDesc.getSubjectColName() ;
//        String pCol = tripleTableDesc.getPredicateColName() ;
//        String oCol = tripleTableDesc.getObjectColName() ;
//    
//        TableTriples1 tripleTable = new TableTriples1(tripleTableDesc.getTableName(), alias) ;
//        tripleTable.addNote(FmtUtils.stringForTriple(triple, null)) ;
//        
//        SqlExprList conditions = new SqlExprList() ;
//        processSlot(context, tripleTable, triple.getSubject(),   sCol, conditions) ; 
//        processSlot(context, tripleTable, triple.getPredicate(), pCol, conditions) ;
//        processSlot(context, tripleTable, triple.getObject(),    oCol, conditions) ;
//    
//        if ( conditions.size() == 0 )
//            return tripleTable ;
//        return SqlRestrict.restrict(tripleTable, conditions) ;
//    }
//
//    private void processSlot(CompileContext context, TableTriples1 triples, Node node, String col, SqlExprList conditions)
//    {
//        SqlColumn thisCol = new SqlColumn(triples, col) ;
//
//        if ( ! node.isVariable() )
//        {
//            String str = codec.encode(node) ;
//            SqlExpr c = new S_Equal(thisCol, new SqlConstant(str)) ;
//            c.addNote("Const: "+FmtUtils.stringForNode(node)) ;
//            conditions.add(c) ;
//            return ;
//        }
//
//        // Variable : in common with QC2
//        Var var = new Var(node) ;
//        if ( triples.getIdScope().hasColumnForVar(var) )
//        {
//            SqlColumn otherCol = triples.getIdScope().getColumnForVar(var) ;
//            SqlExpr c = new S_Equal(otherCol, thisCol) ;
//            conditions.add(c) ;
//            c.addNote("processVar: "+var) ;
//            return ;
//        }
//        triples.setIdColumnForVar(var, thisCol) ;
//    }

    @Override
    protected void constantSlot(Node node, SqlColumn thisCol, SqlExprList conditions)
    {
          String str = codec.encode(node) ;
          SqlExpr c = new S_Equal(thisCol, new SqlConstant(str)) ;
          c.addNote("Const: "+FmtUtils.stringForNode(node)) ;
          conditions.add(c) ;
          return ;
    }
    
    @Override
    protected SqlTable makeTriplesTable(String alias)
    {
        return new TableTriples1(tripleTableDesc.getTableName(), alias) ;
    }
    
    @Override
    protected void startCompile(CompileContext context, Block block)
    { return ; } 


    @Override
    protected SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode)
    {
        // Generate the SQL SELECT projection
        // DRYout - choosing variable shareable with QC2 - finishCompile => makeProject and finishCompile is just a signal 
        Set<Var> x = block.getProjectVars() ;

        if ( x == null )
            x = block.getDefinedVars() ;
        
        sqlNode = makeProject(x, sqlNode) ;
        return sqlNode ;
    }

    private SqlNode makeProject(Collection<Var> projectVars, SqlNode sqlNode)
    {
        for ( Var v : projectVars )
        {
            if ( ! v.isNamedVar() )
                continue ;
            // Value scope == IdScope for layout1
            // CHECK
            SqlColumn c = sqlNode.getIdScope().getColumnForVar(v) ;
            if ( c != null )
                sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(v,c)) ;
//            else
//                log.warn("Can't find column for var: "+v) ;
                
        }
        return sqlNode ;
    }
    
    @Override
    protected SqlNode startBasicBlock(CompileContext context, BlockBGP blockBGP)
    { return null ; }

    @Override
    protected SqlNode finishBasicBlock(CompileContext context, SqlNode sqlNode,  BlockBGP blockBGP)
    { 
        // End of BGP - add any constraints.
        for ( SDBConstraint c : blockBGP.getConstraints() )
        {
            throw new SDBException("ConditionCompiler for layout 1 not writtern") ;
//                SqlExpr sqlExpr = null ;
//                sqlNode = SqlRestrict.restrict(sqlNode, sqlExpr) ;
        }
        return sqlNode ; 
    }
    
    public ConditionCompiler getConditionCompiler()
    {
        return ConditionCompilerNone.get() ;
    }

    @Override
    protected ResultsBuilder getResultBuilder()
    {
        return new ResultsBuilder1(codec) ;
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