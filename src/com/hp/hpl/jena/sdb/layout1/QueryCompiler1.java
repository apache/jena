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
import com.hp.hpl.jena.sdb.core.compiler.*;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.engine.SDBConstraint;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;
import com.hp.hpl.jena.sdb.store.ResultsBuilder;
import com.hp.hpl.jena.sdb.util.Pair;

public class QueryCompiler1 extends QueryCompilerBasicPattern
{
    private static Log log = LogFactory.getLog(QueryCompiler1.class) ;
    
    private EncoderDecoder codec ;
    private TripleTableDesc tripleTableDesc ; // ==> Tablefactory??
    private TriplePatternCompiler tripleCompiler ;

    public QueryCompiler1(EncoderDecoder codec, TripleTableDesc tripleTableDesc)
    {
        if ( tripleTableDesc == null )
            this.tripleTableDesc = new TripleTableDescSPO() ;
        else
            this.tripleTableDesc = tripleTableDesc ;
        this.codec = codec ;
        tripleCompiler = new TripleCompiler1() ;
    }
    
    public QueryCompiler1(EncoderDecoder codec) { this(codec, null) ; }

    @Override
    public TriplePatternCompiler getTriplePatternCompiler()
    { return tripleCompiler ; }
    
    @Override
    protected void startCompile(CompileContext context, Block block)
    { return ; } 


    @Override
    protected SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode, Set<Var> projectVars)
    {
        sqlNode = makeProject(sqlNode, projectVars) ;
        return sqlNode ;
    }

    private SqlNode makeProject(SqlNode sqlNode, Collection<Var> projectVars)
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
    
    class TripleCompiler1 extends TriplePatternCompilerPlain
    {
        @Override
        protected void constantSlot(CompileContext context, Node node, SqlColumn thisCol, SqlExprList conditions)
        {
              String str = codec.encode(node) ;
              SqlExpr c = new S_Equal(thisCol, new SqlConstant(str)) ;
              c.addNote("Const: "+FmtUtils.stringForNode(node, context.getPrefixMapping())) ;
              conditions.add(c) ;
              return ;
        }
        
        @Override
        protected SqlTable accessTriplesTable(String alias)
        {
            return new TableTriples1(tripleTableDesc.getTableName(), alias) ;
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