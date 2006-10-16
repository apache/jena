/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.util.Set;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.compiler.BlockCompiler;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerMain;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;
import com.hp.hpl.jena.sdb.store.ResultsBuilder;
import com.hp.hpl.jena.sdb.util.Pair;

public class QueryCompiler2 extends QueryCompilerMain
{
    private BlockCompiler     blockCompiler ;
    private ResultsBuilder    resultsBuilder ;
    private ConditionCompiler conditionCompiler ;
    
    public QueryCompiler2()
    {
        this(null, null, null) ;
    }
    
    public QueryCompiler2(BlockCompiler blockCompiler,
                          ResultsBuilder resultsBuilder,
                          ConditionCompiler conditionCompiler)
    {
        if ( blockCompiler == null )
            blockCompiler = new BlockCompiler2() ;
        if ( resultsBuilder == null )
            resultsBuilder = new ResultsBuilder2() ;
        if ( conditionCompiler == null)
            conditionCompiler = new ConditionCompiler2() ;
        this.blockCompiler = blockCompiler ;
        this.resultsBuilder = resultsBuilder ;
        this.conditionCompiler = conditionCompiler ;
    }
    
    @Override
    protected BlockCompiler  getBlockCompiler()     { return blockCompiler ; }
    @Override
    protected ResultsBuilder getResultsBuilder()    { return resultsBuilder ; }

    public ConditionCompiler getConditionCompiler() { return conditionCompiler ; }
    
    @Override
    protected void startCompile(CompileContext context, Block block)
    { return ; }

    @Override
    protected SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode, Set<Var> projectVars)
    {
        SqlNode n = makeProject(sqlNode, projectVars) ;
        return n ;
    }
    
    private SqlNode makeProject(SqlNode sqlNode, Set<Var> projectVars)
    {
        for ( Var v : projectVars )
        {
            // See if we have a value column already.
            SqlColumn vCol = sqlNode.getValueScope().getColumnForVar(v) ;
            if ( vCol == null )
            {
                // Should be a column mentioned in the SELECT which is not mentionedd in this block 
                continue ;
            }
    
            SqlTable table = vCol.getTable() ; 
            Var vLex = new Var(v.getName()+"$lex") ;
            SqlColumn cLex = new SqlColumn(table, "lex") ;
    
            Var vDatatype = new Var(v.getName()+"$datatype") ;
            SqlColumn cDatatype = new SqlColumn(table, "datatype") ;
    
            Var vLang = new Var(v.getName()+"$lang") ;
            SqlColumn cLang = new SqlColumn(table, "lang") ;
    
            Var vType = new Var(v.getName()+"$type") ;
            SqlColumn cType = new SqlColumn(table, "type") ;
    
            // Get the 3 parts of the RDF term and its internal type number.
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vLex,  cLex)) ; 
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vDatatype, cDatatype)) ;
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vLang, cLang)) ;
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vType, cType)) ;
        }
        return sqlNode ;
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